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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.{Application, inject}
import uk.gov.hmrc.dprs.services.AcknowledgementReferenceGenerator
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.HttpClientV2Support

import java.time.{Clock, Instant, ZoneId}
import java.util.UUID

class BaseIntegrationSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneServerPerSuite
    with WireMockServerHandler
    with HttpClientV2Support {

  val wsClient: WSClient = app.injector.instanceOf[WSClient]
  val baseUrl: String    = s"http://localhost:$port"
  lazy val fixedClock: Clock  = Clock.fixed(Instant.now.truncatedTo(java.time.temporal.ChronoUnit.MILLIS), ZoneId.systemDefault)
  lazy val fixedAcknowledgeReferenceGenerator = new FixedAcknowledgeReferenceGenerator(UUID.randomUUID().toString)

  override def fakeApplication(): Application = {
    wireMockServer.start() // In order to get the dynamic port
    baseApplicationBuilder()
      .configure(extraApplicationConfig)
      .overrides(inject.bind[HttpClientV2].toInstance(httpClientV2))
      .overrides(inject.bind[Clock].toInstance(fixedClock))
      .overrides(inject.bind[AcknowledgementReferenceGenerator].toInstance(fixedAcknowledgeReferenceGenerator))
      .build()
  }

  def extraApplicationConfig: Map[String, Any] = Map.empty

  def baseApplicationBuilder(): GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .configure(
        "metrics.enabled" -> false
      )
}

object BaseIntegrationSpec {
  class FixedAcknowledgeReferenceGenerator(generated: String) extends AcknowledgementReferenceGenerator {
    override def generate(): String = generated
  }
}

