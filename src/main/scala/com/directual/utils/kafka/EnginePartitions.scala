package com.directual.utils.kafka

import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient

/**
  * Created by noviiden on 27/05/16.
  */

case class KafkaOffsetUtilsException(message: String = "error") extends RuntimeException(message)

case class OffsetDetail(topic: String, group: String, partition: Int, offset: Long, positionEngine: Long = 0) {
  def lag = offset - positionEngine
}

case class PartitionInfo(topic: String, pid: Int)

case class OffsetLag(first: OffsetDetail, second: Option[OffsetDetail]) {
  def lag = second match {
    case Some(s) => first.offset - s.offset
  }
}

case class PartitionSummary(result: Seq[OffsetDetail]) {
  def maxOffset =
    result match {
      case Nil => 0L
      case _   => result.map(_.offset).max
    }
  def maxLag =
    result match {
      case Nil => 0L
      case _   => result.map(_.lag).max
    }
  def sumLag =
    result match {
      case Nil => 0L
      case _   => result.map(_.lag).sum
    }
}

trait EnginePartitions {
  def offset(zkClient: ZkClient, zkUtils: ZkUtils, topic: String, partitionId: Int): Option[OffsetDetail]
}