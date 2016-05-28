package com.directual.utils.kafka

import com.typesafe.scalalogging.LazyLogging
import kafka.utils.{ Json, ZkUtils }
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNoNodeException
import org.apache.zookeeper.data.Stat

import scala.collection.Map
import scala.util.control.NonFatal

/**
  * Get offset from Apache Storm reader
  *
  * Created by noviiden on 27/05/16.
  */
class OffsetFromStorm(group: String, rootPath: Option[String]) extends ProcessPartitionOffset with LazyLogging {
  override def offset(zkClient: ZkClient, topic: String, partitionId: Int): Option[OffsetDetail] = {
    try {

      val patchWithInfo = s"${rootPath.getOrElse("")}/$group/partition_$partitionId"

      if (!ZkUtils.pathExists(zkClient, patchWithInfo)) {
        return None
      }

      val (stateJson, stat: Stat) = ZkUtils.readData(zkClient, patchWithInfo)

      val offset: String = Json.parseFull(stateJson) match {
        case Some(m) =>
          val spoutState = m.asInstanceOf[Map[String, Any]]
          spoutState.getOrElse("offset", "-1").toString
        case None =>
          "-1"
      }

      Some(OffsetDetail(topic, group, partitionId, offset.toLong))

    } catch {
      case e: ZkNoNodeException if NonFatal(e) =>
        logger.error(s"Could not parse partition info. group: [$group] topic: [$topic]", e)
        throw KafkaOffsetUtilsException("Could not parse partition info.")
    }
  }
}
