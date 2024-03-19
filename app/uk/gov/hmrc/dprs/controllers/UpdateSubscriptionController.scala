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

package uk.gov.hmrc.dprs.controllers

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.dprs.services.{BaseService, UpdateSubscriptionService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateSubscriptionController @Inject() (cc: ControllerComponents, updateSubscriptionService: UpdateSubscriptionService)(implicit
  executionContext: ExecutionContext
) extends BaseController(cc) {

  def call(id: String): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[UpdateSubscriptionService.Requests.Request] match {
      case JsSuccess(serviceRequest, _) =>
        updateSubscriptionService.call(id, serviceRequest).map {
          case Right(())   => NoContent
          case Left(error) => handleServiceError(error)
        }
      case JsError(errors) => Future.successful(BadRequest(toJson(convert(errors))))
    }
  }

  private def convert(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[BaseService.Error] =
    convert(
      errors,
      Map(
        "/name"                     -> "invalid-name",
        "/contacts"                 -> "invalid-number-of-contacts",
        "/contacts(0)/type"         -> "invalid-contact-1-type",
        "/contacts(0)/name"         -> "invalid-contact-1-name",
        "/contacts(0)/firstName"    -> "invalid-contact-1-first-name",
        "/contacts(0)/middleName"   -> "invalid-contact-1-middle-name",
        "/contacts(0)/lastName"     -> "invalid-contact-1-last-name",
        "/contacts(0)/landline"     -> "invalid-contact-1-landline",
        "/contacts(0)/mobile"       -> "invalid-contact-1-mobile",
        "/contacts(0)/emailAddress" -> "invalid-contact-1-email-address",
        "/contacts(1)/type"         -> "invalid-contact-2-type",
        "/contacts(1)/name"         -> "invalid-contact-2-name",
        "/contacts(1)/firstName"    -> "invalid-contact-2-first-name",
        "/contacts(1)/middleName"   -> "invalid-contact-2-middle-name",
        "/contacts(1)/lastName"     -> "invalid-contact-2-last-name",
        "/contacts(1)/landline"     -> "invalid-contact-2-landline",
        "/contacts(1)/mobile"       -> "invalid-contact-2-mobile",
        "/contacts(1)/emailAddress" -> "invalid-contact-2-email-address"
      )
    )

}
