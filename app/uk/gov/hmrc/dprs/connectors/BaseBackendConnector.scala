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

package uk.gov.hmrc.dprs.connectors

import play.api.libs.json.{Reads, Writes}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error

import java.time.{Clock, Instant}
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseBackendConnector(wsClient: WSClient, clock: Clock) extends BaseConnector(wsClient) {

  def post[REQUEST, RESPONSE](request: REQUEST, requestHeaders: BaseBackendConnector.Request.Headers)(implicit
    executionContext: ExecutionContext,
    writes: Writes[REQUEST],
    reads: Reads[RESPONSE]
  ): Future[Either[Error, RESPONSE]] =
    post(request, generateHeaders(requestHeaders))

  def get[RESPONSE](path: String, requestHeaders: BaseBackendConnector.Request.Headers)(implicit
    executionContext: ExecutionContext,
    reads: Reads[RESPONSE]
  ): Future[Either[Error, RESPONSE]] =
    get(path, generateHeaders(requestHeaders))

  private def generateHeaders(requestHeaders: BaseBackendConnector.Request.Headers): Map[String, String] =
    Map(
      "accept"            -> "application/json",
      "authorization"     -> requestHeaders.authorisation,
      "date"              -> Instant.now(clock).toString,
      "x-conversation-id" -> requestHeaders.conversationId,
      "x-correlation-id"  -> requestHeaders.correlationId,
      "x-forwarded-host"  -> requestHeaders.forwardedHost
    )
}

object BaseBackendConnector {
  val connectorName: String = "backend"

  object Request {
    final case class Headers(
      authorisation: String,
      conversationId: String,
      correlationId: String,
      forwardedHost: String
    )
  }
}
