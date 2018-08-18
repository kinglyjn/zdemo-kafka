package test02.consumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

/**
 * 消息消费者，手动并逐条offset
 * 1. 同一个组的消费者轮流消费broker的消息，当一个消费者挂掉以后，broker重新rebalance消费者，
 *    并且挂掉的消费者未处理的消息由新的rebalance到的消费者消费掉
 * 2. 消费者在处理某个分区的异常出现错误之后，可以手动重置offset，达到重新消费这条消息的目的
 * 
 */
public class ConsumerByManualOpt {

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
	
	
	 /*
	 * main
	 * 
	 */
	public static void main(String[] args) {
		//
		KafkaConsumer<String, String> consumer = null;
		try {
			consumer = getKafkaConsumerOfSubscribed(Arrays.asList("test0807"));
			
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(200); //
				for (TopicPartition partition : records.partitions()) {
					
					// 处理当前分区消息
					int i = 0;
					List<ConsumerRecord<String, String>> partitionRecords = null;
					try {
						partitionRecords = records.records(partition);
						for (; i<partitionRecords.size(); i++) {
							ConsumerRecord<String, String> record = partitionRecords.get(i);
							System.out.println(
									"Thread-" + Thread.currentThread().getId() 
									+ ", topic=" + record.topic()
									+ ", partition=" + record.partition() 
									+ ", offset=" + record.offset() 
									+ ", k=" + record.key() 
									+ ", v=" + record.value());
							
							if (new Random().nextBoolean()) { //
								System.err.println("出现错误，需要重置offset！");
								throw new RuntimeException();
							}
						}
						long lastOffset = partitionRecords.get(partitionRecords.size()-1).offset();
						consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset)));
					} catch (Exception e) {
						long needToOffset = partitionRecords.get(i).offset();
						consumer.seek(partition, needToOffset);
					}
					
				}
			}
		} finally {
			consumer.close();
		}
	}
}
