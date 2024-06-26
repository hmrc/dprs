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

import org.apache.commons.validator.routines.EmailValidator
import play.api.libs.functional.syntax.toApplicativeOps
import play.api.libs.json.Reads
import play.api.libs.json.Reads.{maxLength, minLength, verifying}

import java.text.SimpleDateFormat
import java.util.Locale
import scala.util.Try
import scala.util.matching.Regex

object ValidationSupport {

  private val dateFormat                     = generateDateFormat()
  private val phoneNumberPattern: Regex      = raw"[A-Z0-9/)(\\\-*#+]*".r
  private val emailValidator: EmailValidator = EmailValidator.getInstance(true)
  private val domesticCountryCodes           = Set("GB", "IM", "JE", "GG")

  def isValidDate(rawDate: String): Boolean = Try(dateFormat.parse(rawDate)).isSuccess

  def isValidPhoneNumber(phoneNumber: String): Boolean = phoneNumberPattern.matches(phoneNumber)

  def isValidEmailAddress(emailAddress: String): Boolean = emailValidator.isValid(emailAddress)

  def isValidCountryCode(rawCountryCode: String): Boolean =
    Locale.getISOCountries.toSeq.contains(rawCountryCode.toUpperCase)

  def isPostalCodeRequired(countryCode: String): Boolean = domesticCountryCodes.contains(countryCode.toUpperCase.trim)

  object Reads {
    def lengthBetween(min: Int, max: Int): Reads[String] =
      minLength[String](min).keepAnd(maxLength[String](max))

    val validPhoneNumber: Reads[String] =
      lengthBetween(1, 24).keepAnd(verifying(ValidationSupport.isValidPhoneNumber))

    val validEmailAddress: Reads[String] = verifying(isValidEmailAddress)
  }

  private def generateDateFormat(): SimpleDateFormat = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    dateFormat.setLenient(false)
    dateFormat
  }

}
