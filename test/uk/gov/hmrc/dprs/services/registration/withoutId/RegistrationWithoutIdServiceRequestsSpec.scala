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

package uk.gov.hmrc.dprs.services.registration.withoutId

import play.api.libs.json.{__, JsonValidationError}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.{beInvalid, beValid}
import uk.gov.hmrc.dprs.services.RegistrationWithoutIdService.Requests.{Address, ContactDetails, Individual, Organisation}

import scala.collection.immutable

class RegistrationWithoutIdServiceRequestsSpec extends BaseSpec {

  "parsing JSON should give the expected result, when it concerns" - {
    "an individual, which is assumed to be" - {
      "valid, where" - {
        "all values are provided" in {
          val rawJson =
            """
              |{
              |    "firstName": "Patrick",
              |    "middleName": "John",
              |    "lastName": "Dyson",
              |    "dateOfBirth": "1970-10-04",
              |    "address": {
              |        "lineOne": "34 Park Lane",
              |        "lineTwo": "Building A",
              |        "lineThree": "Suite 100",
              |        "lineFour": "Manchester",
              |        "postalCode": "M54 1MQ",
              |        "countryCode": "GB"
              |    },
              |    "contactDetails": {
              |        "landline": "747663966",
              |        "mobile": "38390756243",
              |        "fax": "58371813020",
              |        "emailAddress": "Patrick.Dyson@example.com"
              |    }
              |}
              |""".stripMargin

          rawJson should beValid(
            Individual(
              firstName = "Patrick",
              middleName = Some("John"),
              lastName = "Dyson",
              dateOfBirth = "1970-10-04",
              address = Address(lineOne = "34 Park Lane",
                                lineTwo = "Building A",
                                lineThree = "Suite 100",
                                lineFour = Some("Manchester"),
                                postalCode = Some("M54 1MQ"),
                                countryCode = "GB"
              ),
              contactDetails = ContactDetails(landline = Some("747663966"),
                                              mobile = Some("38390756243"),
                                              fax = Some("58371813020"),
                                              emailAddress = Some("Patrick.Dyson@example.com")
              )
            )
          )
        }
        "only mandatory values are provided, when the country is" - {
          "inside the UK and related territories" in {

            val countryCodes =
              Table(
                ("Country Code", "Expected Country Code"),
                ("GB", "GB"),
                ("GG", "GG"),
                ("IM", "IM"),
                ("JE", "JE")
              )

            forAll(countryCodes) { (countryCode, expectedCountryCode) =>
              val rawJson =
                s"""
                  |{
                  |    "firstName": "Patrick",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "$countryCode"
                  |    },
                  |    "contactDetails": {
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beValid(
                Individual(
                  firstName = "Patrick",
                  middleName = None,
                  lastName = "Dyson",
                  dateOfBirth = "1970-10-04",
                  address = Address(
                    lineOne = "34 Park Lane",
                    lineTwo = "Building A",
                    lineThree = "Suite 100",
                    lineFour = None,
                    postalCode = Some("M54 1MQ"),
                    countryCode = expectedCountryCode
                  ),
                  contactDetails = ContactDetails(landline = None, mobile = None, fax = None, emailAddress = None)
                )
              )
            }
          }
          "the country is outside UK and related territories" in {
            val rawJson =
              """
                |{
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "countryCode": "FR"
                |    },
                |    "contactDetails": {
                |    }
                |}
                |""".stripMargin

            rawJson should beValid(
              Individual(
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                dateOfBirth = "1970-10-04",
                address = Address(
                  lineOne = "34 Park Lane",
                  lineTwo = "Building A",
                  lineThree = "Suite 100",
                  lineFour = None,
                  postalCode = None,
                  countryCode = "FR"
                ),
                contactDetails = ContactDetails(landline = None, mobile = None, fax = None, emailAddress = None)
              )
            )
          }
        }
      }
      "invalid, due to" - {
        "the first name, which is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |    "firstName": "",
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "firstName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              """
                |{
                |    "firstName": "Patrick Alexander John Fitzpatrick James",
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
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
                |    "firstName": "Patrick",
                |    "middleName": "",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "middleName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {

            val rawJson =
              """
                |{
                |    "firstName": "Patrick",
                |    "middleName": "Alexander John Fitzpatrick James Edward",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
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
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "lastName": "",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "lastName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              """
                |{
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "lastName": "Alexander III, Earl Of Somewhere Else",
                |    "dateOfBirth": "1970-10-04",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
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
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
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
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "10-04-1970",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
          "non-existent" in {
            val rawJson =
              """
                |{
                |    "firstName": "Patrick",
                |    "middleName": "John",
                |    "lastName": "Dyson",
                |    "dateOfBirth": "1977-02-29",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "Patrick.Dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Individual](Seq((__ \ "dateOfBirth", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
        }
        "the address, namely the" - {
          "first line, which is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "6000-6600 Great Peter Boulevard North",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
            }
          }
          "second line, which is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building Number Two, Northwest Corner",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
            }
          }
          "third line, which is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100, formerly the Presidential Suite",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
            }
          }
          "fourth line, which is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineFour", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester, United Kingdom of Great Britain and Northern Ireland",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "lineFour", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
            }
          }
          "country code, which is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": ""
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](
                Seq(
                  (__ \ "address" \ "countryCode",
                   Seq(JsonValidationError(immutable.Seq("error.minLength"), 1), JsonValidationError(immutable.Seq("error.invalid")))
                  )
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GBR"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](
                Seq(
                  (__ \ "address" \ "countryCode",
                   Seq(JsonValidationError(immutable.Seq("error.maxLength"), 2), JsonValidationError(immutable.Seq("error.invalid")))
                  )
                )
              )
            }
            "unrecognised" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "XX"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
            }
          }
          "postal code, which is" - {
            "expected but absent, when country code is" in {
              val countryCodes =
                Table(
                  "Country Code",
                  "GB",
                  "GG",
                  "IM",
                  "JE"
                )
              forAll(countryCodes) { countryCode =>
                val rawJson =
                  s"""
                    |{
                    |    "firstName": "Patrick",
                    |    "middleName": "John",
                    |    "lastName": "Dyson",
                    |    "dateOfBirth": "1970-10-04",
                    |    "address": {
                    |        "lineOne": "34 Park Lane",
                    |        "lineTwo": "Building A",
                    |        "lineThree": "Suite 100",
                    |        "lineFour": "Manchester",
                    |        "countryCode": "$countryCode"
                    |    },
                    |    "contactDetails": {
                    |        "landline": "747663966",
                    |        "mobile": "38390756243",
                    |        "fax": "58371813020",
                    |        "emailAddress": "Patrick.Dyson@example.com"
                    |    }
                    |}
                    |""".stripMargin

                rawJson should beInvalid[Individual](Seq((__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
              }
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "509480494049",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 10)))))
            }
          }
        }
        "the contact details, namely the" - {
          "landline number, which is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "landline", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966747663966747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "landline", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))))
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "£747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "landline", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
            }
          }
          "mobile number, which is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "mobile", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243383907562433839",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "mobile", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))))
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "£38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "mobile", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
            }
          }
          "fax number, which is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "fax", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "583718130205837181302058371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "fax", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))))
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "£58371813020",
                  |        "emailAddress": "Patrick.Dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "fax", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
            }
          }
          "email address, which is" - {
            "blank" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": ""
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "firstName": "Patrick",
                  |    "middleName": "John",
                  |    "lastName": "Dyson",
                  |    "dateOfBirth": "1970-10-04",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "postalCode": "M54 1MQ",
                  |        "countryCode": "GB"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Individual](Seq((__ \ "contactDetails" \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
            }
          }
        }
      }
    }
  }
  "an organisation, which is assumed to be" - {
    "valid, where" - {
      "all values are provided" in {
        val rawJson =
          """
            |{
            |    "name": "Dyson",
            |    "address": {
            |        "lineOne": "34 Park Lane",
            |        "lineTwo": "Building A",
            |        "lineThree": "Suite 100",
            |        "lineFour": "Manchester",
            |        "postalCode": "M54 1MQ",
            |        "countryCode": "GB"
            |    },
            |    "contactDetails": {
            |        "landline": "747663966",
            |        "mobile": "38390756243",
            |        "fax": "58371813020",
            |        "emailAddress": "dyson@example.com"
            |    }
            |}
            |""".stripMargin

        rawJson should beValid(
          Organisation(
            name = "Dyson",
            address = Address(lineOne = "34 Park Lane",
                              lineTwo = "Building A",
                              lineThree = "Suite 100",
                              lineFour = Some("Manchester"),
                              postalCode = Some("M54 1MQ"),
                              countryCode = "GB"
            ),
            contactDetails =
              ContactDetails(landline = Some("747663966"), mobile = Some("38390756243"), fax = Some("58371813020"), emailAddress = Some("dyson@example.com"))
          )
        )
      }
      "only mandatory values are provided, when the country is" - {
        "inside the UK and related territories" in {
          val countryCodes =
            Table(
              ("Country Code", "Expected Country Code"),
              ("GB", "GB"),
              ("GG", "GG"),
              ("IM", "IM"),
              ("JE", "JE")
            )

          forAll(countryCodes) { (countryCode, expectedCountryCode) =>
            val rawJson =
              s"""
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "$countryCode"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beValid(
              Organisation(
                name = "Dyson",
                address = Address(
                  lineOne = "34 Park Lane",
                  lineTwo = "Building A",
                  lineThree = "Suite 100",
                  lineFour = Some("Manchester"),
                  postalCode = Some("M54 1MQ"),
                  countryCode = expectedCountryCode
                ),
                contactDetails = ContactDetails(landline = Some("747663966"),
                                                mobile = Some("38390756243"),
                                                fax = Some("58371813020"),
                                                emailAddress = Some("dyson@example.com")
                )
              )
            )
          }
        }
        "the country is outside UK and related territories" in {
          val rawJson =
            s"""
               |{
               |    "name": "Dyson",
               |    "address": {
               |        "lineOne": "34 Park Lane",
               |        "lineTwo": "Building A",
               |        "lineThree": "Suite 100",
               |        "lineFour": "Manchester",
               |        "countryCode": "FR"
               |    },
               |    "contactDetails": {
               |        "landline": "747663966",
               |        "mobile": "38390756243",
               |        "fax": "58371813020",
               |        "emailAddress": "dyson@example.com"
               |    }
               |}
               |""".stripMargin

          rawJson should beValid(
            Organisation(
              name = "Dyson",
              address = Address(
                lineOne = "34 Park Lane",
                lineTwo = "Building A",
                lineThree = "Suite 100",
                lineFour = Some("Manchester"),
                postalCode = None,
                countryCode = "FR"
              ),
              contactDetails =
                ContactDetails(landline = Some("747663966"), mobile = Some("38390756243"), fax = Some("58371813020"), emailAddress = Some("dyson@example.com"))
            )
          )
        }
      }
    }
    "invalid, due to" - {
      "the name, which is" - {
        "absent" in {
          val rawJson =
            """
              |{
              |    "address": {
              |        "lineOne": "34 Park Lane",
              |        "lineTwo": "Building A",
              |        "lineThree": "Suite 100",
              |        "lineFour": "Manchester",
              |        "postalCode": "M54 1MQ",
              |        "countryCode": "GB"
              |    },
              |    "contactDetails": {
              |        "landline": "747663966",
              |        "mobile": "38390756243",
              |        "fax": "58371813020",
              |        "emailAddress": "dyson@example.com"
              |    }
              |}
              |""".stripMargin

          rawJson should beInvalid[Organisation](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "too short" in {
          val rawJson =
            """
              |{
              |    "name": "",
              |    "address": {
              |        "lineOne": "34 Park Lane",
              |        "lineTwo": "Building A",
              |        "lineThree": "Suite 100",
              |        "lineFour": "Manchester",
              |        "postalCode": "M54 1MQ",
              |        "countryCode": "GB"
              |    },
              |    "contactDetails": {
              |        "landline": "747663966",
              |        "mobile": "38390756243",
              |        "fax": "58371813020",
              |        "emailAddress": "dyson@example.com"
              |    }
              |}
              |""".stripMargin

          rawJson should beInvalid[Organisation](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
              |{
              |    "name": "The Dyson Electronics Company Of Great Britain And Northern Ireland (aka The Dyson Electronics Company Of Great Britain And Northern Ireland)",
              |    "address": {
              |        "lineOne": "34 Park Lane",
              |        "lineTwo": "Building A",
              |        "lineThree": "Suite 100",
              |        "lineFour": "Manchester",
              |        "postalCode": "M54 1MQ",
              |        "countryCode": "GB"
              |    },
              |    "contactDetails": {
              |        "landline": "747663966",
              |        "mobile": "38390756243",
              |        "fax": "58371813020",
              |        "emailAddress": "dyson@example.com"
              |    }
              |}
              |""".stripMargin

          rawJson should beInvalid[Organisation](Seq((__ \ "name", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 105)))))
        }
      }
      "the address, namely the" - {
        "first line, which is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "6000-6600 Great Peter Boulevard North",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
          }
        }
        "second line, which is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building Number Two, Northwest Corner",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
          }
        }
        "third line, which is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100, formerly the Presidential Suite",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
          }
        }
        "fourth line, which is" - {
          "too short" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineFour", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester, United Kingdom of Great Britain and Northern Ireland",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GB"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "lineFour", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))))
          }
        }
        "country code, which is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
          }
          "too short" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": ""
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin
            rawJson should beInvalid[Organisation](
              Seq(
                (__ \ "address" \ "countryCode",
                 Seq(JsonValidationError(immutable.Seq("error.minLength"), 1), JsonValidationError(immutable.Seq("error.invalid")))
                )
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "GBR"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](
              Seq(
                (__ \ "address" \ "countryCode",
                 Seq(JsonValidationError(immutable.Seq("error.maxLength"), 2), JsonValidationError(immutable.Seq("error.invalid")))
                )
              )
            )
          }
          "unrecognised" in {
            val rawJson =
              """
                |{
                |    "name": "Dyson",
                |    "address": {
                |        "lineOne": "34 Park Lane",
                |        "lineTwo": "Building A",
                |        "lineThree": "Suite 100",
                |        "lineFour": "Manchester",
                |        "postalCode": "M54 1MQ",
                |        "countryCode": "XX"
                |    },
                |    "contactDetails": {
                |        "landline": "747663966",
                |        "mobile": "38390756243",
                |        "fax": "58371813020",
                |        "emailAddress": "dyson@example.com"
                |    }
                |}
                |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
        }
        "postal code, which is" - {
          "expected but absent, when country code is" in {
            val countryCodes =
              Table(
                "Country Code",
                "GB",
                "GG",
                "IM",
                "JE"
              )
            forAll(countryCodes) { countryCode =>
              val rawJson =
                s"""
                  |{
                  |    "name": "Dyson",
                  |    "address": {
                  |        "lineOne": "34 Park Lane",
                  |        "lineTwo": "Building A",
                  |        "lineThree": "Suite 100",
                  |        "lineFour": "Manchester",
                  |        "countryCode": "$countryCode"
                  |    },
                  |    "contactDetails": {
                  |        "landline": "747663966",
                  |        "mobile": "38390756243",
                  |        "fax": "58371813020",
                  |        "emailAddress": "dyson@example.com"
                  |    }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
            }
          }
          "too short" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "509480494049",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 10)))))
          }
        }
      }
      "the contact details, namely the" - {
        "landline number, which is" - {
          "too short" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "",
                 |        "mobile": "38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "landline", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966747663966747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "landline", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))))
          }
          "of an invalid format" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "£747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "landline", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
        }
        "mobile number, which is" - {
          "too short" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "mobile", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243383907562433839",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "mobile", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))))
          }
          "of an invalid format" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "£38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "mobile", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
        }
        "fax number, which is" - {
          "too short" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "fax", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
          }
          "too long" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "583718130205837181302058371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "fax", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))))
          }
          "of an invalid format" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "£58371813020",
                 |        "emailAddress": "dyson@example.com"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "fax", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
        }
        "email address, which is" - {
          "blank" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": ""
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
          "of an invalid format" in {
            val rawJson =
              s"""
                 |{
                 |    "name": "Dyson",
                 |    "address": {
                 |        "lineOne": "34 Park Lane",
                 |        "lineTwo": "Building A",
                 |        "lineThree": "Suite 100",
                 |        "lineFour": "Manchester",
                 |        "postalCode": "M54 1MQ",
                 |        "countryCode": "GB"
                 |    },
                 |    "contactDetails": {
                 |        "landline": "747663966",
                 |        "mobile": "38390756243",
                 |        "fax": "58371813020",
                 |        "emailAddress": "Patrick.Dyson@"
                 |    }
                 |}
                 |""".stripMargin

            rawJson should beInvalid[Organisation](Seq((__ \ "contactDetails" \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))))
          }
        }
      }

    }
  }
}
