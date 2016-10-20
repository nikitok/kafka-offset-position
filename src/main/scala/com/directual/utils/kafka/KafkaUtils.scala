package com.directual.utils.kafka

import com.typesafe.scalalogging.LazyLogging
import kafka.utils.{ ZKStringSerializer, ZkUtils }
import org.I0Itec.zkclient.{ ZkClient, ZkConnection }

import scala.collection._

object KafkaUtils extends LazyLogging {

  val defEngine = new FromKafka

  def createClient(server: String) = {
    new ZkClient(server)
  }

  def createZkUtils(server: String) = {
    val client =kafka.utils.ZkUtils.createZkClient(server, 10000, 10000)
    new kafka.utils.ZkUtils(client, new ZkConnection(server), false)
  }

  def offset(topics: Seq[String])(implicit zkClient: ZkClient, zkUtils: ZkUtils): Seq[OffsetDetail] = {
    topics.flatten(partitions).flatMap(partitionInfo => {
      defEngine.offset(zkClient, zkUtils, partitionInfo.topic, partitionInfo.pid)
    })
  }

  def offset(topics: Seq[String], engine: EnginePartitions = defEngine)(implicit zk: ZkClient, zkUtils: ZkUtils): Seq[OffsetDetail] = {
    offset(topics).map(pid => {
      engine.offset(zk, zkUtils, pid.topic, pid.partition) match {
        case Some(find) => pid.copy(positionEngine = find.offset)
        case _          => pid
      }
    })
  }

  def offsetSummary(topics: String,
                    engine: EnginePartitions = defEngine)(implicit zk: ZkClient, zkUtils: ZkUtils): PartitionSummary = {
    PartitionSummary(offset(Seq(topics), engine))
  }

  def partitions(topic: String)(implicit zk: ZkClient, zkUtils: ZkUtils): Seq[PartitionInfo] = {
    val partitionMap = zkUtils.getPartitionsForTopics(Seq(topic))
    for {
      partitions <- partitionMap.get(topic).toSeq
      pid <- partitions.sorted
    } yield PartitionInfo(topic, pid)
  }

}