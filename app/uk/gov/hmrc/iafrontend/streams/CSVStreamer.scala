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

package uk.gov.hmrc.iafrontend.streams

import java.io.File
import java.nio.file.{Files, Path}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString
import better.files.File.{root, _}
import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.connector.IaConnector
import uk.gov.hmrc.iafrontend.domain.GreenUtr
import uk.gov.hmrc.iafrontend.lock.LockService

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

class CSVStreamer @Inject()(iaConnector: IaConnector,
                            lockService: LockService,
                            CSVStreamerConfig: CSVStreamerConfig) {

  //todo is this ok
  implicit val system = ActorSystem("System")
  implicit val materializer = ActorMaterializer()

  def processFile(filePath: Path)(implicit headerCarrier: HeaderCarrier): Unit = {

    val desSpot = root / "tmp"
    val fileName = filePath.toString.replaceAll(".zip", "")
    filePath.unzipTo(destination = desSpot)
    val csvFile = Files.newDirectoryStream(desSpot.path)
      .filter(_.getFileName.toString.contains(fileName))
      .map(_.toAbsolutePath).head
  upload(parseFile(csvFile),filePath).onComplete{_ =>
      deleteFile(filePath.toString)
      iaConnector.switch().map { _ =>
        lockService.release("ia")
      }
    }
  }

  private def upload(dataSource: Source[Int, _],filePath:Path)(implicit headerCarrier: HeaderCarrier) = {
    Logger.info("Beginning parsing of csv file")
      val sink = Sink.fold[Int, Int](0)((total, batch) =>total + batch)
      dataSource.recover{
        case e => throw e
  }.toMat(sink)(Keep.right).run()
  }

  private def cleanByte(byteString: ByteString): String = byteString.utf8String.replaceAll("[^\\d.]", "").take(10)

  private def sendBatch(batchString: Seq[String])(implicit headerCarrier: HeaderCarrier) = {
    Logger.info("Sending batch")
    iaConnector.sendUtrs(batchString.map(line => {
      GreenUtr(line)
    }).toList)
  }

  def sendBatchesFlow()(implicit hc: HeaderCarrier): Flow[ByteString, Int, NotUsed] =
    Flow[ByteString]
      .via(
        Framing.delimiter(ByteString(","), CSVStreamerConfig.frameSize, allowTruncation = true)
          .map(cleanByte).grouped(CSVStreamerConfig.batchSize)
          .mapAsync(CSVStreamerConfig.parallelism)(sendBatch))


  def parseFile(file: Path)(implicit ex: ExecutionContext, hc: HeaderCarrier): Source[Int, _] = {
    FileIO.fromPath(file).via(sendBatchesFlow)
  }

  private def deleteFile(path: String) = {
    new File(path).delete()
  }
}
