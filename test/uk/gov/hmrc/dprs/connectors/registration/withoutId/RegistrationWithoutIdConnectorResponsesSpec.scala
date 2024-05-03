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

package uk.gov.hmrc.dprs.connectors.registration.withoutId

import uk.gov.hmrc.dprs.connectors.registration.RegistrationWithoutIdConnector.Responses
import uk.gov.hmrc.dprs.services.BaseSpec

class RegistrationWithoutIdConnectorResponsesSpec extends BaseSpec {

  "parsing JSON should give the expected result, when it concerns" - {
    "an individual" in {
      val rawJson =
        s"""
           |{
           |  "registerWithoutIDResponse" : {
           |    "responseCommon" : {
           |      "status" : "OK",
           |      "statusText" : "",
           |      "processingDate" :"2024-02-15T12:04:07.011Z",
           |      "returnParameters" : [ {
           |        "paramName" : "SAP_NUMBER",
           |        "paramValue" : "1960629967"
           |      } ]
           |    },
           |    "responseDetail" : {
           |      "SAFEID" : "XE0000200775706",
           |      "ARN" : "WARN3849921"
           |    }
           |  }
           |}
           |""".stripMargin

      rawJson should BaseSpec.beValid(
        Responses.Individual(
          common = Responses.Common(returnParams = Seq(Responses.ReturnParam("SAP_NUMBER", "1960629967"))),
          detail = Responses.Individual.Detail(safeId = "XE0000200775706", arn = Some("WARN3849921"))
        )
      )
    }
    "an organisation" in {
      val rawJson =
        s"""
           |{
           |  "registerWithoutIDResponse" : {
           |    "responseCommon" : {
           |      "status" : "OK",
           |      "statusText" : "",
           |      "processingDate" :"2024-02-15T12:04:07.011Z",
           |      "returnParameters" : [ {
           |        "paramName" : "SAP_NUMBER",
           |        "paramValue" : "1960629967"
           |      } ]
           |    },
           |    "responseDetail" : {
           |      "SAFEID" : "XE0000200775706",
           |      "ARN" : "WARN3849921"
           |    }
           |  }
           |}
           |""".stripMargin

      rawJson should BaseSpec.beValid(
        Responses.Organisation(
          common = Responses.Common(returnParams = Seq(Responses.ReturnParam("SAP_NUMBER", "1960629967"))),
          detail = Responses.Organisation.Detail(safeId = "XE0000200775706", arn = Some("WARN3849921"))
        )
      )
    }
  }
}
