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

package uk.gov.hmrc.dprs.connectors.platformOperator

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.platformOperator.CreatePlatformOperatorConnector.{Request, Response}
import uk.gov.hmrc.dprs.connectors.{BaseBackendConnector, BaseConnector}
import uk.gov.hmrc.http.StringContextOps

import java.net.URL
import java.time.Clock
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class CreatePlatformOperatorConnector @Inject() (appConfig: AppConfig, wsClient: WSClient, clock: Clock) extends BaseBackendConnector(wsClient, clock) {

  def call(request: Request, requestHeaders: BaseBackendConnector.Request.Headers)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseConnector.Responses.Error, Response]] =
    post[Request, Response](request, requestHeaders)

  override def baseUrl(): URL = url"${appConfig.createPlatformOperatorBaseUrl}"

}

object CreatePlatformOperatorConnector {

  val connectorPath: String = "/dac6/dprs9301/v1"

  final case class Request(originatingSystem: String,
                           transmittingSystem: String,
                           requestType: String,
                           regime: String,
                           requestParameters: Seq[Request.RequestParameter],
                           subscriptionId: String,
                           internalName: String,
                           businessName: Option[String],
                           tradingName: Option[String],
                           ids: Seq[Request.ID],
                           reportingNotification: Request.ReportingNotification,
                           address: Request.Address,
                           primaryContact: Request.Contact,
                           secondaryContact: Option[Request.Contact]
  )

  object Request {
    implicit lazy val writes: OWrites[Request] =
      ((JsPath \ "POManagement" \ "RequestCommon" \ "OriginatingSystem").write[String] and
        (JsPath \ "POManagement" \ "RequestCommon" \ "TransmittingSystem").write[String] and
        (JsPath \ "POManagement" \ "RequestCommon" \ "RequestType").write[String] and
        (JsPath \ "POManagement" \ "RequestCommon" \ "Regime").write[String] and
        (JsPath \ "POManagement" \ "RequestCommon" \ "RequestParameters").write[Seq[RequestParameter]] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "SubscriptionID").write[String] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "POName").write[String] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "BusinessName").writeNullable[String] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "TradingName").writeNullable[String] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "TINDetails").write[Seq[ID]] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "NotificationDetails").write[ReportingNotification] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "AddressDetails").write[Address] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "PrimaryContactDetails").write[Contact] and
        (JsPath \ "POManagement" \ "RequestDetails" \ "SecondaryContactDetails").writeNullable[Contact])(unlift(Request.unapply))

    final case class RequestParameter(name: String, value: String)

    object RequestParameter {
      implicit lazy val writes: OWrites[RequestParameter] =
        ((JsPath \ "ParamName").write[String] and
          (JsPath \ "ParamValue").write[String])(unlift(RequestParameter.unapply))
    }

    final case class ID(_type: String, value: String, countryCodeOfIssue: String)

    object ID {
      implicit lazy val writes: OWrites[ID] =
        ((JsPath \ "TINType").write[String] and
          (JsPath \ "TIN").write[String] and
          (JsPath \ "IssuedBy").write[String])(unlift(ID.unapply))
    }

    final case class ReportingNotification(_type: String, isActiveSeller: Option[Boolean], isDueDiligence: Option[Boolean], year: String)

    object ReportingNotification {
      implicit lazy val writes: OWrites[ReportingNotification] =
        ((JsPath \ "NotificationType").write[String] and
          (JsPath \ "IsActiveSeller").writeNullable[Boolean] and
          (JsPath \ "IsDueDiligence").writeNullable[Boolean] and
          (JsPath \ "FirstNotifiedReportingPeriod").write[String])(unlift(ReportingNotification.unapply))
    }

    final case class Address(lineOne: String, lineTwo: String, lineThree: String, lineFour: Option[String], postalCode: Option[String], countryCode: String)

    object Address {
      implicit lazy val writes: OWrites[Address] =
        ((JsPath \ "AddressLine1").write[String] and
          (JsPath \ "AddressLine2").write[String] and
          (JsPath \ "AddressLine3").write[String] and
          (JsPath \ "AddressLine4").writeNullable[String] and
          (JsPath \ "PostalCode").writeNullable[String] and
          (JsPath \ "CountryCode").write[String])(unlift(Address.unapply))
    }

    final case class Contact(name: String, phone: Option[String], emailAddress: String)

    object Contact {
      implicit lazy val writes: OWrites[Contact] =
        ((JsPath \ "ContactName").write[String] and
          (JsPath \ "PhoneNumber").writeNullable[String] and
          (JsPath \ "EmailAddress").write[String])(unlift(Contact.unapply))
    }
  }

  final case class Response(returnParameter: Response.ReturnParam)

  object Response {

    // This is not a mistake: "ReturnParameters" is not an array, but an object. See ODPR-1110.
    implicit lazy val reads: Reads[Response] =
      (JsPath \ "success" \ "ReturnParameters").read[ReturnParam].map(Response(_))

    final case class ReturnParam(key: String, value: String)

    object ReturnParam {
      implicit val reads: Reads[ReturnParam] =
        ((JsPath \ "Key").read[String] and
          (JsPath \ "Value").read[String])(ReturnParam.apply _)
    }
  }
}
