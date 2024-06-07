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
import play.api.mvc.{ControllerComponents, Request, Result}
import uk.gov.hmrc.dprs.connectors.BaseBackendConnector
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.{Error, ErrorResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID

abstract class BaseController(cc: ControllerComponents) extends BackendController(cc) {

  protected def handleServiceError(errorResponse: ErrorResponse): Result =
    errorResponse match {
      case ErrorResponse(_, None)                => InternalServerError
      case ErrorResponse(statusCode, Some(code)) => Status(statusCode)(toJson(Seq(Error(code))))
    }

  protected def convert(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])],
                        fieldPatternToErrorCode: Map[String, String]
  ): Seq[BaseService.Error] = {

    /** If the path includes an array, use something like this for the json error path key:
      *
      * /ids(#)/type
      *
      * The culprit element index will replace the '#'.
      *
      * In turn, the error code template will have its '#' replaced by that index plus 1; for example "invalid-id-#-type" would become "invalid-id-1-type", if
      * the problem was found in the first element.
      */
    def toErrorCode(jsonError: String): Option[String] = {
      val patternForIndex = ".*(\\d+).*".r
      jsonError match {
        case patternForIndex(rawIndex) =>
          rawIndex.toIntOption.flatMap { index =>
            val correctedKey = jsonError.replace(index.toString, "#")
            fieldPatternToErrorCode.get(correctedKey).map(_.replace("#", (index + 1).toString))
          }
        case _ => fieldPatternToErrorCode.get(jsonError)
      }
    }

    extractSimplePaths(errors)
      .map(toErrorCode(_).map(BaseService.Error(_)))
      .toSeq
      .flatten
  }

  protected def generateRequestHeaders(request: Request[_]): BaseBackendConnector.Request.Headers = BaseBackendConnector.Request.Headers(
    authorisation = request.headers.get("authorization").getOrElse(""),
    conversationId = request.headers.get("x-conversation-id").getOrElse(UUID.randomUUID().toString),
    correlationId = request.headers.get("x-correlation-id").getOrElse(UUID.randomUUID().toString),
    forwardedHost = request.headers.get("x-forwarded-host").getOrElse(request.host)
  )

  private def extractSimplePaths(errors: scala.collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): collection.Seq[String] =
    errors
      .map(_._1)
      .map(_.path)
      .map(_.mkString)

}
