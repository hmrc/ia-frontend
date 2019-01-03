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

package uk.gov.hmrc.iafrontend

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.testsupport.Spec

import scala.concurrent.{ExecutionContext, Future}

trait authMock extends Spec{

  val mockAuth = mock[AuthConnector]
  def mockAuthorise[T](predicate: Predicate = EmptyPredicate,
                       retrievals: Retrieval[T]
                      )(response: Future[T]): Unit = {
    when(
      mockAuth.authorise(
        ArgumentMatchers.eq(predicate),
        ArgumentMatchers.eq(retrievals)
      )(
        ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext])
    ) thenReturn response
    ()
  }
  class testAuth extends AuthorisedFunctions {
    override def authConnector: AuthConnector = mockAuth
  }

}
