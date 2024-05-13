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

package uk.gov.hmrc.dprs.connectors.subscription

import com.google.inject.Singleton
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.BaseConnector
import uk.gov.hmrc.dprs.connectors.subscription.ReadSubscriptionConnector.Responses.Contact.{IndividualDetails, OrganisationDetails}
import uk.gov.hmrc.dprs.connectors.subscription.ReadSubscriptionConnector.Responses.Response
import uk.gov.hmrc.http.StringContextOps

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReadSubscriptionConnector @Inject() (appConfig: AppConfig, wsClient: WSClient) extends BaseConnector(wsClient) {

  def call(id: String)(implicit executionContext: ExecutionContext): Future[Either[BaseConnector.Responses.Error, Response]] =
    get[Response](id)

  override def baseUrl(): URL = url"${appConfig.readSubscriptionBaseUrl}"
}

object ReadSubscriptionConnector {

  val connectorPath: String = "/dac6/dprs0202/v1"
  val connectorName: String = "read-subscription"

  object Responses {

    final case class Response(subscriptionID: String,
                              tradingName: Option[String],
                              isGBUser: Boolean,
                              primaryContact: Option[Contact],
                              secondaryContact: Option[Contact]
    )

    final case class Contact(
      email: String,
      phone: Option[String],
      mobile: Option[String],
      individualDetails: Option[IndividualDetails],
      organisationDetails: Option[OrganisationDetails]
    )

    object Response {
      implicit val reads: Reads[Response] =
        ((JsPath \ "success" \ "customer" \ "id").read[String] and
          (JsPath \ "success" \ "customer" \ "tradingName").readNullable[String] and
          (JsPath \ "success" \ "customer" \ "gbUser").read[Boolean] and
          (JsPath \ "success" \ "customer" \ "primaryContact").readNullable[Contact] and
          (JsPath \ "success" \ "customer" \ "secondaryContact").readNullable[Contact])(Response.apply _)
    }

    object Contact {
      implicit val reads: Reads[Contact] =
        ((JsPath \ "email").read[String] and
          (JsPath \ "phone").readNullable[String] and
          (JsPath \ "mobile").readNullable[String] and
          (JsPath \ "individual").readNullable[IndividualDetails] and
          (JsPath \ "organisation").readNullable[OrganisationDetails])(Contact.apply _)

      final case class IndividualDetails(firstName: String, lastName: String, middleName: Option[String])

      final case class OrganisationDetails(organisationName: String)

      object IndividualDetails {
        implicit val reads: Reads[IndividualDetails] =
          ((JsPath \ "firstName").read[String] and
            (JsPath \ "lastName").read[String] and
            (JsPath \ "middleName").readNullable[String])(IndividualDetails.apply _)
      }

      object OrganisationDetails {
        implicit lazy val reads: Reads[OrganisationDetails] =
          (JsPath \ "organisationName").read[String].map(OrganisationDetails(_))
      }
    }
  }
}
