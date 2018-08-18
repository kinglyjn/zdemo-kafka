package test01.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.alibaba.fastjson.JSON;

import utils.KafkaMessage;

/**
 * 生产者客户端
 *
 */
public class SimpleProducerClient {
	private static Producer<String, String> producer = new KafkaProducer<String,String>(getConfiguration());
	
	/**
	 * 获取配置参数
	 * 
	 */
	public static Properties getConfiguration() {
		Properties props = new Properties();
		
		// bootstrap.servers
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop01:9092,hadoop07:9092,hadoop08:9092");
		// 0不需要回执确认; 1仅需要leader回执确认; all[或者-1]需要leader和所有replica回执确认
		props.put(ProducerConfig.ACKS_CONFIG, "1");
		// 重试一次
		props.put(ProducerConfig.RETRIES_CONFIG, 1); 
		// 空间上buffer满足>16k即发送消息
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		// 时间上满足>5ms即发送消息，时间限制和空间限制只要满足其一即可发送消息
		props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
		// buffer.memory 1G
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1073741824);
		// key.serializer
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		// value.serializer
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		// partitioner.class
		//props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "test01.producer.MyPartitioner"); //指定分区
		
		return props;
	}
	
	/**
	 * 发送自定义包装的消息
	 * 
	 */
	public static <T> void produceMsg(KafkaMessage<T> km, Callback callback) {
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(km.getTopic(), km.getKey(), JSON.toJSONString(km));
		if (callback==null) {
			producer.send(record);
		} else {
			producer.send(record, callback);
		}
		producer.flush();
	}
	
	public static void main(String[] args) {
		String topicName = "test0807";
		String subType = "test";
		SimpleProducerClient.produceMsg(new KafkaMessage<>(topicName, subType, "1", "message-1"), new Callback() {
			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				System.out.println("[*] call ack");
			}
		});
	}
}
