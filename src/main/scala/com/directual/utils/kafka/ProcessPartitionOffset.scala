package com.directual.utils.kafka

import org.I0Itec.zkclient.ZkClient

/**
  * Created by noviiden on 27/05/16.
  */

case class KafkaOffsetUtilsException(message: String = "error") extends RuntimeException(message)

case class OffsetDetail(topic: String, group: String, partition: Int, offset: Long, positionEngine: Long = 0) {
  def lag = offset - positionEngine
}

case class OffsetLag(first: OffsetDetail, second: Option[OffsetDetail]) {
  def lag = second match {
    case Some(s) => first.offset - s.offset
  }
}

trait ProcessPartitionOffset {
  def offset(zkClient: ZkClient, topic: String, partitionId: Int): Option[OffsetDetail]
}