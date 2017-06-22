package com.nitendragautam.consumerjob.services


import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.{Logger, LoggerFactory}

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


  def receive = {

    case recordsExtractor(records) =>

      processRecords(records) //Process Records





  }


  //Process Records
  private def processRecords(records :ConsumerRecords[String ,String])  ={

    records.pairs.foreach{
      case (key,value) => {


     // val message = (new Gson).fromJson( value,EventMessage).asInstanceOf[EventMessage]

      //logService.logMessage("Date Time "+message.dateTime +"Message Client IP "+message.clientIpAddress +" ClientHttp Address "+message.httpStatusCode)


      logger.info( " TxID " +UUID.randomUUID().toString+" Received Message Value " + value)


/*
      val dbName = config.getString("influxdb.dataBase")

      val point = Point
        .measurement("stream")
        .time(message.dateTime ,TimeUnit.MILLISECONDS)
        .addField("ipAddress",message.clientIpAddress)
        .addField("statusCode",message.httpStatusCode)

        .build()

      influxdbRestService.writeDataInfluxDb(point,dbName)
  */

    }

  }
    sender() ! Confirm(records.offsets, commit = true)
}


}