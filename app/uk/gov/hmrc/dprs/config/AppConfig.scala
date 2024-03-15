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

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (servicesConfig: ServicesConfig) {

  val registrationWithIdBaseUrl: String = generateBaseUrl("registration-with-id", "/dac6/dct70b/v1")

  val registrationWithoutIdBaseUrl: String = generateBaseUrl("registration-without-id", "/dac6/dct70a/v1")

  val createSubscriptionBaseUrl: String = generateBaseUrl("create-subscription", "/dac6/dct70c/v1")

  private def generateBaseUrl(key: String, fallback: String): String =
    servicesConfig.baseUrl(key) + servicesConfig.getConfString(key + ".context", fallback)

}
