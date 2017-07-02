package com.nitendragautam.consumerjob.messagehandler

import spray.json._

case class EventMessage(dateTime :String,
                        clientIpAddress :String,
                        httpStatusCode :String,
                        httpRequestField :String)



object EventMessagesJsonProtocol extends DefaultJsonProtocol{
implicit val eventMessageFormat = jsonFormat4(EventMessage)
}