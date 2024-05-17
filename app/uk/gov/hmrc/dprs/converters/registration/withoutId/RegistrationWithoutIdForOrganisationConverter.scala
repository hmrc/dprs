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

package uk.gov.hmrc.dprs.converters.registration.withoutId

import uk.gov.hmrc.dprs.connectors.registration.withoutId.{RegistrationWithoutIdConnector, RegistrationWithoutIdForOrganisationConnector}
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.dprs.services.registration.withoutId.{RegistrationWithoutIdForOrganisationService, RegistrationWithoutIdService}

import java.time.Clock

class RegistrationWithoutIdForOrganisationConverter(clock: Clock, acknowledgementReferenceGenerator: AcknowledgementReferenceGenerator)
    extends RegistrationWithoutIdConverter(clock, acknowledgementReferenceGenerator) {

  def convert(request: RegistrationWithoutIdForOrganisationService.Request): RegistrationWithoutIdForOrganisationConnector.Request =
    RegistrationWithoutIdForOrganisationConnector.Request(
      common = generateRequestCommon(),
      detail = RegistrationWithoutIdForOrganisationConnector.Request.Detail(
        name = request.name,
        address = convert(request.address),
        contactDetails = convert(request.contactDetails)
      )
    )

  def convert(connectorResponse: RegistrationWithoutIdConnector.Response): RegistrationWithoutIdService.Response =
    RegistrationWithoutIdService.Response(ids = extractIds(connectorResponse))

}
