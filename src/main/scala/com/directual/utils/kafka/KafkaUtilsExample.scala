package com.directual.utils.kafka

import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient

/**
  * Created by noviiden on 27/05/16.
  */
object KafkaUtilsExample extends App {

  implicit val zkClient = new ZkClient("host", 10000, 10000, ZKStringSerializer)
  //or implicit val zkClient = KafkaOffsetUtils.createClient("test.directual.com")

  val offsetsNative = KafkaUtils.offset(Seq("topic"))
  //or KafkaUtils.offset(Seq("topic"))(zkClient)
  val offsetsStorm = KafkaUtils.offset(Seq("topic"), new OffsetFromStorm("group", Some("/rootPath")))
  val offsetsConsumerGroup = KafkaUtils.offset(Seq("topic"), new OffsetFromConsumerGroup("group"))

}
