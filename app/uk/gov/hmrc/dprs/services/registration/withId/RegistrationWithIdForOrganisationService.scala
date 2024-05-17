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

package uk.gov.hmrc.dprs.services.registration.withId

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
import play.api.libs.json.Reads.{minLength, verifying}
import play.api.libs.json.{JsPath, OWrites, Reads}
import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForOrganisationConnector
import uk.gov.hmrc.dprs.converters.registration.withId.RegistrationWithIdForOrganisationConverter
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForOrganisationService.Request.RequestId
import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.lengthBetween

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithIdForOrganisationService @Inject() (clock: Clock,
                                                          acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                                          registrationWithIdForOrganisationConnector: RegistrationWithIdForOrganisationConnector
) extends RegistrationWithIdService {

  private val converter = new RegistrationWithIdForOrganisationConverter(clock, acknowledgementReferenceGenerator)

  def call(
    request: RegistrationWithIdForOrganisationService.Request
  )(implicit
    executionContext: ExecutionContext
  ): Future[Either[ErrorResponse, RegistrationWithIdForOrganisationService.Response]] =
    registrationWithIdForOrganisationConnector.call(converter.convert(request)).map {
      case Right(response) => Right(converter.convert(response))
      case Left(error)     => Left(convert(error))
    }

}

object RegistrationWithIdForOrganisationService {

  final case class Request(id: RequestId, name: String, _type: Request.Type)

  object Request {

    final case class RequestId(idType: RequestIdType, value: String)

    object RequestId {
      implicit val reads: Reads[RequestId] =
        ((JsPath \ "type").read[RequestIdType] and
          (JsPath \ "value").read(lengthBetween(1, 35)))(RequestId.apply _)

    }

    trait RequestIdType

    object RequestIdType {
      val all: Set[RequestIdType] = Set(UTR, EORI)

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

    implicit val reads: Reads[Request] =
      ((JsPath \ "id").read[RequestId] and
        (JsPath \ "name").read(lengthBetween(1, 35)) and
        (JsPath \ "type").read[Type])(Request.apply _)
  }

  final case class Response(ids: Seq[RegistrationWithIdService.Response.Id],
                            name: String,
                            _type: Response.Type,
                            address: RegistrationWithIdService.Response.Address,
                            contactDetails: RegistrationWithIdService.Response.ContactDetails
  )

  object Response {
    implicit val writes: OWrites[Response] =
      ((JsPath \ "ids").write[Seq[RegistrationWithIdService.Response.Id]] and
        (JsPath \ "name").write[String] and
        (JsPath \ "type").write[String] and
        (JsPath \ "address").write[RegistrationWithIdService.Response.Address] and
        (JsPath \ "contactDetails").write[RegistrationWithIdService.Response.ContactDetails])(forOrganisation =>
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

    sealed trait IdType

    object IdType {
      case object ARN extends IdType

      case object SAP extends IdType

      case object SAFE extends IdType
    }
  }

}
