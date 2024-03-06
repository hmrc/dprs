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
import uk.gov.hmrc.dprs.connectors.RegistrationWithoutIdConnector.{Request, Responses}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithoutIdConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2) extends BaseConnector(httpClientV2) {

  def forIndividual(
    request: Request
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Either[BaseConnector.Error, Responses.Individual]] =
    post[RegistrationWithoutIdConnector.Request, Responses.Individual](request)

  def forOrganisation(
    request: Request
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Either[BaseConnector.Error, Responses.Organisation]] =
    post[RegistrationWithoutIdConnector.Request, Responses.Organisation](request)

  override def url(): URL = url"${appConfig.registrationWithoutIdBaseUrl}"

}

object RegistrationWithoutIdConnector {

  final case class Request(common: Request.Common, detail: Request.Detail)

  object Request {

    implicit lazy val writes: OWrites[Request] =
      ((JsPath \ "registerWithoutIDRequest" \ "requestCommon").write[Common] and
        (JsPath \ "registerWithoutIDRequest" \ "requestDetail").write[Detail])(unlift(Request.unapply))

    final case class Common(receiptDate: String, regime: String, acknowledgementReference: String)

    object Common {
      implicit val writes: OWrites[Common] =
        ((JsPath \ "receiptDate").write[String] and
          (JsPath \ "regime").write[String] and
          (JsPath \ "acknowledgementReference").write[String])(unlift(Common.unapply))
    }

    final case class Detail(individual: Option[Individual], organisation: Option[Organisation], address: Address, contactDetails: ContactDetails)

    object Detail {
      implicit lazy val writes: OWrites[Detail] =
        ((JsPath \ "individual").writeNullable[Individual] and
          (JsPath \ "organisation").writeNullable[Organisation] and
          (JsPath \ "address").write[Address] and
          (JsPath \ "contactDetails").write[ContactDetails])(unlift(Detail.unapply))
    }

    final case class Individual(firstName: String, middleName: Option[String], lastName: String, dateOfBirth: String)

    object Individual {
      implicit val writes: OWrites[Individual] =
        ((JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").writeNullable[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "dateOfBirth").write[String])(unlift(Individual.unapply))
    }

    final case class Organisation(name: String)

    object Organisation {
      implicit val writes: OWrites[Organisation] =
        (JsPath \ "organisationName").write[String].contramap(_.name)
    }

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

  object Responses {

    trait GenericDetail {
      def arn: Option[String]
      def safeId: String
    }

    trait GenericResponse {
      def common: Common
      def detail: GenericDetail
    }

    final case class Common(returnParams: Seq[ReturnParam])

    object Common {
      implicit lazy val reads: Reads[Common] =
        (JsPath \ "returnParameters").read[Seq[ReturnParam]].map(Common(_))
    }

    final case class ReturnParam(name: String, value: String)

    object ReturnParam {
      implicit val reads: Reads[ReturnParam] =
        ((JsPath \ "paramName").read[String] and
          (JsPath \ "paramValue").read[String])(ReturnParam.apply _)
    }

    final case class Individual(
      common: Common,
      detail: Individual.Detail
    ) extends GenericResponse

    object Individual {

      implicit val reads: Reads[Individual] =
        ((JsPath \ "registerWithoutIDResponse" \ "responseCommon").read[Common] and
          (JsPath \ "registerWithoutIDResponse" \ "responseDetail").read[Detail])(Individual.apply _)

      final case class Detail(safeId: String, arn: Option[String]) extends GenericDetail

      object Detail {
        implicit lazy val reads: Reads[Detail] =
          ((JsPath \ "SAFEID").read[String] and
            (JsPath \ "ARN").readNullable[String])(Detail.apply _)
      }

    }

    final case class Organisation(
      common: Common,
      detail: Organisation.Detail
    ) extends GenericResponse

    object Organisation {

      implicit lazy val reads: Reads[Organisation] =
        ((JsPath \ "registerWithoutIDResponse" \ "responseCommon").read[Common] and
          (JsPath \ "registerWithoutIDResponse" \ "responseDetail").read[Detail])(Organisation.apply _)

      final case class Detail(safeId: String, arn: Option[String]) extends GenericDetail

      object Detail {
        implicit lazy val reads: Reads[Detail] =
          ((JsPath \ "SAFEID").read[String] and
            (JsPath \ "ARN").readNullable[String])(Detail.apply _)
      }

    }

  }
}
