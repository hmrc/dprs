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

package uk.gov.hmrc.dprs.connectors.subscription

import uk.gov.hmrc.dprs.connectors.CreateSubscriptionConnector.Responses.Response
import uk.gov.hmrc.dprs.services.BaseSpec

class CreateSubscriptionConnectorResponseSpec extends BaseSpec {

  "parsing JSON should give the expected result" in {
    val rawJson =
      """
        |{
        |  "createSubscriptionForMDRResponse" : {
        |    "responseCommon" : {
        |      "status" : "OK",
        |      "processingDate" : "2024-02-15T12:04:07.011Z"
        |    },
        |    "responseDetail" : {
        |      "subscriptionID" : "5ac7862e-34d6-4491-b30d-6d823857e79a"
        |    }
        |  }
        |}
        |""".stripMargin

    rawJson should BaseSpec.beValid(Response("5ac7862e-34d6-4491-b30d-6d823857e79a"))
  }

}
