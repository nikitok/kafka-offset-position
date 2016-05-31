package com.directual.utils.kafka

import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient

/**
  * Created by noviiden on 27/05/16.
  */
object KafkaUtilsExample extends App {

  implicit val zkClient = KafkaUtils.createClient("host")
  //implicit val zkClient = new ZkClient("host", 10000, 10000, ZKStringSerializer)

  val offsetsNative = KafkaUtils.offset(Seq("topic"))
  println(offsetsNative)
  val offsetsStorm = KafkaUtils.offset(Seq("topic"), new FromStorm("group", Some("/rootPath")))
  println(offsetsStorm)
  val offsetsConsumerGroup = KafkaUtils.offset(Seq("topic"), new FromConsumerGroup("group"))
  println(offsetsConsumerGroup)

  val summary = KafkaUtils.offsetSummary("topic", new FromStorm("group", Some("/rootPath")))
  println(summary.maxOffset)
  println(summary.sumLag)
  println(summary.maxLag)

  /**
    *
    * //structure OffsetDetail
    * case class OffsetDetail(topic: String, group: String, partition: Int, offset: Long, positionEngine: Long = 0) {
    * def lag = offset - positionEngine
    * }
    *
    * List(OffsetDetail(topic,,0,58420,0), OffsetDetail(topic,,1,58219,0), OffsetDetail(topic,,2,58205,0), OffsetDetail(topic,,3,57918,0), OffsetDetail(topic,,4,58217,0), OffsetDetail(topic,,5,57885,0), OffsetDetail(topic,,6,58079,0), OffsetDetail(topic,,7,58423,0), OffsetDetail(topic,,8,58203,0), OffsetDetail(topic,,9,58095,0))
    *
    * List(OffsetDetail(topic,,0,58420,58420), OffsetDetail(topic,,1,58219,58219), OffsetDetail(topic,,2,58205,58205), OffsetDetail(topic,,3,57918,57918), OffsetDetail(topic,,4,58217,58217), OffsetDetail(topic,,5,57885,57885), OffsetDetail(topic,,6,58079,58079), OffsetDetail(topic,,7,58423,58423), OffsetDetail(topic,,8,58203,58203), OffsetDetail(topic,,9,58095,58095))
    *
    * List(OffsetDetail(topic,,0,58420,0), OffsetDetail(topic,,1,58219,0), OffsetDetail(topic,,2,58205,0), OffsetDetail(topic,,3,57918,0), OffsetDetail(topic,,4,58217,0), OffsetDetail(topic,,5,57885,0), OffsetDetail(topic,,6,58079,0), OffsetDetail(topic,,7,58423,0), OffsetDetail(topic,,8,58203,0), OffsetDetail(topic,,9,58095,0))
    *
    * 58423
    * 0
    * 0
    */

}
