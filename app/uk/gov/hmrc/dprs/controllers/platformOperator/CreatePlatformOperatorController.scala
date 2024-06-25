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

package uk.gov.hmrc.dprs.controllers.platformOperator

import com.google.inject.Inject
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.dprs.controllers.BaseController
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.platformOperator.CreatePlatformOperatorService

import scala.concurrent.{ExecutionContext, Future}

class CreatePlatformOperatorController @Inject() (cc: ControllerComponents, createPlatformOperatorService: CreatePlatformOperatorService)(implicit
  executionContext: ExecutionContext
) extends BaseController(cc) {

  def call(subscriptionId: String): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[CreatePlatformOperatorService.Request] match {
      case JsSuccess(serviceRequest, _) =>
        createPlatformOperatorService.call(subscriptionId, serviceRequest, generateRequestHeaders(request)).map {
          case Right(Some(response)) => Created.withHeaders(("Location", generateReadPlatformOperatorUrl(response.platformOperatorId)))
          case Right(None)           => InternalServerError
          case Left(error)           => handleServiceError(error)
        }
      case JsError(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(toJson(convert(errors))))
    }
  }

  private def generateReadPlatformOperatorUrl(platformOperatorId: String)(implicit request: Request[JsValue]): String = {
    val scheme = if (request.secure) "https" else "http"
    s"$scheme://${request.host}${request.uri}/$platformOperatorId"
  }

  private def convert(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[BaseService.Error] =
    convert(
      errors,
      Map(
        "/internalName"                         -> "invalid-internal-name",
        "/businessName"                         -> "invalid-business-name",
        "/tradingName"                          -> "invalid-trading-name",
        "/ids(#)/type"                          -> "invalid-id-#-type",
        "/ids(#)/value"                         -> "invalid-id-#-value",
        "/ids(#)/countryCodeOfIssue"            -> "invalid-id-#-country-code-of-issue",
        "/contacts"                             -> "invalid-number-of-contacts",
        "/contacts(#)/name"                     -> "invalid-contact-#-name",
        "/contacts(#)/phone"                    -> "invalid-contact-#-phone",
        "/contacts(#)/emailAddress"             -> "invalid-contact-#-email-address",
        "/address/lineOne"                      -> "invalid-address-line-one",
        "/address/lineTwo"                      -> "invalid-address-line-two",
        "/address/lineThree"                    -> "invalid-address-line-three",
        "/address/lineFour"                     -> "invalid-address-line-four",
        "/address/postalCode"                   -> "invalid-address-postal-code",
        "/address/countryCode"                  -> "invalid-address-country-code",
        "/reportingNotification/type"           -> "invalid-reporting-notification-type",
        "/reportingNotification/isActiveSeller" -> "invalid-reporting-notification-is-active-seller",
        "/reportingNotification/isDueDiligence" -> "invalid-reporting-notification-is-due-diligence",
        "/reportingNotification/year"           -> "invalid-reporting-notification-year"
      )
    )
}
