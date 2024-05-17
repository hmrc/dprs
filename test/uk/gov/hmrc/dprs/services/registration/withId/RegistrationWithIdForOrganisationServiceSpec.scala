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
import uk.gov.hmrc.dprs.services.registration.withId.RegistrationWithIdForOrganisationService.{Request, Response}

import scala.collection.immutable

class RegistrationWithIdForOrganisationServiceSpec extends BaseSpec {

  "parsing JSON should give the expected result, when the request is" - {
    "valid" - {
      "with a recognised id type" in {
        val types =
          Table(
            ("ID Type", "Expected ID Type (Raw)"),
            ("UTR", "UTR"),
            ("EORI", "EORI")
          )
        forAll(types) { (idType, expectedRawIdType) =>
          val rawJson =
            s"""
               |{
               |  "id": {
               |    "type": "$idType",
               |    "value": "1234567890"
               |  },
               |  "name": "Dyson",
               |  "type": "CorporateBody"
               |}
               |""".stripMargin

          val expectedIdType = Request.RequestIdType.all.find(_.toString == expectedRawIdType).get
          rawJson should beValid(
            Request(id = Request.RequestId(expectedIdType, "1234567890"), "Dyson", Request.Type.CorporateBody)
          )
        }
      }
      "with a recognised type" in {
        val types =
          Table(
            ("Type", "Expected Type (Raw)"),
            ("NotSpecified", "NotSpecified"),
            ("Partnership", "Partnership"),
            ("LimitedLiabilityPartnership", "LimitedLiabilityPartnership"),
            ("CorporateBody", "CorporateBody"),
            ("UnincorporatedBody", "UnincorporatedBody")
          )
        forAll(types) { (_type, expectedRawType) =>
          val rawJson =
            s"""
               |{
               |  "id": {
               |    "type": "UTR",
               |    "value": "1234567890"
               |  },
               |  "name": "Dyson",
               |  "type": "${_type}"
               |}
               |""".stripMargin
          val expectedType = Request.Type.all.find(_.toString == expectedRawType).get
          rawJson should beValid(
            Request(id = Request.RequestId(Request.RequestIdType.UTR, "1234567890"), "Dyson", expectedType)
          )
        }
      }
    }
    "invalid, due to" - {
      "the id, which" - {
        "is missing" in {
          val rawJson =
            """
              |{
              |  "name": "Dyson",
              |  "type": "CorporateBody"
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
                |    "value": "1234567890"
                |  },
                |  "name": "Dyson",
                |  "type": "CorporateBody"
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
                |    "value": "1234567890"
                |  },
                |  "name": "Dyson",
                |  "type": "CorporateBody"
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
                |    "value": "1234567890"
                |  },
                |  "name": "Dyson",
                |  "type": "CorporateBody"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
          "unrecognised" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "NINO",
                |    "value": "1234567890"
                |  },
                |  "name": "Dyson",
                |  "type": "CorporateBody"
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
                |    "type": "UTR"
                |  },
                |  "name": "Dyson",
                |  "type": "CorporateBody"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |  "id": {
                |    "type": "UTR",
                |    "value": ""
                |  },
                |  "name": "Dyson",
                |  "type": "CorporateBody"
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
                |  "name": "Dyson",
                |  "type": "CorporateBody"
                |}
                |""".stripMargin

            rawJson should beInvalid[Request](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
          }
        }
      }
      "the name, which is" - {
        "absent" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "UTR",
              |    "value": "1234567890"
              |  },
              |  "type": "CorporateBody"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "too short" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "UTR",
              |    "value": "1234567890"
              |  },
              |  "name": "",
              |  "type": "CorporateBody"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "UTR",
              |    "value": "1234567890"
              |  },
              |  "name": "Dyson Home Appliance Corporation Of The United Kingdom",
              |  "type": "CorporateBody"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
        }
      }
      "the type, which is" - {
        "absent" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "UTR",
              |    "value": "1234567890"
              |  },
              |  "name": "Dyson"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "blank" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "UTR",
              |    "value": "1234567890"
              |  },
              |  "name": "Dyson",
              |  "type": ""
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](
            Seq((__ \ "type", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1), JsonValidationError(immutable.Seq("error.invalid")))))
          )
        }
        "recognised, but with a different casing" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "UTR",
              |    "value": "1234567890"
              |  },
              |  "name": "Dyson",
              |  "type": "CORPORATEBODY"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
        }
        "unrecognised" in {
          val rawJson =
            """
              |{
              |  "id": {
              |    "type": "UTR",
              |    "value": "1234567890"
              |  },
              |  "name": "Dyson",
              |  "type": "Charity"
              |}
              |""".stripMargin

          rawJson should beInvalid[Request](Seq((__ \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
        }
      }
      "several mandatory fields missing" in {
        val rawJson =
          """
            |{
            |  "id": {
            |    "type": "UTR"
            |  },
            |  "name": "Dyson"
            |}
            |""".stripMargin

        rawJson should beInvalid[Request](
          Seq(
            (__ \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing")))),
            (__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
          )
        )
      }
    }
  }
  "writing the response to JSON should give the expected output" in {
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
      val _type = Response.Type.all.find(_.toString == rawType).get
      val organisation = Response(
        ids = Seq(
          RegistrationWithIdService.Response.Id("ARN", "WARN1442450"),
          RegistrationWithIdService.Response.Id("SAFE", "XE0000586571722"),
          RegistrationWithIdService.Response.Id("SAP", "8231791429")
        ),
        name = "Dyson",
        _type = _type,
        address = RegistrationWithIdService.Response.Address(lineOne = "2627 Gus Hill",
                                                             lineTwo = Some("Apt. 898"),
                                                             lineThree = None,
                                                             lineFour = Some("West Corrinamouth"),
                                                             postalCode = "OX2 3HD",
                                                             countryCode = "AD"
        ),
        contactDetails = RegistrationWithIdService.Response.ContactDetails(
          landline = Some("176905117"),
          mobile = Some("62281724761"),
          fax = Some("08959633679"),
          emailAddress = Some("edward.goodenough@example.com")
        )
      )

      val json = toJson(organisation)

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
