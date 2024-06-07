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

package uk.gov.hmrc.dprs.support

import org.scalatest.prop.TableFor2
import uk.gov.hmrc.dprs.services.BaseSpec
import uk.gov.hmrc.dprs.support.ValidationSupport.{isPostalCodeRequired, isValidCountryCode, isValidDate, isValidEmailAddress, isValidPhoneNumber}

class ValidationSupportSpec extends BaseSpec {

  "when" - {
    "validating" - {
      "dates" in {
        assert(
          isValidDate _,
          Table(
            ("value", "verdict"),
            ("1977-02-29", false),
            ("2024-02-28", true),
            ("2024-2-28", true),
            ("2024-02-29", true),
            ("2024-02-29", true),
            ("2024-01-03", true),
            ("2024-32-03", false),
            ("2024-12-26", true)
          )
        )
      }
      "phone numbers" in {
        assert(
          isValidPhoneNumber _,
          Table(
            ("value", "verdict"),
            ("+44-4848-667-261", true),
            ("(44)1438-744-016", true),
            ("07070526950", true),
            ("+44-6044-156-173#22", true),
            ("+44-6044-156-173/22", true),
            ("+44-6044-156-173*22", true),
            ("£44-7192-282-397", false)
          )
        )
      }
      "email addresses" in {
        assert(
          isValidEmailAddress _,
          Table(
            ("value", "verdict"),
            ("someone@example.com", true),
            ("bob,roberts@example.com", false),
            ("someone@example", true),
            ("someone@wtf", true),
            ("someone@problems.wtf", true),
            ("someone@apple", true),
            ("@example.com", false),
            ("Loremipsumdolorsitametconsetetursadipscingelitrseddiam@example.com", true)
          )
        )
      }
      "country codes" in {
        assert(
          isValidCountryCode _,
          Table(
            ("value", "verdict"),
            ("GB", true),
            ("DK", true),
            ("IE", true),
            ("Ie", true),
            ("ie", true),
            ("PC", false),
            ("XX", false)
          )
        )
      }
    }
    "deciding if a postal code is required, when the country code is" - {
      assert(
        isPostalCodeRequired _,
        Table(
          ("value", "verdict"),
          ("GB", true),
          ("IM", true),
          ("im", true),
          (" im ", true),
          ("JE", true),
          ("GG", true),
          ("DK", false),
          ("IE", false)
        )
      )
    }
  }

  private def assert(validator: String => Boolean, expectations: TableFor2[String, Boolean]): Unit =
    forAll(expectations) { (value, expectedVerdict) =>
      validator(value) shouldBe expectedVerdict
    }
}
