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
import play.api.libs.json.{JsPath, JsonValidationError}
import play.api.mvc.{ControllerComponents, Result}
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.{Error, ErrorCodeWithStatus}
import uk.gov.hmrc.dprs.support.JsonErrors
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

abstract class BaseController(cc: ControllerComponents) extends BackendController(cc) {

  protected def handleServiceError(errorCodeWithStatus: ErrorCodeWithStatus): Result =
    errorCodeWithStatus match {
      case ErrorCodeWithStatus(_, None)                => InternalServerError
      case ErrorCodeWithStatus(statusCode, Some(code)) => Status(statusCode)(toJson(Seq(Error(code))))
    }

  // I changed the code to return error messages as well as codes

  protected final def convert(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])],
                        fieldToErrorCode: Map[String, String]
  ): Seq[BaseService.Error] =
    extractPathsAndDetails(errors)
      .map(t => fieldToErrorCode.get(t._1).map(BaseService.Error(_, t._2)))
      .toSeq
      .flatten

  private def extractPathsAndDetails(errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]): scala.collection.Seq[(String, Option[String])] =
    errors.map { e =>
      val path = e._1.path.mkString
      val errors = e._2
      (path, if (errors.isEmpty) None else Some(JsonErrors.get(path, errors.head.message, errors.head.args)))
    }

}
