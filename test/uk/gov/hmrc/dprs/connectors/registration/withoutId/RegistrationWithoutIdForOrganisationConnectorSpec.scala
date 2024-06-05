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

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector
import uk.gov.hmrc.dprs.connectors.registration.withoutId.RegistrationWithoutIdConnector.Response
import uk.gov.hmrc.dprs.connectors.registration.withoutId.RegistrationWithoutIdForOrganisationConnector.Request
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs

class RegistrationWithoutIdForOrganisationConnectorSpec extends BaseSpec {

  "when" - {
    "writing the request object, we should generate the expected JSON request" in {
      val request = Request(
        common = RegistrationConnector.Request.Common(receiptDate = "2024-02-15T11:32:43.364Z",
                                                      regime = "MDR",
                                                      acknowledgementReference = "0badb375-30dd-41c8-821b-e7d92ebd2ce4"
        ),
        detail = Request.Detail(
          name = "Dyson",
          address = RegistrationWithoutIdConnector.Request.Address(lineOne = "34 Park Lane",
                                                                   lineTwo = "Building A",
                                                                   lineThree = "Suite 100",
                                                                   lineFour = Some("Manchester"),
                                                                   postalCode = Some("M54 1MQ"),
                                                                   countryCode = "GB"
          ),
          contactDetails = RegistrationWithoutIdConnector.Request.ContactDetails(landline = Some("747663966"),
                                                                                 mobile = Some("38390756243"),
                                                                                 fax = Some("58371813020"),
                                                                                 emailAddress = Some("dyson@example.com")
          )
        )
      )

      val json = toJson(request)

      json should beSameAs(s"""
                              |{
                              |  "registerWithoutIDRequest": {
                              |    "requestCommon": {
                              |      "receiptDate": "2024-02-15T11:32:43.364Z",
                              |      "regime": "MDR",
                              |      "acknowledgementReference": "0badb375-30dd-41c8-821b-e7d92ebd2ce4",
                              |      "requestParameters": []
                              |    },
                              |    "requestDetail": {
                              |      "organisation": {
                              |        "organisationName": "Dyson"
                              |      },
                              |      "address": {
                              |        "addressLine1": "34 Park Lane",
                              |        "addressLine2": "Building A",
                              |        "addressLine3": "Suite 100",
                              |        "addressLine4": "Manchester",
                              |        "postalCode": "M54 1MQ",
                              |        "countryCode": "GB"
                              |      },
                              |      "contactDetails": {
                              |        "phoneNumber": "747663966",
                              |        "mobileNumber": "38390756243",
                              |        "faxNumber": "58371813020",
                              |        "emailAddress": "dyson@example.com"
                              |      }
                              |    }
                              |  }
                              |}
                              |""".stripMargin)
    }
    "reading the JSON response, we should generate the expected response object" in {
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
        Response(
          common = RegistrationConnector.Response.Common(returnParams = Seq(RegistrationConnector.Response.Common.ReturnParam("SAP_NUMBER", "1960629967"))),
          detail = Response.Detail(safeId = "XE0000200775706", arn = Some("WARN3849921"))
        )
      )
    }
  }
}
