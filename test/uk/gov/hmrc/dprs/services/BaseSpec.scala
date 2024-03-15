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

package uk.gov.hmrc.dprs.services

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._
import uk.gov.hmrc.dprs.FixedAcknowledgeReferenceGenerator
import uk.gov.hmrc.dprs.services.BaseSpec.CustomMatchers.{BeInvalid, BeSameAs, BeValid}

import java.time.{Clock, Instant, ZoneId}
import java.util.UUID
import scala.concurrent.{Await, Awaitable}

class BaseSpec extends AnyFreeSpec with Matchers with TableDrivenPropertyChecks with MockitoSugar {

  val acknowledgementReference: String                                      = UUID.randomUUID().toString
  val acknowledgementReferenceGenerator: FixedAcknowledgeReferenceGenerator = new FixedAcknowledgeReferenceGenerator(acknowledgementReference)
  protected val fixedClock: Clock = Clock.fixed(Instant.now.truncatedTo(java.time.temporal.ChronoUnit.MILLIS), ZoneId.systemDefault)
  val currentDateTime: String     = Instant.now(fixedClock).toString

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, 1.second)

}

object BaseSpec {
  object CustomMatchers {

    class BeSameAs(expectedRawJson: String) extends Matcher[JsValue] {
      override def apply(actualJson: JsValue): MatchResult = {
        val expectedJson = Json.parse(expectedRawJson)
        MatchResult(
          actualJson == expectedJson,
          s"We expected the response to be json [\n${Json.prettyPrint(expectedJson)}\n], but it was actually [\n${Json.prettyPrint(actualJson)}\n].",
          s"We didn't expect the response to be json [\n${Json.prettyPrint(expectedJson)}], but it was indeed."
        )
      }
    }

    class BeValid[T](expectedObject: T)(implicit reads: Reads[T]) extends Matcher[String] {
      override def apply(rawJson: String): MatchResult = {
        val result = Json.parse(rawJson).validate[T]
        MatchResult(
          result.isSuccess && result.get == expectedObject,
          s"We expected the json validation to successfully result in a [$expectedObject], but the result was actually [$result].",
          s"We didn't expect the json validation to result in a [$expectedObject], but it actually did."
        )
      }
    }

    class BeInvalid[T](expectedErrors: Seq[(JsPath, Seq[JsonValidationError])])(implicit reads: Reads[T]) extends Matcher[String] {
      override def apply(rawJson: String): MatchResult = {
        val result         = Json.parse(rawJson).validate[T]
        val expectedResult = JsError(expectedErrors)
        MatchResult(
          result == expectedResult,
          s"We expected the json validation to fail with result [\n$expectedResult\n], but it was actually [\n$result\n].",
          s"We didn't expect the json validation to fail with result [\n$expectedResult\n], but it did."
        )
      }
    }
  }

  def beSameAs(expectedRawJson: String) = new BeSameAs(expectedRawJson)

  def beValid[T](expectedObject: T)(implicit reads: Reads[T]) = new BeValid(expectedObject)

  def beInvalid[T](expectedErrors: Seq[(JsPath, Seq[JsonValidationError])])(implicit reads: Reads[T]) = new BeInvalid[T](expectedErrors)

}
