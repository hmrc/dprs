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

package uk.gov.hmrc.dprs.services.registration.withoutId

import play.api.libs.json.Json
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs
import uk.gov.hmrc.dprs.services.RegistrationWithoutIdService.Responses.{Id, Individual, Organisation}

class RegistrationWithoutIdServiceResponsesSpec extends BaseSpec {

  "writing to JSON should give the expected output, when it concerns" - {
    "an individual" in {
      val individual = Individual(ids =
        Seq(Id("ARN", "WARN3849921"), Id("SAFE", "XE0000200775706"), Id("SAP", "1960629967"), Id("AAA", "6709b659-6be1-4bde-8a77-f42ab7b5b8ba"))
      )

      val json = Json.toJson(individual)

      json should beSameAs("""
          |{
          |  "ids": [
          |    {
          |      "type": "ARN",
          |      "value": "WARN3849921"
          |    },
          |    {
          |      "type": "SAFE",
          |      "value": "XE0000200775706"
          |    },
          |    {
          |      "type": "SAP",
          |      "value": "1960629967"
          |    },
          |    {
          |      "type": "AAA",
          |      "value": "6709b659-6be1-4bde-8a77-f42ab7b5b8ba"
          |    }
          |  ]
          |}
          |""".stripMargin)

    }
    "an organisation" in {
      val organisation = Organisation(ids =
        Seq(Id("ARN", "WARN3849921"), Id("SAFE", "XE0000200775706"), Id("SAP", "1960629967"), Id("XXX", "6709b659-6be1-4bde-8a77-f42ab7b5b8ba"))
      )

      val json = Json.toJson(organisation)
      json should beSameAs("""
          |{
          |  "ids": [
          |    {
          |      "type": "ARN",
          |      "value": "WARN3849921"
          |    },
          |    {
          |      "type": "SAFE",
          |      "value": "XE0000200775706"
          |    },
          |    {
          |      "type": "SAP",
          |      "value": "1960629967"
          |    },
          |    {
          |      "type": "XXX",
          |      "value": "6709b659-6be1-4bde-8a77-f42ab7b5b8ba"
          |    }
          |  ]
          |}
          |""".stripMargin)
    }
  }
}
