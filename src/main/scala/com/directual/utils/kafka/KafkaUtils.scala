package com.directual.utils.kafka

import com.typesafe.scalalogging.LazyLogging
import kafka.utils.{ ZKStringSerializer, ZkUtils }
import org.I0Itec.zkclient.ZkClient

import scala.collection._

object KafkaUtils extends LazyLogging {

  val defProcessor = new OffsetFromKafka

  def createClient(server: String) = new ZkClient(server, 10000, 10000, ZKStringSerializer)

  def offset(topics: Seq[String])(implicit zkClient: ZkClient): Seq[OffsetDetail] = {
    topics.flatten(partitions).flatMap(partitionInfo => {
      defProcessor.offset(zkClient, partitionInfo._1, partitionInfo._2)
    })
  }

  def offset(topics: Seq[String], engine: ProcessPartitionOffset = defProcessor)(implicit zkClient: ZkClient): Seq[OffsetDetail] = {
    offset(topics).map(pid => {
      engine.offset(zkClient, pid.topic, pid.partition) match {
        case Some(find) => pid.copy(positionEngine = find.offset)
        case _          => pid
      }
    })
  }

  def partitions(topic: String)(implicit zkClient: ZkClient): Seq[(String, Int)] = {
    val partitionMap = ZkUtils.getPartitionsForTopics(zkClient, Seq(topic))
    for {
      partitions <- partitionMap.get(topic).toSeq
      pid <- partitions.sorted
    } yield topic -> pid
  }

}