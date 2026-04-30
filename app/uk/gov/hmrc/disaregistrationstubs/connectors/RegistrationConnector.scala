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

package uk.gov.hmrc.disaregistrationstubs.connectors

import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.disaregistrationstubs.models.TaxEnrolmentCallbackRequest
import uk.gov.hmrc.http.HttpReadsInstances.readUnit
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, UpstreamErrorResponse}
import uk.gov.hmrc.http.StringContextOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationConnector @Inject() (
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpErrorFunctions
    with Logging {

  def callCallback(
    callbackUrl: String,
    payload: TaxEnrolmentCallbackRequest
  )(implicit hc: HeaderCarrier): Future[Unit] =
    http
      .post(url"$callbackUrl")
      .withBody(Json.toJson(payload))
      .setHeader("Content-Type" -> "application/json")
      .execute[Unit]
      .recoverWith { case errResponse: UpstreamErrorResponse =>
        logger.error(
          s"Tax Enrolments callback failed - Status: ${errResponse.statusCode}, Body: ${errResponse.message}"
        )
        Future.failed(errResponse)
      }
}
