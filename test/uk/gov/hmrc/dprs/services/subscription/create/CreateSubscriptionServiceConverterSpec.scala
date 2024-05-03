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

package uk.gov.hmrc.dprs.services.subscription.create

import uk.gov.hmrc.dprs.connectors.subscription.CreateSubscriptionConnector
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.subscription.CreateSubscriptionService.Requests.Request
import uk.gov.hmrc.dprs.services.subscription.CreateSubscriptionService.{Converter, Requests, Responses}

class CreateSubscriptionServiceConverterSpec extends BaseSpec {

  private val converter = new Converter

  "when converting from" - {
    "a service request to a connector request, expecting" - {
      "success" - {
        "for one of the recognised id types" in {
          val idTypes =
            Table(
              ("Raw ID Type", "Expected Raw ID Type"),
              ("NINO", "NINO"),
              ("UTR", "UTR"),
              ("SAFE", "SAFE")
            )

          forAll(idTypes) { (rawType, expectedRawType) =>
            val idType = Request.IdType.all.find(_.toString == rawType).get
            val serviceRequest = Requests.Request(
              id = Request.Id(
                idType = idType,
                value = "AA000000A"
              ),
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
                Request.Organisation(name = "Dyson", landline = Some("847663966"), mobile = Some("48390756243"), emailAddress = "info@dyson.com")
              )
            )

            val connectorRequest = converter.convert(serviceRequest)

            connectorRequest shouldBe Some(
              CreateSubscriptionConnector.Requests.Request(
                idType = expectedRawType,
                idNumber = "AA000000A",
                tradingName = Some("Harold Winter"),
                gbUser = true,
                primaryContact = CreateSubscriptionConnector.Requests.Contact(
                  landline = Some("747663966"),
                  mobile = Some("38390756243"),
                  emailAddress = "Patrick.Dyson@example.com",
                  individualDetails = Some(
                    CreateSubscriptionConnector.Requests.Contact.IndividualDetails(firstName = "Patrick", middleName = Some("John"), lastName = "Dyson")
                  ),
                  organisationDetails = None
                ),
                secondaryContact = Some(
                  CreateSubscriptionConnector.Requests.Contact(
                    landline = Some("847663966"),
                    mobile = Some("48390756243"),
                    emailAddress = "info@dyson.com",
                    individualDetails = None,
                    organisationDetails = Some(CreateSubscriptionConnector.Requests.Contact.OrganisationDetails(name = "Dyson"))
                  )
                )
              )
            )
          }
        }
      }
      "failure, when" - {
        "there are no contacts" in {
          val serviceRequest = Requests.Request(id = Request.Id(
                                                  idType = Request.IdType.NINO,
                                                  value = "AA000000A"
                                                ),
                                                name = Some("Harold Winter"),
                                                contacts = Seq.empty
          )

          val connectorRequest = converter.convert(serviceRequest)

          connectorRequest shouldBe empty
        }
      }
    }
    "a connector response to a service response" in {
      val connectorResponse = CreateSubscriptionConnector.Responses.Response(dprsReference = "XSP1234567890")

      val serviceResponse = converter.convert(connectorResponse)

      serviceResponse shouldBe Responses.Response(id = "XSP1234567890")
    }
  }

}
