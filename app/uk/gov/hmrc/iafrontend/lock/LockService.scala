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

import javax.inject.Inject
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future


class LockException(msg: String, cause: Throwable) extends Exception(msg, cause)

class LockService @Inject()(http: HttpClient, lockConfig: LockConfig) {

  private val lockName = "ia"

  def withLock[A](f: => Future[A])(implicit hc: HeaderCarrier): Future[A] = {
    val result = for {
      _ <- acquire
      result <- f
      _ <- release(lockName)
    } yield result

    //    result.onComplete(_ => release(lockName))
    result
  }

   def acquire(implicit hc: HeaderCarrier): Future[Unit] = {
    val timeOut = lockConfig.timeout
    val url = s"$baseUrl/locks/$lockName?timeout=${timeOut.toSeconds}"
    val logMessage = s"Acquiring lock [lockName=$lockName] [timeOut=$timeOut] [url=$url]"

    Logger.info(logMessage)
    http.POSTEmpty(url)
      .map(_ => Logger.info(s"$logMessage - Done"))
      .recover {
        case e: Upstream4xxResponse if e.upstreamResponseCode == Status.CONFLICT =>
          Logger.error(s"$logMessage - Failed. The '$lockName' lockName is already in use. Have you run recon before it finished?", e)
          throw new LockException(s"The '$lockName' lockName is already in use. Have you run recon before it finished?", e)
        case e =>
          Logger.error(s"$logMessage - Failed. The '$lockName' lockName is already in use. Have you run recon before it finished?", e)
          throw new LockException(s"$logMessage - Failed", e)
      }
  }

  def release(lock: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    val url = s"$baseUrl/locks/ia"
    val logMessage = s"Releasing lock [lockName=$lockName] [url=$url]"
    Logger.info(logMessage)
    http
      .DELETE(url)
      .map(_ => Logger.info(s"$logMessage - Done"))
      .recover {
        case uk.gov.hmrc.http.Upstream4xxResponse(_, 409, _, _) => ()
        case ex =>
          val timeOut = lockConfig.timeout
          val errMessage = s"$logMessage - Failed. If The recon finished you can ignore that error. The lock will automatically expire in $timeOut"
          Logger.error(errMessage)
          throw new LockException(errMessage, ex)
      }
  }

  private val baseUrl = lockConfig.baseUrl
}
