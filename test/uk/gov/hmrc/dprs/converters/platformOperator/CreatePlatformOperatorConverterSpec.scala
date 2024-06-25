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

package uk.gov.hmrc.dprs.converters.platformOperator

import uk.gov.hmrc.dprs.connectors.platformOperator.CreatePlatformOperatorConnector.{Request => ConnectorRequest, Response => ConnectorResponse}
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.services.platformOperator.CreatePlatformOperatorService.{Request => ServiceRequest, Response => ServiceResponse}

class CreatePlatformOperatorConverterSpec extends BaseSpec {

  private val converter = new CreatePlatformOperatorConverter

  "when converting from" - {
    "a service request to a connector request, where it has" - {
      "two contacts" in {
        val subscriptionId = "345567808"
        val serviceRequest = ServiceRequest(
          internalName = "Amazon UK",
          businessName = Some("Amazon UK Ltd"),
          tradingName = Some("Amazon"),
          ids = Seq(
            ServiceRequest.ID(_type = ServiceRequest.IDType.UTR, value = "68936493", countryCodeOfIssue = "GB")
          ),
          contacts = Seq(
            ServiceRequest.Contact(name = "John Smith", phone = Some("0789876568"), emailAddress = "jsmith@example.com"),
            ServiceRequest.Contact(name = "Paul Smith", phone = Some("0889876568"), emailAddress = "psmith@example.com")
          ),
          address = ServiceRequest.Address(lineOne = "22",
                                           lineTwo = "High Street",
                                           lineThree = "Dawley",
                                           lineFour = Some("Telford"),
                                           postalCode = Some("TF22 2RE"),
                                           countryCode = "GB"
          ),
          reportingNotification = ServiceRequest.ReportingNotification(_type = ServiceRequest.ReportingNotification.ReportingNotificationType.RPO,
                                                                       isActiveSeller = true,
                                                                       isDueDiligence = false,
                                                                       year = 2024
          )
        )

        val connectorRequest = converter.convert(subscriptionId, serviceRequest)

        connectorRequest shouldBe Some(
          ConnectorRequest(
            originatingSystem = "MDTP",
            transmittingSystem = "EIS",
            requestType = "CREATE",
            regime = "DPI",
            requestParameters = Seq.empty,
            subscriptionId = subscriptionId,
            internalName = "Amazon UK",
            businessName = Some("Amazon UK Ltd"),
            tradingName = Some("Amazon"),
            ids = Seq(ConnectorRequest.ID(_type = "UTR", value = "68936493", countryCodeOfIssue = "GB")),
            reportingNotification =
              ConnectorRequest.ReportingNotification(_type = "RPO", isActiveSeller = Some(true), isDueDiligence = Some(false), year = "2024"),
            address = ConnectorRequest.Address(lineOne = "22",
                                               lineTwo = "High Street",
                                               lineThree = "Dawley",
                                               lineFour = Some("Telford"),
                                               postalCode = Some("TF22 2RE"),
                                               countryCode = "GB"
            ),
            primaryContact = ConnectorRequest.Contact(name = "John Smith", phone = Some("0789876568"), emailAddress = "jsmith@example.com"),
            secondaryContact = Some(ConnectorRequest.Contact(name = "Paul Smith", phone = Some("0889876568"), emailAddress = "psmith@example.com"))
          )
        )
      }
      "only one contact" in {
        val subscriptionId = "345567808"
        val serviceRequest = ServiceRequest(
          internalName = "Amazon UK",
          businessName = Some("Amazon UK Ltd"),
          tradingName = Some("Amazon"),
          ids = Seq(
            ServiceRequest.ID(_type = ServiceRequest.IDType.UTR, value = "68936493", countryCodeOfIssue = "GB")
          ),
          contacts = Seq(
            ServiceRequest.Contact(name = "John Smith", phone = Some("0789876568"), emailAddress = "jsmith@example.com")
          ),
          address = ServiceRequest.Address(lineOne = "22",
                                           lineTwo = "High Street",
                                           lineThree = "Dawley",
                                           lineFour = Some("Telford"),
                                           postalCode = Some("TF22 2RE"),
                                           countryCode = "GB"
          ),
          reportingNotification = ServiceRequest.ReportingNotification(_type = ServiceRequest.ReportingNotification.ReportingNotificationType.RPO,
                                                                       isActiveSeller = true,
                                                                       isDueDiligence = false,
                                                                       year = 2024
          )
        )

        val connectorRequest = converter.convert(subscriptionId, serviceRequest)

        connectorRequest shouldBe Some(
          ConnectorRequest(
            originatingSystem = "MDTP",
            transmittingSystem = "EIS",
            requestType = "CREATE",
            regime = "DPI",
            requestParameters = Seq.empty,
            subscriptionId = subscriptionId,
            internalName = "Amazon UK",
            businessName = Some("Amazon UK Ltd"),
            tradingName = Some("Amazon"),
            ids = Seq(ConnectorRequest.ID(_type = "UTR", value = "68936493", countryCodeOfIssue = "GB")),
            reportingNotification =
              ConnectorRequest.ReportingNotification(_type = "RPO", isActiveSeller = Some(true), isDueDiligence = Some(false), year = "2024"),
            address = ConnectorRequest.Address(lineOne = "22",
                                               lineTwo = "High Street",
                                               lineThree = "Dawley",
                                               lineFour = Some("Telford"),
                                               postalCode = Some("TF22 2RE"),
                                               countryCode = "GB"
            ),
            primaryContact = ConnectorRequest.Contact(name = "John Smith", phone = Some("0789876568"), emailAddress = "jsmith@example.com"),
            secondaryContact = None
          )
        )
      }
      "no contacts" in {
        val subscriptionId = "345567808"
        val serviceRequest = ServiceRequest(
          internalName = "Amazon UK",
          businessName = Some("Amazon UK Ltd"),
          tradingName = Some("Amazon"),
          ids = Seq(
            ServiceRequest.ID(_type = ServiceRequest.IDType.UTR, value = "68936493", countryCodeOfIssue = "GB")
          ),
          contacts = Seq.empty,
          address = ServiceRequest.Address(lineOne = "22",
                                           lineTwo = "High Street",
                                           lineThree = "Dawley",
                                           lineFour = Some("Telford"),
                                           postalCode = Some("TF22 2RE"),
                                           countryCode = "GB"
          ),
          reportingNotification = ServiceRequest.ReportingNotification(_type = ServiceRequest.ReportingNotification.ReportingNotificationType.RPO,
                                                                       isActiveSeller = true,
                                                                       isDueDiligence = false,
                                                                       year = 2024
          )
        )

        val connectorRequest = converter.convert(subscriptionId, serviceRequest)

        connectorRequest shouldBe empty
      }
      "three contacts" in {
        val subscriptionId = "345567808"
        val serviceRequest = ServiceRequest(
          internalName = "Amazon UK",
          businessName = Some("Amazon UK Ltd"),
          tradingName = Some("Amazon"),
          ids = Seq(
            ServiceRequest.ID(_type = ServiceRequest.IDType.UTR, value = "68936493", countryCodeOfIssue = "GB")
          ),
          contacts = Seq(
            ServiceRequest.Contact(name = "John Smith", phone = Some("0789876568"), emailAddress = "jsmith@example.com"),
            ServiceRequest.Contact(name = "Paul Smith", phone = Some("0889876568"), emailAddress = "psmith@example.com"),
            ServiceRequest.Contact(name = "Phillipa Smith", phone = Some("0889876568"), emailAddress = "Phillipa.Smither@example.com")
          ),
          address = ServiceRequest.Address(lineOne = "22",
                                           lineTwo = "High Street",
                                           lineThree = "Dawley",
                                           lineFour = Some("Telford"),
                                           postalCode = Some("TF22 2RE"),
                                           countryCode = "GB"
          ),
          reportingNotification = ServiceRequest.ReportingNotification(_type = ServiceRequest.ReportingNotification.ReportingNotificationType.RPO,
                                                                       isActiveSeller = true,
                                                                       isDueDiligence = false,
                                                                       year = 2024
          )
        )

        val connectorRequest = converter.convert(subscriptionId, serviceRequest)

        connectorRequest shouldBe None
      }
    }
    "a connector response to a service response, when the param is" - {
      "present" in {
        val connectorResponse = ConnectorResponse(returnParameter = ConnectorResponse.ReturnParam("POID", "PO12345"))

        val serviceResponse = converter.convert(connectorResponse)

        serviceResponse shouldBe Some(ServiceResponse(platformOperatorId = "PO12345"))
      }
      "absent" in {
        val connectorResponse = ConnectorResponse(returnParameter = ConnectorResponse.ReturnParam("_POID", "PO12345"))

        val serviceResponse = converter.convert(connectorResponse)

        serviceResponse shouldBe empty
      }

    }
  }
}
