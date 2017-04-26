package com.nitendragautam.consumerjob.main


import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration._


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





}
