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

package uk.gov.hmrc.iafrontend.testsupport

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json

object WireMockResponses {

  private val locksUrlPattern: UrlPathPattern = urlPathEqualTo("/locks/ia")
  private val acquireMB: MappingBuilder =
    post(locksUrlPattern)
      .willReturn(
        aResponse()
          .withStatus(201)
          .withBody("")
      )

  def acquireLock(timeout: Long): StubMapping = stubFor(
    acquireMB.withQueryParam("timeout", equalTo(timeout.toString))
  )

  def acquireLock(): StubMapping = stubFor(acquireMB)

  def acquireExistingLock(timeout: Long): StubMapping = {
    stubFor(
      post(locksUrlPattern)
        .withQueryParam("timeout", equalTo(timeout.toString))
        .willReturn(
          aResponse()
            .withStatus(409)
            .withBody("")
        ))
  }

  def verifyAcquireLock(timeout: Long): Unit = verify(1, postRequestedFor(locksUrlPattern))

  //  def acquireLockLoggedRequests = findAll(postRequestedFor(locksUrlPattern)).asScala

  def releaseLock(): StubMapping = {
    stubFor(delete(urlPathEqualTo(s"/locks/ia")).willReturn(
      aResponse()
        .withStatus(409)
        .withBody("")
    ))
  }

  def verifyReleaseLock() = verify(1, deleteRequestedFor(locksUrlPattern))

  def sendUtrs(): StubMapping = {
    stubFor(post(urlEqualTo(s"/ia/upload")).willReturn(
      aResponse()
        .withStatus(200)
        .withBody("2")
    ))
  }

  def sendUtrsFail(): StubMapping = {
    stubFor(post(urlEqualTo(s"/ia/upload")).willReturn(
      aResponse()
        .withStatus(400)
    ))
  }

  def switch(): StubMapping = {
    stubFor(post(urlEqualTo(s"/ia/switch")).willReturn(
      aResponse()
        .withStatus(200)
    ))
  }

  def count(): StubMapping = {
    stubFor(get(urlEqualTo(s"/ia/count")).willReturn(
      aResponse()
        .withStatus(200)
        .withBody("6")
    ))
  }

  def authOk(): StubMapping = {
    stubFor(post(urlEqualTo(s"/auth/authorise")).willReturn(
      ok("""{"individualEnrolments":{"sa":"2222222222", "nino": "AA000003D"}, "allEnrolments" : [{"key":"Insolvency_Analytics_User", "identifiers": [{"key" : "UTR" , "value": "2222222226"}], "state": "Activated"}]}""")))
  }

}
