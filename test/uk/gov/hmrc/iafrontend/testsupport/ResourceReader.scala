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

package uk.gov.hmrc.iafrontend.testsupport

import java.io.File
import java.nio.file.{Paths, Files => nioFiles}

import akka.util.ByteString
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart

object ResourceReader {

  val testFilePath = System.getProperty("user.dir") + "/test/uk/gov/hmrc/iafrontend/resources/goodUtr.zip"

  def getMultiPartFromResource(resourcePath: String, newFileLocation: String, fileName: String): MultipartFormData[TemporaryFile] = {
    returnMultiPartBody(returnTempFile(resourcePath, newFileLocation), fileName)
  }

  def returnTempFile(resourcePath: String, newFileLocation: String): TemporaryFile = {
    if (nioFiles.exists(Paths.get(newFileLocation))) {
      nioFiles.delete(Paths.get(newFileLocation))
    }
    nioFiles.copy(Paths.get(System.getProperty("user.dir") + s"/test/resources/${resourcePath}"), Paths.get(newFileLocation))
    val newFile = new File(newFileLocation)
    SingletonTemporaryFileCreator.create(newFile.toPath)
  }

  def returnMultiPartBody(temporaryFile: TemporaryFile, fileName: String): MultipartFormData[TemporaryFile] = {
    new MultipartFormData(
      Map("" -> Seq("dummydata")),
      List(
        FilePart("file", fileName, Some("application/zip"), temporaryFile)
      ),
      List()
    )
  }

  def fileToMultipleByteStr(filename: String): scala.collection.immutable.Iterable[ByteString] =
    scala.io.Source.fromFile(filename)
      .getLines()
      .map(ByteString.apply).to[collection.immutable.Iterable]

}
