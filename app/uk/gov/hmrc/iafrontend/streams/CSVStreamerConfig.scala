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

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.iafrontend.streams.CSVStreamerConfig._
//todo find out a good number for parallelism
case class CSVStreamerConfig(batchSize:Int,frameSize:Int , parallelism:Int ) {
  @Inject
  def this (configuration: Configuration) = {
    this(
      configuration.getInt("batch-size").getOrElse(defaultBatchSize),
      configuration.getInt("frame-size").getOrElse(defaultFrameSize),
      configuration.getInt("parallelism").getOrElse(defaultParallelism)
    )
  }
}
object CSVStreamerConfig{
  val defaultBatchSize = 50000
  val defaultFrameSize = 60000
  //todo set to cpu cores
  val defaultParallelism= 5
}
