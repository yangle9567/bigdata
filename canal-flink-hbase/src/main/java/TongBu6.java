import avro.shaded.com.google.common.collect.Maps;
import avro.shaded.com.google.common.collect.Sets;
import check.MyValueStateFunction;
import check.ParseXmlDom4jUtil;
import check.XmlArgsBean;
import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.FlatMessage;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import smzdm.config.GlobalConfig;
import smzdm.model.Flow;
import smzdm.schema.FlatMessageSchema;
import smzdm.sink.MyHbaseSink;

import java.util.*;

/**
 * 实时增量同步模块
 *
 * @author legend
 * @create 2020-05-29-11:04
 */
public class TongBu6 {

    public static final MapStateDescriptor<String, Flow> flowStateDescriptor =
            new MapStateDescriptor<String, Flow>("flowBroadCastState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(new TypeHint<Flow>() {
            }));
    public static void main(String[] args) throws Exception {
        /*String topic = "topic_135";
        int secondsdelay = Integer.valueOf(args[0]);
        String groupid = args[1];*/
        int secondsdelay = 0;
        String topic = "";
        String groupid ="";
        String tablename = "";
        String bootservers = "";//"hadoop001:6667,hadoop002:6667,hadoop003:6667"
        String zkservers = "";//"hadoop001:2181,hadoop002:2181,hadoop003:2181"
        String xmlargspath = "";

        //ParameterTool parameter = ParameterTool.fromPropertiesFile("TongBu.properties");
        ParameterTool parameter = ParameterTool.fromArgs(args);
        secondsdelay = Integer.valueOf(parameter.get("secondsdelay"));
        topic = parameter.get("topic");
        groupid = parameter.get("groupid");
        tablename = parameter.get("tablename");
        bootservers = parameter.get("bootservers");
        zkservers = parameter.get("zkservers");
        xmlargspath = parameter.get("xmlargspath");
        XmlArgsBean xmlArgsBean = ParseXmlDom4jUtil.getxmlArgsBean(xmlargspath);
        String xmlArgsBeanStr = JSON.toJSONString(xmlArgsBean);
        //System.out.println("topic="+topic+" groupid="+groupid+" tablename="+tablename);
        //获取执行环境more
        StreamExecutionEnvironment sEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties props = new Properties();
        props.put("bootstrap.servers", bootservers);
        props.put("zookeeper.connect", zkservers);
        props.put("group.id", groupid);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("flink.partition-discovery.interval-millis","30000");
        //System.out.println(" 1 sEnv 并行度： "+sEnv.getParallelism());
        sEnv.setParallelism(1);
        //System.out.println("2 sEnv 并行度： "+sEnv.getParallelism());

        //消费kafka数据
        FlinkKafkaConsumer<FlatMessage> myConsumer = new FlinkKafkaConsumer(topic, new FlatMessageSchema(), props);
        myConsumer.setStartFromGroupOffsets();
        DataStreamSource<FlatMessage> message = sEnv.addSource(myConsumer);
        //System.out.println("message 并行度： "+message.getParallelism());
        message.timeWindowAll(Time.seconds(secondsdelay));
        //GlobalConfig.HBASE_TABLE_NAME = topic;
        //System.out.println("GlobalConfig.HBASE_TABLE_NAME="+GlobalConfig.HBASE_TABLE_NAME);
        String finalTopic = topic;
        DataStream<FlatMessage> message_youhui = message.filter(new FilterFunction<FlatMessage>() {
            @Override
            public boolean filter(FlatMessage flatMessage) throws Exception {
                String dbtbname = flatMessage.getDatabase()+"."+flatMessage.getTable();
                ////System.out.println("dbname.tbname= "+dbtbname);
                if(null != flatMessage && dbtbname.equalsIgnoreCase(finalTopic)){
                    return true;
                }else{
                    return false;
                }
            }
        });


        DataStream<FlatMessage> message_youhui2 = message_youhui.map(new MapFunction<FlatMessage, FlatMessage>() {
            @Override
            public FlatMessage map(FlatMessage flatMessage) throws Exception {
                FlatMessage newFlatMessage = assembleNewFlatMessage(JSON.parseObject(xmlArgsBeanStr, XmlArgsBean.class),flatMessage);
                //System.out.println("newFlatMessage="+newFlatMessage.toString());
                return newFlatMessage;
            }
        });

        /*DataStream<FlatMessage> message_youhuitag = message.filter(new FilterFunction<FlatMessage>() {
            @Override
            public boolean filter(FlatMessage flatMessage) throws Exception {
                if(null == flatMessage){
                    return false;
                }
                if(flatMessage.getDatabase().equals("dbzdm_youhui") && flatMessage.getTable().equals("youhui_tag_type_item")){
                    if(null != flatMessage){
                        //System.out.println("3 filter true flatMessage="+flatMessage.toString());
                    }else{
                        //System.out.println("3.0 filter true flatMessage="+flatMessage.getTable());
                    }
                    return true;
                }else{
                    ////System.out.println("3 filter false flatMessage="+flatMessage.toString());
                    return false;
                }
            }
        });*/
        //System.out.println("message_youhui2="+message_youhui2.print());
        SingleOutputStreamOperator<FlatMessage> message_youhuitag_merge = message_youhui2.keyBy(new KeySelector<FlatMessage, String>() {
            @Override
            public String getKey(FlatMessage flatMessage) throws Exception {
                //System.out.println("keyBy flatMessage="+flatMessage.toString());
                if (null != flatMessage && flatMessage.getTable().equals("youhui_tag_type_item")) {
                    String tbarticleid = flatMessage.getData().get(0).get("id");
                    return tbarticleid;
                } else {
                    return "0";
                }
            }
        }).map(new MyValueStateFunction());

        //4 message_youhuitag中给相同的文章id聚合不同的tag_id 多个之间用逗号分割
        //System.out.println("message_youhuitag_merge="+message_youhuitag_merge.print());

        //System.out.println("message 并行度： "+message.getParallelism());
        //存储到hbase
        DataStreamSink<FlatMessage> dataStreamSink = message_youhuitag_merge.addSink(new MyHbaseSink());
        sEnv.execute("TongBu");
    }

    private static FlatMessage assembleNewFlatMessage(XmlArgsBean xmlArgsBean,FlatMessage flatMessage) {
        String dbtbname = flatMessage.getDatabase()+"."+flatMessage.getTable();
        //System.out.println("dbtbname="+dbtbname);
        Map<String, String> tableinputoutputcolsMap = xmlArgsBean.getAssembleMap().get(dbtbname);
        if(null==tableinputoutputcolsMap || !xmlArgsBean.getAssembleMap().containsKey(dbtbname)){
            return null;
        }
        List<Map<String, String>> data = flatMessage.getData();
        List<Map<String, String>> newdata = new ArrayList<Map<String, String>>();
        //System.out.println("datasize="+data.size()+"data="+data);
        for (Map<String, String> map : data) {
            //System.out.println("mapoldsize="+map.size()+"mapold="+map.keySet());
            Map<String, String> intersectionSetByGuava = getIntersectionSetByGuava(map, tableinputoutputcolsMap);
            //修改选中保留列的对应输出列名
            for (String inputcol : tableinputoutputcolsMap.keySet()) {
                if(!inputcol.equals(tableinputoutputcolsMap.get(inputcol))){
                    intersectionSetByGuava.put(tableinputoutputcolsMap.get(inputcol),intersectionSetByGuava.get(inputcol));
                    intersectionSetByGuava.remove(inputcol);
                }
            }
            //System.out.println("mapnewsize="+intersectionSetByGuava.size()+"mapnew="+intersectionSetByGuava.keySet());
            newdata.add(intersectionSetByGuava);
        }
        flatMessage.setData(newdata);
        //System.out.println("newdatasize="+newdata.size()+"newdata="+newdata);
        return flatMessage;
    }


    /**
     * 取Map集合的交集（String,String）
     *
     * @param map1 大集合
     * @param map2 小集合
     * @return 两个集合的交集
     */
    public static Map<String, String> getIntersectionSetByGuava(Map<String, String> map1, Map<String, String> map2) {
        Set<String> bigMapKey = map1.keySet();
        Set<String> smallMapKey = map2.keySet();
        Set<String> differenceSet = Sets.intersection(bigMapKey, smallMapKey);
        Map<String, String> result = Maps.newHashMap();
        for (String key : differenceSet) {
            result.put(key, map1.get(key));
        }
        return result;
    }
}
