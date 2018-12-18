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

package uk.gov.hmrc.iafrontend.lock


import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.testsupport.ServiceSpec

import scala.concurrent.{Future, Promise}
import scala.util.Success

class LockSpec extends ServiceSpec  {

  val lockService: LockService = injector.instanceOf[LockService]
  val lockConfig = injector.instanceOf[LockConfig]
  val timeout = lockConfig.timeout.toSeconds

  implicit val hc = HeaderCarrier()

  "happy path" in  {
    LockResponses.acquireLock(timeout)
    LockResponses.releaseLock()
    val p: Promise[String] = Promise()

    def completeP(): Future[String] = {
      p.complete(Success("computation that requires a lock"))
      p.future
    }

    p.isCompleted shouldBe false withClue "We should not start computation before acquiring the lock"
    val result = lockService.withLock(completeP()).futureValue
    p.isCompleted shouldBe true withClue "Computation should be already started because we acquired the lock"
    result shouldBe "computation that requires a lock"
    LockResponses.verifyAcquireLock(timeout)
    LockResponses.verifyReleaseLock()
  }

  "fail case when lock is already in use" in {

    LockResponses.acquireExistingLock(timeout)

    val p: Promise[String] = Promise()

    def completeP(): Future[String] = {
      p.complete(Success("computation that requires a lock"))
      p.future
    }
    p.isCompleted shouldBe false withClue "We should not start computation before acquiring the lock"

    val throwable = lockService.withLock(completeP()).failed.futureValue
    throwable shouldBe an [LockException]
    throwable.getMessage shouldBe "The 'ia' lockName is already in use. Have you run ia before it finished?"

    p.isCompleted shouldBe false withClue "Computation should NOT started because lock was not successfully acquired"
    LockResponses.verifyAcquireLock(timeout)

  }
}
