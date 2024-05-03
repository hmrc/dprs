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

package uk.gov.hmrc.dprs.services.subscription.update

import play.api.libs.json.{__, JsonValidationError}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.{beInvalid, beValid}
import uk.gov.hmrc.dprs.services.subscription.UpdateSubscriptionService.Requests.Request

import scala.collection.immutable

class UpdateSubscriptionServiceRequestsSpec extends BaseSpec {

  "parsing JSON should give the expected result, when assumed to be" - {
    "valid, having" - {
      "only one contact, which is an" - {
        "individual" in {
          val rawJson =
            """
              |{
              |    "name": "Harold Winter",
              |    "contacts": [
              |        {
              |            "type": "I",
              |            "firstName": "Patrick",
              |            "middleName": "John",
              |            "lastName": "Dyson",
              |            "landline": "747663966",
              |            "mobile": "38390756243",
              |            "emailAddress": "Patrick.Dyson@example.com"
              |        }
              |    ]
              |}
              |""".stripMargin

          rawJson should beValid(
            Request(
              name = Some("Harold Winter"),
              contacts = Seq(
                Request.Individual(
                  firstName = "Patrick",
                  middleName = Some("John"),
                  lastName = "Dyson",
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com"
                )
              )
            )
          )
        }
        "organisation" in {
          val rawJson =
            """
              |{
              |    "name": "Harold Winter",
              |    "contacts": [
              |        {
              |            "type": "O",
              |            "name": "Dyson",
              |            "landline": "847663966",
              |            "mobile": "48390756243",
              |            "emailAddress": "info@example.com"
              |        }
              |    ]
              |}
              |""".stripMargin

          rawJson should beValid(
            Request(
              name = Some("Harold Winter"),
              contacts = Seq(
                Request.Organisation(name = "Dyson", landline = Some("847663966"), mobile = Some("48390756243"), emailAddress = "info@example.com")
              )
            )
          )
        }
      }
      "two contacts, one of each type" in {
        val rawJson =
          """
            |{
            |    "name": "Harold Winter",
            |    "contacts": [
            |        {
            |            "type": "I",
            |            "firstName": "Patrick",
            |            "middleName": "John",
            |            "lastName": "Dyson",
            |            "landline": "747663966",
            |            "mobile": "38390756243",
            |            "emailAddress": "Patrick.Dyson@example.com"
            |        },
            |        {
            |            "type": "O",
            |            "name": "Dyson",
            |            "landline": "847663966",
            |            "mobile": "48390756243",
            |            "emailAddress": "info@example.com"
            |        }
            |    ]
            |}
            |""".stripMargin

        rawJson should beValid(
          Request(
            name = Some("Harold Winter"),
            contacts = Seq(
              Request.Individual(
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              ),
              Request.Organisation(name = "Dyson", landline = Some("847663966"), mobile = Some("48390756243"), emailAddress = "info@example.com")
            )
          )
        )
      }
      "no name" in {
        val rawJson =
          """
            |{
            |    "contacts": [
            |        {
            |            "type": "I",
            |            "firstName": "Patrick",
            |            "middleName": "John",
            |            "lastName": "Dyson",
            |            "landline": "747663966",
            |            "mobile": "38390756243",
            |            "emailAddress": "Patrick.Dyson@example.com"
            |        }
            |    ]
            |}
            |""".stripMargin

        rawJson should beValid(
          Request(
            name = None,
            contacts = Seq(
              Request.Individual(
                firstName = "Patrick",
                middleName = Some("John"),
                lastName = "Dyson",
                landline = Some("747663966"),
                mobile = Some("38390756243"),
                emailAddress = "Patrick.Dyson@example.com"
              )
            )
          )
        )
      }
    }
    "invalid, due to" - {
      "there being no contacts" in {
        val rawJson =
          """
            |{
            |    "name": "Harold Winter",
            |    "contacts": [
            |    ]
            |}
            |""".stripMargin

        rawJson should beInvalid[Request](Seq((__ \ "contacts", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
      }
      "there being three contacts" in {
        val rawJson =
          """
            |{
            |    "name": "Harold Winter",
            |    "contacts": [
            |        {
            |            "type": "I",
            |            "firstName": "Patrick",
            |            "middleName": "John",
            |            "lastName": "Dyson",
            |            "landline": "747663966",
            |            "mobile": "38390756243",
            |            "emailAddress": "Patrick.Dyson@example.com"
            |        },
            |        {
            |            "type": "I",
            |            "firstName": "Patricia",
            |            "middleName": "Jane",
            |            "lastName": "Dyson",
            |            "landline": "847663966",
            |            "mobile": "48390756243",
            |            "emailAddress": "Patricia.Dyson@example.com"
            |        },
            |        {
            |            "type": "O",
            |            "name": "Dyson",
            |            "landline": "847663966",
            |            "mobile": "48390756243",
            |            "emailAddress": "info@example.com"
            |        }
            |    ]
            |}
            |""".stripMargin

        rawJson should beInvalid[Request](Seq((__ \ "contacts", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 2)))))
      }
      "the first contact is" - {
        "an individual, where the" - {
          "first name is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "firstName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "firstName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick Alexander John Fitzpatrick James",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "firstName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
                )
              )
            }
          }
          "middle name is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "middleName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "Alexander John Fitzpatrick James Edward",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "middleName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
                )
              )
            }
          }
          "last name is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "lastName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "lastName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Alexander III, Earl Of Somewhere Else",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "lastName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
                )
              )
            }
          }
          "landline is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "landline", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966747663966747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "landline", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "£747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "landline", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "mobile is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243383907562433839",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "£38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "email address is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": ""
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
        }
        "an organisation, where the" - {
          "name is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "name", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "name", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "The Dyson Electronics Company Of Great Britain And Northern Ireland (aka The Dyson Electronics Company Of Great Britain And Northern Ireland)",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |      ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "name", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 105)))
                )
              )
            }
          }
          "landline is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |         {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "landline", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966747663966747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "landline", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "£747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "landline", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "mobile is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243383907562433839",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "£48390756243",
                  |            "emailAddress": "info@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "email address is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "blank" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |         {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": ""
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 0 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
        }
      }
      "the second contact is" - {
        "an individual, where the" - {
          "first name is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "firstName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "firstName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia Rachel Judy Hannah Elizabeth",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "firstName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
                )
              )
            }
          }
          "middle name is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "",
                  |            "lastName": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "middleName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane Rachel Judy Hannah Bruce Elizabeth",
                  |            "lastName": "Dyson",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "middleName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
                )
              )
            }
          }
          "last name is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "lastName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "lastName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson, the III, Lady Something Or Other",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "lastName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
                )
              )
            }
          }
          "landline is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "landline", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966747663966747663966747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "landline", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "£747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "landline", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "mobile is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243383907562433839075624338390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "£38390756243",
                  |            "emailAddress": "Patricia.Dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "email address is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "blank" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": ""
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patricia",
                  |            "middleName": "Jane",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
        }
        "an organisation, where the" - {
          "name is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "name", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "dyson@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "name", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "The Dyson Electronics Company Of Great Britain And Northern Ireland (aka The Dyson Electronics Company Of Great Britain And Northern Ireland)",
                  |            "landline": "847663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "name", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 105)))
                )
              )
            }
          }
          "landline is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "landline", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966747663966747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "landline", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "£747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "landline", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "mobile is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243383907562433839",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "£48390756243",
                  |            "emailAddress": "info@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "mobile", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "email address is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "blank" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": ""
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "loremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnLoremipsumdolorsisum@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
            "of an invalid format" in {
              val rawJson =
                """
                  |{
                  |    "id": {
                  |        "type": "NINO",
                  |        "value": "AA000000A"
                  |    },
                  |    "name": "Harold Winter",
                  |    "contacts": [
                  |        {
                  |            "type": "I",
                  |            "firstName": "Patrick",
                  |            "middleName": "John",
                  |            "lastName": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "38390756243",
                  |            "emailAddress": "Patrick.Dyson@example.com"
                  |        },
                  |        {
                  |            "type": "O",
                  |            "name": "Dyson",
                  |            "landline": "747663966",
                  |            "mobile": "48390756243",
                  |            "emailAddress": "@example.com"
                  |        }
                  |    ]
                  |}
                  |""".stripMargin

              rawJson should beInvalid[Request](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
        }
      }
    }
  }

}
