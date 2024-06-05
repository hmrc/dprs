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

package uk.gov.hmrc.dprs.connectors.registration

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}

import scala.Function.unlift

object RegistrationConnector {

  object Request {

    final case class Common(receiptDate: String, regime: String, acknowledgementReference: String, requestParameters: Seq[Common.RequestParameter] = Seq.empty)

    object Common {

      implicit val writes: OWrites[Common] =
        ((JsPath \ "receiptDate").write[String] and
          (JsPath \ "regime").write[String] and
          (JsPath \ "acknowledgementReference").write[String] and
          (JsPath \ "requestParameters").write[Seq[Common.RequestParameter]])(unlift(Common.unapply))

      final case class RequestParameter(name: String, value: String)

      object RequestParameter {
        implicit val writes: OWrites[RequestParameter] =
          ((JsPath \ "paramName").write[String] and
            (JsPath \ "paramValue").write[String])(unlift(RequestParameter.unapply))
      }

    }

  }

  object Response {

    final case class Common(returnParams: Seq[Common.ReturnParam])

    object Common {
      implicit lazy val reads: Reads[Common] =
        (JsPath \ "returnParameters").read[Seq[ReturnParam]].map(Common(_))

      final case class ReturnParam(name: String, value: String)

      object ReturnParam {
        implicit val reads: Reads[ReturnParam] =
          ((JsPath \ "paramName").read[String] and
            (JsPath \ "paramValue").read[String])(ReturnParam.apply _)
      }
    }

  }

}
