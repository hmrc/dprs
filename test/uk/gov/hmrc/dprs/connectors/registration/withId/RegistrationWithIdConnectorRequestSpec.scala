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

package uk.gov.hmrc.dprs.connectors.registration.withId

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.dprs.connectors.RegistrationWithIdConnector.Request
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs

class RegistrationWithIdConnectorRequestSpec extends BaseSpec {

  "writing to JSON should give the expected object, when it concerns" - {
    "an individual" in {
      val request = Request(
        common = Request.Common(receiptDate = "2024-02-15T11:32:43.364Z", regime = "MDR", acknowledgementReference = "0badb375-30dd-41c8-821b-e7d92ebd2ce4"),
        detail = Request.Detail(
          idType = "NINO",
          idNumber = "AA000000A",
          requiresNameMatch = true,
          isAnAgent = false,
          individual = Some(
            Request.Individual(firstName = "Patrick", middleName = Some("John"), lastName = "Dyson", dateOfBirth = "1970-10-04")
          ),
          organisation = None
        )
      )

      val json = toJson(request)

      json should beSameAs(
        """
          |{
          |  "registerWithIDRequest": {
          |    "requestCommon": {
          |      "receiptDate": "2024-02-15T11:32:43.364Z",
          |      "regime": "MDR",
          |      "acknowledgementReference": "0badb375-30dd-41c8-821b-e7d92ebd2ce4"
          |    },
          |    "requestDetail": {
          |      "IDType": "NINO",
          |      "IDNumber": "AA000000A",
          |      "requiresNameMatch": true,
          |      "isAnAgent": false,
          |      "individual": {
          |        "firstName": "Patrick",
          |        "middleName": "John",
          |        "lastName": "Dyson",
          |        "dateOfBirth": "1970-10-04"
          |      }
          |    }
          |  }
          |}
          |""".stripMargin
      )

    }
    "an organisation" in {
      val request = Request(
        common = Request.Common(receiptDate = "2024-02-15T11:46:47.516Z", regime = "MDR", acknowledgementReference = "b79efb02-bc04-401b-9833-112d701c39bb"),
        detail = Request.Detail(
          idType = "UTR",
          idNumber = "1234567890",
          requiresNameMatch = true,
          isAnAgent = false,
          individual = None,
          organisation = Some(Request.Organisation(name = "Dyson", _type = "0004"))
        )
      )

      val json = toJson(request)

      json should beSameAs(
        """
          |{
          |  "registerWithIDRequest": {
          |    "requestCommon": {
          |      "receiptDate": "2024-02-15T11:46:47.516Z",
          |      "regime": "MDR",
          |      "acknowledgementReference": "b79efb02-bc04-401b-9833-112d701c39bb"
          |    },
          |    "requestDetail": {
          |      "IDType": "UTR",
          |      "IDNumber": "1234567890",
          |      "requiresNameMatch": true,
          |      "isAnAgent": false,
          |      "organisation": {
          |        "organisationName": "Dyson",
          |        "organisationType": "0004"
          |      }
          |    }
          |  }
          |}
          |""".stripMargin
      )

    }
  }
}
