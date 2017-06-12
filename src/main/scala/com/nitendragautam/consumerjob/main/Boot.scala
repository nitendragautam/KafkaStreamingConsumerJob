package com.nitendragautam.consumerjob.main


import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

/**
  * KafkaStreaming Consumer which consumers from Kafka Topic
  */
object Boot extends App{
//Define Actor System

  implicit val system = ActorSystem("ConsumerActorSystem")
  implicit val materializer =ActorMaterializer()
  implicit val executionContext = system.dispatcher




  val config = ConfigFactory.load()
  val consumerConfig =config.getConfig("kafka.consumer")
  val kafkaTopic =consumerConfig.getString("topic")

//Todo {Read the data from Kafka Topic using Kafka Streaming API}

val consumerSettings = ConsumerSettings(system ,new StringDeserializer ,new StringDeserializer)
    .withBootstrapServers(config.getString("kafka.bootstrap.servers"))
  .withGroupId(config.getString("kafka.consumer.group.id"))
  .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")


  Consumer.committableSource(consumerSettings
    ,Subscriptions.topics(config.getString("kafka.consumer.topic")))
  .map{
    msg =>{
     val msg1= msg.record.value()
println(msg1)

    }
  }
  .runWith(Sink.ignore)


}
