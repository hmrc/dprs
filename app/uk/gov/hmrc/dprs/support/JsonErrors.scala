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

import scala.annotation.tailrec

object JsonErrors extends RegExHelper {
  private val marker = "{}"

  private val map = Map(
    "error.minLength"    -> s"Field is shorter than $marker character(s).",
    "error.maxLength"    -> s"Field is longer than $marker character(s).",
    "error.invalid"      -> s"Field is invalid.",
    "error.path.missing" -> s"Field is missing."
  )

  def get(path: String, key: String, args: Seq[Any]): String =
    addParam(s"[$path]: ${map.getOrElse(key, key)}", args)

  @tailrec
  private def addParam(message: String, args: Seq[Any]): String =
    if (args.isEmpty || !message.contains(marker)) {
      message
    } else {
      addParam(message.replaceFirst(escapeRegEx(marker), args.head.toString), args.tail)
    }

}
