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

package uk.gov.hmrc.dprs.config

import uk.gov.hmrc.dprs.connectors.registration.{RegistrationWithIdConnector, RegistrationWithoutIdConnector}
import uk.gov.hmrc.dprs.connectors.subscription.{CreateSubscriptionConnector, ReadSubscriptionConnector, UpdateSubscriptionConnector}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (servicesConfig: ServicesConfig) {

  val registrationWithIdBaseUrl: String = generateBaseUrl(RegistrationWithIdConnector.connectorName, RegistrationWithIdConnector.connectorPath)

  val registrationWithoutIdBaseUrl: String = generateBaseUrl(RegistrationWithoutIdConnector.connectorName, RegistrationWithoutIdConnector.connectorPath)

  val createSubscriptionBaseUrl: String = generateBaseUrl(CreateSubscriptionConnector.connectorName, CreateSubscriptionConnector.connectorPath)

  val updateSubscriptionBaseUrl: String = generateBaseUrl(UpdateSubscriptionConnector.connectorName, UpdateSubscriptionConnector.connectorPath)

  val readSubscriptionBaseUrl: String = generateBaseUrl(ReadSubscriptionConnector.connectorName, ReadSubscriptionConnector.connectorPath)

  private def generateBaseUrl(key: String, fallback: String): String =
    servicesConfig.baseUrl(key) + servicesConfig.getConfString(key + ".context", fallback)

}
