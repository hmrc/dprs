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
import uk.gov.hmrc.dprs.connectors.registration.RegistrationConnector
import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForIndividualConnector.{Request, Response}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.{beSameAs, beValid}

class RegistrationWithIdForIndividualConnectorSpec extends BaseSpec {

  "when" - {
    "writing the request object, we should generate the expected JSON request" in {
      val request = Request(
        common = RegistrationConnector.Request.Common(receiptDate = "2024-02-15T11:32:43.364Z",
                                                      regime = "MDR",
                                                      acknowledgementReference = "0badb375-30dd-41c8-821b-e7d92ebd2ce4"
        ),
        detail = Request.Detail(
          idType = "NINO",
          idNumber = "AA000000A",
          requiresNameMatch = true,
          isAnAgent = false,
          firstName = "Patrick",
          middleName = Some("John"),
          lastName = "Dyson",
          dateOfBirth = "1970-10-04"
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
    "reading the JSON response, we should generate the expected response object" in {
      val rawJson =
        """
          |{
          |  "registerWithIDResponse": {
          |    "responseCommon": {
          |      "status": "OK",
          |      "statusText": "",
          |      "processingDate": "2024-02-15T12:04:07.011Z",
          |      "returnParameters": [
          |        {
          |          "paramName": "SAP_NUMBER",
          |          "paramValue": "1960629967"
          |        }
          |      ]
          |    },
          |    "responseDetail": {
          |      "SAFEID": "XE0000200775706",
          |      "ARN": "WARN3849921",
          |      "isEditable": true,
          |      "isAnAgent": false,
          |      "isAnIndividual": true,
          |      "individual": {
          |        "firstName": "Patrick",
          |        "middleName": "John",
          |        "lastName": "Dyson",
          |        "dateOfBirth": "1970-10-04"
          |      },
          |      "address": {
          |        "addressLine1": "26424 Cecelia Junction",
          |        "addressLine2": "Suite 858",
          |        "addressLine4": "West Siobhanberg",
          |        "postalCode": "OX2 3HD",
          |        "countryCode": "AD"
          |      },
          |      "contactDetails": {
          |        "phoneNumber": "747663966",
          |        "mobileNumber": "38390756243",
          |        "faxNumber": "58371813020",
          |        "emailAddress": "Patrick.Dyson@example.com"
          |      }
          |    }
          |  }
          |}
          |""".stripMargin

      rawJson should beValid(
        Response(
          common = RegistrationWithIdConnector.Response
            .Common(returnParams = Seq(RegistrationWithIdConnector.Response.Common.ReturnParam("SAP_NUMBER", "1960629967"))),
          detail = Response.Detail(
            safeId = "XE0000200775706",
            arn = Some("WARN3849921"),
            firstName = "Patrick",
            middleName = Some("John"),
            lastName = "Dyson",
            dateOfBirth = Some("1970-10-04"),
            address = RegistrationWithIdConnector.Response.Address(lineOne = "26424 Cecelia Junction",
                                                                   lineTwo = Some("Suite 858"),
                                                                   lineThree = None,
                                                                   lineFour = Some("West Siobhanberg"),
                                                                   postalCode = "OX2 3HD",
                                                                   countryCode = "AD"
            ),
            contactDetails = RegistrationWithIdConnector.Response.ContactDetails(landline = Some("747663966"),
                                                                                 mobile = Some("38390756243"),
                                                                                 fax = Some("58371813020"),
                                                                                 emailAddress = Some("Patrick.Dyson@example.com")
            )
          )
        )
      )
    }
  }
}
