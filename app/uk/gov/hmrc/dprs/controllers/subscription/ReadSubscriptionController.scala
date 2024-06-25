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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.dprs.controllers.BaseController
import uk.gov.hmrc.dprs.services.subscription.ReadSubscriptionService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReadSubscriptionController @Inject() (cc: ControllerComponents, readSubscriptionService: ReadSubscriptionService)(implicit
  executionContext: ExecutionContext
) extends BaseController(cc) {

  def call(id: String): Action[AnyContent] = Action.async { implicit request =>
    readSubscriptionService.call(id, generateRequestHeaders(request)).map {
      case Right(serviceResponse) => Ok(toJson(serviceResponse))
      case Left(error)            => handleServiceError(error)
    }
  }
}
