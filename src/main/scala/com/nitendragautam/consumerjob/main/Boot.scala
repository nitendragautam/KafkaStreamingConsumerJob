package com.nitendragautam.consumerjob.main



import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.google.gson.Gson
import com.nitendragautam.consumerjob.domain.KafkaMessage
import com.nitendragautam.consumerjob.services.{InfluxdbRestService, LogService}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.influxdb.dto.Point

import scala.concurrent.Future

/**
  * KafkaStreaming Consumer which consumers from Kafka Topic
  */
object Boot extends App{
//Define Actor System

  implicit val system = ActorSystem("ConsumerActorSystem")
  implicit val materializer =ActorMaterializer()
  implicit val executionContext = system.dispatcher

val db = new DB
val logService = new LogService
  val influxdbRestService = new InfluxdbRestService
  val config = ConfigFactory.load()

  val kafkaTopic =config.getString("akka.kafka.consumer.topic")
//Todo {Read the data from Kafka Topic using Kafka Streaming API}

val consumerSettings = ConsumerSettings(system ,new StringDeserializer ,new StringDeserializer)
    .withBootstrapServers(config.getString("akka.kafka.consumer.bootstrap.servers"))
  .withGroupId(config.getString("akka.kafka.consumer.group.id"))
  .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")


  Consumer.committableSource(consumerSettings,Subscriptions.topics(kafkaTopic))
  .map{
    msg =>
      //Process KafkaMessages
      db.processKafkaMessages(msg.record.value())


  }
    .runWith(Sink.ignore)





}


class DB {
  /*
Processes the Kafka Messages and
 */
  val logService = new LogService
  def processKafkaMessages(kafkaMessage :String): Future[Done] ={
    logService.logMessage("Kafka Message processing "+kafkaMessage)
    val message =
      (new Gson).fromJson(kafkaMessage ,KafkaMessage.getClass).asInstanceOf[KafkaMessage]





    /*
    val dbName = config.getString("influxdb.dataBase")
    logService.logMessage("DataBase "+dbName)
    val point = Point
      .measurement("stream")
      .time(message.dateTime ,TimeUnit.MILLISECONDS)
      .addField("ipAddress",message.clientIpAddress)
      .addField("statusCode",message.httpStatusCode)

      .build()

    influxdbRestService.writeDataInfluxDb(point,dbName)
*/
    Future.successful(Done)
  }
}