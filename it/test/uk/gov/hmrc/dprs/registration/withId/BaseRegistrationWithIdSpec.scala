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

package uk.gov.hmrc.dprs.registration.withId

import uk.gov.hmrc.dprs.BaseIntegrationWithConnectorSpec

class BaseRegistrationWithIdSpec extends BaseIntegrationWithConnectorSpec {

  override val connectorPath                = "/dac6/dct70b/v1"

  override def extraApplicationConfig: Map[String, Any] = Map(
    "microservice.services.registration-with-id.host"    -> wireMockHost,
    "microservice.services.registration-with-id.port"    -> wireMockPort,
    "microservice.services.registration-with-id.context" -> connectorPath
  )

}
