
1.偏移量offset的存储：
众所周知，由于Zookeeper并不适合大批量的频繁写入操作，新版Kafka已推荐将consumer的位移信息保存在Kafka内部的topic中，
即__consumer_offsets topic，并且默认提供了kafka_consumer_groups.sh脚本供用户查看consumer信息。不过依然有很多
用户希望了解__consumer_offsets topic内部到底保存了什么信息，特别是想查询某些consumer group的位移是如何在该topic
中保存的。针对这些问题，本文将结合一个实例探讨如何使用kafka-simple-consumer-shell脚本来查询该内部topic。



2.查看某个消费者组的消费偏移情况：
--------------------------------------------------------------------------------
$ bin/kafka-consumer-groups.sh --bootstrap-server mq:9092,mq:9093,mq:9094 --list
group01
console-consumer-15755
test-consumer-group

$ bin/kafka-consumer-groups.sh --bootstrap-server mq:9092,mq:9093,mq:9094 --describe --group group01
TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
test01          2          3               3               0               -               -               -
test01          1          5               5               0               -               -               -
test01          0          3               3               0               -               -               -
--------------------------------------------------------------------------------




3.某个消费者组的偏移offset在__consumer_offsets的物理存储：
Kafka会使用下面公式计算该group位移保存在__consumer_offsets的哪个分区上：
	Math.abs(groupID.hashCode()) % numPartitions  //__consumer_offsets的numPartitions默认为50（编号为0-49）
	



4.主题__consumer_offsets的描述：
---------------------------------------------------------------------------------
$ kafka-topics.sh --zookeeper mq:2181/kafka --describe --topic __consumer_offsets
Topic:__consumer_offsets        PartitionCount:50       ReplicationFactor:1     Configs:segment.bytes=104857600,cleanup.policy=compact,compression.type=producer
        Topic: __consumer_offsets       Partition: 0    Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 1    Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 2    Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 3    Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 4    Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 5    Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 6    Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 7    Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 8    Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 9    Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 10   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 11   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 12   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 13   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 14   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 15   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 16   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 17   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 18   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 19   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 20   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 21   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 22   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 23   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 24   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 25   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 26   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 27   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 28   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 29   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 30   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 31   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 32   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 33   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 34   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 35   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 36   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 37   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 38   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 39   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 40   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 41   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 42   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 43   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 44   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 45   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 46   Leader: 0       Replicas: 0     Isr: 0
        Topic: __consumer_offsets       Partition: 47   Leader: 1       Replicas: 1     Isr: 1
        Topic: __consumer_offsets       Partition: 48   Leader: 2       Replicas: 2     Isr: 2
        Topic: __consumer_offsets       Partition: 49   Leader: 0       Replicas: 0     Isr: 0
	
	
	
	



