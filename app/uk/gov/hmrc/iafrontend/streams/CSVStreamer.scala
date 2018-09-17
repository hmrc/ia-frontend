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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CSVStreamer @Inject()(iaConnector: IaConnector) {

  implicit val system = ActorSystem("System")
  implicit val materializer = ActorMaterializer()

  def upload(dataSource: Source[Int, _])(implicit headerCarrier: HeaderCarrier) = {
    Logger.info("Beginning parsing and dropping off db")
    iaConnector.drop().flatMap { _ =>
      val sink = Sink.fold[Int, Int](0)((total, batch) => total + batch)
      dataSource.toMat(sink)(Keep.right).run()
    }
  }

  def bodyParser(implicit ex: ExecutionContext, hc: HeaderCarrier): BodyParser[Source[Int, _]] = BodyParser { bs =>
    val lineDelimiter: Flow[ByteString, Int, NotUsed] = Flow[ByteString]
      .via(
        Framing.delimiter(ByteString(","), 60000, allowTruncation = true)
          .map(line => line.utf8String).grouped(50000)
          .mapAsync(5)(lines => {
            iaConnector.sendUtrs(lines.map(line => GreenUtr(line)).toList)
          })
      )

    Accumulator.source[ByteString]
      .map(_.via(lineDelimiter))
      .map(Right.apply)
  }
}
