package com.nitendragautam.consumerjob.main


import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.stream.ActorMaterializer
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





}
