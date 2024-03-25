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

package uk.gov.hmrc.dprs.connectors

import com.google.inject.Singleton
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.UpdateSubscriptionConnector.Requests.Contact.{IndividualDetails, OrganisationDetails}
import uk.gov.hmrc.dprs.connectors.UpdateSubscriptionConnector.Requests.Request
import uk.gov.hmrc.dprs.connectors.UpdateSubscriptionConnector.Responses.Response
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateSubscriptionConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2) extends BaseConnector(httpClientV2) {

  def call(request: Request)(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Either[BaseConnector.Error, Response]] =
    post[Request, Response](request)

  override def url(): URL = url"${appConfig.updateSubscriptionBaseUrl}"

}

object UpdateSubscriptionConnector {

  val connectorPath: String = "/dac6/dct70e/v1"
  val connectorName: String = "update-subscription"

  object Requests {

    final case class Request(common: Requests.Common, detail: Requests.Detail)

    final case class Common(receiptDate: String, regime: String, acknowledgementReference: String, originatingSystem: String)

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "updateSubscriptionForMDRRequest" \ "requestCommon").write[Common] and
          (JsPath \ "updateSubscriptionForMDRRequest" \ "requestDetail").write[Detail])(unlift(Request.unapply))
    }

    object Common {
      implicit val writes: OWrites[Common] =
        ((JsPath \ "receiptDate").write[String] and
          (JsPath \ "regime").write[String] and
          (JsPath \ "acknowledgementReference").write[String] and
          (JsPath \ "originatingSystem").write[String])(unlift(Common.unapply))
    }

    final case class Detail(idType: String,
                            idNumber: String,
                            tradingName: Option[String],
                            isGBUser: Boolean,
                            primaryContact: Contact,
                            secondaryContact: Option[Contact]
    )

    object Detail {
      implicit lazy val writes: OWrites[Detail] =
        ((JsPath \ "IDType").write[String] and
          (JsPath \ "IDNumber").write[String] and
          (JsPath \ "tradingName").writeNullable[String] and
          (JsPath \ "isGBUser").write[Boolean] and
          (JsPath \ "primaryContact").write[Contact] and
          (JsPath \ "secondaryContact").writeNullable[Contact])(unlift(Detail.unapply))
    }

    final case class Contact(
      landline: Option[String],
      mobile: Option[String],
      emailAddress: String,
      individualDetails: Option[IndividualDetails],
      organisationDetails: Option[OrganisationDetails]
    )

    object Contact {
      implicit lazy val writes: OWrites[Contact] =
        ((JsPath \ "phone").writeNullable[String] and
          (JsPath \ "mobile").writeNullable[String] and
          (JsPath \ "email").write[String] and
          (JsPath \ "individual").writeNullable[IndividualDetails] and
          (JsPath \ "organisation").writeNullable[OrganisationDetails])(unlift(Contact.unapply))

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
          (JsPath \ "organisationName").write[String].contramap(_.name)
      }
    }
  }

  object Responses {

    final case class Response(id: String)

    object Response {
      implicit lazy val reads: Reads[Response] =
        (JsPath \ "updateSubscriptionForMDRResponse" \ "responseDetail" \ "subscriptionID").read[String].map(Response(_))
    }

  }
}
