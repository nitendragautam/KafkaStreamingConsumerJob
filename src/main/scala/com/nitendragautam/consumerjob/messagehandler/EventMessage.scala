package com.nitendragautam.consumerjob.messagehandler

import spray.json._

case class EventMessage(dateTime :String,
                        clientIpAddress :String,
                        httpStatusCode :String)



object EventMessagesJsonProtocol extends DefaultJsonProtocol{
implicit val eventMessageFormat = jsonFormat3(EventMessage)
}