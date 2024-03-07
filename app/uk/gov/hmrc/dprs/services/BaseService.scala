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

import play.api.http.Status.{BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsPath, OWrites}
import uk.gov.hmrc.dprs.services.BaseService.ErrorCodeWithStatus

abstract class BaseService {

  protected def convert(errorStatusCode: Int): ErrorCodeWithStatus = errorStatusCode match {
    case INTERNAL_SERVER_ERROR => ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some("eis-returned-internal-server-error"))
    case SERVICE_UNAVAILABLE   => ErrorCodeWithStatus(SERVICE_UNAVAILABLE, Some("eis-returned-service-unavailable"))
    case CONFLICT              => ErrorCodeWithStatus(CONFLICT, Some("eis-returned-conflict"))
    case BAD_REQUEST           => ErrorCodeWithStatus(INTERNAL_SERVER_ERROR)
    case otherStatusCode       => ErrorCodeWithStatus(otherStatusCode)
  }

}

object BaseService {

  final case class ErrorCodeWithStatus(statusCode: Int, code: Option[String] = None)

  final case class Error(code: String)

  object Error {
    implicit val writes: OWrites[Error] =
      (JsPath \ "code").write[String].contramap(_.code)
  }

}
