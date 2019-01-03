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

package uk.gov.hmrc.iafrontend.csvstreamer

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.FileTestHelper._
import uk.gov.hmrc.iafrontend.connector.IaConnector
import uk.gov.hmrc.iafrontend.domain.GreenUtr
import uk.gov.hmrc.iafrontend.lock.LockService
import uk.gov.hmrc.iafrontend.streams.{CSVStreamer, CSVStreamerConfig}
import uk.gov.hmrc.iafrontend.testsupport.Spec

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CSVStreamerSpec extends Spec with BeforeAndAfterEach{


  val mockIA = mock[IaConnector]
  val mockLock = mock[LockService]
  implicit val hc = HeaderCarrier()
  implicit val system = ActorSystem("System")
  implicit val materializer = ActorMaterializer()


  override def beforeEach(): Unit = {
    reset(mockIA)
  }
  "CSVStreamer" should {
    "flow should send data to ia in batchs" in {
      val streamer = new CSVStreamer(mockIA,mockLock,CSVStreamerConfig(2,400,1))
      val flowToTest: Flow[ByteString, Int, NotUsed] = streamer.sendBatchesFlow()
      when(mockIA.sendUtrs(ArgumentMatchers.any[List[GreenUtr]])(ArgumentMatchers.any[HeaderCarrier])).thenReturn(Future.successful(2))
      val future = akka.stream.scaladsl.Source[ByteString](fileToMultipleByteStr(testFilePath)).via(flowToTest).runWith(Sink.fold(Seq.empty[Int])(_ :+ _))
      val result = Await.result(future,Duration.Inf)
      assert(result == List(2))
    }
    "flow should call ia everythime there is a batch size met in" in {
      val streamer = new CSVStreamer(mockIA,mockLock,CSVStreamerConfig(1,400,1))
      val flowToTest: Flow[ByteString, Int, NotUsed] = streamer.sendBatchesFlow()
      when(mockIA.sendUtrs(ArgumentMatchers.any[List[GreenUtr]])(ArgumentMatchers.any[HeaderCarrier])).thenReturn(Future.successful(2))
      val future = akka.stream.scaladsl.Source[ByteString](fileToMultipleByteStr(testFilePath)).via(flowToTest).runWith(Sink.fold(Seq.empty[Int])(_ :+ _))
      val result = Await.result(future,Duration.Inf)
      verify(mockIA, times(2)).sendUtrs(ArgumentMatchers.any[List[GreenUtr]])(ArgumentMatchers.any[HeaderCarrier])
    }
  }
}
