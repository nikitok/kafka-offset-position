package com.directual.utils.kafka

import com.typesafe.scalalogging.LazyLogging
import kafka.api.{ OffsetRequest, PartitionOffsetRequestInfo }
import kafka.common.TopicAndPartition
import kafka.consumer.SimpleConsumer
import kafka.utils.{ Json, ZkUtils }
import org.I0Itec.zkclient.ZkClient

import scala.collection.{ Map, immutable, mutable }

/**
  * Created by noviiden on 27/05/16.
  */
class OffsetFromKafka extends ProcessPartitionOffset with LazyLogging {

  val consumerMap: mutable.Map[Int, Option[SimpleConsumer]] = mutable.Map()

  override def offset(zkClient: ZkClient, topic: String, partitionId: Int): Option[OffsetDetail] = {
    ZkUtils.getLeaderForPartition(zkClient, topic, partitionId) match {
      case Some(bid) =>
        val consumerOpt = consumerMap.getOrElseUpdate(bid, getConsumer(zkClient, bid))
        consumerOpt map {
          consumer =>
            val topicAndPartition = TopicAndPartition(topic, partitionId)
            val request =
              OffsetRequest(immutable.Map(topicAndPartition -> PartitionOffsetRequestInfo(OffsetRequest.LatestTime, 1)))
            val offset = consumer.getOffsetsBefore(request).partitionErrorAndOffsets(topicAndPartition).offsets.head

            OffsetDetail(topic, "", partitionId, offset)
        }
      case None =>
        //error("No broker for partition %s - %s".format(topic, pid))
        None
    }
  }

  protected def getConsumer(zkClient: ZkClient, bid: Int): Option[SimpleConsumer] = {
    try {
      ZkUtils.readDataMaybeNull(zkClient, ZkUtils.BrokerIdsPath + "/" + bid) match {
        case (Some(brokerInfoString), _) =>
          Json.parseFull(brokerInfoString) match {
            case Some(m) =>
              val brokerInfo = m.asInstanceOf[Map[String, Any]]
              val host = brokerInfo.get("host").get.asInstanceOf[String]
              val port = brokerInfo.get("port").get.asInstanceOf[Int]
              Some(new SimpleConsumer(host, port, 10000, 100000, "ConsumerOffsetChecker"))
            case None =>
              throw KafkaOffsetUtilsException("Broker id %d does not exist".format(bid))
          }
        case (None, _) =>
          throw KafkaOffsetUtilsException("Broker id %d does not exist".format(bid))
      }
    } catch {
      case t: Throwable =>
        logger.error("Could not parse broker info", t)
        throw KafkaOffsetUtilsException("Could not parse broker info")
    }
  }
}