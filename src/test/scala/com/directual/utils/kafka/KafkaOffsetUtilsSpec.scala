package com.directual.utils.kafka

import java.util.concurrent.Executors
import java.util.{ Properties, UUID }

import _root_.info.batey.kafka.unit.KafkaUnit
import kafka.consumer.{ KafkaStream, ConsumerConfig, Consumer, ConsumerConnector }
import kafka.producer.KeyedMessage
import kafka.utils.{ ZKStringSerializer, ZkUtils }
import org.I0Itec.zkclient.ZkClient
import org.apache.zookeeper.data.Stat
import org.scalatest.{ Matchers, BeforeAndAfter, FlatSpec }

import scala.concurrent.Future

/**
  * Created by noviiden on 27/05/16.
  */
class KafkaOffsetUtilsSpec extends FlatSpec with BeforeAndAfter with Matchers {

  var kafkaServer: KafkaUnit = _
  var topicName = "test_topic_" + UUID.randomUUID().toString
  var zkClient: ZkClient = null

  before {
    kafkaServer = new KafkaUnit(2100, 9299)
    kafkaServer.startup()
    kafkaServer.createTopic(topicName, 3)
    kafkaServer.sendMessages(new KeyedMessage[String, String](topicName, "I'm ready"))
    kafkaServer.readMessages(topicName, 1)
    zkClient = KafkaUtils.createClient("localhost:2100")
  }

  "KafkaOffsetUtils" should "return correct def impl (kafka offset without lag)" in {
    implicit val zk = zkClient

    KafkaUtils.offset(Seq(topicName)) should have size 3

    KafkaUtils.offset(Seq(topicName)).map(_.offset).max should be(1)

    kafkaServer.sendMessages(new KeyedMessage[String, String](topicName, "msg"))

    KafkaUtils.offset(Seq(topicName)).map(_.offset).sum should be(2)
    KafkaUtils.offset(Seq(topicName)).map(_.offset).sum should be(2)
  }

  "KafkaOffsetUtils" should "return correct Storm impl" in {
    implicit val zk = zkClient

    //if storm reader have not been started read msg
    val offset = KafkaUtils.offset(Seq(topicName), new FromStorm("testGroup", None))
    offset should have size 3

    assert(offset.map(_.positionEngine).sum == 0)

    //mock result node
    createStormTrack(zkClient)

    val offsetaAfterCommitStorm = KafkaUtils.offset(Seq(topicName), new FromStorm("testGroup", None))
    assert(offsetaAfterCommitStorm.exists(_.positionEngine == 23))
  }

  "KafkaOffsetUtils" should "return correct ConsumerGroup impl" in {
    implicit val zk = zkClient

    //if storm reader have not been started read msg
    val offset = KafkaUtils.offset(Seq(topicName), new FromConsumerGroup("testGroup"))
    offset should have size 3

    assert(offset.map(_.positionEngine).sum == 0)

    //create ConsumerReader
    import scala.concurrent.ExecutionContext.Implicits.global
    val consumer = createConsumer("localhost:2100", "testGroup")
    val consumerStreams = consumer.createMessageStreams(Map(topicName -> 1))
    val streams: Option[List[KafkaStream[Array[Byte], Array[Byte]]]] = consumerStreams.get(topicName)
    val executor = Executors.newFixedThreadPool(1)
    streams foreach { streams =>
      streams.foreach(stream => {
        Future {
          stream foreach { messageAndData =>
            val msg = messageAndData.message()
          }
        }
      })
    }
    kafkaServer.sendMessages(new KeyedMessage[String, String](topicName, "send test"))
    Thread.sleep(500) // wait commit offset

    //refresh offsetEngine's
    val offsetAfterCreateGroup = KafkaUtils.offset(Seq(topicName), new FromConsumerGroup("testGroup"))
    assert(offsetAfterCreateGroup.map(_.positionEngine).sum > 0)
  }

  def createConsumer(zkQuorum: String, groupId: String): ConsumerConnector = {
    val consumerProps = new Properties

    consumerProps.put("zookeeper.connect", zkQuorum)
    consumerProps.put("group.id", groupId)
    consumerProps.put("consumer.id", groupId)
    consumerProps.put("zookeeper.session.timeout.ms", "500")
    consumerProps.put("zookeeper.sync.time.ms", "250")
    consumerProps.put("auto.commit.interval.ms", "100")
    consumerProps.put("auto.commit.enable", "true")
    consumerProps.put("partition.assignment.strategy", "range")

    Consumer.create(new ConsumerConfig(consumerProps))
  }

  def createStormTrack(client: ZkClient) = {
    val data =
      s"""{"topology":
         |{"id":"57b5a8a8-99ed-46c2-9231-fc8cf98e6d2e","name":"directualEngine"},
         |"offset":23,"partition":0,"broker":{"host":"localhost","port":2100},
         |"topic":"directual_q_new"}
       """.stripMargin.trim

    val patchWithInfo = s"/testGroup/partition_0"

    ZkUtils.createPersistentPath(client, patchWithInfo, data)
  }

  after {
    kafkaServer.shutdown()
  }
}
