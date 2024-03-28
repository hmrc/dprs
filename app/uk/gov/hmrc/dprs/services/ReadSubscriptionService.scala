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
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites}
import uk.gov.hmrc.dprs.connectors.{BaseConnector, ReadSubscriptionConnector}
import uk.gov.hmrc.dprs.services.BaseService.{ErrorCodeWithStatus, ErrorCodes}
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.Converter
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.Responses.Contact
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject()(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                        readSubscriptionConnector: ReadSubscriptionConnector)
  extends BaseService {

  private val converter = new Converter(clock, acknowledgementReferenceGenerator)
  override val errorStatusCodeConversions = Map(
    INTERNAL_SERVER_ERROR -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.internalServerError)),
    SERVICE_UNAVAILABLE -> ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some(ErrorCodes.serviceUnavailableError)),
    CONFLICT -> ErrorCodeWithStatus(CONFLICT, Some(ErrorCodes.conflict)),
    BAD_REQUEST -> ErrorCodeWithStatus(INTERNAL_SERVER_ERROR)
  )

  def call(id: String)(implicit headerCarrier: HeaderCarrier,executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, Unit]] = {
    val request = converter.convert(id)
    readSubscriptionConnector.call(request).map {
      case Right(connectorResponse) => Right(converter.convert(connectorResponse))
      case Left(BaseConnector.Error(statusCode)) => Left(convert(statusCode))
    }
  }

}

object ReadSubscriptionService {

  object Responses {

    final case class Response(id: String, name: String, contacts: Seq[Contact])

    final case class Contact(contactType: String, firstname: String, middleName: String,
                              lastName: String, landline: String, mobile: String, emailAddress: String)

    object Response {
      implicit val writes: OWrites[Response] =
        ((JsPath \ "id").write[String] and
          (JsPath \ "name").write[String] and
          (JsPath \ "contacts").write[Seq[Contact]])(unlift(Response.unapply))
    }

    object Contact {
      implicit val writes: OWrites[Contact] =
        ((JsPath \ "type").write[String] and
          (JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").write[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "landline").write[String] and
          (JsPath \ "mobile").write[String] and
          (JsPath \ "emailAddress").write[String])(unlift(Contact.unapply))
    }
  }

  class Converter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

    /** We're awaiting the specs for the underlying API; in the meantime, we'll use the one for MDR; this matches the expectations of the stub service.
      */
    private val regime = "MDR"
    private val originatingSystem = "MDTP"

    def convert(id: String): ReadSubscriptionConnector.Requests.Request = {

      ReadSubscriptionConnector.Requests.Request(
        common = generateRequestCommon(),
        detail = ReadSubscriptionConnector.Requests.Detail(
          idType = "MDR",
          idNumber = id
        )
      )
    }

    def convert(response: ReadSubscriptionConnector.Responses.Response): Responses.Response =
      Responses.Response(
        id = response.subscriptionID,
        name = response.tradingName,
        contacts = Seq(
          Contact(contactType = "I", firstname = response.primaryContact.individual.firstName,
            middleName = response.primaryContact.individual.lastName, lastName = response.primaryContact.individual.lastName,
            landline = response.primaryContact.phone, mobile = response.primaryContact.mobile,
            emailAddress = response.primaryContact.email),
        Contact(contactType = "O", firstname = response.secondaryContact.organisation.organisationName,
          middleName = response.primaryContact.individual.lastName, lastName = response.primaryContact.individual.lastName,
          landline = response.primaryContact.phone, mobile = response.primaryContact.mobile,
          emailAddress = response.secondaryContact.email))
      )

    private def generateRequestCommon() = ReadSubscriptionConnector.Requests.Common(
      receiptDate = Instant.now(clock).toString,
      regime = regime,
      acknowledgementReference = acknowledgementReferenceGenerator.generate(),
      originatingSystem = originatingSystem
    )
  }
}
