package test02.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

/**
 * 消息消费者，手动offset
 * @author zhangqingli
 *
 */
public class SimpleConsumer2 {

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
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
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
		KafkaConsumer<String, String> consumer = getKafkaConsumerOfSubscribed(Arrays.asList("test0807"));
		
		final int batchSize = 5;
		List<ConsumerRecord<String, String>> bufferList = new ArrayList<>();
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(200); //
			for (ConsumerRecord<String, String> record : records) {
				bufferList.add(record);
			}
			if (bufferList.size() >= batchSize) {
				//批量处理消息线程
				try {
					System.err.println(bufferList);
				} finally {
					consumer.commitSync();
				}

				bufferList.clear();
			}
		}
	}
}
