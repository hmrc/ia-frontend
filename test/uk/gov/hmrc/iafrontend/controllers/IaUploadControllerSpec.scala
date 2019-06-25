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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.libs.json.Reads
import play.api.mvc.{ControllerComponents, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthProviders, Enrolment, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.auth.StrideAuthenticatedAction
import uk.gov.hmrc.iafrontend.connector.IaConnector
import uk.gov.hmrc.iafrontend.lock.LockService
import uk.gov.hmrc.iafrontend.streams.{CSVStreamer, CSVStreamerConfig}
import uk.gov.hmrc.iafrontend.testsupport.{Spec, WithFakeApplication}
import uk.gov.hmrc.iafrontend.{authMock, views}
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.iafrontend.views.html.upload_check
import uk.gov.hmrc.iafrontend.views.html.upload

import scala.concurrent.Future

class IaUploadControllerSpec extends Spec with WithFakeApplication with authMock{

  val appConfig = fakeApplication.injector.instanceOf[AppConfig]
  val fakeRequestGet = FakeRequest("GET", "/")

  val mockIA = mock[IaConnector]
  val mockLock = mock[LockService]
  val streamer = new CSVStreamer(mockIA,mockLock,CSVStreamerConfig(50,400,1))
  val mockStrideAuth = mock[StrideAuthenticatedAction]
  val testAuthRequest = new StrideAuthenticatedAction(new testAuth,appConfig,fakeApplication.injector.instanceOf[ControllerComponents])
  implicit val appConfigImpl = appConfig
  val controller = new IaUploadController(
    stream = streamer,
    iaConnector = mockIA,
    lockService = mockLock,
    strideAuth = testAuthRequest,
    mcc = fakeApplication.injector.instanceOf[MessagesControllerComponents],
    uploadCheck = fakeApplication.injector.instanceOf[upload_check],
    upload = fakeApplication.injector.instanceOf[upload]
  )
  implicit val system = ActorSystem("System")
  implicit val materializer = ActorMaterializer()
  when(mockIA.count()(ArgumentMatchers.any[HeaderCarrier])) thenReturn Future.successful("We have 2 records")
  "GET /upload " should {
    "return 200" in {
      mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("Insolvency_Analytics_User")))))
      val result = controller.getUploadPage()(FakeRequest("GET", "/")).futureValue
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("Insolvency_Analytics_User")))))
      val result = controller.getUploadPage()(FakeRequest("GET", "/"))
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

  }
  "GET /upload/check " should {
    "return 200" in {
      mockAuthorise(AuthProviders(PrivilegedApplication), Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("Insolvency_Analytics_User")))))
      val result = controller.getUploadCheck()(FakeRequest("GET", "/")).futureValue
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      mockAuthorise(AuthProviders(PrivilegedApplication), Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("Insolvency_Analytics_User")))))
      val result = controller.getUploadCheck()(FakeRequest("GET", "/"))
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
    "call the ia count in" in {
      mockAuthorise(AuthProviders(PrivilegedApplication), Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("Insolvency_Analytics_User")))))
      val result = controller.getUploadCheck()(FakeRequest("GET", "/"))
    }

  }
//todo write tests
   /*"Post /upload " should {
     "return 200" in {
       mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("Insolvency_Analytics_User")))))
       when(mockIA.drop()( ArgumentMatchers.any[HeaderCarrier])).thenReturn(Future.successful(()))

       val result = controller.submitUploadPage()(fakeRequestPostForm.withMultipartFormDataBody(getMockFileZipedCsvFile)).run()
       status(result) shouldBe Status.OK
     }
   }
*/
}

