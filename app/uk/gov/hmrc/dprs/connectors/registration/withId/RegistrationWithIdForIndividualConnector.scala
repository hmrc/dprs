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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.BaseConnector
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector
import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForIndividualConnector.Response

import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithIdForIndividualConnector @Inject() (appConfig: AppConfig, wsClient: WSClient) extends RegistrationWithIdConnector(appConfig, wsClient) {

  def call(
    request: RegistrationWithIdForIndividualConnector.Request
  )(implicit executionContext: ExecutionContext): Future[Either[BaseConnector.Responses.Error, Response]] =
    post[RegistrationWithIdForIndividualConnector.Request, Response](request)

}

object RegistrationWithIdForIndividualConnector {

  final case class Request(common: RegistrationConnector.Request.Common, detail: Request.Detail)

  object Request {
    implicit lazy val writes: OWrites[Request] =
      ((JsPath \ "registerWithIDRequest" \ "requestCommon").write[RegistrationConnector.Request.Common] and
        (JsPath \ "registerWithIDRequest" \ "requestDetail").write[Detail])(unlift(Request.unapply))

    final case class Detail(idType: String,
                            idNumber: String,
                            requiresNameMatch: Boolean,
                            isAnAgent: Boolean,
                            firstName: String,
                            middleName: Option[String],
                            lastName: String,
                            dateOfBirth: String
    )

    object Detail {
      implicit lazy val writes: OWrites[Detail] =
        ((JsPath \ "IDType").write[String] and
          (JsPath \ "IDNumber").write[String] and
          (JsPath \ "requiresNameMatch").write[Boolean] and
          (JsPath \ "isAnAgent").write[Boolean] and
          (JsPath \ "individual" \ "firstName").write[String] and
          (JsPath \ "individual" \ "middleName").writeNullable[String] and
          (JsPath \ "individual" \ "lastName").write[String] and
          (JsPath \ "individual" \ "dateOfBirth").write[String])(unlift(Detail.unapply))
    }

  }

  final case class Response(
    common: RegistrationWithIdConnector.Response.Common,
    detail: Response.Detail
  )

  object Response {

    implicit val reads: Reads[Response] =
      ((JsPath \ "registerWithIDResponse" \ "responseCommon").read[RegistrationWithIdConnector.Response.Common] and
        (JsPath \ "registerWithIDResponse" \ "responseDetail").read[Detail])(Response.apply _)

    final case class Detail(safeId: String,
                            arn: Option[String],
                            firstName: String,
                            middleName: Option[String],
                            lastName: String,
                            dateOfBirth: Option[String],
                            address: RegistrationWithIdConnector.Response.Address,
                            contactDetails: RegistrationWithIdConnector.Response.ContactDetails
    )

    object Detail {
      implicit lazy val reads: Reads[Detail] = ((JsPath \ "SAFEID").read[String] and
        (JsPath \ "ARN").readNullable[String] and
        (JsPath \ "individual" \ "firstName").read[String] and
        (JsPath \ "individual" \ "middleName").readNullable[String] and
        (JsPath \ "individual" \ "lastName").read[String] and
        (JsPath \ "individual" \ "dateOfBirth").readNullable[String] and
        (JsPath \ "address").read[RegistrationWithIdConnector.Response.Address] and
        (JsPath \ "contactDetails").read[RegistrationWithIdConnector.Response.ContactDetails])(Detail.apply _)
    }

  }

}
