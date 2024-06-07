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

package uk.gov.hmrc.dprs.connectors.registration.withId

import com.google.inject.Singleton
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.BaseBackendConnector
import uk.gov.hmrc.http.StringContextOps

import java.time.Clock
import javax.inject.Inject

@Singleton
class RegistrationWithIdConnector @Inject() (appConfig: AppConfig, wsClient: WSClient, clock: Clock) extends BaseBackendConnector(wsClient, clock) {

  override def baseUrl() = url"${appConfig.registrationWithIdBaseUrl}"

}

object RegistrationWithIdConnector {

  val connectorPath: String = "/dac6/DPRS0102/v1"

  object Response {

    final case class Address(lineOne: String,
                             lineTwo: Option[String],
                             lineThree: Option[String],
                             lineFour: Option[String],
                             postalCode: String,
                             countryCode: String
    )

    object Address {
      implicit val reads: Reads[Address] =
        ((JsPath \ "addressLine1").read[String] and
          (JsPath \ "addressLine2").readNullable[String] and
          (JsPath \ "addressLine3").readNullable[String] and
          (JsPath \ "addressLine4").readNullable[String] and
          (JsPath \ "postalCode").read[String] and
          (JsPath \ "countryCode").read[String])(Address.apply _)
    }

    final case class ContactDetails(
      landline: Option[String],
      mobile: Option[String],
      fax: Option[String],
      emailAddress: Option[String]
    )

    object ContactDetails {
      implicit val reads: Reads[ContactDetails] =
        ((JsPath \ "phoneNumber").readNullable[String] and
          (JsPath \ "mobileNumber").readNullable[String] and
          (JsPath \ "faxNumber").readNullable[String] and
          (JsPath \ "emailAddress").readNullable[String])(ContactDetails.apply _)
    }

  }
}
