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
import play.api.libs.json.{JsPath, OWrites, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.{BaseBackendConnector, BaseConnector}
import uk.gov.hmrc.dprs.connectors.subscription.CreateSubscriptionConnector.Requests.Contact.{IndividualDetails, OrganisationDetails}
import uk.gov.hmrc.dprs.connectors.subscription.CreateSubscriptionConnector.Requests.Request
import uk.gov.hmrc.dprs.connectors.subscription.CreateSubscriptionConnector.Responses.Response
import uk.gov.hmrc.http.StringContextOps

import java.net.URL
import java.time.Clock
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateSubscriptionConnector @Inject() (appConfig: AppConfig, wsClient: WSClient, clock: Clock) extends BaseBackendConnector(wsClient, clock) {

  def call(
    request: Request,
    requestHeaders: BaseBackendConnector.Request.Headers
  )(implicit executionContext: ExecutionContext): Future[Either[BaseConnector.Responses.Error, Response]] =
    post[Request, Response](request, requestHeaders)

  override def baseUrl(): URL = url"${appConfig.createSubscriptionBaseUrl}"

}

object CreateSubscriptionConnector {

  val connectorPath: String = "/dac6/dprs0201/v1"

  object Requests {

    final case class Request(idType: String,
                             idNumber: String,
                             tradingName: Option[String],
                             gbUser: Boolean,
                             primaryContact: Contact,
                             secondaryContact: Option[Contact]
    )

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "idType").write[String] and
          (JsPath \ "idNumber").write[String] and
          (JsPath \ "tradingName").writeNullable[String] and
          (JsPath \ "gbUser").write[Boolean] and
          (JsPath \ "primaryContact").write[Contact] and
          (JsPath \ "secondaryContact").writeNullable[Contact])(unlift(Request.unapply))
    }

    final case class Contact(
      individualDetails: Option[IndividualDetails],
      organisationDetails: Option[OrganisationDetails],
      emailAddress: String,
      landline: Option[String],
      mobile: Option[String]
    )

    object Contact {
      implicit lazy val writes: OWrites[Contact] =
        ((JsPath \ "individual").writeNullable[IndividualDetails] and
          (JsPath \ "organisation").writeNullable[OrganisationDetails] and
          (JsPath \ "email").write[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "phone").writeNullable[String])(unlift(Contact.unapply))

      final case class IndividualDetails(firstName: String, middleName: Option[String], lastName: String)

      object IndividualDetails {
        implicit lazy val writes: OWrites[IndividualDetails] =
          ((JsPath \ "firstName").write[String] and
            (JsPath \ "middleName").writeNullable[String] and
            (JsPath \ "lastName").write[String])(unlift(IndividualDetails.unapply))
      }

      final case class OrganisationDetails(name: String)

      object OrganisationDetails {
        implicit lazy val writes: OWrites[OrganisationDetails] =
          (JsPath \ "name").write[String].contramap(_.name)
      }
    }
  }

  object Responses {
    final case class Response(dprsReference: String)

    object Response {
      implicit lazy val reads: Reads[Response] =
        (JsPath \ "success" \ "dprsReference").read[String].map(Response(_))
    }
  }
}
