# kafka消费者offset记录位置和方式:

kafka消费者在会保存其消费的进度，也就是offset，`存储的位置根据选用的kafka api不同而不同`。

## 根据旧版本的 kafka.javaapi.consumer.ConsumerConnector 消费offset默认的存储情况

首先来说说消费者如果是根据javaapi来消费，也就是 kafka.javaapi.consumer.ConsumerConnector，通过配置参数zookeeper.connect
来消费。这种情况下，消费者的offset会更新到 zookeeper的 consumers/{group}/offsets/{topic}/{partition} 目录下，例如：

``` bash
# 这里关键是 --zookeeper xxx 的配置参数
$ kafka-console-consumer.sh --zookeeper host01:2181/kafka --topic topic02 --group group01

$ zkCli.sh
[zk: localhost:2181(CONNECTED) 28] get /kafka/consumers/group01/offsets/topic02/0
1
cZxid = 0x3000001c6
ctime = Sat Sep 29 18:44:00 EDT 2018
mZxid = 0x3000001c6
mtime = Sat Sep 29 18:44:00 EDT 2018
pZxid = 0x3000001c6
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 1
numChildren = 0
```

## 根据kafka默认的api，即 org.apache.kafka.clients.consumer.KafkaConsumer 消费offset默认的存储情况

如果是根据kafka默认的api来消费，即 org.apache.kafka.clients.consumer.KafkaConsumer，我们会配置参数 
bootstrap.servers 来消费。而其消费者的offset会更新到一个kafka自带的名为__consumer_offsets的topic下面，例如

``` bash
# 这里关键是 --bootstrap-server xxx 的配置参数
kafka-console-consumer.sh --bootstrap-server host01:9092,host02:9092,host03:9092 --topic topic01 --group group01
```

### 1.偏移量offset的存储：

由于Zookeeper并不适合大批量的频繁写入操作，新版Kafka已推荐将consumer的位移信息保存在Kafka内部的topic中，
即__consumer_offsets topic，并且默认提供了kafka_consumer_groups.sh脚本供用户查看consumer信息。

### 2.某个消费者组的偏移offset在__consumer_offsets的物理存储：

``` bash
# Kafka会使用下面公式计算该group位移保存在__consumer_offsets的哪个分区上
# __consumer_offsets的numPartitions默认为50（编号为0-49）
Math.abs(groupID.hashCode()) % numPartitions  
```

### 3.查看某个消费者组的消费偏移情况：

``` bash
$ bin/kafka-consumer-groups.sh --bootstrap-server mq:9092,mq:9093,mq:9094 --list
group01
console-consumer-15755
test-consumer-group

$ bin/kafka-consumer-groups.sh --bootstrap-server mq:9092,mq:9093,mq:9094 --describe --group group01
TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
test01          2          3               3               0               -               -               -
test01          1          5               5               0               -               -               -
test01          0          3               3               0               -               -               -
```

### 4.主题__consumer_offsets的描述：

``` bash
$ kafka-topics.sh --zookeeper xxx:2181/kafka --describe --topic __consumer_offsets
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
```	
