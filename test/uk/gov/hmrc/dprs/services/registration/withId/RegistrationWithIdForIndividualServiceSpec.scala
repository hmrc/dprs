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

package uk.gov.hmrc.dprs.services.registration.withId

import play.api.libs.json.Json.toJson
import play.api.libs.json.{__, JsonValidationError}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.{beInvalid, beSameAs, beValid}
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForIndividualService.{Request, Response}

import scala.collection.immutable

class RegistrationWithIdForIndividualServiceSpec extends BaseSpec {

  "parsing JSON should give the expected result, when the request is" - {
    "valid" - {
      "with an id where the type is recognised" in {
        val types =
          Table(
            ("Type", "Expected Type (Raw)"),
            ("UTR", "UTR"),
            ("NINO", "NINO"),
            ("EORI", "EORI")
          )
        forAll(types) { (_type, expectedRawType) =>
          val expectedType = Request.RequestIdType.all.find(_.toString == expectedRawType).get
          val rawJson =
            s"""
               |{
               |  "id": {
               |    "type": "${_type}",
               |    "value": "AA000000A"
               |  },
               |  "firstName": "Patrick",
               |  "middleName": "John",
               |  "lastName": "Dyson",
               |  "dateOfBirth": "1970-10-04"
               |}
               |""".stripMargin

          rawJson should beValid(
            Request(id = Request.RequestId(expectedType, "AA000000A"),
                    firstName = "Patrick",
                    middleName = Some("John"),
                    lastName = "Dyson",
                    dateOfBirth = "1970-10-04"
            )
          )
        }
      }
      "even without a middle name" in {
        val rawJson =
          """
            |{
            |  "id": {
            |    "type": "NINO",
            |    "value": "AA000000A"
            |  },
            |  "firstName": "Patrick",
            |  "lastName": "Dyson",
            |  "dateOfBirth": "1970-10-04"
            |}
            |""".stripMargin

        rawJson should beValid(
          Request(
            id = Request.RequestId(Request.RequestIdType.NINO, "AA000000A"),
            firstName = "Patrick",
            middleName = None,
            lastName = "Dyson",
            dateOfBirth = "1970-10-04"
          )
        )
      }
    }
    "invalid, due to" - {
      "the id, which" - {
        "is missing" in {
          val rawJson =
            """
              |{
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "id", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "has a type which is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "value": "AA000000A"
                |  },
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "blank" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "",
                |    "value": "AA000000A"
                |  },
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](
              Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1), JsonValidationError(immutable.Seq("error.invalid")))))
            )
          }
          "recognised, but with a different casing" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "utr",
                |    "value": "AA000000A"
                |  },
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
          "unrecognised" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "VAT",
                |    "value": "AA000000A"
                |  },
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
        }
        "has a value which is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "NINO"
                |  },
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "NINO",
                |    "value": ""
                |  },
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "UTR",
                |    "value": "XX3902342094804482044449234029408242"
                |  },
                |  "firstName": "Patrick",
                |  "middleName": "John",
                |  "lastName": "Dyson",
                |  "dateOfBirth": "1970-10-04"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
          }
        }
      }
      "the first name, which is" - {
        "absent" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "middleName": "John",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "too short" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "",
              |  "middleName": "John",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick Alexander John Fitzpatrick James",
              |  "middleName": "John",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
        }
      }
      "the middle name, which is" - {
        "too short" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "middleName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "Alexander John Fitzpatrick James Edward",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "middleName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
        }
      }
      "the last name, which is" - {
        "absent" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "too short" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "lastName": "",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "lastName": "Alexander III, Earl Of Somewhere Else",
              |  "dateOfBirth": "1970-10-04"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
        }
      }
      "the date of birth, which is" - {
        "absent" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "lastName": "Dyson"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "too short" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "lastName": "Dyson",
              |  "dateOfBirth": ""
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](
            Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1), JsonValidationError(immutable.Seq("error.invalid")))))
          )
        }
        "of an invalid format" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "10-04-1970"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](
            Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.invalid")))))
          )
        }
        "non-existent" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "NINO",
              |    "value": "AA000000A"
              |  },
              |  "firstName": "Patrick",
              |  "middleName": "John",
              |  "lastName": "Dyson",
              |  "dateOfBirth": "1977-02-29"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](
            Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.invalid")))))
          )
        }
      }
      "several mandatory fields missing" in {
        val rawJson =
          """
            |{
            |  "id": {
            |    "type": "NINO"
            |  },
            |  "middleName": "John"
            |}
            |""".stripMargin

        rawJson should beInvalid[Request](
          Seq(
            (__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.path.missing")))),
            (__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.path.missing")))),
            (__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.path.missing")))),
            (__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
          )
        )

      }
    }
  }
  "writing the response to JSON should give the expected output" in {
    val response = Response(
      ids = Seq(
        RegistrationWithIdService.Response.Id("ARN", "WARN3849921"),
        RegistrationWithIdService.Response.Id("SAFE", "XE0000200775706"),
        RegistrationWithIdService.Response.Id("SAP", "1960629967")
      ),
      firstName = "Patrick",
      middleName = Some("John"),
      lastName = "Dyson",
      dateOfBirth = Some("1970-10-04"),
      address = RegistrationWithIdService.Response.Address(
        lineOne = "26424 Cecelia Junction",
        lineTwo = Some("Suite 858"),
        lineThree = None,
        lineFour = Some("West Siobhanberg"),
        postalCode = "OX2 3HD",
        countryCode = "AD"
      ),
      contactDetails = RegistrationWithIdService.Response.ContactDetails(
        landline = Some("747663966"),
        mobile = Some("38390756243"),
        fax = Some("58371813020"),
        emailAddress = Some("Patrick.Dyson@example.com")
      )
    )

    val json = toJson(response)

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

}
