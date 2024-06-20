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

package uk.gov.hmrc.dprs.services.subscription

import play.api.http.Status._
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Json, OWrites}
import uk.gov.hmrc.dprs.connectors.BaseConnector.Responses.Error
import uk.gov.hmrc.dprs.connectors.subscription.ReadSubscriptionConnector
import uk.gov.hmrc.dprs.services.BaseService
import uk.gov.hmrc.dprs.services.BaseService.ErrorResponse
import uk.gov.hmrc.dprs.services.subscription.ReadSubscriptionService.Converter
import uk.gov.hmrc.dprs.services.subscription.ReadSubscriptionService.Responses.Response.ConnectorErrorCode

import javax.inject.Inject
import scala.Function.unlift
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject() (readSubscriptionConnector: ReadSubscriptionConnector) extends BaseService {

  private val converter = new Converter

  def call(id: String)(implicit
    executionContext: ExecutionContext
  ): Future[Either[BaseService.ErrorResponse, ReadSubscriptionService.Responses.Response]] =
    readSubscriptionConnector.call(id).map {
      case Right(connectorResponse) => Right(converter.convert(connectorResponse))
      case Left(connectorError)     => Left(convert(connectorError))
    }

  override protected def convert(connectorError: Error): ErrorResponse = {
    import BaseService.{ErrorCodes => ServiceErrorCodes}
    import ConnectorErrorCode._
    connectorError match {
      case Error(UNPROCESSABLE_ENTITY, Some(`couldNotBeProcessed`))     => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.serviceUnavailableError))
      case Error(UNPROCESSABLE_ENTITY, Some(`invalidId`))               => ErrorResponse(SERVICE_UNAVAILABLE)
      case Error(UNPROCESSABLE_ENTITY, Some(`createOrAmendInProgress`)) => ErrorResponse(SERVICE_UNAVAILABLE, Some(ServiceErrorCodes.serviceUnavailableError))
      case Error(FORBIDDEN, _)                                          => ErrorResponse(FORBIDDEN, Some(ServiceErrorCodes.forbidden))
      case Error(BAD_GATEWAY, _)                                        => ErrorResponse(BAD_GATEWAY, Some(ServiceErrorCodes.badGateway))
      case Error(INTERNAL_SERVER_ERROR, _)                              => ErrorResponse(INTERNAL_SERVER_ERROR, Some(ServiceErrorCodes.internalServerError))
      case _                                                            => ErrorResponse(connectorError.status)
    }
  }
}

object ReadSubscriptionService {

  object Responses {

    final case class Response(id: String, name: Option[String], contacts: Seq[Contact])

    sealed trait Contact {

      def landline: Option[String]

      def mobile: Option[String]

      def emailAddress: String
    }

    object Contact {

      implicit val writes: OWrites[Contact] = {
        case individual: Individual =>
          Json.obj(
            "type"         -> "I",
            "firstName"    -> individual.firstName,
            "middleName"   -> individual.middleName,
            "lastName"     -> individual.lastName,
            "landline"     -> individual.landline,
            "mobile"       -> individual.mobile,
            "emailAddress" -> individual.emailAddress
          )
        case organisation: Organisation =>
          Json.obj(
            "type"         -> "O",
            "name"         -> organisation.name,
            "landline"     -> organisation.landline,
            "mobile"       -> organisation.mobile,
            "emailAddress" -> organisation.emailAddress
          )
      }
    }

    final case class Individual(firstName: Option[String],
                                middleName: Option[String],
                                lastName: Option[String],
                                landline: Option[String],
                                mobile: Option[String],
                                emailAddress: String
    ) extends Contact

    final case class Organisation(name: String, landline: Option[String], mobile: Option[String], emailAddress: String) extends Contact

    object Response {
      implicit val writes: OWrites[Response] =
        ((JsPath \ "id").write[String] and
          (JsPath \ "name").writeNullable[String] and
          (JsPath \ "contacts").write[Seq[Contact]])(unlift(Response.unapply))

      object ConnectorErrorCode {
        val couldNotBeProcessed     = "003"
        val createOrAmendInProgress = "201"
        val forbidden               = "403"
        val internalServerError     = "500"
        val invalidId               = "016"
        val badGateway              = "502"
      }
    }
  }

  class Converter {

    def convert(response: ReadSubscriptionConnector.Responses.Response): Responses.Response =
      Responses.Response(
        id = response.subscriptionID,
        name = response.tradingName,
        contacts = {
          val primaryConverted   = response.primaryContact.map(convert)
          val secondaryConverted = response.secondaryContact.map(convert)

          Seq(primaryConverted, secondaryConverted).flatten.flatten
        }
      )

    def convert(contact: ReadSubscriptionConnector.Responses.Contact): Option[ReadSubscriptionService.Responses.Contact] =
      (contact.individualDetails, contact.organisationDetails) match {
        case (Some(individualDetails), _) =>
          Some(
            ReadSubscriptionService.Responses.Individual(
              firstName = Some(individualDetails.firstName),
              middleName = individualDetails.middleName,
              lastName = Some(individualDetails.lastName),
              landline = contact.phone,
              mobile = contact.mobile,
              emailAddress = contact.email
            )
          )
        case (_, Some(organisationDetails)) =>
          Some(
            ReadSubscriptionService.Responses.Organisation(
              name = organisationDetails.organisationName,
              landline = contact.phone,
              mobile = contact.mobile,
              emailAddress = contact.email
            )
          )
        case (_, _) =>
          None
        case _ =>
          None
      }
  }
}
