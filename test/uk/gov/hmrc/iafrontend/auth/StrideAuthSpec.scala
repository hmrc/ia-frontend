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

package uk.gov.hmrc.iafrontend.auth

import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.iafrontend.{TestHelper, authMock}
import uk.gov.hmrc.auth.core.Enrolment

import scala.concurrent.Future

class StrideAuthSpec extends authMock with TestHelper{

  val testAuthRequest = new StrideAuthenticatedAction(new testAuth,appConfig)
  "StrideAuthenticatedAction " should {

    "Allow a valid authorized request " in {
      mockAuthorise(AuthProviders(PrivilegedApplication),Retrievals.allEnrolments)(Future.successful(Enrolments(Set(Enrolment("hmrc-c")))))
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

