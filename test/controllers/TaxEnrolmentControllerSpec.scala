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

package controllers

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import utils.BaseUnitSpec

import scala.concurrent.Future

class TaxEnrolmentControllerSpec extends BaseUnitSpec {

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
      .build()

  private val subscriptionId = "subscription-id-123"

  private val validBody = Json.parse(
    """
      |{
      |  "serviceName": "HMRC-DISA-ORG",
      |  "callback": "http://localhost:1203/disa-registration/callback/subscriptions",
      |  "etmpId": "62724841-c368-4645-b755-839e19d2af39"
      |}
      |""".stripMargin
  )

  private def request(body: play.api.libs.json.JsValue) =
    FakeRequest(PUT, s"/tax-enrolments/subscriptions/$subscriptionId/subscriber")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(body)

  "TaxEnrolmentController.subscribe" should {

    "return 204 when authorise returns credentials with a success credId" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            eqTo(Retrievals.credentials)
          )(any(), any())
        ).thenReturn(Future.successful(Some(Credentials("successful-cred-id", "GovernmentGateway"))))

        val result = route(app, request(validBody)).get

        status(result) mustBe NO_CONTENT
        contentAsString(result) mustBe empty
      }
    }

    "return 400 when authorise returns the bad request scenario credId" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            eqTo(Retrievals.credentials)
          )(any(), any())
        ).thenReturn(Future.successful(Some(Credentials("tax-enrolment-bad-request", "GovernmentGateway"))))

        val result = route(app, request(validBody)).get

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("application/json")
        (contentAsJson(result) \ "error").as[String] mustBe "Bad request from Tax Enrolments stub"
      }
    }

    "return 401 when authorise returns no credentials" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            eqTo(Retrievals.credentials)
          )(any(), any())
        ).thenReturn(Future.successful(None))

        val result = route(app, request(validBody)).get

        status(result) mustBe UNAUTHORIZED
      }
    }

    "return 401 when authorise fails" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            eqTo(Retrievals.credentials)
          )(any(), any())
        ).thenReturn(Future.failed(InsufficientEnrolments()))

        val result = route(app, request(validBody)).get

        status(result) mustBe UNAUTHORIZED
      }
    }

    "return 400 when request payload is invalid" in {
      running(fakeApplication()) {
        when(
          mockAuthConnector.authorise(
            any(),
            eqTo(Retrievals.credentials)
          )(any(), any())
        ).thenReturn(Future.successful(Some(Credentials("successful-cred-id", "GovernmentGateway"))))

        val invalidBody = Json.parse("""{"json":"bad"}""")

        val result = route(app, request(invalidBody)).get

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("application/json")
        (contentAsJson(result) \ "error").as[String] mustBe "Invalid request payload"
      }
    }
  }
}
