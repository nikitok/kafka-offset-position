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
class FromKafka extends EnginePartitions with LazyLogging {

  val consumerMap: mutable.Map[Int, Option[SimpleConsumer]] = mutable.Map()

  override def offset(zkClient: ZkClient, zkUtils: ZkUtils, topic: String, partitionId: Int): Option[OffsetDetail] = {
    zkUtils.getLeaderForPartition(topic, partitionId) match {
      case Some(bid) =>
        val consumerOpt = consumerMap.getOrElseUpdate(bid, getConsumer(zkUtils, bid))
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

  protected def getConsumer(zkUtils: ZkUtils, bid: Int): Option[SimpleConsumer] = {
    try {
      zkUtils.readDataMaybeNull(ZkUtils.BrokerIdsPath + "/" + bid) match {
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