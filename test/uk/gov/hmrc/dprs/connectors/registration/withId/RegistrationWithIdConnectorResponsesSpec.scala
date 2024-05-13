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

import uk.gov.hmrc.dprs.connectors.registration.RegistrationWithIdConnector.Responses
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.beValid

class RegistrationWithIdConnectorResponsesSpec extends BaseSpec {

  "parsing JSON should give the expected result, when it concerns" - {
    "an individual" in {
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
        Responses.Individual(
          common = Responses.Common(returnParams = Seq(Responses.ReturnParam("SAP_NUMBER", "1960629967"))),
          detail = Responses.Individual.Detail(
            safeId = "XE0000200775706",
            arn = Some("WARN3849921"),
            firstName = "Patrick",
            middleName = Some("John"),
            lastName = "Dyson",
            dateOfBirth = Some("1970-10-04"),
            address = Responses.Address(lineOne = "26424 Cecelia Junction",
                                        lineTwo = Some("Suite 858"),
                                        lineThree = None,
                                        lineFour = Some("West Siobhanberg"),
                                        postalCode = "OX2 3HD",
                                        countryCode = "AD"
            ),
            contactDetails = Responses.ContactDetails(landline = Some("747663966"),
                                                      mobile = Some("38390756243"),
                                                      fax = Some("58371813020"),
                                                      emailAddress = Some("Patrick.Dyson@example.com")
            )
          )
        )
      )
    }
    "an organisation" in {
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
        Responses.Organisation(
          common = Responses.Common(returnParams = Seq(Responses.ReturnParam("SAP_NUMBER", "1960629967"))),
          detail = Responses.Organisation.Detail(
            safeId = "XE0000200775706",
            arn = Some("WARN3849921"),
            name = "Dyson",
            typeCode = Some("0003"),
            address = Responses.Address(lineOne = "2627 Gus Hill",
                                        lineTwo = Some("Apt. 898"),
                                        lineThree = None,
                                        lineFour = Some("West Corrinamouth"),
                                        postalCode = "OX2 3HD",
                                        countryCode = "AD"
            ),
            contactDetails = Responses.ContactDetails(landline = Some("176905117"),
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
