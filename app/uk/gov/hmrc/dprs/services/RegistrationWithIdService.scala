/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.dprs.services

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{minLength, verifying}
import play.api.libs.json.{JsPath, OWrites, Reads}
import uk.gov.hmrc.dprs.connectors.{BaseConnector, RegistrationWithIdConnector}
import uk.gov.hmrc.dprs.services.BaseService.ErrorCodeWithStatus
import uk.gov.hmrc.dprs.services.RegistrationWithIdService.Requests.Organisation
import uk.gov.hmrc.dprs.services.RegistrationWithIdService.Requests.Organisation.RequestId
import uk.gov.hmrc.dprs.services.RegistrationWithIdService.Responses.IdType
import uk.gov.hmrc.dprs.services.RegistrationWithIdService._
import uk.gov.hmrc.dprs.support.ValidationSupport
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.lengthBetween
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithIdService @Inject() (clock: Clock,
                                           acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                           registrationWithIdConnector: RegistrationWithIdConnector
) extends BaseService {

  private val converter = new RegistrationWithIdService.Converter(clock, acknowledgementReferenceGenerator)

  def registerIndividual(
    request: Requests.Individual
  )(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Either[ErrorCodeWithStatus, Responses.Individual]] =
    registrationWithIdConnector.forIndividual(converter.convert(request)).map {
      case Right(response)                       => Right(converter.convert(response))
      case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
    }

  def registerOrganisation(
    request: Requests.Organisation
  )(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Either[ErrorCodeWithStatus, Responses.Organisation]] =
    registrationWithIdConnector.forOrganisation(converter.convert(request)).map {
      case Right(response)                       => Right(converter.convert(response))
      case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
    }

}

object RegistrationWithIdService {

  object Requests {

    final case class Individual(id: Individual.RequestId, firstName: String, middleName: Option[String], lastName: String, dateOfBirth: String)

    object Individual {

      final case class RequestId(idType: RequestIdType, value: String)

      object RequestId {
        implicit val reads: Reads[RequestId] =
          ((JsPath \ "type").read[RequestIdType] and
            (JsPath \ "value").read(lengthBetween(1, 35)))(RequestId.apply _)
      }

      trait RequestIdType

      object RequestIdType {

        val all = Set(UTR, NINO, EORI)

        case object UTR extends RequestIdType

        case object NINO extends RequestIdType

        case object EORI extends RequestIdType

        implicit val reads: Reads[RequestIdType] =
          JsPath.read[String](minLength[String](1).andKeep(verifying(all.map(_.toString).contains))).map {
            case "UTR"  => RequestIdType.UTR
            case "NINO" => RequestIdType.NINO
            case "EORI" => RequestIdType.EORI
          }
      }

      implicit val reads: Reads[Individual] =
        ((JsPath \ "id").read[RequestId] and
          (JsPath \ "firstName").read[String](lengthBetween(1, 35)) and
          (JsPath \ "middleName").readNullable(lengthBetween(1, 35)) and
          (JsPath \ "lastName").read(lengthBetween(1, 35)) and
          (JsPath \ "dateOfBirth").read(minLength[String](1).keepAnd(verifying[String](ValidationSupport.isValidDate))))(Individual.apply _)
    }

    final case class Organisation(id: RequestId, name: String, _type: Organisation.Type)

    object Organisation {

      final case class RequestId(idType: RequestIdType, value: String)

      object RequestId {
        implicit val reads: Reads[RequestId] =
          ((JsPath \ "type").read[RequestIdType] and
            (JsPath \ "value").read(lengthBetween(1, 35)))(RequestId.apply _)

      }

      trait RequestIdType

      object RequestIdType {
        val all = Set(UTR, EORI)

        case object UTR extends RequestIdType

        case object EORI extends RequestIdType

        implicit val reads: Reads[RequestIdType] =
          JsPath.read[String](minLength[String](1)).keepAnd(verifying[String](all.map(_.toString).contains)).map {
            case "UTR"  => UTR
            case "EORI" => EORI
          }
      }

      sealed trait Type

      object Type {

        val all: Set[Type] = Set(NotSpecified, Partnership, LimitedLiabilityPartnership, CorporateBody, UnincorporatedBody)

        case object NotSpecified extends Type

        case object Partnership extends Type

        case object LimitedLiabilityPartnership extends Type

        case object CorporateBody extends Type

        case object UnincorporatedBody extends Type

        implicit val reads: Reads[Type] =
          JsPath.read[String](minLength[String](1)).keepAnd(verifying(all.map(_.toString).contains)).map {
            case "NotSpecified"                => NotSpecified
            case "Partnership"                 => Partnership
            case "LimitedLiabilityPartnership" => LimitedLiabilityPartnership
            case "CorporateBody"               => CorporateBody
            case "UnincorporatedBody"          => UnincorporatedBody
          }

      }

      implicit val reads: Reads[Organisation] =
        ((JsPath \ "id").read[RequestId] and
          (JsPath \ "name").read(lengthBetween(1, 35)) and
          (JsPath \ "type").read[Type])(Organisation.apply _)
    }

  }

  object Responses {

    sealed trait IdType

    object IdType {
      case object ARN extends IdType

      case object SAP extends IdType

      case object SAFE extends IdType
    }

    final case class Individual(ids: Seq[Id],
                                firstName: String,
                                middleName: Option[String],
                                lastName: String,
                                dateOfBirth: Option[String],
                                address: Address,
                                contactDetails: ContactDetails
    )

    object Individual {
      implicit val writes: OWrites[Individual] =
        ((JsPath \ "ids").write[Seq[Id]] and
          (JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").writeNullable[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "dateOfBirth").writeNullable[String] and
          (JsPath \ "address").write[Address] and
          (JsPath \ "contactDetails").write[ContactDetails])(unlift(Individual.unapply))
    }

    final case class Organisation(ids: Seq[Id], name: String, _type: Organisation.Type, address: Address, contactDetails: ContactDetails)

    object Organisation {
      implicit val writes: OWrites[Organisation] =
        ((JsPath \ "ids").write[Seq[Id]] and
          (JsPath \ "name").write[String] and
          (JsPath \ "type").write[String] and
          (JsPath \ "address").write[Address] and
          (JsPath \ "contactDetails").write[ContactDetails])(forOrganisation =>
          (forOrganisation.ids, forOrganisation.name, forOrganisation._type.toString, forOrganisation.address, forOrganisation.contactDetails)
        )

      sealed trait Type

      object Type {

        val all: Set[Type] =
          Set(NotSpecified, Partnership, LimitedLiabilityPartnership, CorporateBody, UnincorporatedBody, UnknownOrganisationType)

        case object NotSpecified extends Type

        case object Partnership extends Type

        case object LimitedLiabilityPartnership extends Type

        case object CorporateBody extends Type

        case object UnincorporatedBody extends Type

        case object UnknownOrganisationType extends Type

        implicit val reads: Reads[Type] = {
          val recognised = all.map(_.toString)
          JsPath.read[String](minLength[String](1).filter(recognised.contains _)).map {
            case "NotSpecified"                => NotSpecified
            case "Partnership"                 => Partnership
            case "LimitedLiabilityPartnership" => LimitedLiabilityPartnership
            case "CorporateBody"               => CorporateBody
            case "UnincorporatedBody"          => UnincorporatedBody
            case _                             => UnknownOrganisationType
          }
        }
      }
    }

    final case class Id(idType: String, value: String)

    object Id {
      implicit val writes: OWrites[Id] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "value").write[String])(unlift(Id.unapply))
    }

    final case class Address(lineOne: String,
                             lineTwo: Option[String],
                             lineThree: Option[String],
                             lineFour: Option[String],
                             postalCode: String,
                             countryCode: String
    )

    object Address {
      implicit val writes: OWrites[Address] =
        ((JsPath \ "lineOne").write[String] and
          (JsPath \ "lineTwo").writeNullable[String] and
          (JsPath \ "lineThree").writeNullable[String] and
          (JsPath \ "lineFour").writeNullable[String] and
          (JsPath \ "postalCode").write[String] and
          (JsPath \ "countryCode").write[String])(unlift(Address.unapply))
    }

    final case class ContactDetails(landline: Option[String], mobile: Option[String], fax: Option[String], emailAddress: Option[String])

    object ContactDetails {
      implicit val writes: OWrites[ContactDetails] =
        ((JsPath \ "landline").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "fax").writeNullable[String] and
          (JsPath \ "emailAddress").writeNullable[String])(unlift(ContactDetails.unapply))
    }

  }

  class Converter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

    /** We're awaiting the specs for the underlying API; in the meantime, we'll use the one for MDR; this matches the expectations of the stub service.
      */
    private val regime = "MDR"

    private val requestOrganisationTypeToCode: Map[Organisation.Type, String] = {
      import Requests.Organisation.Type._
      Map(
        NotSpecified                -> "0000",
        Partnership                 -> "0001",
        LimitedLiabilityPartnership -> "0002",
        CorporateBody               -> "0003",
        UnincorporatedBody          -> "0004"
      )
    }

    private val responseOrganisationTypeCode = {
      import Responses.Organisation.Type._
      Map(
        "0000" -> NotSpecified,
        "0001" -> Partnership,
        "0002" -> LimitedLiabilityPartnership,
        "0003" -> CorporateBody,
        "0004" -> UnincorporatedBody
      )
    }

    def convert(request: Requests.Individual): RegistrationWithIdConnector.Request =
      RegistrationWithIdConnector.Request(
        common = generateRequestCommon(),
        detail = RegistrationWithIdConnector.Request.Detail(
          idType = request.id.idType.toString,
          idNumber = request.id.value,
          requiresNameMatch = true,
          isAnAgent = false, // TODO: Will this always be false?
          individual = Some(
            RegistrationWithIdConnector.Request.Individual(
              firstName = request.firstName,
              middleName = request.middleName,
              lastName = request.lastName,
              dateOfBirth = request.dateOfBirth
            )
          ),
          organisation = None
        )
      )

    def convert(request: Requests.Organisation): RegistrationWithIdConnector.Request =
      RegistrationWithIdConnector.Request(
        common = generateRequestCommon(),
        detail = RegistrationWithIdConnector.Request.Detail(
          idType = request.id.idType.toString,
          idNumber = request.id.value,
          requiresNameMatch = true,
          isAnAgent = false, // TODO: Should we pass this in?
          individual = None,
          organisation = Some(RegistrationWithIdConnector.Request.Organisation(name = request.name, _type = convert(request._type)))
        )
      )

    private def convert(organisationType: Requests.Organisation.Type): String = requestOrganisationTypeToCode.getOrElse(organisationType, "0000")

    def convert(connectorResponse: RegistrationWithIdConnector.Responses.Individual): Responses.Individual =
      Responses.Individual(
        ids = extractIds(connectorResponse),
        firstName = connectorResponse.detail.firstName,
        middleName = connectorResponse.detail.middleName,
        lastName = connectorResponse.detail.lastName,
        dateOfBirth = connectorResponse.detail.dateOfBirth,
        address = convert(connectorResponse.detail.address),
        contactDetails = convert(connectorResponse.detail.contactDetails)
      )

    def convert(connectorResponse: RegistrationWithIdConnector.Responses.Organisation): Responses.Organisation =
      Responses.Organisation(
        ids = extractIds(connectorResponse),
        name = connectorResponse.detail.name,
        _type = connectorResponse.detail.typeCode.map(convert).getOrElse(Responses.Organisation.Type.UnknownOrganisationType),
        address = convert(connectorResponse.detail.address),
        contactDetails = convert(connectorResponse.detail.contactDetails)
      )

    private def convert(connectorAddress: RegistrationWithIdConnector.Responses.Address): Responses.Address =
      Responses.Address(
        lineOne = connectorAddress.lineOne,
        lineTwo = connectorAddress.lineTwo,
        lineThree = connectorAddress.lineThree,
        lineFour = connectorAddress.lineFour,
        postalCode = connectorAddress.postalCode,
        countryCode = connectorAddress.countryCode
      )

    private def convert(connectorContactDetails: RegistrationWithIdConnector.Responses.ContactDetails): Responses.ContactDetails =
      Responses.ContactDetails(
        landline = connectorContactDetails.landline,
        mobile = connectorContactDetails.mobile,
        fax = connectorContactDetails.fax,
        emailAddress = connectorContactDetails.emailAddress
      )

    private def extractIds(connectorResponse: RegistrationWithIdConnector.Responses.GenericResponse): Seq[Responses.Id] =
      Seq(
        Some(Responses.Id(IdType.SAFE.toString, connectorResponse.detail.safeId)),
        connectorResponse.detail.arn.map(Responses.Id(IdType.ARN.toString, _)),
        connectorResponse.common.returnParams.find(_.name == "SAP_NUMBER").map(returnParam => Responses.Id(IdType.SAP.toString, returnParam.value))
      ).flatten.sortBy(_.idType)

    private def generateRequestCommon() = RegistrationWithIdConnector.Request.Common(
      receiptDate = Instant.now(clock).toString,
      regime = regime,
      acknowledgementReference = acknowledgementReferenceGenerator.generate()
    )

    private def convert(rawCode: String): Responses.Organisation.Type =
      responseOrganisationTypeCode.getOrElse(rawCode, Responses.Organisation.Type.UnknownOrganisationType)
  }

}
