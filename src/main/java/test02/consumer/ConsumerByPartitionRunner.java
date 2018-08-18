package test02.consumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;


/**
 * 多线程手动消费消息：
 * 一个主题的相同分区的消息使用同一个线程处理
 * 
 * [注1]
 * KafkaConsumer是非线程安全的，一个线程始终都只能操作同
 * 一个客户端对象且一个客户端对象始终是由同一个线程来操作的。
 * 
 * [注2]
 * KafkaConsumer.assign(partitions)
 * 按分区消费消息，通过此方法的手动主题分配不能再使用消费者的组管理功能！
 * 因此，当组成员或集群和主题元数据更改时不会触发重新平衡操作。
 * 
 */
public class ConsumerByPartitionRunner implements Runnable {
	private volatile AtomicBoolean isClosed = new AtomicBoolean(false);
	private TopicPartition topicPatition;
	public ConsumerByPartitionRunner(TopicPartition topicPartition) {
		this.topicPatition = topicPartition;
	}
	
	
	/**
	 * 获取配置参数
	 * 
	 */
	private static Properties getConfiguration() {
		Properties props = new Properties();
		
		// bootstrap.servers 需要本地设置hosts路由映射
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop01:9092,hadoop07:9092,hadoop08:9092");
		// group.id
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "group01");
		// enable.auto.commit & auto.commit.interval.ms 设置自动offset，offset的值在0.10.1.1版本以后默认存放在__consumer_offsets主题中
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
		// request.timeout.ms
		props.put("request.timeout.ms", "305000"); 
		// session.timeout.ms
		props.put("session.timeout.ms", "10000");
		// max.poll.interval.ms
		props.put("max.poll.interval.ms", "300000"); 
		// max.poll.records 一次从kafka中poll出来的数据条数，这些数据需要在在session.timeout.ms这个时间内处理完
		props.put("max.poll.records", 500);
		// auto.offset.reset，枚举值为earliest|latest|none
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		// key.deserializer
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		// value.deserializer
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		
		return props;
	}
	
	/**
	 * 获取 KafkaConsumer
	 * 
	 */
	public static <K,V> KafkaConsumer<K,V> getKafkaConsumerOfSubscribed(List<String> topics) {
		KafkaConsumer<K,V> consumer = new KafkaConsumer<>(getConfiguration());
		consumer.subscribe(topics);
		return consumer;
	}
	public static <K,V> KafkaConsumer<K,V> getKafkaConsumerOfAssigned(Collection<TopicPartition> partitions) {
		KafkaConsumer<K,V> consumer = new KafkaConsumer<>(getConfiguration());
		consumer.assign(partitions);
		return consumer;
	}
	
	/**
	 * 关闭 KafkaConsumer
	 * 
	 */
	public static <K,V> void close(KafkaConsumer<K,V> consumer) {
		consumer.close();
	}
	
	
	/**
	 * run
	 * 
	 */
	public void run() {
		//
		KafkaConsumer<String, String> consumer = null;
		try {
			consumer = getKafkaConsumerOfAssigned(Arrays.asList(topicPatition)); //
			
			while (!isClosed.get()) {
				ConsumerRecords<String, String> records = consumer.poll(200); //这些消息属于手动分配的同一个分区
				for (ConsumerRecord<String, String> record : records) {
					long offset = record.offset(); 
					try { 	
						//处理消息
						System.out.println(
								"Thread-" + Thread.currentThread().getId()
								+ ", topic=" + record.topic()
								+ ", partition=" + record.partition()
								+ ", offset="+ offset 
								+ ", k=" + record.key() 
								+ ", v=" + record.value());
						consumer.commitAsync();
						
						if (new Random().nextBoolean()) { //
							System.err.println("出现错误，需要重置offset！");
							throw new RuntimeException();
						}
					} catch (Exception e) {
						consumer.seek(topicPatition, offset); //重置offset
						break;
					}
				}
			}
		} catch (WakeupException e) {
			if (!isClosed.get()) {
				throw e;
			}
		} finally {
			consumer.wakeup();
			consumer.close();
		}
	}

	
	/**
	 * 获取topic的分区列表信息
	 * 
	 */
	public static List<PartitionInfo> getPartitionInfoList(String topic) {
		KafkaConsumer<String, String> consumer = null;
		List<PartitionInfo> partitionInfoList;
		try {
			consumer = new KafkaConsumer<>(getConfiguration());
			partitionInfoList = consumer.partitionsFor(topic);
		} finally {
			consumer.close();
		}
		return partitionInfoList;
	}
	
	
	/*
	  $ kafka-topics.sh --describe --zookeeper nimbusz:2181/kafka --topic test
		Topic:test      PartitionCount:3        ReplicationFactor:2     Configs:
        Topic: test     Partition: 0    Leader: 0       Replicas: 0,1   Isr: 0,1
        Topic: test     Partition: 1    Leader: 2       Replicas: 1,2   Isr: 2,1
        Topic: test     Partition: 2    Leader: 2       Replicas: 2,0   Isr: 2,0
	 */
	public static void main(String[] args) {
		List<PartitionInfo> partitionInfoList = ConsumerByPartitionRunner.getPartitionInfoList("test0807");
		partitionInfoList.forEach(v -> System.err.println(v));
		
		for (PartitionInfo partitionInfo : partitionInfoList) {
			TopicPartition topicPartition = new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
			new Thread(new ConsumerByPartitionRunner(topicPartition)).start();
			System.out.println("[*] start partition " + partitionInfo.partition() + " handler!");
		}
	}
	
}
	
	
	

