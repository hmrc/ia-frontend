/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.{AuthProviders, Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.iafrontend.{TestHelper, authMock}
import uk.gov.hmrc.iafrontend.auth.StrideAuthenticatedAction
import uk.gov.hmrc.iafrontend.streams.CSVStreamer
import uk.gov.hmrc.iafrontend.testsupport.Spec
import uk.gov.hmrc.play.test.WithFakeApplication

import scala.concurrent.Future

class IaUploadControllerSpec extends Spec with WithFakeApplication with TestHelper with authMock{

  val mockStreamer = mock[CSVStreamer]
  val mockStrideAuth = mock[StrideAuthenticatedAction]
  val testAuthRequest = new StrideAuthenticatedAction(new testAuth,appConfig)
  val controller = new IaUploadController(mockStreamer,testAuthRequest, messageApi, appConfig)

  "GET /upload " should {
    "return 200" in {
      mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("hmrc-c")))))
      val result = controller.getUploadPage()(fakeRequestGet)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("hmrc-c")))))
      val result = controller.getUploadPage()(fakeRequestGet)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

  }

  /*("Post /upload " should {
    "return 200" in {
      val result = controller.submitUploadPage()(fakeRequestPostForm)

    }

    "return HTML" in {
      val result = controller.submitUploadPage()(fakeRequestPostForm).futureValue

    }
*/
}

