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

import play.api.http.Status.{BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE}
import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{minLength, verifying}
import play.api.libs.json._
import uk.gov.hmrc.dprs.connectors.{BaseConnector, RegistrationWithoutIdConnector}
import uk.gov.hmrc.dprs.services.BaseService.{ErrorCodeWithStatus, ErrorCodes}
import uk.gov.hmrc.dprs.services.RegistrationWithoutIdService.Responses.IdType
import uk.gov.hmrc.dprs.services.RegistrationWithoutIdService.{Requests, Responses}
import uk.gov.hmrc.dprs.support.ValidationSupport
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.{lengthBetween, validEmailAddress, validPhoneNumber}
import uk.gov.hmrc.dprs.support.ValidationSupport.isValidCountryCode
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithoutIdService @Inject() (clock: Clock,
                                              acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                              registrationWithoutIdConnector: RegistrationWithoutIdConnector
) extends BaseService {

  private val converter = new RegistrationWithoutIdService.Converter(clock, acknowledgementReferenceGenerator)
  override val errorStatusCodeConversions: Map[Int, ErrorCodeWithStatus] =
    Map(
      INTERNAL_SERVER_ERROR -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.internalServerError)),
      SERVICE_UNAVAILABLE   -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.serviceUnavailableError)),
      CONFLICT              -> ErrorCodeWithStatus(CONFLICT, Some(ErrorCodes.conflict)),
      BAD_REQUEST           -> ErrorCodeWithStatus(INTERNAL_SERVER_ERROR)
    )

  def registerIndividual(
    request: Requests.Individual
  )(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, Responses.Individual]] =
    registrationWithoutIdConnector.forIndividual(converter.convert(request)).map {
      case Right(response)                       => Right(converter.convert(response))
      case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
    }

  def registerOrganisation(
    request: Requests.Organisation
  )(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, Responses.Organisation]] =
    registrationWithoutIdConnector.forOrganisation(converter.convert(request)).map {
      case Right(response)                       => Right(converter.convert(response))
      case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
    }

}

object RegistrationWithoutIdService {

  object Requests {

    trait RegistrationRequest {
      def address: Address
      def contactDetails: ContactDetails
    }

    final case class Individual(firstName: String,
                                middleName: Option[String],
                                lastName: String,
                                dateOfBirth: String,
                                address: Address,
                                contactDetails: ContactDetails
    ) extends RegistrationRequest

    object Individual {
      implicit val reads: Reads[Individual] =
        ((JsPath \ "firstName").read(lengthBetween(1, 35)) and
          (JsPath \ "middleName").readNullable(lengthBetween(1, 35)) and
          (JsPath \ "lastName").read(lengthBetween(1, 35)) and
          (JsPath \ "dateOfBirth").read(minLength[String](1).keepAnd(verifying[String](ValidationSupport.isValidDate))) and
          (JsPath \ "address").read[Address] and
          (JsPath \ "contactDetails").read[ContactDetails])(Individual.apply _)
    }

    final case class Organisation(name: String, address: Address, contactDetails: ContactDetails) extends RegistrationRequest

    object Organisation {
      implicit val reads: Reads[Organisation] =
        ((JsPath \ "name").read(lengthBetween(1, 105)) and
          (JsPath \ "address").read[Address] and
          (JsPath \ "contactDetails").read[ContactDetails])(Organisation.apply _)
    }

    final case class Address(lineOne: String, lineTwo: String, lineThree: String, lineFour: Option[String], postalCode: Option[String], countryCode: String)

    object Address {

      private def postalCodePresentIfExpected(address: RegistrationWithoutIdService.Requests.Address): Boolean = {
        val countryCodesForWhichWeNeedAPostalCode = Set("GB", "IM", "JE", "GG")
        if (countryCodesForWhichWeNeedAPostalCode.contains(address.countryCode.toUpperCase.trim)) address.postalCode.isDefined
        else true
      }

      implicit val reads: Reads[Address] =
        ((JsPath \ "lineOne").read(lengthBetween(1, 35)) and
          (JsPath \ "lineTwo").read(lengthBetween(1, 35)) and
          (JsPath \ "lineThree").read(lengthBetween(1, 35)) and
          (JsPath \ "lineFour").readNullable(lengthBetween(1, 35)) and
          (JsPath \ "postalCode").readNullable(lengthBetween(1, 10)) and
          (JsPath \ "countryCode").read(lengthBetween(1, 2).andKeep(verifying(isValidCountryCode))))(Address.apply _)
          .flatMapResult { address =>
            if (!postalCodePresentIfExpected(address)) JsError(JsPath(List(KeyPathNode("postalCode"))), "error.path.missing")
            else JsSuccess(address)
          }
    }

    final case class ContactDetails(
      landline: Option[String],
      mobile: Option[String],
      fax: Option[String],
      emailAddress: Option[String]
    )
    object ContactDetails {
      implicit val reads: Reads[ContactDetails] =
        ((JsPath \ "landline").readNullable(validPhoneNumber) and
          (JsPath \ "mobile").readNullable(validPhoneNumber) and
          (JsPath \ "fax").readNullable(validPhoneNumber) and
          (JsPath \ "emailAddress").readNullable(validEmailAddress))(ContactDetails.apply _)
    }

  }

  object Responses {

    sealed trait IdType

    object IdType {
      case object ARN extends IdType

      case object SAP extends IdType

      case object SAFE extends IdType
    }

    final case class Individual(ids: Seq[Id])

    object Individual {
      implicit val writes: OWrites[Individual] =
        (JsPath \ "ids").write[Seq[Id]].contramap(_.ids)
    }

    final case class Organisation(ids: Seq[Id])

    object Organisation {

      implicit val writes: OWrites[Organisation] =
        (JsPath \ "ids").write[Seq[Id]].contramap(_.ids)

      sealed trait Type

    }

    final case class Id(idType: String, value: String)

    object Id {
      implicit val writes: OWrites[Id] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "value").write[String])(unlift(Id.unapply))
    }

    final case class Error(code: String)

    object Error {
      implicit val writes: OWrites[Error] =
        (JsPath \ "code").write[String].contramap(_.code)
    }

  }

  class Converter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

    /** We're awaiting the specs for the underlying API; in the meantime, we'll use the one for MDR; this matches the expectations of the stub service.
      */
    private val regime = "MDR"

    def convert(request: Requests.Individual): RegistrationWithoutIdConnector.Request =
      RegistrationWithoutIdConnector.Request(
        common = generateRequestCommon(),
        detail = RegistrationWithoutIdConnector.Request.Detail(
          individual = Some(
            RegistrationWithoutIdConnector.Request.Individual(
              firstName = request.firstName,
              middleName = request.middleName,
              lastName = request.lastName,
              dateOfBirth = request.dateOfBirth
            )
          ),
          organisation = None,
          address = convert(request.address),
          contactDetails = convert(request.contactDetails)
        )
      )

    def convert(request: Requests.Organisation): RegistrationWithoutIdConnector.Request =
      RegistrationWithoutIdConnector.Request(
        common = generateRequestCommon(),
        detail = RegistrationWithoutIdConnector.Request.Detail(
          individual = None,
          organisation = Some(RegistrationWithoutIdConnector.Request.Organisation(name = request.name)),
          address = convert(request.address),
          contactDetails = convert(request.contactDetails)
        )
      )

    private def convert(address: RegistrationWithoutIdService.Requests.Address): RegistrationWithoutIdConnector.Request.Address =
      RegistrationWithoutIdConnector.Request.Address(
        lineOne = address.lineOne,
        lineTwo = address.lineTwo,
        lineThree = address.lineThree,
        lineFour = address.lineFour,
        postalCode = address.postalCode,
        countryCode = address.countryCode
      )

    private def convert(contactDetails: RegistrationWithoutIdService.Requests.ContactDetails): RegistrationWithoutIdConnector.Request.ContactDetails =
      RegistrationWithoutIdConnector.Request.ContactDetails(
        landline = contactDetails.landline,
        mobile = contactDetails.mobile,
        fax = contactDetails.fax,
        emailAddress = contactDetails.emailAddress
      )

    def convert(connectorResponse: RegistrationWithoutIdConnector.Responses.Individual): Responses.Individual =
      Responses.Individual(ids = extractIds(connectorResponse))

    def convert(connectorResponse: RegistrationWithoutIdConnector.Responses.Organisation): Responses.Organisation =
      Responses.Organisation(ids = extractIds(connectorResponse))

    private def extractIds(connectorResponse: RegistrationWithoutIdConnector.Responses.GenericResponse): Seq[Responses.Id] =
      Seq(
        Some(Responses.Id(IdType.SAFE.toString, connectorResponse.detail.safeId)),
        connectorResponse.detail.arn.map(Responses.Id(IdType.ARN.toString, _)),
        connectorResponse.common.returnParams.find(_.name == "SAP_NUMBER").map(returnParam => Responses.Id(IdType.SAP.toString, returnParam.value))
      ).flatten.sortBy(_.idType)

    private def generateRequestCommon() = RegistrationWithoutIdConnector.Request.Common(
      receiptDate = Instant.now(clock).toString,
      regime = regime,
      acknowledgementReference = acknowledgementReferenceGenerator.generate()
    )

  }

}
