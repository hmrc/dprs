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

package uk.gov.hmrc.dprs.services.subscription.read

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.Responses
import uk.gov.hmrc.dprs.services.ReadSubscriptionService.Responses.{Individual, Organisation}

class ReadSubscriptionServiceResponsesSpec extends BaseSpec {

  "writing to JSON should give the expected output" in {
    val response = Responses.Response(
      id = "5d10d157-26d6-4355-857b-bc691ee3145b",
      name = Some("Baumbach-Waelchi"),
      contacts = Seq(
        Individual(
          firstName = Some("Josefina"),
          middleName = None,
          lastName = Some("Zieme"),
          landline = Some("687394104"),
          mobile = Some("73744443225"),
          emailAddress = "christopher.wisoky@example.com"
        ),
        Organisation(
          name = "Daugherty, Mante and Rodriguez",
          landline = None,
          mobile = None,
          emailAddress = "cody.halvorson@example.com"
        )
      )
    )
    val json = toJson(response)

    json should beSameAs(
      """
        |{
        |    "id": "5d10d157-26d6-4355-857b-bc691ee3145b",
        |    "name": "Baumbach-Waelchi",
        |    "contacts": [
        |        {
        |            "type": "I",
        |            "firstName": "Josefina",
        |            "middleName": null,
        |            "lastName": "Zieme",
        |            "landline": "687394104",
        |            "mobile": "73744443225",
        |            "emailAddress": "christopher.wisoky@example.com"
        |        },
        |        {
        |            "type": "O",
        |            "name": "Daugherty, Mante and Rodriguez",
        |            "landline": null,
        |            "mobile": null,
        |            "emailAddress": "cody.halvorson@example.com"
        |        }
        |    ]
        |}
        |""".stripMargin
    )
  }

}
