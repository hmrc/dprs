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
import uk.gov.hmrc.dprs.services.BaseService.ErrorCodeWithStatus

abstract class BaseService {

  def errorStatusCodeConversions: Map[Int, ErrorCodeWithStatus]

  protected def convert(errorStatusCode: Int): ErrorCodeWithStatus =
    errorStatusCodeConversions.getOrElse(errorStatusCode, ErrorCodeWithStatus(errorStatusCode))

}

object BaseService {

  final case class ErrorCodeWithStatus(statusCode: Int, code: Option[String] = None)

  final case class Error(code: String)

  object Error {
    implicit val writes: OWrites[Error] =
      (JsPath \ "code").write[String].contramap(_.code)
  }

  object ErrorCodes {
    val internalServerError     = "eis-returned-internal-server-error"
    val serviceUnavailableError = "eis-returned-service-unavailable"
    val conflict                = "eis-returned-conflict"
    val notFound                = "eis-returned-not-found"
    val unauthorised            = "eis-returned-unauthorised"
    val forbidden               = "eis-returned-forbidden"
  }

}
