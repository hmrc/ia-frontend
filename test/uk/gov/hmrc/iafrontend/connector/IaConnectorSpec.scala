/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.iafrontend.connector

import play.api.Logger
import uk.gov.hmrc.iafrontend.config.IaConfig
import uk.gov.hmrc.iafrontend.domain.GreenUtr
import uk.gov.hmrc.iafrontend.testsupport.{ITSpec, WireMockResponses}

class IaConnectorSpec extends ITSpec {

  val iaConfig = fakeApplication().injector.instanceOf[IaConfig]
  val connector = fakeApplication().injector.instanceOf[IaConnector]

  "sendUtrs success" in {
    WireMockResponses.sendUtrs
    val response = connector.sendUtrs(List(GreenUtr("1234567890"))).futureValue
    response shouldBe 2
  }

  "sendUtrs failed" in {
    WireMockResponses.sendUtrsFail
    val failure = connector.sendUtrs(List(GreenUtr("1234567890"))).failed.futureValue
    Logger.warn(failure.getMessage)
    failure.getMessage should include("returned 400")
  }

  "switch success" in {
    WireMockResponses.switch
    val response: Unit = connector.switch.futureValue
    response shouldBe (())
  }

  "count success" in {
    WireMockResponses.count
    val response = connector.count().futureValue
    response shouldBe "6"
  }

}
