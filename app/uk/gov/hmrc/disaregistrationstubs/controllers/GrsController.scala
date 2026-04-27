package uk.gov.hmrc.disaregistrationstubs.controllers

import play.api.Logging
import play.api.mvc.*
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GrsController @Inject() (
  cc: ControllerComponents,
  val authConnector: AuthConnector
)(implicit executionContext: ExecutionContext) extends BackendController(cc)
  with Logging with AuthorisedFunctions {

    private val SuccessJourneyId             = "grs-success"
    private val InternalServerErrorJourneyId = "grs-internal-server-error"

    def triggerCallback(journeyId: String): Action[AnyContent] =
      Action.async { implicit request =>
        authorised() {
          journeyId match {
            case SuccessJourneyId =>
              logger.info(s"GRS success scenario triggered for journeyId: [$journeyId]")
              Future.successful(Redirect(s"/incorporated-identity-callback?journeyId=$journeyId"))

            case InternalServerErrorJourneyId =>
              logger.info(s"GRS internal server error scenario triggered for journeyId: [$journeyId]")
              Future.successful(InternalServerError)

            case _ =>
              logger.info(s"GRS default success scenario triggered for journeyId: [$journeyId]")
              Future.successful(Redirect(s"/incorporated-identity-callback?journeyId=$journeyId"))
          }
        }.recover { case _: AuthorisationException =>
            logger.warn(s"Missing bearer token for GRS journeyId: [$journeyId]")
            Unauthorized
          }
      }
}
