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
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Errors
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Exceptions.ResponseParsingException

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class BaseConnector(wsClient: WSClient) {

  def url(): URL

  /** We would have liked to use HttpClientV2, but when it encounters a 400 or 500 status code, the response body is inaccessible.
    */
  def post[S, T](request: S)(implicit
    executionContext: ExecutionContext,
    writes: Writes[S],
    reads: Reads[T]
  ): Future[Either[Errors, T]] =
    wsClient
      .url(url().toString)
      .post(toJson(request))
      .transform {
        case Success(wsResponse) =>
          wsResponse.status match {
            case OK | CREATED    => asResponse(wsResponse)
            case otherStatusCode => asErrors(otherStatusCode, wsResponse)
          }
        case Failure(exception) => Failure(exception)
      }

  private def asResponse[T](wsResponse: WSResponse)(implicit reads: Reads[T]): Try[Either[Errors, T]] =
    wsResponse.json
      .validate[T]
      .map(response => Success(Right(response)))
      .getOrElse(Failure(new ResponseParsingException()))

  private def asErrors[T](statusCode: Int, wsResponse: WSResponse): Try[Either[Errors, T]] =
    if (wsResponse.body.nonEmpty)
      wsResponse.json
        .validate[BaseConnector.Responses.ErrorDetail]
        .map(errorDetail => Success(Left(Errors(statusCode, Some(errorDetail)))))
        .getOrElse(Failure(new ResponseParsingException()))
    else Success(Left(Errors(statusCode)))

}

object BaseConnector {

  final case class Error(statusCode: Int)

  object Responses {

    object Exceptions {
      final class ResponseParsingException extends RuntimeException
    }

    final case class Errors(status: Int, errorDetail: Option[ErrorDetail] = None)

    final case class ErrorDetail(errorCode: Option[String])

    object ErrorDetail {
      implicit lazy val reads: Reads[ErrorDetail] =
        (JsPath \ "errorDetail" \ "errorCode").readNullable[String].map(ErrorDetail(_))
    }
  }

}
