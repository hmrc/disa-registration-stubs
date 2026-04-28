/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.DefaultAwaitTimeout
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.AuthConnector
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseUnitSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with EitherValues
    with ScalaFutures
    with MockitoSugar
    with DefaultAwaitTimeout
    with GuiceOneAppPerSuite {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = Mockito.reset()

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .overrides(inject.bind[AuthConnector].toInstance(mockAuthConnector))
    .build()

  def authorisedUser(credId: Option[String] = None): Unit =
    when(
      mockAuthConnector.authorise(
        any(),
        any()
      )(any(), any())
    ).thenReturn(
      credId.fold(Future.unit)(credId => Future.successful(Some(Credentials(credId, "GovernmentGateway"))))
    )
}
