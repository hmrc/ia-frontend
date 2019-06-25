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

package uk.gov.hmrc.iafrontend.auth

import play.api.mvc.Results._
import play.api.mvc.{ControllerComponents, Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.iafrontend.authMock
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.iafrontend.config.AppConfig
import uk.gov.hmrc.iafrontend.testsupport.WithFakeApplication

import scala.concurrent.Future

class StrideAuthSpec extends authMock with WithFakeApplication{

  val fakeRequestGet = FakeRequest("GET", "/")
  val testAuthRequest = new StrideAuthenticatedAction(new testAuth,
    fakeApplication.injector.instanceOf[AppConfig],
    fakeApplication.injector.instanceOf[ControllerComponents])
  "StrideAuthenticatedAction " should {

    "Allow a valid authorized request " in {
      mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("Insolvency_Analytics_User")))))
      val result: Result = testAuthRequest.invokeBlock(fakeRequestGet, (test: Request[Product with Serializable]) => Future.successful(Ok(""))).futureValue
      status(result) shouldBe 200
    }

    "Allow a valid authorized request and be case insensitive" in {
      mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("insolvency_analytics_user")))))
      val result: Result = testAuthRequest.invokeBlock(fakeRequestGet, (test: Request[Product with Serializable]) => Future.successful(Ok(""))).futureValue
      status(result) shouldBe 200
    }

    "return an unauthorized " in {
      mockAuthorise(AuthProviders(PrivilegedApplication),retrievals = Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("b")))))
      val result: Result = testAuthRequest.invokeBlock(fakeRequestGet, (test: Request[Product with Serializable]) => Future.successful(Ok(""))).futureValue
      status(result) shouldBe 401
    }
    "redirect to stride login page if the requestor is not logged in " in {
      mockAuthorise(AuthProviders(PrivilegedApplication),retrievals = Retrievals.allEnrolments)(Future.failed(MissingBearerToken("Hello")))
      val result: Result = testAuthRequest.invokeBlock(fakeRequestGet, (test: Request[Product with Serializable]) => Future.successful(Ok(""))).futureValue
      status(result) shouldBe 303
    }
  }
}


