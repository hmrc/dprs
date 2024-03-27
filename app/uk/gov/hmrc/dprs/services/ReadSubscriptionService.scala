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

import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Json, OWrites}
import uk.gov.hmrc.dprs.connectors.{BaseConnector, ReadSubscriptionConnector}
import uk.gov.hmrc.dprs.services.BaseService.{ErrorCodeWithStatus, ErrorCodes}
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.Converter
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject() (clock: Clock,
                                         acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                         readSubscriptionConnector: ReadSubscriptionConnector
) extends BaseService {

  private val converter = new Converter(clock, acknowledgementReferenceGenerator)
  override val errorStatusCodeConversions: Map[Int, ErrorCodeWithStatus] =
    Map(
      INTERNAL_SERVER_ERROR -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.internalServerError)),
      SERVICE_UNAVAILABLE   -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.serviceUnavailableError)),
      NOT_FOUND             -> ErrorCodeWithStatus(NOT_FOUND, Some(ErrorCodes.notFound)),
      BAD_REQUEST           -> ErrorCodeWithStatus(INTERNAL_SERVER_ERROR)
    )

  def call(id: String)(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, ReadSubscriptionService.Responses.Response]] = {
    val request = converter.convert(id)
    readSubscriptionConnector.call(request).map {
      case Right(connectorResponse)              => Right(converter.convert(connectorResponse))
      case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
    }
  }
}

object ReadSubscriptionService {

  object Responses {

    final case class Response(id: String, name: Option[String], contacts: Seq[Contact])

    sealed trait Contact {

      def landline: Option[String]

      def mobile: Option[String]

      def emailAddress: String
    }

    object Contact {

      implicit val writes: OWrites[Contact] = {
        case individual: Individual =>
          Json.obj(
            "type"         -> "I",
            "firstName"    -> individual.firstName,
            "middleName"   -> individual.middleName,
            "lastName"     -> individual.lastName,
            "landline"     -> individual.landline,
            "mobile"       -> individual.mobile,
            "emailAddress" -> individual.emailAddress
          )
        case organisation: Organisation =>
          Json.obj(
            "type"         -> "O",
            "name"         -> organisation.name,
            "landline"     -> organisation.landline,
            "mobile"       -> organisation.mobile,
            "emailAddress" -> organisation.emailAddress
          )
      }
    }

    final case class Individual(firstName: Option[String],
                                middleName: Option[String],
                                lastName: Option[String],
                                landline: Option[String],
                                mobile: Option[String],
                                emailAddress: String
    ) extends Contact

    final case class Organisation(name: String, landline: Option[String], mobile: Option[String], emailAddress: String) extends Contact

    object Response {
      implicit val writes: OWrites[Response] =
        ((JsPath \ "id").write[String] and
          (JsPath \ "name").writeNullable[String] and
          (JsPath \ "contacts").write[Seq[Contact]])(unlift(Response.unapply))
    }
  }

  class Converter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

    /** We're awaiting the specs for the underlying API; in the meantime, we'll use the one for MDR; this matches the expectations of the stub service.
      */
    private val regime            = "MDR"
    private val originatingSystem = "MDTP"

    def convert(id: String): ReadSubscriptionConnector.Requests.Request = ReadSubscriptionConnector.Requests.Request(
      common = generateRequestCommon(),
      detail = ReadSubscriptionConnector.Requests.Detail(
        idType = "MDR",
        idNumber = id
      )
    )

    def convert(response: ReadSubscriptionConnector.Responses.Response): Responses.Response =
      Responses.Response(
        id = response.subscriptionID,
        name = response.tradingName,
        contacts = {
          val primaryConverted   = response.primaryContact.map(convert)
          val secondaryConverted = response.secondaryContact.map(convert)

          Seq(primaryConverted, secondaryConverted).flatten.flatten
        }
      )

    def convert(contact: ReadSubscriptionConnector.Responses.Contact): Option[ReadSubscriptionService.Responses.Contact] =
      (contact.individualDetails, contact.organisationDetails) match {
        case (Some(individualDetails), _) =>
          Some(
            ReadSubscriptionService.Responses.Individual(
              firstName = Some(individualDetails.firstName),
              middleName = individualDetails.middleName,
              lastName = Some(individualDetails.lastName),
              landline = contact.phone,
              mobile = contact.mobile,
              emailAddress = contact.email
            )
          )
        case (_, Some(organisationDetails)) =>
          Some(
            ReadSubscriptionService.Responses.Organisation(
              name = organisationDetails.organisationName,
              landline = contact.phone,
              mobile = contact.mobile,
              emailAddress = contact.email
            )
          )
        case (_, _) =>
          None
        case _ =>
          None
      }

    private def generateRequestCommon() = ReadSubscriptionConnector.Requests.Common(
      receiptDate = Instant.now(clock).toString,
      regime = regime,
      acknowledgementReference = acknowledgementReferenceGenerator.generate(),
      originatingSystem = originatingSystem
    )
  }
}
