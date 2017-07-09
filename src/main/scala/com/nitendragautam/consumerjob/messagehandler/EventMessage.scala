package com.nitendragautam.consumerjob.messagehandler



case class EventMessage(dateTime : String,
                        clientIpAddress : String,
                        httpStatusCode : String,
                        httpRequestField : String,
                        httpRequestBytes : String)


