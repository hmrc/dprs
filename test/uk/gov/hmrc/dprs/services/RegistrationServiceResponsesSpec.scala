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

package uk.gov.hmrc.dprs.services

import play.api.libs.json.Json
import uk.gov.hmrc.dprs.services.BaseSpec.beSameAs
import uk.gov.hmrc.dprs.services.RegistrationService.Responses

class RegistrationServiceResponsesSpec extends BaseSpec {

  "writing to JSON should give the expected object, when it concerns" - {
    "an individual" in {
      import uk.gov.hmrc.dprs.services.RegistrationService.Responses.Individual
      val individual = Individual(
        ids = Seq(
          Responses.Id("ARN", "WARN3849921"),
          Responses.Id("SAFE", "XE0000200775706"),
          Responses.Id("SAP", "1960629967")
        ),
        firstName = "Patrick",
        middleName = Some("John"),
        lastName = "Dyson",
        dateOfBirth = Some("1970-10-04"),
        address = Responses.Address(
          lineOne = "26424 Cecelia Junction",
          lineTwo = Some("Suite 858"),
          lineThree = None,
          lineFour = Some("West Siobhanberg"),
          postalCode = "OX2 3HD",
          countryCode = "AD"
        ),
        contactDetails = Responses.ContactDetails(
          landline = Some("747663966"),
          mobile = Some("38390756243"),
          fax = Some("58371813020"),
          emailAddress = Some("Patrick.Dyson@example.com")
        )
      )

      val json = Json.toJson(individual)

      json should beSameAs(
        """
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
          |    }
          |  ],
          |  "firstName": "Patrick",
          |  "middleName": "John",
          |  "lastName": "Dyson",
          |  "dateOfBirth": "1970-10-04",
          |  "address": {
          |    "lineOne": "26424 Cecelia Junction",
          |    "lineTwo": "Suite 858",
          |    "lineFour": "West Siobhanberg",
          |    "postalCode": "OX2 3HD",
          |    "countryCode": "AD"
          |  },
          |  "contactDetails": {
          |    "landline": "747663966",
          |    "mobile": "38390756243",
          |    "fax": "58371813020",
          |    "emailAddress": "Patrick.Dyson@example.com"
          |  }
          |}
          |""".stripMargin
      )

    }
    "an organisation" in {
      val types =
        Table(
          ("Type (Raw)", "Expected Type"),
          ("NotSpecified", "NotSpecified"),
          ("Partnership", "Partnership"),
          ("LimitedLiabilityPartnership", "LimitedLiabilityPartnership"),
          ("CorporateBody", "CorporateBody"),
          ("UnincorporatedBody", "UnincorporatedBody"),
          ("UnknownOrganisationType", "UnknownOrganisationType")
        )

      forAll(types) { (rawType, expectedType) =>
        val _type = Responses.Organisation.Type.all.find(_.toString == rawType).get
        val organisation = Responses.Organisation(
          ids = Seq(
            Responses.Id("ARN", "WARN1442450"),
            Responses.Id("SAFE", "XE0000586571722"),
            Responses.Id("SAP", "8231791429")
          ),
          name = "Dyson",
          _type = _type,
          address = Responses.Address(lineOne = "2627 Gus Hill",
                                      lineTwo = Some("Apt. 898"),
                                      lineThree = None,
                                      lineFour = Some("West Corrinamouth"),
                                      postalCode = "OX2 3HD",
                                      countryCode = "AD"
          ),
          contactDetails = Responses.ContactDetails(
            landline = Some("176905117"),
            mobile = Some("62281724761"),
            fax = Some("08959633679"),
            emailAddress = Some("edward.goodenough@example.com")
          )
        )

        val json = Json.toJson(organisation)

        json should beSameAs(s"""
              |{
              |  "name": "Dyson",
              |  "type": "$expectedType",
              |  "ids": [
              |    {
              |      "type": "ARN",
              |      "value": "WARN1442450"
              |    },
              |    {
              |      "type": "SAFE",
              |      "value": "XE0000586571722"
              |    },
              |    {
              |      "type": "SAP",
              |      "value": "8231791429"
              |    }
              |  ],
              |  "address": {
              |    "lineOne": "2627 Gus Hill",
              |    "lineTwo": "Apt. 898",
              |    "lineFour": "West Corrinamouth",
              |    "postalCode": "OX2 3HD",
              |    "countryCode": "AD"
              |  },
              |  "contactDetails": {
              |    "landline": "176905117",
              |    "mobile": "62281724761",
              |    "fax": "08959633679",
              |    "emailAddress": "edward.goodenough@example.com"
              |  }
              |}
              |""".stripMargin)
      }
    }
  }
}
