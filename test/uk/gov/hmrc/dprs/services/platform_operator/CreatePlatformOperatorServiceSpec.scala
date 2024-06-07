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

package uk.gov.hmrc.dprs.services.platform_operator

import play.api.libs.json.{__, JsonValidationError}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.BaseSpec.{beInvalid, beValid}
import uk.gov.hmrc.dprs.services.platform_operator.CreatePlatformOperatorService.{Request => ServiceRequest}

import scala.collection.immutable

class CreatePlatformOperatorServiceSpec extends BaseSpec {

  "parsing JSON should give the expected result, when the request is" - {
    "valid, with" - {
      "one contact" in {
        val rawJson =
          """
            |{
            |  "internalName": "Dyson",
            |  "businessName": "Dyson Inc.",
            |  "tradingName": "Dyson",
            |  "ids": [
            |    {
            |      "type": "UTR",
            |      "value": "1234567890",
            |      "countryCodeOfIssue": "GB"
            |    }
            |  ],
            |  "contacts": [
            |    {
            |      "name": "Patrick Dyson",
            |      "phone": "38390756243",
            |      "emailAddress": "Patrick.Dyson@example.com"
            |    }
            |  ],
            |  "address": {
            |    "lineOne": "26424 Cecelia Junction",
            |    "lineTwo": "Suite 858",
            |    "lineThree": "Building Two",
            |    "lineFour": "West Siobhanberg",
            |    "postalCode": "OX2 3HD",
            |    "countryCode": "GB"
            |  },
            |  "reportingNotification": {
            |    "type": "RPO",
            |    "isActiveSeller": true,
            |    "isDueDiligence": false,
            |    "year": 2024
            |  }
            |}
            |""".stripMargin

        rawJson should beValid(
          ServiceRequest(
            internalName = "Dyson",
            businessName = Some("Dyson Inc."),
            tradingName = Some("Dyson"),
            ids = Seq(
              ServiceRequest.ID(_type = ServiceRequest.IDType.UTR, value = "1234567890", countryCodeOfIssue = "GB")
            ),
            contacts = Seq(
              ServiceRequest.Contact(name = "Patrick Dyson", phone = Some("38390756243"), emailAddress = "Patrick.Dyson@example.com")
            ),
            address = ServiceRequest.Address(
              lineOne = "26424 Cecelia Junction",
              lineTwo = "Suite 858",
              lineThree = "Building Two",
              lineFour = Some("West Siobhanberg"),
              postalCode = Some("OX2 3HD"),
              countryCode = "GB"
            ),
            reportingNotification = ServiceRequest.ReportingNotification(
              _type = ServiceRequest.ReportingNotification.ReportingNotificationType.RPO,
              isActiveSeller = true,
              isDueDiligence = false,
              year = 2024
            )
          )
        )
      }
      "two contacts" in {
        val rawJson =
          """
            |{
            |  "internalName": "Dyson",
            |  "businessName": "Dyson Inc.",
            |  "tradingName": "Dyson",
            |  "ids": [
            |    {
            |      "type": "UTR",
            |      "value": "1234567890",
            |      "countryCodeOfIssue": "GB"
            |    }
            |  ],
            |  "contacts": [
            |    {
            |      "name": "Patrick Dyson",
            |      "phone": "38390756243",
            |      "emailAddress": "Patrick.Dyson@example.com"
            |    },
            |    {
            |      "name": "Phillipa Dyson",
            |      "phone": "38390756246",
            |      "emailAddress": "Phillipa.Dyson@example.com"
            |    }
            |  ],
            |  "address": {
            |    "lineOne": "26424 Cecelia Junction",
            |    "lineTwo": "Suite 858",
            |    "lineThree": "Building Two",
            |    "lineFour": "West Siobhanberg",
            |    "postalCode": "OX2 3HD",
            |    "countryCode": "GB"
            |  },
            |  "reportingNotification": {
            |    "type": "RPO",
            |    "isActiveSeller": true,
            |    "isDueDiligence": false,
            |    "year": 2024
            |  }
            |}
            |""".stripMargin

        rawJson should beValid(
          ServiceRequest(
            internalName = "Dyson",
            businessName = Some("Dyson Inc."),
            tradingName = Some("Dyson"),
            ids = Seq(
              ServiceRequest.ID(_type = ServiceRequest.IDType.UTR, value = "1234567890", countryCodeOfIssue = "GB")
            ),
            contacts = Seq(
              ServiceRequest.Contact(name = "Patrick Dyson", phone = Some("38390756243"), emailAddress = "Patrick.Dyson@example.com"),
              ServiceRequest.Contact(name = "Phillipa Dyson", phone = Some("38390756246"), emailAddress = "Phillipa.Dyson@example.com")
            ),
            address = ServiceRequest.Address(
              lineOne = "26424 Cecelia Junction",
              lineTwo = "Suite 858",
              lineThree = "Building Two",
              lineFour = Some("West Siobhanberg"),
              postalCode = Some("OX2 3HD"),
              countryCode = "GB"
            ),
            reportingNotification = ServiceRequest.ReportingNotification(
              _type = ServiceRequest.ReportingNotification.ReportingNotificationType.RPO,
              isActiveSeller = true,
              isDueDiligence = false,
              year = 2024
            )
          )
        )
      }
    }
    "invalid, due to" - {
      "the internal name, which is" - {
        "absent" in {
          val rawJson =
            """
                |{
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

          rawJson should beInvalid[ServiceRequest](Seq((__ \ "internalName", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))))
        }
        "too short" in {
          val rawJson =
            """
                |{
                |  "internalName": "",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

          rawJson should beInvalid[ServiceRequest](Seq((__ \ "internalName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
                |{
                |  "internalName": "A long internal name for the Platform Operator. A long internal name for the Platform Operator. Maximum is 105.",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

          rawJson should beInvalid[ServiceRequest](Seq((__ \ "internalName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 105)))))
        }
      }
      "the business name, which is" - {
        "too short" in {
          val rawJson =
            """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

          rawJson should beInvalid[ServiceRequest](Seq((__ \ "businessName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "A long business name for the Platform Operator. A long business name for the Platform Operator. Maximum is 105.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

          rawJson should beInvalid[ServiceRequest](Seq((__ \ "businessName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 105)))))
        }
      }
      "the trading name, which is" - {
        "too short" in {
          val rawJson =
            """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

          rawJson should beInvalid[ServiceRequest](Seq((__ \ "tradingName", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))))
        }
        "too long" in {
          val rawJson =
            """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "A long trading name for the Platform Operator. A long trading name for the Platform Operator. Maximum is 105.",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

          rawJson should beInvalid[ServiceRequest](Seq((__ \ "tradingName", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 80)))))
        }
      }
      "the ids, when one of them has" - {
        "a type, which is" - {
          "absent" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "ids" \ 0 \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "too short" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "ids" \ 0 \ "type", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1), JsonValidationError(immutable.Seq("error.invalid"))))
              )
            )
          }
          "unsupported" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "NINO",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "ids" \ 0 \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
              )
            )
          }
        }
        "a value, which is" - {
          "absent" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "ids" \ 0 \ "value", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "too short" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "ids" \ 0 \ "value", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "123456789012345678901234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "ids" \ 0 \ "value", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 25)))
              )
            )
          }
        }
        "a country code of issue, which is" - {
          "absent" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "too short" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "B"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "countryCode",
                 Seq(JsonValidationError(immutable.Seq("error.minLength"), 2), JsonValidationError(immutable.Seq("error.invalid")))
                )
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "BBB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
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
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "XX"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
              )
            )
          }
        }
      }
      "the contacts, when" - {
        "there are none" in {
          val rawJson =
            """
              |{
              |  "internalName": "Dyson",
              |  "businessName": "Dyson Inc.",
              |  "tradingName": "Dyson",
              |  "ids": [
              |    {
              |      "type": "UTR",
              |      "value": "1234567890",
              |      "countryCodeOfIssue": "GB"
              |    }
              |  ],
              |  "contacts": [
              |  ],
              |  "address": {
              |    "lineOne": "26424 Cecelia Junction",
              |    "lineTwo": "Suite 858",
              |    "lineThree": "Building Two",
              |    "lineFour": "West Siobhanberg",
              |    "postalCode": "OX2 3HD",
              |    "countryCode": "GB"
              |  },
              |  "reportingNotification": {
              |    "type": "RPO",
              |    "isActiveSeller": true,
              |    "isDueDiligence": false,
              |    "year": 2024
              |  }
              |}
              |""".stripMargin

          rawJson should beInvalid[ServiceRequest](
            Seq(
              (__ \ "contacts", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
            )
          )
        }
        "there are three" in {
          val rawJson =
            """
              |{
              |  "internalName": "Dyson",
              |  "businessName": "Dyson Inc.",
              |  "tradingName": "Dyson",
              |  "ids": [
              |    {
              |      "type": "UTR",
              |      "value": "1234567890",
              |      "countryCodeOfIssue": "GB"
              |    }
              |  ],
              |  "contacts": [
              |    {
              |      "name": "Patrick Dyson",
              |      "phone": "38390756243",
              |      "emailAddress": "Patrick.Dyson@example.com"
              |    },
              |    {
              |      "name": "Phillipa Dyson",
              |      "phone": "38390756246",
              |      "emailAddress": "Phillipa.Dyson@example.com"
              |    },
              |    {
              |      "name": "Paul Dyson",
              |      "phone": "38390756248",
              |      "emailAddress": "Paul.Dyson@example.com"
              |    }
              |  ],
              |  "address": {
              |    "lineOne": "26424 Cecelia Junction",
              |    "lineTwo": "Suite 858",
              |    "lineThree": "Building Two",
              |    "lineFour": "West Siobhanberg",
              |    "postalCode": "OX2 3HD",
              |    "countryCode": "GB"
              |  },
              |  "reportingNotification": {
              |    "type": "RPO",
              |    "isActiveSeller": true,
              |    "isDueDiligence": false,
              |    "year": 2024
              |  }
              |}
              |""".stripMargin

          rawJson should beInvalid[ServiceRequest](
            Seq(
              (__ \ "contacts", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 2)))
            )
          )
        }
        "one of them has" - {
          "a name, which is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 1 \ "name", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 1 \ "name", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Elisabeth Louis Parker Stephenson Musk Yeti Elisabeth Louis Parker Stephenson Musk Yeti Elisabeth Louis Parker Stephenson Musk Yeti Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 1 \ "name", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 105)))
                )
              )
            }
          }
          "a phone number, which is" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 0 \ "phone", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "383907562433839075624338390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 0 \ "phone", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 24)))
                )
              )
            }
            "invalid" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "£38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 0 \ "phone", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
          "an email address, which is" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": ""
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress",
                   Seq(JsonValidationError(immutable.Seq("error.minLength"), 1), JsonValidationError(immutable.Seq("error.invalid")))
                  )
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson_Phillipa.Dyson_Phillipa.Dyson_Phillipa.Dyson_Phillipa.Dyson_Phillipa.Dyson_Phillipa.Dyson_Phillipa.Dyson_Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress",
                   Seq(JsonValidationError(immutable.Seq("error.maxLength"), 132), JsonValidationError(immutable.Seq("error.invalid")))
                  )
                )
              )
            }
            "invalid" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "contacts" \ 1 \ "emailAddress", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
                )
              )
            }
          }
        }
      }
      "the address, where" - {
        "line one is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "too short" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction Boulevard West",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineOne", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
              )
            )
          }
        }
        "line two is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "too short" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858834583594358345834593453423",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineTwo", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
              )
            )
          }
        }
        "line three is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "too short" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two Hundred And Ninety Eight",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineThree", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
              )
            )
          }
        }
        "line four is" - {
          "too short" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineFour", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "Just East of the Sun, West of the Moon",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "lineFour", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 35)))
              )
            )
          }
        }
        "country code is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "too short" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "G"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "countryCode",
                 Seq(JsonValidationError(immutable.Seq("error.minLength"), 2), JsonValidationError(immutable.Seq("error.invalid")))
                )
              )
            )
          }
          "too long" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GBG"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
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
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "XX"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "address" \ "countryCode", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
              )
            )
          }
        }
        "postal code is" - {
          "expected, but" - {
            "absent" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
                )
              )
            }
            "too short" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD OX2",
                  |    "countryCode": "GB"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 10)))
                )
              )
            }
          }
          "unexpected, but" - {
            "too short" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "",
                  |    "countryCode": "DK"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.minLength"), 1)))
                )
              )
            }
            "too long" in {
              val rawJson =
                """
                  |{
                  |  "internalName": "Dyson",
                  |  "businessName": "Dyson Inc.",
                  |  "tradingName": "Dyson",
                  |  "ids": [
                  |    {
                  |      "type": "UTR",
                  |      "value": "1234567890",
                  |      "countryCodeOfIssue": "GB"
                  |    }
                  |  ],
                  |  "contacts": [
                  |    {
                  |      "name": "Patrick Dyson",
                  |      "phone": "38390756243",
                  |      "emailAddress": "Patrick.Dyson@example.com"
                  |    },
                  |    {
                  |      "name": "Phillipa Dyson",
                  |      "phone": "38390756246",
                  |      "emailAddress": "Phillipa.Dyson@example.com"
                  |    }
                  |  ],
                  |  "address": {
                  |    "lineOne": "26424 Cecelia Junction",
                  |    "lineTwo": "Suite 858",
                  |    "lineThree": "Building Two",
                  |    "lineFour": "West Siobhanberg",
                  |    "postalCode": "OX2 3HD OX2",
                  |    "countryCode": "DK"
                  |  },
                  |  "reportingNotification": {
                  |    "type": "RPO",
                  |    "isActiveSeller": true,
                  |    "isDueDiligence": false,
                  |    "year": 2024
                  |  }
                  |}
                  |""".stripMargin

              rawJson should beInvalid[ServiceRequest](
                Seq(
                  (__ \ "address" \ "postalCode", Seq(JsonValidationError(immutable.Seq("error.maxLength"), 10)))
                )
              )
            }
          }
        }
      }
      "the reporting notification, where" - {
        "the type is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "reportingNotification" \ "type", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "unsupported" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "IPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "reportingNotification" \ "type", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
              )
            )
          }
        }
        "'is active seller' is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isDueDiligence": false,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "reportingNotification" \ "isActiveSeller", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
        }
        "'is due diligence' is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "year": 2024
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "reportingNotification" \ "isDueDiligence", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
        }
        "the year is" - {
          "absent" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "reportingNotification" \ "year", Seq(JsonValidationError(immutable.Seq("error.path.missing"))))
              )
            )
          }
          "before 2024" in {
            val rawJson =
              """
                |{
                |  "internalName": "Dyson",
                |  "businessName": "Dyson Inc.",
                |  "tradingName": "Dyson",
                |  "ids": [
                |    {
                |      "type": "UTR",
                |      "value": "1234567890",
                |      "countryCodeOfIssue": "GB"
                |    }
                |  ],
                |  "contacts": [
                |    {
                |      "name": "Patrick Dyson",
                |      "phone": "38390756243",
                |      "emailAddress": "Patrick.Dyson@example.com"
                |    },
                |    {
                |      "name": "Phillipa Dyson",
                |      "phone": "38390756246",
                |      "emailAddress": "Phillipa.Dyson@example.com"
                |    }
                |  ],
                |  "address": {
                |    "lineOne": "26424 Cecelia Junction",
                |    "lineTwo": "Suite 858",
                |    "lineThree": "Building Two",
                |    "lineFour": "West Siobhanberg",
                |    "postalCode": "OX2 3HD",
                |    "countryCode": "GB"
                |  },
                |  "reportingNotification": {
                |    "type": "RPO",
                |    "isActiveSeller": true,
                |    "isDueDiligence": false,
                |    "year": 2023
                |  }
                |}
                |""".stripMargin

            rawJson should beInvalid[ServiceRequest](
              Seq(
                (__ \ "reportingNotification" \ "year", Seq(JsonValidationError(immutable.Seq("error.invalid"))))
              )
            )
          }
        }
      }
    }
  }
}
