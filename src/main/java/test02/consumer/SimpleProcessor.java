package test02.consumer;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import java.util.Properties;

/**
 * @Author Kingly
 * @Date 2018/10/22
 * @Description
 */
public class SimpleProcessor implements Processor<byte[], byte[]> {
    private ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public void process(byte[] key, byte[] value) {
        String in = new String(value);

        if (in!=null && in.contains(">>>")) { // 如果包含 >>> 则去除
            in = in.split(">>>")[1];
        }
        context.forward(key, in.getBytes());
    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }


    /**
     * main
     *
     * producer ----> topic01    topic02 ----> consumer
     *                  |           |\
     *                  |           |
     *                  +-----------+
     *                   processor(相当于一个消费者，消费源主题的消息，经其处理后，将消息放到目标主题中供其他消费者消费)
     *
     */
    public static void main(String[] args) {
        String fromTopic = "topic01";
        String toTopic = "topic02";

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "simpleProcessor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "host01:9092,host02:9092,host03:9092");

        // 模仿storm构建kafka拓扑
        StreamsConfig config = new StreamsConfig(props);
        Topology topology = new Topology();
        topology.addSource("mysource", fromTopic)
                .addProcessor("myprocessor", new ProcessorSupplier<byte[],byte[]>() {
                    @Override
                    public Processor<byte[],byte[]> get() {
                        return new SimpleProcessor();
                    }
                }, "mysource")
                .addSink("mysink", toTopic, "myprocessor");
        KafkaStreams streams = new KafkaStreams(topology, config);
        streams.start();
    }

}
