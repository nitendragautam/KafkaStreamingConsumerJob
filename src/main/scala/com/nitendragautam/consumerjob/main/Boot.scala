package com.nitendragautam.consumerjob.main




import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.nitendragautam.consumerjob.services.ConsumerActorService

/**
  * KafkaStreaming Consumer which consumers from Kafka Topic
  */
object Boot extends App{
//Define Actor System

  implicit val system = ActorSystem("ConsumerActorSystem")
  implicit val materializer =ActorMaterializer()
  implicit val executionContext = system.dispatcher

//Start Consumer Actor
ConsumerActorService.apply(system)



}