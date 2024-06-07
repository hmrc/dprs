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

package uk.gov.hmrc.dprs.connectors.registration.withoutId

import com.google.inject.Singleton
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.BaseBackendConnector
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector
import uk.gov.hmrc.http.StringContextOps

import java.net.URL
import java.time.Clock
import javax.inject.Inject
import scala.Function.unlift

@Singleton
class RegistrationWithoutIdConnector @Inject() (appConfig: AppConfig, wsClient: WSClient, clock: Clock) extends BaseBackendConnector(wsClient, clock) {

  override def baseUrl(): URL = url"${appConfig.registrationWithoutIdBaseUrl}"

}

object RegistrationWithoutIdConnector {

  val connectorPath: String = "/dac6/DPRS0101/v1"

  object Request {

    final case class Address(lineOne: String, lineTwo: String, lineThree: String, lineFour: Option[String], postalCode: Option[String], countryCode: String)

    object Address {
      implicit val writes: OWrites[Address] =
        ((JsPath \ "addressLine1").write[String] and
          (JsPath \ "addressLine2").write[String] and
          (JsPath \ "addressLine3").write[String] and
          (JsPath \ "addressLine4").writeNullable[String] and
          (JsPath \ "postalCode").writeNullable[String] and
          (JsPath \ "countryCode").write[String])(unlift(Address.unapply))
    }

    final case class ContactDetails(
      landline: Option[String],
      mobile: Option[String],
      fax: Option[String],
      emailAddress: Option[String]
    )

    object ContactDetails {
      implicit val writes: OWrites[ContactDetails] =
        ((JsPath \ "phoneNumber").writeNullable[String] and
          (JsPath \ "mobileNumber").writeNullable[String] and
          (JsPath \ "faxNumber").writeNullable[String] and
          (JsPath \ "emailAddress").writeNullable[String])(unlift(ContactDetails.unapply))
    }

  }

  final case class Response(
    common: RegistrationConnector.Response.Common,
    detail: RegistrationWithoutIdConnector.Response.Detail
  )

  object Response {

    implicit lazy val reads: Reads[Response] =
      ((JsPath \ "registerWithoutIDResponse" \ "responseCommon").read[RegistrationConnector.Response.Common] and
        (JsPath \ "registerWithoutIDResponse" \ "responseDetail").read[RegistrationWithoutIdConnector.Response.Detail])(Response.apply _)

    final case class Detail(safeId: String, arn: Option[String])

    object Detail {
      implicit lazy val reads: Reads[Detail] =
        ((JsPath \ "SAFEID").read[String] and
          (JsPath \ "ARN").readNullable[String])(Detail.apply _)
    }
  }

}
