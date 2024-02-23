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
import uk.gov.hmrc.dprs.connectors.RegistrationWithIdConnector.{Request, Responses}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationWithIdConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2) extends BaseConnector(httpClientV2) {

  def forIndividual(
    request: Request
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Either[BaseConnector.Error, Responses.Individual]] =
    post[RegistrationWithIdConnector.Request, Responses.Individual](request)

  def forOrganisation(
    request: Request
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Either[BaseConnector.Error, Responses.Organisation]] =
    post[RegistrationWithIdConnector.Request, Responses.Organisation](request)

  override def url() = url"${appConfig.registrationWithIdBaseUrl}"

}

object RegistrationWithIdConnector {

  final case class Request(common: Request.Common, detail: Request.Detail)

  object Request {

    implicit lazy val writes: OWrites[Request] =
      ((JsPath \ "registerWithIDRequest" \ "requestCommon").write[Common] and
        (JsPath \ "registerWithIDRequest" \ "requestDetail").write[Detail])(unlift(Request.unapply))

    final case class Common(receiptDate: String, regime: String, acknowledgementReference: String)

    object Common {
      implicit val writes: OWrites[Common] =
        ((JsPath \ "receiptDate").write[String] and
          (JsPath \ "regime").write[String] and
          (JsPath \ "acknowledgementReference").write[String])(unlift(Common.unapply))
    }

    final case class Detail(idType: String,
                            idNumber: String,
                            requiresNameMatch: Boolean,
                            isAnAgent: Boolean,
                            individual: Option[Individual],
                            organisation: Option[Organisation]
    )

    object Detail {
      implicit lazy val writes: OWrites[Detail] =
        ((JsPath \ "IDType").write[String] and
          (JsPath \ "IDNumber").write[String] and
          (JsPath \ "requiresNameMatch").write[Boolean] and
          (JsPath \ "isAnAgent").write[Boolean] and
          (JsPath \ "individual").writeNullable[Individual] and
          (JsPath \ "organisation").writeNullable[Organisation])(unlift(Detail.unapply))
    }

    final case class Individual(firstName: String, middleName: Option[String], lastName: String, dateOfBirth: String)

    object Individual {
      implicit val writes: OWrites[Individual] =
        ((JsPath \ "firstName").write[String] and
          (JsPath \ "middleName").writeNullable[String] and
          (JsPath \ "lastName").write[String] and
          (JsPath \ "dateOfBirth").write[String])(unlift(Individual.unapply))
    }

    final case class Organisation(name: String, _type: String)

    object Organisation {
      implicit val writes: OWrites[Organisation] =
        ((JsPath \ "organisationName").write[String] and
          (JsPath \ "organisationType").write[String])(unlift(Organisation.unapply))
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

    final case class Individual(
      common: Common,
      detail: Individual.Detail
    ) extends GenericResponse

    object Individual {

      implicit val reads: Reads[Individual] =
        ((JsPath \ "registerWithIDResponse" \ "responseCommon").read[Common] and
          (JsPath \ "registerWithIDResponse" \ "responseDetail").read[Detail])(Individual.apply _)

      final case class Detail(safeId: String,
                              arn: Option[String],
                              firstName: String,
                              middleName: Option[String],
                              lastName: String,
                              dateOfBirth: Option[String],
                              address: Address,
                              contactDetails: ContactDetails
      ) extends GenericDetail

      object Detail {
        implicit lazy val reads: Reads[Detail] = ((JsPath \ "SAFEID").read[String] and
          (JsPath \ "ARN").readNullable[String] and
          (JsPath \ "individual" \ "firstName").read[String] and
          (JsPath \ "individual" \ "middleName").readNullable[String] and
          (JsPath \ "individual" \ "lastName").read[String] and
          (JsPath \ "individual" \ "dateOfBirth").readNullable[String] and
          (JsPath \ "address").read[Address] and
          (JsPath \ "contactDetails").read[ContactDetails])(Detail.apply _)
      }

    }

    final case class Organisation(
      common: Common,
      detail: Organisation.Detail
    ) extends GenericResponse

    object Organisation {

      implicit lazy val reads: Reads[Organisation] =
        ((JsPath \ "registerWithIDResponse" \ "responseCommon").read[Common] and
          (JsPath \ "registerWithIDResponse" \ "responseDetail").read[Detail])(Organisation.apply _)

      final case class Detail(safeId: String, arn: Option[String], name: String, typeCode: Option[String], address: Address, contactDetails: ContactDetails)
          extends GenericDetail

      object Detail {
        implicit lazy val reads: Reads[Detail] =
          ((JsPath \ "SAFEID").read[String] and
            (JsPath \ "ARN").readNullable[String] and
            (JsPath \ "organisation" \ "organisationName").read[String] and
            (JsPath \ "organisation" \ "code").readNullable[String] and
            (JsPath \ "address").read[Address] and
            (JsPath \ "contactDetails").read[ContactDetails])(Detail.apply _)
      }

    }

  }
}
