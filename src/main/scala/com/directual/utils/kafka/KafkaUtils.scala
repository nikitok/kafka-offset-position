package com.directual.utils.kafka

import com.typesafe.scalalogging.LazyLogging
import kafka.utils.{ ZKStringSerializer, ZkUtils }
import org.I0Itec.zkclient.ZkClient

import scala.collection._

object KafkaUtils extends LazyLogging {

  val defEngine = new FromKafka

  def createClient(server: String) = new ZkClient(server, 10000, 10000, ZKStringSerializer)

  def offset(topics: Seq[String])(implicit zkClient: ZkClient): Seq[OffsetDetail] = {
    topics.flatten(partitions).flatMap(partitionInfo => {
      defEngine.offset(zkClient, partitionInfo.topic, partitionInfo.pid)
    })
  }

  def offset(topics: Seq[String], engine: EnginePartitions = defEngine)(implicit zk: ZkClient): Seq[OffsetDetail] = {
    offset(topics).map(pid => {
      engine.offset(zk, pid.topic, pid.partition) match {
        case Some(find) => pid.copy(positionEngine = find.offset)
        case _          => pid
      }
    })
  }

  def offsetSummary(topics: String,
                    engine: EnginePartitions = defEngine)(implicit zk: ZkClient): PartitionSummary = {
    PartitionSummary(offset(Seq(topics), engine))
  }

  def partitions(topic: String)(implicit zk: ZkClient): Seq[PartitionInfo] = {
    val partitionMap = ZkUtils.getPartitionsForTopics(zk, Seq(topic))
    for {
      partitions <- partitionMap.get(topic).toSeq
      pid <- partitions.sorted
    } yield PartitionInfo(topic, pid)
  }

}