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

package uk.gov.hmrc.iafrontend

import java.io.File
import java.nio.file.Files

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart

object FileTestHelper {
  //todo move to file refactor
  def postSource(tmpFile: File) = {
    import play.api.mvc.MultipartFormData._
    Source(FilePart("name", "hello.txt", Option("text/plain"),
      FileIO.fromPath(tmpFile.toPath)) :: DataPart("key", "value") :: List())
  }
  val testFilePath = System.getProperty("user.dir") + "/test/uk/gov/hmrc/iafrontend/resources/goodUtr.csv"
  val fileTest: File = new File(testFilePath)
   def getMockFileCSV = {
    val userDirectory = System.getProperty("user.dir")
    def deleteIfExists(filePath:String):Unit = {
      val fileTemp = new File(filePath)
      if (fileTemp.exists) {
        fileTemp.delete()
      }
      ()
    }
    deleteIfExists(System.getProperty("user.dir") + "/test/uk/gov/hmrc/iafrontend/resources/copy/goodUtr.csv")
    Files.copy(fileTest.toPath, new java.io.File(System.getProperty("user.dir") + "/test/uk/gov/hmrc/iafrontend/resources/copy/goodUtr.csv").toPath)
    val tempFile = TemporaryFile(new java.io.File(userDirectory + "/test/uk/gov/hmrc/iafrontend/resources/copy/goodUtr.csv"))
    val part = FilePart[TemporaryFile](
      key = "fileParam",
      filename = "goodUtr.csv",
      contentType = Some("Content-Type: multipart/form-data"),
      ref = tempFile)
    val file = MultipartFormData(dataParts = Map(), files = Seq(part), badParts = Seq())
    file
  }

  def fileToMultipleByteStr(filename : String) :  scala.collection.immutable.Iterable[ByteString] =
    scala.io.Source.fromFile(filename)
      .getLines()
      .map(ByteString.apply).to[collection.immutable.Iterable]
}