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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.config.AppConfig
import uk.gov.hmrc.dprs.connectors.BaseConnector
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector

import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithoutIdForOrganisationConnector @Inject() (appConfig: AppConfig, wsClient: WSClient)
    extends RegistrationWithoutIdConnector(appConfig, wsClient) {

  def call(
    request: RegistrationWithoutIdForOrganisationConnector.Request
  )(implicit executionContext: ExecutionContext): Future[Either[BaseConnector.Responses.Error, RegistrationWithoutIdConnector.Response]] =
    post[RegistrationWithoutIdForOrganisationConnector.Request, RegistrationWithoutIdConnector.Response](request)

}

object RegistrationWithoutIdForOrganisationConnector {

  final case class Request(common: RegistrationConnector.Request.Common, detail: Request.Detail)

  object Request {
    implicit lazy val writes: OWrites[Request] =
      ((JsPath \ "registerWithoutIDRequest" \ "requestCommon").write[RegistrationConnector.Request.Common] and
        (JsPath \ "registerWithoutIDRequest" \ "requestDetail").write[Detail])(unlift(Request.unapply))

    final case class Detail(
      name: String,
      address: RegistrationWithoutIdConnector.Request.Address,
      contactDetails: RegistrationWithoutIdConnector.Request.ContactDetails
    )

    object Detail {
      implicit lazy val writes: OWrites[Detail] =
        ((JsPath \ "organisation" \ "organisationName").write[String] and
          (JsPath \ "address").write[RegistrationWithoutIdConnector.Request.Address] and
          (JsPath \ "contactDetails").write[RegistrationWithoutIdConnector.Request.ContactDetails])(unlift(Detail.unapply))

    }
  }

}
