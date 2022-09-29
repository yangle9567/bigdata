import com.alibaba.otter.canal.protocol.FlatMessage;
import smzdm.model.Flow;
import smzdm.schema.FlatMessageSchema;
import smzdm.sink.MyHbaseSink;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
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
public class SyncApp_YL {

    public static final MapStateDescriptor<String, Flow> flowStateDescriptor =
            new MapStateDescriptor<String, Flow>("flowBroadCastState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(new TypeHint<Flow>() {
            }));
    public static void main(String[] args) throws Exception {
        int secondsdelay = 0;
        String topic = "";
        String groupid ="";
        String tablename = "";

        ParameterTool parameter = ParameterTool.fromArgs(args);
        secondsdelay = Integer.valueOf(parameter.get("secondsdelay"));
        topic = parameter.get("topic");
        groupid = parameter.get("groupid");
        tablename = parameter.get("tablename");


        //获取执行环境
        StreamExecutionEnvironment sEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop001:6667,hadoop002:6667,hadoop003:6667");
        props.put("zookeeper.connect", "hadoop001:2181,hadoop002:2181,hadoop003:2181");
        props.put("group.id", groupid);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("flink.partition-discovery.interval-millis","30000");
        //System.out.println("sEnv 并行度： "+sEnv.getParallelism());

        //消费kafka数据
        FlinkKafkaConsumer<FlatMessage> myConsumer = new FlinkKafkaConsumer<>(topic, new FlatMessageSchema(), props);
        myConsumer.setStartFromGroupOffsets();

        DataStream<FlatMessage> message = sEnv.addSource(myConsumer);
        //System.out.println("message 并行度： "+message.getParallelism());
        message.timeWindowAll(Time.seconds(secondsdelay));

        DataStream<FlatMessage> message_youhui = message.filter(new FilterFunction<FlatMessage>() {
            @Override
            public boolean filter(FlatMessage flatMessage) throws Exception {
                if(null != flatMessage && flatMessage.getTable().equalsIgnoreCase("youhui")){
                    return true;
                }else{
                    return false;
                }
            }
        });

        /*//同库，同表数据进入同一个分组，一个分区
        KeyedStream<FlatMessage, String> keyedMessage = message.keyBy(new KeySelector<FlatMessage, String>() {
            @Override
            public String getKey(FlatMessage value) throws Exception {
                return value.getDatabase() + value.getTable();
            }
        });*/
        //System.out.println("message_youhui 并行度： "+message_youhui.getParallelism());
        //keyedMessage.print();

        DataStreamSink<FlatMessage> dataStreamSink = message_youhui.addSink(new MyHbaseSink()).setParallelism(10);

        sEnv.execute("SyncApp_yl");
    }
}
