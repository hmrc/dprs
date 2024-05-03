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

import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Exceptions.ResponseParsingException

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class BaseConnector(wsClient: WSClient) {

  def baseUrl(): URL

  /** We would have liked to use HttpClientV2, but when it encounters a 400 or 500 status code, the response body is inaccessible.
    */
  def post[REQUEST, RESPONSE](request: REQUEST)(implicit
    executionContext: ExecutionContext,
    writes: Writes[REQUEST],
    reads: Reads[RESPONSE]
  ): Future[Either[Error, RESPONSE]] =
    wsClient
      .url(baseUrl().toString)
      .post(toJson(request))
      .transform {
        case Success(wsResponse) => handleResponse(wsResponse)
        case Failure(exception)  => Failure(exception)
      }

  def get[RESPONSE](path: String)(implicit
    executionContext: ExecutionContext,
    reads: Reads[RESPONSE]
  ): Future[Either[Error, RESPONSE]] =
    wsClient
      .url(baseUrl().toString + "/" + path)
      .get()
      .transform {
        case Success(wsResponse) => handleResponse(wsResponse)
        case Failure(exception)  => Failure(exception)
      }

  private def handleResponse[RESPONSE](wsResponse: WSResponse)(implicit reads: Reads[RESPONSE]): Try[Either[Error, RESPONSE]] = wsResponse.status match {
    case OK | CREATED    => asSuccessfulResponse(wsResponse)
    case otherStatusCode => asErrors(otherStatusCode, wsResponse)
  }

  private def asSuccessfulResponse[RESPONSE](wsResponse: WSResponse)(implicit reads: Reads[RESPONSE]): Try[Either[Error, RESPONSE]] =
    wsResponse.json
      .validate[RESPONSE]
      .map(response => Success(Right(response)))
      .getOrElse(Failure(new ResponseParsingException(wsResponse.body)))

  private def asErrors[RESPONSE](statusCode: Int, wsResponse: WSResponse): Try[Either[Error, RESPONSE]] =
    if (wsResponse.body.nonEmpty)
      Try(wsResponse.json.validate[BaseConnector.Responses.ErrorDetail] match {
        case JsSuccess(errorDetail, _) => Success(Left(Error(statusCode, Some(errorDetail))))
        case JsError(_)                => Failure(new ResponseParsingException(wsResponse.body))
      }).getOrElse(Failure(new ResponseParsingException(wsResponse.body)))
    else Success(Left(Error(statusCode)))

}

object BaseConnector {

  object Responses {

    object Exceptions {
      final class ResponseParsingException(body: String) extends RuntimeException(body)
    }

    final case class Error(status: Int, errorDetail: Option[ErrorDetail] = None)

    object Error {
      def unapply(error: Error): Option[(Int, Option[String])] = Some((error.status, error.errorDetail.flatMap(_.errorCode)))
    }

    final case class ErrorDetail(errorCode: Option[String])

    object ErrorDetail {
      implicit lazy val reads: Reads[ErrorDetail] =
        (JsPath \ "errorDetail" \ "errorCode").readNullable[String].map(ErrorDetail(_))
    }
  }

}
