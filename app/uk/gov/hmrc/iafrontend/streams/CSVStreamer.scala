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

package uk.gov.hmrc.iafrontend.streams

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

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CSVStreamer @Inject()(iaConnector: IaConnector,
                            CSVStreamerConfig: CSVStreamerConfig) {

  //todo is this ok
  implicit val system = ActorSystem("System")
  implicit val materializer = ActorMaterializer()

  def processFile(filePath: Path)(implicit headerCarrier: HeaderCarrier): Future[Int] = {

    val desSpot = root / "tmp"
    val fileName = filePath.toString.replaceAll(".zip","")
    filePath.unzipTo(destination = desSpot)
    val csvFile = Files.newDirectoryStream(desSpot.path)
      .filter(_.getFileName.toString.contains(fileName))
      .map(_.toAbsolutePath).head
    upload(parseFile(csvFile))
  }

  private def upload(dataSource: Source[Int, _])(implicit headerCarrier: HeaderCarrier) = {
    Logger.info("Beginning parsing and dropping off db")
    iaConnector.drop().flatMap { _ =>
      val sink = Sink.fold[Int, Int](0)((total, batch) => total + batch)

      dataSource.toMat(sink)(Keep.right).run()
    }
  }

  private def cleanByte(byteString: ByteString): String = byteString.utf8String.replaceAll("[^\\d.]", "").take(10)

  private def sendBatch(batchString: Seq[String])(implicit headerCarrier: HeaderCarrier) = {
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
}
