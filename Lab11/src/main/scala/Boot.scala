import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import org.slf4j.LoggerFactory
import actors.FileManager
import actors.FileManager.{DownloadAllFiles, GetFile, UploadAllFiles, UploadFile}
import models.{ErrorResponse, PathMd, SuccessfulResponse}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._


object Boot extends App with SprayJsonSerializer {
  implicit val system: ActorSystem = ActorSystem("file-manager-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val timeout: Timeout = Timeout(10.seconds)

  val log = LoggerFactory.getLogger("Boot")

  val clientRegion: Regions = Regions.EU_CENTRAL_1

  val credentials = new BasicAWSCredentials("AKIA5X3SG6UTHIMVMYXK", "OODFhKNvRks5qI9TOuyN2apD25S4HaiOFRVPo1kb")

  val client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(clientRegion)
    .build()

  val bucketName = "iseke-file-bucket"

  val worker = system.actorOf(FileManager.props(client, bucketName))

  createBucket(client, bucketName)

  val route =
    concat(
      path("file1") {
        concat(
          get {
            parameters('filename.as[String]) { fileName =>
              val gett = (worker ? GetFile(fileName)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
              onSuccess(gett) {
                case Left(error) => {
                  complete(error.status, error)
                }
                case Right(successful) => {
                  complete(successful.status, successful)
                }
              }
            }
          },
          post {
            entity(as[PathMd]) { pathModel =>
              val postt = (worker ? UploadFile(pathModel.path)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
              onSuccess(postt) {
                case Left(error) => {
                  complete(error.status, error)
                }
                case Right(successful) => {
                  complete(successful.status, successful)
                }
              }
            }
          }
        )
      },
      pathPrefix("file2") {
        concat(
          path("in") {
            get {
              complete {
                worker ! DownloadAllFiles
                "Downloaded"
              }
            }
          },
          path("out") {
            get {
              complete {
                worker ! UploadAllFiles
                "Uploaded"
              }
            }
          }
        )
      }
    )


  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)


  def createBucket(s3client: AmazonS3, bucket: String): Unit = {
    if (!s3client.doesBucketExistV2(bucket)) {
      s3client.createBucket(bucket)
      log.info(s"Bucket with name: $bucket created")
    } else {
      log.info(s"Bucket $bucket already exists")
    }
  }


}
