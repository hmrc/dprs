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

package uk.gov.hmrc.dprs.controllers.registration

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.dprs.controllers.BaseController
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.registration.RegistrationWithIdService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithIdController @Inject() (cc: ControllerComponents, registrationWithIdService: RegistrationWithIdService)(implicit
  executionContext: ExecutionContext
) extends BaseController(cc) {

  def forIndividual(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[RegistrationWithIdService.Requests.Individual] match {
      case JsSuccess(requestForIndividual, _) =>
        registrationWithIdService.registerIndividual(requestForIndividual).map {
          case Right(responseForIndividual) => Ok(toJson(responseForIndividual))
          case Left(error)                  => handleServiceError(error)
        }
      case JsError(errors) => Future.successful(BadRequest(toJson(convertForIndividual(errors))))
    }
  }

  def forOrganisation(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[RegistrationWithIdService.Requests.Organisation] match {
      case JsSuccess(requestForIndividual, _) =>
        registrationWithIdService.registerOrganisation(requestForIndividual).map {
          case Right(responseForIndividual) => Ok(toJson(responseForIndividual))
          case Left(error)                  => handleServiceError(error)
        }
      case JsError(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(toJson(convertForOrganisation(errors))))
    }
  }

  private def convertForIndividual(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[BaseService.Error] =
    convert(
      errors,
      Map(
        "/id/type"     -> "invalid-id-type",
        "/id/value"    -> "invalid-id-value",
        "/firstName"   -> "invalid-first-name",
        "/middleName"  -> "invalid-middle-name",
        "/lastName"    -> "invalid-last-name",
        "/dateOfBirth" -> "invalid-date-of-birth"
      )
    )

  private def convertForOrganisation(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[BaseService.Error] =
    convert(
      errors,
      Map(
        "/id/type"  -> "invalid-id-type",
        "/id/value" -> "invalid-id-value",
        "/name"     -> "invalid-name",
        "/type"     -> "invalid-type"
      )
    )

}
