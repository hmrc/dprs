package uk.gov.hmrc.dprs.connectors

import com.google.inject.Singleton
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, OWrites, Reads}
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector.Requests.Request
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

    final case class Response(detail: Detail)

    final case class Detail(subscriptionID: String, tradingName: String, isGBUser: Boolean,
                              primaryContact: PrimaryContact, secondaryContact: SecondaryContact)

    final case class PrimaryContact(email: String, phone: String, mobile: String, individual: Individual)

    final case class Individual(firstName: String, lastName: String)

    final case class SecondaryContact(email: String, organisation: Organisation)

    final case class Organisation(organisationName: String)

    object Response {
      implicit lazy val reads: Reads[Response] =
        (JsPath \ "displaySubscriptionForMDRResponse" \ "responseDetail").read[Detail])(Response.apply _)
    }

  }
}