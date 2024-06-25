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

package uk.gov.hmrc.dprs.controllers.subscription

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.dprs.controllers.BaseController
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.subscription.CreateSubscriptionService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateSubscriptionController @Inject() (cc: ControllerComponents, createSubscriptionService: CreateSubscriptionService)(implicit
  executionContext: ExecutionContext
) extends BaseController(cc) {

  def call(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[CreateSubscriptionService.Requests.Request] match {
      case JsSuccess(serviceRequest, _) =>
        createSubscriptionService.call(serviceRequest, generateRequestHeaders(request)).map {
          case Right(serviceResponse) => Ok(toJson(serviceResponse))
          case Left(error)            => handleServiceError(error)
        }
      case JsError(errors) => Future.successful(BadRequest(toJson(convert(errors))))
    }
  }

  private def convert(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[BaseService.Error] =
    convert(
      errors,
      Map(
        "/id/type"                  -> "invalid-id-type",
        "/id/value"                 -> "invalid-id-value",
        "/name"                     -> "invalid-name",
        "/contacts"                 -> "invalid-number-of-contacts",
        "/contacts(#)/type"         -> "invalid-contact-#-type",
        "/contacts(#)/name"         -> "invalid-contact-#-name",
        "/contacts(#)/firstName"    -> "invalid-contact-#-first-name",
        "/contacts(#)/middleName"   -> "invalid-contact-#-middle-name",
        "/contacts(#)/lastName"     -> "invalid-contact-#-last-name",
        "/contacts(#)/landline"     -> "invalid-contact-#-landline",
        "/contacts(#)/mobile"       -> "invalid-contact-#-mobile",
        "/contacts(#)/emailAddress" -> "invalid-contact-#-email-address"
      )
    )

}
