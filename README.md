# Kafka libriary for get position client

[![Build Status](https://travis-ci.org/nikitok/kafka-offset-position.svg?branch=master)](https://travis-ci.org/nikitok/kafka-offset-position)

## How to start

First you should add dependency to your pom file

```xml
<dependency>
  <groupId>com.directual.utils</groupId>
  <artifactId>kafka</artifactId>
  <version>0.7</version>
</dependency>
```

```scala

  implicit val zkClient = new ZkClient("host", 10000, 10000, ZKStringSerializer)
  //or implicit val zkClient = KafkaOffsetUtils.createClient("test.directual.com")

  val offsetsNative = KafkaUtils.offset(Seq("topic")) //or KafkaUtils.offset(Seq("topic"))(zkClient)
  //res0: Seq[com.directual.utils.kafka.OffsetDetail] = List(OffsetDetail(topic,,0,23,0), OffsetDetail(topic,,0,32,0))
  val offsetsStorm = KafkaUtils.offset(Seq("topic"), new OffsetFromStorm("group", Some("/rootPath")))
  //res1: Seq[com.directual.utils.kafka.OffsetDetail] = List(OffsetDetail(topic,,0,23,11), OffsetDetail(topic,,0,32,8))
  val offsetsConsumerGroup = KafkaUtils.offset(Seq("topic"), new OffsetFromConsumerGroup("group"))
  //res2: Seq[com.directual.utils.kafka.OffsetDetail] = List(OffsetDetail(topic,,0,23,4), OffsetDetail(topic,,0,32,5))
  
  //get summary lag
  val s = KafkaUtils.offset(Seq("topic"), new OffsetFromConsumerGroup("group")).map(_.lag).sum
  //res3: Long = 36

```
Example usage :
* [Get storm offset or kafka consumer](https://github.com/nikitok/kafka-offset-position/blob/master/src/main/scala/com/directual/utils/kafka/KafkaUtilsExample.scala)

