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
import uk.gov.hmrc.dprs.connectors.registration.withId.RegistrationWithIdForOrganisationConnector.{Request, Response}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.{beSameAs, beValid}

class RegistrationWithIdForOrganisationConnectorSpec extends BaseSpec {

  "when" - {
    "writing the request object, we should generate the expected JSON request" in {
      val request = Request(
        common = RegistrationConnector.Request.Common(receiptDate = "2024-02-15T11:46:47.516Z",
                                                      regime = "MDR",
                                                      acknowledgementReference = "b79efb02-bc04-401b-9833-112d701c39bb"
        ),
        detail = Request.Detail(
          idType = "UTR",
          idNumber = "1234567890",
          requiresNameMatch = true,
          isAnAgent = false,
          name = "Dyson",
          _type = "0004"
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
          |      "organisation" : {
          |        "organisationName" : "Dyson",
          |        "isAGroup" : false,
          |        "organisationType" : "Unincorporated Body",
          |        "code" : "0003"
          |      },
          |      "address" : {
          |        "addressLine1" : "2627 Gus Hill",
          |        "addressLine2" : "Apt. 898",
          |        "addressLine4" : "West Corrinamouth",
          |        "postalCode" : "OX2 3HD",
          |        "countryCode" : "AD"
          |      },
          |      "contactDetails" : {
          |        "phoneNumber" : "176905117",
          |        "mobileNumber" : "62281724761",
          |        "faxNumber" : "08959633679",
          |        "emailAddress" : "edward.goodenough@example.com"
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
            name = "Dyson",
            typeCode = Some("0003"),
            address = RegistrationWithIdConnector.Response.Address(lineOne = "2627 Gus Hill",
                                                                   lineTwo = Some("Apt. 898"),
                                                                   lineThree = None,
                                                                   lineFour = Some("West Corrinamouth"),
                                                                   postalCode = "OX2 3HD",
                                                                   countryCode = "AD"
            ),
            contactDetails = RegistrationWithIdConnector.Response.ContactDetails(landline = Some("176905117"),
                                                                                 mobile = Some("62281724761"),
                                                                                 fax = Some("08959633679"),
                                                                                 emailAddress = Some("edward.goodenough@example.com")
            )
          )
        )
      )
    }

  }
}
