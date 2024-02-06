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

package uk.gov.hmrc.dprs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait WireMockServerHandler extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  val wireMockServerHost = "localhost"
  val wireMockServer: WireMockServer =
    new WireMockServer(wireMockConfig.dynamicPort().notifier(new ConsoleNotifier(true)))

  override def beforeAll(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(wireMockServerHost, wireMockServer.port())
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    wireMockServer.resetToDefaultMappings()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }
}
