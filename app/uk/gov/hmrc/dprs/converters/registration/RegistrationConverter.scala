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

package uk.gov.hmrc.dprs.converters.registration

import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector.Request.Common.RequestParameter
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator

import java.time.{Clock, Instant}

abstract class RegistrationConverter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator) {

  private val regime = "DPRS"

  final protected def generateRequestCommon(): RegistrationConnector.Request.Common = RegistrationConnector.Request.Common(
    receiptDate = Instant.now(clock).toString,
    regime = regime,
    acknowledgementReference = acknowledgementReferenceGenerator.generate(),
    requestParameters = Seq(RequestParameter("REGIME", regime))
  )
}
