package com.nitendragautam.consumerjob.main


import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.google.gson.Gson
import com.nitendragautam.consumerjob.domain.KafkaMessage
import com.nitendragautam.consumerjob.services.InfluxdbRestService
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.influxdb.dto.Point

/**
  * KafkaStreaming Consumer which consumers from Kafka Topic
  */
object Boot extends App{
//Define Actor System

  implicit val system = ActorSystem("ConsumerActorSystem")
  implicit val materializer =ActorMaterializer()
  implicit val executionContext = system.dispatcher



  val influxdbRestService = new InfluxdbRestService
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
     val kafkaMessages= msg.record.value()
      //Process KafkaMessages
processKafkaMessages(kafkaMessages)

    }
  }
  .runWith(Sink.ignore)

/*
Processes the Kafka Messages and
 */

  def processKafkaMessages(kafkaMessage :String): Unit ={

val message = (new Gson).fromJson(kafkaMessage ,KafkaMessage.getClass).asInstanceOf[KafkaMessage]
val dbName = config.getString("influxdb.dataBase")

    val point1 = Point

      .measurement("streamanalytics")
      .time(message.dateTime ,TimeUnit.MILLISECONDS)
      .tag("ipAddress",message.clientIpAddress)
      .tag("statusCode",message.httpStatusCode)

      .build()

    influxdbRestService.writeDataInfluxDb(point1,dbName)

  }
}
