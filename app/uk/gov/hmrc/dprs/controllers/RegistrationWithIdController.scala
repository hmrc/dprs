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

import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.dprs.services.RegistrationService
import uk.gov.hmrc.dprs.services.RegistrationService.Responses
import uk.gov.hmrc.dprs.services.RegistrationService.Responses.{Error, ErrorCodeWithStatus}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWithIdController @Inject() (cc: ControllerComponents, registrationService: RegistrationService)(implicit executionContext: ExecutionContext)
    extends BackendController(cc) {

  def forIndividual(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[RegistrationService.Requests.Individual] match {
      case JsSuccess(requestForIndividual, _) =>
        registrationService.registerIndividual(requestForIndividual).map {
          case Right(responseForIndividual) => Ok(Json.toJson(responseForIndividual))
          case Left(error)                  => handleServiceError(error)
        }
      case JsError(errors) => Future.successful(BadRequest(Json.toJson(convertForIndividual(errors))))
    }
  }

  def forOrganisation(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body.validate[RegistrationService.Requests.Organisation] match {
      case JsSuccess(requestForIndividual, _) =>
        registrationService.registerOrganisation(requestForIndividual).map {
          case Right(responseForIndividual) => Ok(Json.toJson(responseForIndividual))
          case Left(error)                  => handleServiceError(error)
        }
      case JsError(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]) =>
        Future.successful(BadRequest(Json.toJson(convertForOrganisation(errors))))
    }
  }

  private def handleServiceError(errorCodeWithStatus: ErrorCodeWithStatus): Result =
    errorCodeWithStatus match {
      case ErrorCodeWithStatus(_, None)                => InternalServerError
      case ErrorCodeWithStatus(statusCode, Some(code)) => Status(statusCode)(Json.toJson(Seq(Error(code))))
    }

  private def convertForIndividual(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[Responses.Error] =
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

  private def convertForOrganisation(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): Seq[Responses.Error] =
    convert(
      errors,
      Map(
        "/id/type"  -> "invalid-id-type",
        "/id/value" -> "invalid-id-value",
        "/name"     -> "invalid-name",
        "/type"     -> "invalid-type"
      )
    )

  private def convert(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])],
                      fieldToErrorCode: Map[String, String]
  ): Seq[Responses.Error] =
    extractSimplePaths(errors)
      .map(fieldToErrorCode.get(_).map(Error(_)))
      .toSeq
      .flatten

  private def extractSimplePaths(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): collection.Seq[String] =
    errors
      .map(_._1)
      .map(_.path)
      .map(_.mkString)

}
