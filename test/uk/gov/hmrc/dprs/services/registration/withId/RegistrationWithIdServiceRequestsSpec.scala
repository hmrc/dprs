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

import play.api.libs.json._
import uk.gov.hmrc.dprs.services.BaseSpec.{beInvalid, beValid}
import uk.gov.hmrc.dprs.services.{BaseSpec, RegistrationWithIdService}

import scala.collection.immutable

class RegistrationWithIdServiceRequestsSpec extends BaseSpec {

  "parsing JSON should give the expected result, when it concerns" - {
    "an individual, which is assumed to be" - {
      import RegistrationWithIdService.Requests.Individual
      import RegistrationWithIdService.Requests.Individual.RequestId
      import RegistrationWithIdService.Requests.Individual.RequestIdType._
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
            val expectedType = Individual.RequestIdType.all.find(_.toString == expectedRawType).get
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
              Individual(id = RequestId(expectedType, "AA000000A"),
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
            Individual(id = RequestId(NINO, "AA000000A"), firstName = "Patrick", middleName = None, lastName = "Dyson", dateOfBirth = "1970-10-04")
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

            rawJson should beInvalid[Individual](Seq((__ \ "id", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

              rawJson should beInvalid[Individual](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

              rawJson should beInvalid[Individual](
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

              rawJson should beInvalid[Individual](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
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

              rawJson should beInvalid[Individual](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
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

              rawJson should beInvalid[Individual](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

              rawJson should beInvalid[Individual](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
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

              rawJson should beInvalid[Individual](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "middleName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "middleName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
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

            rawJson should beInvalid[Individual](Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

            rawJson should beInvalid[Individual](
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

            rawJson should beInvalid[Individual](
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

            rawJson should beInvalid[Individual](
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

          rawJson should beInvalid[Individual](
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
    "an organisation, which is assumed to be" - {
      import RegistrationWithIdService.Requests.Organisation
      import RegistrationWithIdService.Requests.Organisation.RequestId
      import RegistrationWithIdService.Requests.Organisation.RequestIdType._
      import uk.gov.hmrc.dprs.services.RegistrationWithIdService.Requests.Organisation.Type._
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

            val expectedIdType = Organisation.RequestIdType.all.find(_.toString == expectedRawIdType).get
            rawJson should beValid(
              Organisation(id = RequestId(expectedIdType, "1234567890"), "Dyson", CorporateBody)
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
            val expectedType = Organisation.Type.all.find(_.toString == expectedRawType).get
            rawJson should beValid(
              Organisation(id = RequestId(UTR, "1234567890"), "Dyson", expectedType)
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

            rawJson should beInvalid[Organisation](Seq((__ \ "id", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

              rawJson should beInvalid[Organisation](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

              rawJson should beInvalid[Organisation](
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

              rawJson should beInvalid[Organisation](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
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

              rawJson should beInvalid[Organisation](Seq((__ \ "id" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
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

              rawJson should beInvalid[Organisation](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

              rawJson should beInvalid[Organisation](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
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

              rawJson should beInvalid[Organisation](Seq((__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
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

            rawJson should beInvalid[Organisation](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

            rawJson should beInvalid[Organisation](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
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

            rawJson should beInvalid[Organisation](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
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

            rawJson should beInvalid[Organisation](Seq((__ \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
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

            rawJson should beInvalid[Organisation](
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

            rawJson should beInvalid[Organisation](Seq((__ \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
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

            rawJson should beInvalid[Organisation](Seq((__ \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
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

          rawJson should beInvalid[Organisation](
            Seq(
              (__ \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing")))),
              (__ \ "id" \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
            )
          )
        }
      }
    }
  }
}
