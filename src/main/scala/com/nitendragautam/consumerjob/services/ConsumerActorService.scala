package com.nitendragautam.consumerjob.services


import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.nitendragautam.consumerjob.messagehandler.EventMessage
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.serialization.StringDeserializer
import org.influxdb.dto.Point
import org.slf4j.{Logger, LoggerFactory}
import spray.json._

import scala.concurrent.duration._
/**
  * Created by nitendragautam on 6/21/2017.
  */
object ConsumerActorService {
  def apply(system :ActorSystem ): ActorRef ={
    val config =ConfigFactory.load()

    val kafkaConsumerConf = KafkaConsumer.Conf(
      new StringDeserializer,
      new StringDeserializer,
      bootstrapServers = config.getString("akka.kafka.consumer.bootstrap.servers"),
      groupId = config.getString("akka.kafka.consumer.group.id"),
      enableAutoCommit = false,
      autoOffsetReset = OffsetResetStrategy.EARLIEST)
      .withConf(config)

    val actorConf =KafkaConsumerActor.Conf(1.seconds,3.seconds)
    system.actorOf(Props(new ConsumerActors(config,kafkaConsumerConf,actorConf)))
  }
}


/*
Akka based actors for Kafka consumers
 */
class ConsumerActors(config :Config ,kafkaConfig: KafkaConsumer.Conf[String,String],
                     actorConfig: KafkaConsumerActor.Conf) extends Actor {
  private val logger: Logger = LoggerFactory.getLogger(classOf[ConsumerActors])
  val influxdbRestService = new InfluxdbRestService

  val recordsExtractor = ConsumerRecords.extractor[String, String]

  val kafkaTopic = config.getString("akka.kafka.consumer.topic")

  val kafkaConsumerActor = context.actorOf(
    KafkaConsumerActor.props(kafkaConfig, actorConfig, self)
  )

  context.watch(kafkaConsumerActor)
  kafkaConsumerActor ! Subscribe.AutoPartition(List(kafkaTopic))

  import com.nitendragautam.consumerjob.messagehandler.EventMessagesJsonProtocol._
  def receive = {

    case recordsExtractor(records) =>

      processRecords(records) //Process Records

  }


  //Process Records
def processRecords(records :ConsumerRecords[String ,String])  = {

  records.pairs.foreach {

    case (key, value) => {


      val kafkaMessage = value.parseJson.convertTo[EventMessage]

      logger.info("Date Time " + kafkaMessage.dateTime +
        " Message Client IP " + kafkaMessage.clientIpAddress +
        " ClientHttp Address " + kafkaMessage.httpStatusCode
        +" httpRequestField " +kafkaMessage.httpRequestField
        +"httpRequestBytes "+kafkaMessage.httpRequestBytes)



      val dbName = config.getString("influxdb.dataBase")

      val point = Point
        .measurement("stream")
        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .tag("statusCode",kafkaMessage.httpStatusCode)
        .tag("ipAddress", kafkaMessage.clientIpAddress)
        .tag("httpRequestField",kafkaMessage.httpRequestField)
        .addField("httpRequestBytes" ,kafkaMessage.httpRequestBytes)
        .build()
      influxdbRestService.writeDataInfluxDb(point, dbName)


    }

  }
  sender() ! Confirm(records.offsets, commit = true)
}}
