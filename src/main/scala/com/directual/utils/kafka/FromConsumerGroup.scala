package com.directual.utils.kafka

import com.typesafe.scalalogging.LazyLogging
import kafka.consumer._
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient
import org.apache.zookeeper.data.Stat

import scala.collection.mutable

/**
  * Created by noviiden on 27/05/16.
  */
class FromConsumerGroup(group: String) extends EnginePartitions with LazyLogging {
  val consumerMap: mutable.Map[Int, Option[SimpleConsumer]] = mutable.Map()

  override def offset(zkClient: ZkClient, zkUtils: ZkUtils, topic: String, partitionId: Int): Option[OffsetDetail] = {

    val offsetPath = s"${ZkUtils.ConsumersPath}/$group/offsets/$topic/$partitionId"

    if (!zkUtils.pathExists(offsetPath)) {
      return None
    }

    val (offset, stat: Stat) = zkUtils.readData(offsetPath)

    Some(OffsetDetail(topic, group, partitionId, offset.toLong))
  }
}
