package test01.producer;

import java.util.Properties;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * kafka消息生产者测试
 * 
 * 生产者包括一个缓冲区池，它保存尚未发送到服务器的记录，以及一个后台I/O线程，负责将这些记录转换为请求并将其
 * 传输到集群。使用后未能关闭生产者将泄漏这些资源。该send()方法是异步的。当被调用时，它将记录添加到待处理记
 * 录发送的缓冲区并立即返回。这允许生产者将各个记录收集在一起以获得效率。acks配置其请求被视为完整性的标准。
 * "all"意味着领导者将等待完整的同步副本来确认记录。只要至少有一个同步复制品仍然存在，这将保证记录不会丢失。
 * 这是最强大的保证。这相当于设置acks=-1。如果请求失败，生产者可以自动重试，通过设置retries来指定。
 * buffer.memory控制生产者可用于缓冲的总内存量。如果记录的发送速度比可以传输到服务器的速度快，那么这个缓冲
 * 空间就会耗尽。当缓冲区空间耗尽时，附加的发送呼叫将被阻塞。max.block.ms决定阻塞时间的阈值，超出此时间时，
 * 会引发TimeoutException。key.serializer和value.serializer指导如何将用户提供的ProducerRecord的键
 * 和值转换成字节。您可以使用提供的ByteArraySerializer或 StringSerializer用于简单的字符串或字节类型。
 * 该客户端可以与0.10.0版本或更高版本的broker进行通信。旧的或较新的broker可能不支持某些功能。当调用运行的
 * broker程序版本不可用的API时，会产生UnsupportedVersionException异常。
 *
 */
public class SimpleProducer {

	/**
	 * 获取配置参数
	 * 
	 */
	private static Properties getConfiguration() {
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
	
	
	 /*
	 * main
	 * 向服务器发送消息。如果topic不存在，则默认创建分区数和副本数都为1的topicxxx，然后向该topic发送消息
	 * 
	 */
	public static void main(String[] args) {
		Producer<String, String> producer = new KafkaProducer<String,String>(getConfiguration());
		String topicName = "test0807";
		
		// 生产消息
		for(int i = 0; i < 2; i++) {
			producer.send(new ProducerRecord<String, String>(topicName, Integer.toString(i), "这是发送的消息-"+i), new Callback() {
				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					System.out.println("[call ack] metadata is: "+ metadata + ", and exception is: " + exception);
				}
			});
		}
		
		producer.close();
	}
}
