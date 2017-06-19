package com.nitendragautam.consumerjob.services

import org.slf4j.{Logger, LoggerFactory}


class LogService {
  private val logger: Logger =
    LoggerFactory.getLogger(classOf[LogService])

  def logMessage(logMessage :String): Unit ={
    logger.info(logMessage)
  }
}
