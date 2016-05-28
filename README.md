# Kafka libriary for get position client


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
  val offsetsStorm = KafkaUtils.offset(Seq("topic"), new OffsetFromStorm("group", Some("/rootPath")))
  val offsetsConsumerGroup = KafkaUtils.offset(Seq("topic"), new OffsetFromConsumerGroup("group"))

```
Example usage :
* [Get storm offset or kafka consumer](https://github.com/nikitok/kafka-offset-position/src/main/scala/KafkaUtilsExample.scala)

