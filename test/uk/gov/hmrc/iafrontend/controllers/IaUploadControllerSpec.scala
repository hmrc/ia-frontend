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

package uk.gov.hmrc.iafrontend.controllers

import org.mockito.Mockito.when
import play.api.http.Status
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.hmrc.iafrontend.authMock
import uk.gov.hmrc.iafrontend.testsupport.{ITSpec, ResourceReader, TestConnector, WireMockResponses}

class IaUploadControllerSpec extends ITSpec with authMock {

  val testConnector = fakeApplication().injector.instanceOf[TestConnector]

  "Get /upload should return 200" in {
    WireMockResponses.authOk
    val result = testConnector.getUpload.futureValue
    result.status shouldBe Status.OK
  }

  "return HTML getUploadPage" in {
    WireMockResponses.authOk
    val result = testConnector.getUpload.futureValue
    result.header("Content-Type") match {
      case None    => "content-type" shouldBe "does not contain utf-8"
      case Some(x) => x should include("UTF-8"); x should include("""text/html""")
    }
  }

  "GET /upload/check should return 200" in {
    WireMockResponses.count
    WireMockResponses.authOk
    val result = testConnector.getUploadCheck.futureValue
    result.status shouldBe Status.OK
  }

  "return HTML getUploadCheck" in {
    WireMockResponses.count
    WireMockResponses.authOk
    val result = testConnector.getUploadCheck.futureValue
    result.header("Content-Type") match {
      case None    => "content-type" shouldBe "does not contain utf-8"
      case Some(x) => x should include("UTF-8"); x should include("""text/html""")
    }
  }

  "call the upload in " in {
    WireMockResponses.acquireLock
    WireMockResponses.releaseLock
    WireMockResponses.sendUtrs
    WireMockResponses.count
    WireMockResponses.switch
    WireMockResponses.authOk

    val fileBody = ResourceReader.getMultiPartFromResource("/source.zip", "/tmp/source.zip", "source.zip")
    implicit val request = mock[Request[MultipartFormData[TemporaryFile]]]
    when(request.body) thenReturn fileBody

    val controller = fakeApplication().injector.instanceOf[IaUploadController]
    val result: Result = controller.doUpload().futureValue
    status(result) shouldBe Status.SEE_OTHER
  }

}

