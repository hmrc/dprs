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

import uk.gov.hmrc.dprs.connectors.ReadSubscriptionConnector
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.Converter
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject()(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator,
                                        readSubscriptionConnector: ReadSubscriptionConnector)
  extends BaseService {

  private val converter = new Converter(clock, acknowledgementReferenceGenerator)
  def call(id: String)(implicit headerCarrier: HeaderCarrier,executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorCodeWithStatus, Unit]] = {
    val request = converter.convert(id)
    readSubscriptionConnector.call()
  }

}

object ReadSubscriptionService {

  class Converter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

    /** We're awaiting the specs for the underlying API; in the meantime, we'll use the one for MDR; this matches the expectations of the stub service.
      */
    private val regime = "MDR"
    private val originatingSystem = "MDTP"

    def convert(id: String): ReadSubscriptionConnector.Requests.Request = {

      ReadSubscriptionConnector.Requests.Request(
        common = generateRequestCommon(),
        detail = ReadSubscriptionConnector.Requests.Detail(
          idType = "MDR",
          idNumber = id
        )
      )
    }

    private def generateRequestCommon() = ReadSubscriptionConnector.Requests.Common(
      receiptDate = Instant.now(clock).toString,
      regime = regime,
      acknowledgementReference = acknowledgementReferenceGenerator.generate(),
      originatingSystem = originatingSystem
    )
  }
}
