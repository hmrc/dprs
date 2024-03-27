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
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, OWrites, Reads}
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector.Requests.Request
import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector.Responses.Contact.{IndividualDetails, OrganisationDetails}
import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector.Responses.Response
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URL
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReadSubscriptionConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2) extends BaseConnector(httpClientV2) {

  def call(request: Request)(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Either[BaseConnector.Error, Response]] =
    post[Request, Response](request)

  override def url(): URL = url"${appConfig.readSubscriptionBaseUrl}"
}

object ReadSubscriptionConnector {

  val connectorPath: String = "/dac6/dct70d/v1"
  val connectorName: String = "read-subscription"

  object Requests {

    final case class Request(common: Requests.Common, detail: Requests.Detail)

    final case class Common(receiptDate: String, regime: String, acknowledgementReference: String, originatingSystem: String)

    final case class Detail(idType: String, idNumber: String)

    object Request {
      implicit lazy val writes: OWrites[Request] =
        ((JsPath \ "displaySubscriptionForMDRRequest" \ "requestCommon").write[Common] and
          (JsPath \ "displaySubscriptionForMDRRequest" \ "requestDetail").write[Detail])(unlift(Request.unapply))
    }

    object Common {
      implicit val writes: OWrites[Common] =
        ((JsPath \ "receiptDate").write[String] and
          (JsPath \ "regime").write[String] and
          (JsPath \ "acknowledgementReference").write[String] and
          (JsPath \ "originatingSystem").write[String])(unlift(Common.unapply))
    }

    object Detail {
      implicit lazy val writes: OWrites[Detail] =
        ((JsPath \ "IDType").write[String] and
          (JsPath \ "IDNumber").write[String])(unlift(Detail.unapply))
    }
  }

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
        ((JsPath \ "displaySubscriptionForMDRResponse" \ "responseDetail" \ "subscriptionID").read[String] and
          (JsPath \ "displaySubscriptionForMDRResponse" \ "responseDetail" \ "tradingName").readNullable[String] and
          (JsPath \ "displaySubscriptionForMDRResponse" \ "responseDetail" \ "isGBUser").read[Boolean] and
          (JsPath \ "displaySubscriptionForMDRResponse" \ "responseDetail" \ "primaryContact").readNullable[Contact] and
          (JsPath \ "displaySubscriptionForMDRResponse" \ "responseDetail" \ "secondaryContact").readNullable[Contact])(Response.apply _)
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
