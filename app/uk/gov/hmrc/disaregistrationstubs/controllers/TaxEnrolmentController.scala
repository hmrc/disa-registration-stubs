/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.disaregistrationstubs.controllers

import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.disaregistrationstubs.models.TaxEnrolmentSubscriberRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxEnrolmentController @Inject() (
  cc: ControllerComponents,
  override val authConnector: AuthConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with AuthorisedFunctions
    with Logging {

  // Specific success case credIds to be added in 1910
  private val BadRequestCredId = "tax-enrolment-bad-request"

  def subscribe(subscriptionId: String): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      authorised()
        .retrieve(Retrievals.credentials) {
          case Some(Credentials(credId, _)) =>
            request.body.validate[TaxEnrolmentSubscriberRequest] match {
              case JsSuccess(payload, _) => handleScenario(subscriptionId, credId, payload)
              case JsError(errors)       =>
                logger.warn(
                  s"Parsing Tax Enrolments subscriber request failed for subscriptionId: [$subscriptionId], errors: ${JsError
                      .toJson(errors)}"
                )
                Future.successful(BadRequest(Json.obj("error" -> "Invalid request payload")))
            }
          case None                         =>
            logger.warn(s"No credentials returned from authorise call for subscriptionId: [$subscriptionId]")
            Future.successful(Unauthorized)
        }
        .recover { case _: AuthorisationException =>
          logger.warn(s"Authorise call failed for Tax Enrolments subscriptionId: [$subscriptionId]")
          Unauthorized
        }
    }

  private def handleScenario(
    subscriptionId: String,
    credId: String,
    payload: TaxEnrolmentSubscriberRequest
  ): Future[Result] =
    credId match {
      case BadRequestCredId =>
        logger.info(
          s"Tax Enrolments bad request response triggered for subscriptionId: [$subscriptionId], etmpId: [${payload.etmpId}]"
        )
        Future.successful(BadRequest(Json.obj("error" -> "Bad request from Tax Enrolments stub")))
      case _                =>
        logger.info(
          s"Tax Enrolments success response triggered for subscriptionId: [$subscriptionId], etmpId: [${payload.etmpId}]"
        )
        Future.successful(NoContent)
    }
}
