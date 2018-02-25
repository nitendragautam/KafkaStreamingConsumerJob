package com.nitendragautam.consumerjob.services


import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.google.gson.{Gson, GsonBuilder}
import com.nitendragautam.consumerjob.messagehandler._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.serialization.StringDeserializer
import org.influxdb.dto.Point
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

    val actorConf =KafkaConsumerActor.Conf(2.seconds,0.seconds)
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

  val dateFormat = "yyyy-MM-dd-HH-mm-ss"
  val recordsExtractor = ConsumerRecords.extractor[String, String]

  val kafkaTopic = config.getString("akka.kafka.consumer.topic")

  val kafkaConsumerActor = context.actorOf(
    KafkaConsumerActor.props(kafkaConfig, actorConfig, self)
  )

  context.watch(kafkaConsumerActor)

  kafkaConsumerActor ! Subscribe.AutoPartition(List(kafkaTopic))


  def receive = {

    case recordsExtractor(records) => {

     processRecords(records)
        //Process Records
        sender() ! Confirm(records.offsets,commit = true)

    }
  }


  //Process Records
def processRecords(records :ConsumerRecords[String ,String]) {

  //Get the records Values as No Key is Passed
  records.values.foreach {

    case (value) => {
      val gson = new GsonBuilder().create()
val kafkaMessage = gson.fromJson(value ,classOf[EventMessage])




      val dbName = config.getString("influxdb.dataBase")

      val point = Point
        .measurement("stream")
        .time(getEpochTime(kafkaMessage.dateTime,dateFormat), TimeUnit.MILLISECONDS)
        .tag("httpStatusCode",kafkaMessage.httpStatusCode)
        .tag("clientIpAddress", kafkaMessage.clientIpAddress)
        .tag("httpRequestField",kafkaMessage.httpRequestField)
        .addField("httpRequestBytes",Integer.parseInt(kafkaMessage.httpRequestBytes))
        .build()
      influxdbRestService.writeDataInfluxDb(point, dbName)
      logger.info("Current Date: " + getTodaysDate() + " dateTime " + kafkaMessage.dateTime +
        " clientIpAddress " + kafkaMessage.clientIpAddress +
        " httpStatusCode " + kafkaMessage.httpStatusCode
        +" httpRequestField " +kafkaMessage.httpRequestField
        +" httpRequestBytes "+kafkaMessage.httpRequestBytes)

    }

  }

}

  private def getTodaysDate(): String ={
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE,0)
    dateFormat.format(cal.getTime())
  }

  private def getEpochTime(dateString: String ,dateFormat: String): Long ={
    val format = new SimpleDateFormat(dateFormat)
    val date = format.parse(dateString)
    val timeInMilliSec = date.getTime

    timeInMilliSec
  }
}



