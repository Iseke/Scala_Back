package telegram_bot

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait tgSerializer extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val messageFormat: RootJsonFormat[TelegramMessage] = jsonFormat2(TelegramMessage)
}
