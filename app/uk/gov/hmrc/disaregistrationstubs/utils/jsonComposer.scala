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

package uk.gov.hmrc.disaregistrationstubs.utils

import play.api.libs.json.{JsObject, JsValue, Json}

import java.time.Instant

trait jsonComposer {

  private val mockIdentifiers = Json.obj(
    "key" -> "f52b4104-7e69-4bb8-baec-5aaf9897e849",
    "value" -> "97541e00-a712-452b-af21-0be4db0b7b1d"
  )

  private val mockIdentifiers1 = Json.obj(
    "key" -> "d3e222b8-b9ff-4571-8f83-1a0fbc221195",
    "value" -> "547d7434-8c33-431d-bffa-35a7d1103c30"
  )

  val errorObj: JsObject = Json.obj(
    "created" -> System.currentTimeMillis().toString,
    "lastModified" -> System.currentTimeMillis().toString,
    "credId" -> "d8474a25-71b6-45ed-859e-77dd5f087be6",
    "serviceName" -> "516b9976-00fd-4da6-b59c-4d09054912bb",
    "identifiers" -> Json.arr(mockIdentifiers, mockIdentifiers1),
    "callback" -> "url passed in by the subscriber service",
    "state" -> "ERROR",
    "etmpId" -> "da4053bf-2ea3-4cb8-bb9c-65b70252b656",
    "errorResponse" -> "SomeError"
  )

  val pendingObj: JsObject = Json.obj(
    "created" -> System.currentTimeMillis().toString,
    "lastModified" -> System.currentTimeMillis().toString,
    "credId" -> "d8474a25-71b6-45ed-859e-77dd5f087be6",
    "serviceName" -> "516b9976-00fd-4da6-b59c-4d09054912bb",
    "identifiers" -> Json.arr(mockIdentifiers, mockIdentifiers1),
    "callback" -> "url passed in by the subscriber service",
    "state" -> "PENDING",
    "etmpId" -> "da4053bf-2ea3-4cb8-bb9c-65b70252b656"
  )

  val succeededObj: JsObject = Json.obj(
    "created" -> System.currentTimeMillis().toString,
    "lastModified" -> System.currentTimeMillis().toString,
    "credId" -> "d8474a25-71b6-45ed-859e-77dd5f087be6",
    "serviceName" -> "516b9976-00fd-4da6-b59c-4d09054912bb",
    "identifiers" -> Json.arr(mockIdentifiers, mockIdentifiers1),
    "callback" -> "url passed in by the subscriber service",
    "state" -> "SUCCEEDED",
    "etmpId" -> "da4053bf-2ea3-4cb8-bb9c-65b70252b656"
  )

  val offlineObj: JsObject = Json.obj(
    "created" -> System.currentTimeMillis().toString,
    "lastModified" -> System.currentTimeMillis().toString,
    "credId" -> "d8474a25-71b6-45ed-859e-77dd5f087be6",
    "serviceName" -> "516b9976-00fd-4da6-b59c-4d09054912bb",
    "identifiers" -> Json.arr(mockIdentifiers, mockIdentifiers1),
    "callback" -> "url passed in by the subscriber service",
    "state" -> "OFFLINE",
    "etmpId" -> "da4053bf-2ea3-4cb8-bb9c-65b70252b656",
    "groupIdentifier" -> "9F9416A1-3977-4FC1-AB5E-0352417FD5B8",
  )

}
