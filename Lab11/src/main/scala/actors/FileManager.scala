package actors

import java.io.{File, FilenameFilter}
import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging, Props}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsRequest, ObjectMetadata, PutObjectRequest, PutObjectResult}
import models.{ErrorResponse, SuccessfulResponse}

import scala.util.{Failure, Success, Try}

object FileManager{
   val filePrefix = "./src/main/resources/s3"
   val inPrefix = "./src/main/resources/in"
   val outPrefix = "./src/main/resources/out"

  def downloadFile(client: AmazonS3, bucketName: String, objectKey: String, fullPath: String): ObjectMetadata = {
    val file = new File(fullPath)
    createMissingDirectories(file) // creating missing directories
    client.getObject(new GetObjectRequest(bucketName, objectKey), file)
  }

  def uploadFile(client: AmazonS3, bucketName: String, objectKey: String, filePath: String): PutObjectResult = {
    val metadata = new ObjectMetadata()
    metadata.setContentType("plain/text")
    metadata.addUserMetadata("user-type", "customer")

    val request = new PutObjectRequest(bucketName, objectKey, new File(filePath))
    request.setMetadata(metadata)
    client.putObject(request)
  }

  def createMissingDirectories(file: File): Unit = {
    val dirs = file.getParentFile()

    if (dirs != null) {
      dirs.mkdirs()
    }
  }

  case class GetFile(fileName: String)

  case class UploadFile(fileName: String)

  case object UploadAllFiles

  case object DownloadAllFiles

  def props(client: AmazonS3, bucketName: String) = Props(new FileManagerActor(client, bucketName))

}

class FileManagerActor(client: AmazonS3, bucketName: String) extends Actor with ActorLogging {

  import FileManager._

  override def receive: Receive = {
    case GetFile(fileName) =>
      val rootSender = sender()
      val objectKey = fileName

      if (client.doesObjectExist(bucketName, objectKey)) {
        val fullPath = s"${filePrefix}/${fileName}"
        downloadFile(client, bucketName, fileName, fullPath)

        rootSender ! Right(SuccessfulResponse(200, s"Successfully downloaded file with FILENAME: ${fileName}"))
        log.info("Successfully get file-object with FILENAME: {} from AWS S3", fileName)
      } else {
        rootSender ! Left(ErrorResponse(404, s"File with FILENAME: ${fileName} does not exist inside the bucket"))
        log.info(s"Failed to get file with FILENAME: ${fileName}. It does not exist inside the bucket")
      }

    case UploadFile(fileName) =>
      val rootSender = sender()
      val objectKey = fileName

      if (client.doesObjectExist(bucketName, objectKey)) {
        rootSender ! Left(ErrorResponse(409, s"File with FILENAME: ${fileName} already exists"))
        log.info(s"Failed to upload file with FILENAME: ${fileName}. It already exists")
      } else {
        val filePath = s"${filePrefix}/${fileName}"

        Try(uploadFile(client, bucketName, objectKey, filePath)) match {
          case Success(_) =>
            rootSender ! Right(SuccessfulResponse(201, s"Successfully uploaded file with FILENAME: ${fileName}"))
            log.info("Successfully put file-object with FILENAME: {} to AWS S3", fileName)
          case Failure(exception) =>
            rootSender ! Left(ErrorResponse(500, s"Internal error occurred while uploading a file with FILENAME: ${fileName}"))
            log.info(s"Failed to upload file with FILENAME: ${fileName}. Error message: ${exception.getMessage}")
        }
      }

    case UploadAllFiles =>
      val mainDirectory: File = new File(outPrefix)
      uploadDirectoryContents(mainDirectory)

      def uploadDirectoryContents(dir: File): Unit = {
        val files: Array[File] = dir.listFiles(new FilenameFilter {
          override def accept(dir: File, name: String): Boolean =
            !name.equals(".DS_Store")
        })

        for (file <- files) {
          var path = Paths.get(file.getPath)
          path = path.subpath(5, path.getNameCount)

          if (file.isDirectory)
            uploadDirectoryContents(file)
          else
            uploadFile(client, bucketName, path.toString, outPrefix + "/" + path.toString)

        }
      }

    case DownloadAllFiles =>
      val objects = client.listObjects(new ListObjectsRequest().withBucketName(bucketName))
      objects.getObjectSummaries.forEach(objectSummary => downloadFile(client, bucketName, objectSummary.getKey, inPrefix + "/" + objectSummary.getKey))
  }
}