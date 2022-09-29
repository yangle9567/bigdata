import com.alibaba.otter.canal.protocol.FlatMessage;
import smzdm.model.Flow;
import smzdm.schema.FlatMessageSchema;
import smzdm.sink.MyHbaseSink;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

/**
 * 实时增量同步模块
 *
 * @author legend
 * @create 2020-05-29-11:04
 */
public class SyncApp_YL2 {

    public static final MapStateDescriptor<String, Flow> flowStateDescriptor =
            new MapStateDescriptor<String, Flow>("flowBroadCastState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(new TypeHint<Flow>() {
            }));
    public static void main(String[] args) throws Exception {
        String topic = args[0];
        int secondsdelay = Integer.valueOf(args[1]);
        String startfrom = args[2];
        //获取执行环境
        StreamExecutionEnvironment sEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop001:6667,hadoop002:6667,hadoop003:6667");
        props.put("zookeeper.connect", "hadoop001:2181,hadoop002:2181,hadoop003:2181");
        props.put("group.id", "group1");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("flink.partition-discovery.interval-millis","30000");
        //System.out.println("sEnv 并行度： "+sEnv.getParallelism());

        //消费kafka数据
        FlinkKafkaConsumer<FlatMessage> myConsumer = new FlinkKafkaConsumer<>(topic, new FlatMessageSchema(), props);
        if(startfrom.equals(0)){
            myConsumer.setStartFromEarliest();
        }else{
            myConsumer.setStartFromLatest();
        }

        DataStream<FlatMessage> message = sEnv.addSource(myConsumer);
        //System.out.println("message 并行度： "+message.getParallelism());
        message.timeWindowAll(Time.seconds(secondsdelay));

        //同库，同表数据进入同一个分组，一个分区
        KeyedStream<FlatMessage, String> keyedMessage = message.keyBy(new KeySelector<FlatMessage, String>() {
            @Override
            public String getKey(FlatMessage value) throws Exception {
                return value.getDatabase() + value.getTable();
            }
        });
        //System.out.println("keyedMessage 并行度： "+keyedMessage.getParallelism());
        //keyedMessage.print();

        DataStreamSink<FlatMessage> dataStreamSink = message.addSink(new MyHbaseSink()).setParallelism(10);

        sEnv.execute("SyncApp_yl");
    }
}
