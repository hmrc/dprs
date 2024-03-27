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

import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject()(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator)
  extends BaseService {

  def call(id: String, serviceRequest: ReadSubscriptionService.Requests.Request)(implicit
                                                                                 headerCarrier: HeaderCarrier,
                                                                                 executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, Unit]] = {}

}

object ReadSubscriptionService {

  object Requests {

    final case class Request(id: Int)
  }
}
