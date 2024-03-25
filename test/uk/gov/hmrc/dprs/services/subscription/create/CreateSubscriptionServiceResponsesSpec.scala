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

package uk.gov.hmrc.dprs.services.subscription.create

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs
import uk.gov.hmrc.dprs.services.{BaseSpec, CreateSubscriptionService}

class CreateSubscriptionServiceResponsesSpec extends BaseSpec {

  "writing to JSON should give the expected output" in {
    val response = CreateSubscriptionService.Responses.Response(id = "820e79a9-d3af-43c1-9e2f-e36a06ed4699")

    val json = toJson(response)

    json should beSameAs(
      """
        |{
        |  "id": "820e79a9-d3af-43c1-9e2f-e36a06ed4699"
        |}
        |""".stripMargin
    )
  }

}
