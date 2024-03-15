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

import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json.toJson
import play.api.libs.json.Writes
import uk.gov.hmrc.dprs.connectors.BaseConnector.Error
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.UpstreamErrorResponse.{Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

abstract class BaseConnector(httpClientV2: HttpClientV2) {

  def url(): URL

  def post[S, T](request: S)(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext,
    writes: Writes[S],
    httpReads: uk.gov.hmrc.http.HttpReads[T]
  ): Future[Either[Error, T]] =
    httpClientV2
      .post(url())
      .withBody(toJson(request))
      .execute[T]
      .transform {
        case Success(response)                           => Success(Right(response))
        case Failure(Upstream5xxResponse(errorResponse)) => Success(Left(Error(errorResponse.statusCode)))
        case Failure(Upstream4xxResponse(errorResponse)) => Success(Left(Error(errorResponse.statusCode)))
        case Failure(_)                                  => Success(Left(Error(BAD_REQUEST)))
      }
}

object BaseConnector {

  final case class Error(statusCode: Int)

}
