import com.alibaba.otter.canal.protocol.FlatMessage;
import smzdm.model.Flow;
import smzdm.schema.FlatMessageSchema;
import smzdm.sink.MyHbaseSink;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;
import net.jpountz.lz4.LZ4Exception;

/**
 * 实时增量同步模块
 *
 * @author legend
 * @create 2020-05-29-11:04
 */
public class TongBu2 {

    public static final MapStateDescriptor<String, Flow> flowStateDescriptor =
            new MapStateDescriptor<String, Flow>("flowBroadCastState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(new TypeHint<Flow>() {
            }));
    public static void main(String[] args) throws Exception {
        /*String topic = "topic_135";
        int secondsdelay = Integer.valueOf(args[0]);
        String groupid = args[1];*/

        LZ4Exception lz4Exception = new LZ4Exception();
        //System.out.println("lz4:"+lz4Exception.toString());


        int secondsdelay = 10;
        String topic = "dbzdm_youhui.youhui_new";
        String groupid ="group1";
        String tablename = "dbzdm_youhui.youhui";

        /*ParameterTool parameter = ParameterTool.fromArgs(args);
        secondsdelay = Integer.valueOf(parameter.get("secondsdelay"));
        topic = parameter.get("topic");
        groupid = parameter.get("groupid");
        tablename = parameter.get("tablename");*/

        //System.out.println("topic="+topic+" groupid"+groupid+" tablename="+tablename);
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
        //props.put("compression.type","lz4");
        //System.out.println("sEnv 并行度： "+sEnv.getParallelism());

        //消费kafka数据
        FlinkKafkaConsumer<FlatMessage> myConsumer = new FlinkKafkaConsumer<>(topic, new FlatMessageSchema(), props);
        myConsumer.setStartFromLatest();

        DataStream<FlatMessage> message = sEnv.addSource(myConsumer);
        //System.out.println("message 并行度： "+message.getParallelism());
        message.timeWindowAll(Time.seconds(secondsdelay));

        String finalTablename = tablename;
        //System.out.println("finalTablename="+finalTablename);
        DataStream<FlatMessage> message_youhui = message.filter(new FilterFunction<FlatMessage>() {
            @Override
            public boolean filter(FlatMessage flatMessage) throws Exception {
                //System.out.println("infilter finalTablename="+finalTablename);
                if(null != flatMessage && flatMessage.getTable().equalsIgnoreCase(finalTablename)){
                    return true;
                }else{
                    return false;
                }
            }
        });

        //System.out.println("message_youhui 并行度： "+message_youhui.getParallelism());
        //keyedMessage.print();

        DataStreamSink<FlatMessage> dataStreamSink = message_youhui.addSink(new MyHbaseSink());

        sEnv.execute("TongBu2");
    }
}
