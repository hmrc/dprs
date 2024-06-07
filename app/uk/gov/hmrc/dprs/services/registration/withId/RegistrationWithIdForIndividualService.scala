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

import play.api.libs.json.OWrites
import uk.gov.hmrc.dprs.connectors.BaseBackendConnector
import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForIndividualConnector
import uk.gov.hmrc.dprs.converters.registration.withId.RegistrationWithIdForIndividualConverter
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithIdForIndividualService @Inject() (clock: Clock,
                                                        acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                                        registrationWithIdForIndividualConnector: RegistrationWithIdForIndividualConnector
) extends RegistrationWithIdService {

  private val converter = new RegistrationWithIdForIndividualConverter(clock, acknowledgementReferenceGenerator)

  def call(
    request: RegistrationWithIdForIndividualService.Request,
    requestHeaders: BaseBackendConnector.Request.Headers
  )(implicit
    executionContext: ExecutionContext
  ): Future[Either[ErrorResponse, RegistrationWithIdForIndividualService.Response]] =
    registrationWithIdForIndividualConnector.call(converter.convert(request), requestHeaders).map {
      case Right(response) => Right(converter.convert(response))
      case Left(error)     => Left(convert(error))
    }

}

object RegistrationWithIdForIndividualService {

  import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps}
  import play.api.libs.json.Reads.{minLength, verifying}
  import play.api.libs.json.{JsPath, Reads}
  import uk.gov.hmrc.dprs.support.ValidationSupport
  import uk.gov.hmrc.dprs.support.ValidationSupport.Reads.lengthBetween

  final case class Request(id: Request.RequestId, firstName: String, middleName: Option[String], lastName: String, dateOfBirth: String)

  object Request {

    final case class RequestId(idType: RequestIdType, value: String)

    object RequestId {
      implicit val reads: Reads[RequestId] =
        ((JsPath \ "type").read[RequestIdType] and
          (JsPath \ "value").read(lengthBetween(1, 35)))(RequestId.apply _)
    }

    sealed trait RequestIdType

    object RequestIdType {

      val all: Set[RequestIdType] = Set(UTR, NINO, EORI)

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

    implicit val reads: Reads[Request] =
      ((JsPath \ "id").read[RequestId] and
        (JsPath \ "firstName").read[String](lengthBetween(1, 35)) and
        (JsPath \ "middleName").readNullable(lengthBetween(1, 35)) and
        (JsPath \ "lastName").read(lengthBetween(1, 35)) and
        (JsPath \ "dateOfBirth").read(minLength[String](1).keepAnd(verifying[String](ValidationSupport.isValidDate))))(Request.apply _)
  }

  final case class Response(ids: Seq[RegistrationWithIdService.Response.Id],
                            firstName: String,
                            middleName: Option[String],
                            lastName: String,
                            dateOfBirth: Option[String],
                            address: RegistrationWithIdService.Response.Address,
                            contactDetails: RegistrationWithIdService.Response.ContactDetails
  )

  object Response {
    implicit val writes: OWrites[Response] =
      ((JsPath \ "ids").write[Seq[RegistrationWithIdService.Response.Id]] and
        (JsPath \ "firstName").write[String] and
        (JsPath \ "middleName").writeNullable[String] and
        (JsPath \ "lastName").write[String] and
        (JsPath \ "dateOfBirth").writeNullable[String] and
        (JsPath \ "address").write[RegistrationWithIdService.Response.Address] and
        (JsPath \ "contactDetails").write[RegistrationWithIdService.Response.ContactDetails])(unlift(Response.unapply))

    sealed trait IdType

    object IdType {
      case object ARN extends IdType

      case object SAP extends IdType

      case object SAFE extends IdType
    }
  }
}
