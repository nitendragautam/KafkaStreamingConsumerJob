package com.nitendragautam.consumerjob.services

import com.typesafe.config.ConfigFactory
import org.influxdb.InfluxDBFactory
import org.influxdb.dto._
import org.slf4j.{Logger, LoggerFactory}

/**
  * Code for Manipulating InFluxDb database
  */
class InfluxdbRestService {


  private val logger: Logger =
    LoggerFactory.getLogger(classOf[InfluxdbRestService])
  val config = ConfigFactory.load()


  val influxDB =
    InfluxDBFactory.connect(config.getString("influxdb.baseURL"),
      config.getString("influxdb.user"),
      config.getString("influxdb.pass"))

  def pingInfluxDb(): Pong ={
    influxDB.ping()
  }

  /*
  Create a InfluxDb Database if does not exists
   */

  def createDatabase(dbName :String): Unit ={
    //If Database Does not Exists
    if(isDatabaseExists(dbName)==false){
      influxDB.createDatabase(dbName)
    }
  }


  /*
  Query the Database and Gives the Results
   */
  def queryDatabase(queryString :String ,dbName :String): QueryResult={
    val query = new Query(queryString ,dbName)
    influxDB.query(query)

  }


  /*
  Write the Data into the Database
   */

  def writeDataInfluxDb(dataPoint :Point ,dbName :String): Unit ={
    val batchPoints = BatchPoints.database(dbName)
      .tag("async","true")
      .retentionPolicy("autogen")
      .build()
    batchPoints.point(dataPoint)
    influxDB.write(batchPoints)
    logger.info(" Data Written to InfluxDB ")
  }


  /*
  Check if Database Exists
   */

  def isDatabaseExists(dbName :String): Boolean ={

    influxDB.databaseExists(dbName)
  }



}