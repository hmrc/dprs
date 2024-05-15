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

class JsonErrorsSpec extends BaseSpec {

  val path = "test"

  "JsonErrors" - {
    "get" in {
      assert(
        JsonErrors.get(path, _, Seq("a", 1, "b", 2)),
        Table(
          ("value", "result"),
          ("({}, {}) and ({}, {})", s"[$path]: (a, 1) and (b, 2)"),
          ("({}, {})", s"[$path]: (a, 1)"),
          ("({}, {}), ({}, {}) and ({}, {})", s"[$path]: (a, 1), (b, 2) and ({}, {})")
        )
      )
    }
  }
}
