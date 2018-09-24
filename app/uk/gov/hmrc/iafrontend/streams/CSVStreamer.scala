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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString
import javax.inject.Inject
import play.api.Logger
import play.api.libs.streams.Accumulator
import play.api.mvc.BodyParser
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iafrontend.connector.IaConnector
import uk.gov.hmrc.iafrontend.domain.GreenUtr

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class CSVStreamer @Inject()(iaConnector: IaConnector) {

  //todo is this ok
  implicit val system = ActorSystem("System")
  implicit val materializer = ActorMaterializer()

  def upload(dataSource: Source[Int, _])(implicit headerCarrier: HeaderCarrier) = {
    Logger.info("Beginning parsing and dropping off db")
    iaConnector.drop().flatMap { _ =>
      val sink = Sink.fold[Int, Int](0)((total, batch) => total + batch)

      dataSource.toMat(sink)(Keep.right).run()
    }
  }

  private def cleanByte(byteString: ByteString):String = byteString.utf8String.split(" ").last.replaceAll("[^\\d.]", "").take(10)
  private  def sendBatch(batchString:Seq[String])(implicit headerCarrier: HeaderCarrier) = {
    Logger.info("Sending batch")
    iaConnector.sendUtrs(batchString.map(line => GreenUtr(line)).toList)
  }
  def sendBatchesFlow()(implicit hc:HeaderCarrier): Flow[ByteString, Int, NotUsed] =
    Flow[ByteString]
      .via(
        Framing.delimiter(ByteString(","), 60000, allowTruncation = true)
          .map(cleanByte).grouped(50000)
          .mapAsync(15)(sendBatch))
  def bodyParser(implicit ex: ExecutionContext, hc: HeaderCarrier): BodyParser[Source[Int, _]] = BodyParser { bs =>
    //todo write tests and perhaps just use clean byte on first bit of data
    Accumulator.source[ByteString]
      .map(_.via(sendBatchesFlow))
      .map(Right.apply)
  }
}
