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

package connectors

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{RETURNS_SELF, reset, verify, when}
import play.api.Application
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.disaregistrationstubs.connectors.RegistrationConnector
import uk.gov.hmrc.disaregistrationstubs.models.TaxEnrolmentCallbackRequest
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import utils.BaseUnitSpec

import scala.concurrent.Future

class RegistrationConnectorSpec extends BaseUnitSpec {

  private val mockHttpClient: HttpClientV2 =
    mock[HttpClientV2]

  private val mockRequestBuilder: RequestBuilder =
    mock[RequestBuilder](RETURNS_SELF)

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[HttpClientV2].toInstance(mockHttpClient)
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient, mockRequestBuilder)
  }

  private lazy val connector: RegistrationConnector =
    app.injector.instanceOf[RegistrationConnector]

  private val callbackUrl =
    "http://localhost:1201/disa-registration/callback/subscriptions"

  private val payload = TaxEnrolmentCallbackRequest(
    url = "http://tax-enrolments.service/tax-enrolments/subscriptions/subscription-id-123",
    state = "SUCCEEDED",
    errorResponse = None
  )

  "RegistrationConnector.callCallback" should {

    "POST to the callback URL and return Unit when the callback succeeds" in {
      when(
        mockHttpClient.post(eqTo(url"$callbackUrl"))(any[HeaderCarrier])
      ).thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.execute[Unit](any(), any())
      ).thenReturn(Future.unit)

      val result = connector.callCallback(callbackUrl, payload).futureValue

      result shouldBe ()

      verify(mockHttpClient).post(eqTo(url"$callbackUrl"))(any[HeaderCarrier])
      verify(mockRequestBuilder).setHeader("Content-Type" -> "application/json")
      verify(mockRequestBuilder).execute[Unit](any(), any())
    }

    "fail with UpstreamErrorResponse when the callback returns an upstream error" in {
      val upstreamError = UpstreamErrorResponse(
        message = "callback failed",
        statusCode = INTERNAL_SERVER_ERROR,
        reportAs = INTERNAL_SERVER_ERROR
      )

      when(
        mockHttpClient.post(eqTo(url"$callbackUrl"))(any[HeaderCarrier])
      ).thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.execute[Unit](any(), any())
      ).thenReturn(Future.failed(upstreamError))

      val result = connector.callCallback(callbackUrl, payload).failed.futureValue

      result shouldBe upstreamError

      verify(mockHttpClient).post(eqTo(url"$callbackUrl"))(any[HeaderCarrier])
      verify(mockRequestBuilder).setHeader("Content-Type" -> "application/json")
      verify(mockRequestBuilder).execute[Unit](any(), any())
    }
  }
}
