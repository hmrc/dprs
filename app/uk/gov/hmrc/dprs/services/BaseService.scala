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

package uk.gov.hmrc.dprs.services

import play.api.libs.json.{JsPath, OWrites}
import uk.gov.hmrc.dprs.connectors.BaseConnector
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse

abstract class BaseService {

  protected def convert(connectorError: BaseConnector.Responses.Error): ErrorResponse

}

object BaseService {

  final case class ErrorResponse(statusCode: Int, code: Option[String] = None)

  final case class Error(code: String)

  object Error {
    implicit val writes: OWrites[Error] =
      (JsPath \ "code").write[String].contramap(_.code)
  }

  object ErrorCodes {
    val conflict                = "eis-returned-conflict"
    val couldNotBeProcessed     = "eis-returned-could-not-be-processed"
    val duplicateSubmission     = "eis-returned-duplicate-submission"
    val forbidden               = "eis-returned-forbidden"
    val internalError           = "eis-returned-internal-error"
    val internalServerError     = "eis-returned-internal-server-error"
    val notFound                = "eis-returned-not-found"
    val serviceUnavailableError = "eis-returned-service-unavailable"
    val unauthorised            = "eis-returned-unauthorised"
    val badGateway              = "eis-returned-bad-gateway"
  }

}
