package com.nitendragautam.consumerjob.messagehandler

case class EventMessage(dateTime :Long,
                        clientIpAddress :String,
                        httpStatusCode :String)

