import check.SdkExposureMessage;
import check.SdketlValueStateFunction;
import check.SdketlValueStateFunction2;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import smzdm.schema.SdketlMessageSchema;
import smzdm.sink.MyHbaseSyncService;

import java.util.Map;
import java.util.Properties;

public class etl2 {
    public static void main(String[] args) throws Exception {

        //ParameterTool parameterTool = ParameterTool.fromPropertiesFile("etl.properties");
        /*String sourcetopic=parameterTool.get("sourcetopic");
        String sinktopic=parameterTool.get("sinktopic");*/
        MyHbaseSyncService myHbaseSyncService = null;
        //Map<String,String> map=new

        String sourcetopic="test";
        String sinktopic="dwd_sdk_log";//源头数据app-sdk-log  // dwd_sdk_log

        Properties props = new Properties();
        /*props.put("bootstrap.servers", parameterTool.get("sourcekafkaipport"));
        props.put("zookeeper.connect", parameterTool.get("sourcekafkaipport"));*/

        props.put("bootstrap.servers", "10.45.4.146:9092");
        props.put("zookeeper.connect", "10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181");
        props.put("group.id", "group1");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("flink.partition-discovery.interval-millis","30000");
        //获取执行环境
        StreamExecutionEnvironment sEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        //sEnv.registerCachedFile("","");
        //消费kafka数据
        FlinkKafkaConsumer<SdkExposureMessage> flinkKafkaConsumer = new FlinkKafkaConsumer(sourcetopic, new SdketlMessageSchema(),props);
        flinkKafkaConsumer.setStartFromGroupOffsets();
        DataStream<SdkExposureMessage> message = sEnv.addSource(flinkKafkaConsumer);
        message.print();
        //1 通过数据中的文章id联查大宽表信息
        DataStream<SdkExposureMessage> message2 = message.map(new MapFunction<SdkExposureMessage, SdkExposureMessage>() {
            @Override
            public SdkExposureMessage map(SdkExposureMessage sdkExposureMessage) throws Exception {
                //1a 根据列表页取数口径 重装各曝光，详情点击，与电商点击的（文章ID，频道ID，流量）
                sdkExposureMessage=reload(sdkExposureMessage);
                return sdkExposureMessage;
            }
        }).keyBy(new KeySelector<SdkExposureMessage, String>() {
            @Override
            public String getKey(SdkExposureMessage sdkExposureMessage) throws Exception {
                //System.out.println("keyBy sdkExposureMessage="+sdkExposureMessage.toString());
                if (null != sdkExposureMessage) {
                    return sdkExposureMessage.getArticleid();
                } else {
                    return "key0";
                }
            }
        }).map(new SdketlValueStateFunction2());//1b 通过数据里面的文章id联查大宽表信息,并用Operator State缓存相同文章的大宽表文章属性信息
        message2.print();
        //2 分流过滤出首页推荐曝光信息
        DataStream<SdkExposureMessage> message_sdkhomerec = message2.filter(new FilterFunction<SdkExposureMessage>() {
            @Override
            public boolean filter(SdkExposureMessage sdkExposureMessage) throws Exception {
                String sp=getsp(sdkExposureMessage.getDatamap().get("ecp"));
                if(null != sdkExposureMessage && sdkExposureMessage.getDatamap().get("type").equals("show") && sdkExposureMessage.getDatamap().get("ec").equals("01") && sdkExposureMessage.getDatamap().get("ea").equals("01") && sp.equals("0")){
                    //2 组织去重字段key  曝光去重事件 接收到曝光日志 解析出 【"repeat_" + deviceId + "" + itemId + "" + channelId】
                    String key="repeat_" + sdkExposureMessage.getDatamap().get("did") + "" + sdkExposureMessage.getDatamap().get("imei") + "" + sdkExposureMessage.getChannel();
                    sdkExposureMessage.setKey(key);
                    return true;
                }else{
                    return false;
                }
            }
        });
        message_sdkhomerec.print();
        //3 对首页推荐曝光信息进行按key去重
        SingleOutputStreamOperator<SdkExposureMessage> message_sdkhomerec2 = message_sdkhomerec.keyBy(new KeySelector<SdkExposureMessage, String>() {
            @Override
            public String getKey(SdkExposureMessage sdkExposureMessage) throws Exception {
                //System.out.println("keyBy sdkExposureMessage="+sdkExposureMessage.toString());
                if (null != sdkExposureMessage) {
                    return sdkExposureMessage.getKey();
                } else {
                    return null;
                }
            }
        }).map(new SdketlValueStateFunction());//message_youhuitag中给相同的文章id聚合不同的tag_id 多个之间用逗号分割
        message_sdkhomerec2.print();
        Properties props2 = new Properties();
        props2.put("bootstrap.servers", "10.45.4.146:9092");
        props2.put("zookeeper.connect", "10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181");
        props2.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props2.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props2.put("flink.partition-discovery.interval-millis","30000");
        //4 结果流数据写入kafka数据
        FlinkKafkaProducer<SdkExposureMessage> flinkKafkaProducer = new FlinkKafkaProducer<>(sinktopic, new SdketlMessageSchema(), props2);
        message_sdkhomerec2.addSink(flinkKafkaProducer);

        sEnv.execute("SdkEtl");

    }

    private static SdkExposureMessage reload(SdkExposureMessage sdkExposureMessage) {
         String articleid="";
         String chanalid="";
         String tvflow="";
        Map<String, String> datamap = sdkExposureMessage.getDatamap();
        //a1 曝光的（文章ID，频道ID，流量）
        String sp=getsp(datamap.get("ecp"));
        if(datamap.get("type").equals("show") && datamap.get("ec").equals("01") && datamap.get("ea").equals("01") && sp.equals("0")){
            sdkExposureMessage.setSys("1");//业务（1：曝光，2：详情点击，3：电商点击）
            articleid=getKey(datamap.get("ecp"),"a");
            chanalid=getKey(datamap.get("ecp"),"c");
            tvflow=getKey(datamap.get("ecp"),"tv");
        }

        // a2，详情点击的（文章ID，频道ID，流量）
        if(datamap.get("type").equals("event") && datamap.get("ec").equals("首页") && datamap.get("ea").equals("首页站内文章点击")){
            sdkExposureMessage.setSys("2");//业务（1：曝光，2：详情点击，3：电商点击）
            articleid=datamap.get("el").split("_")[2];
            chanalid=getKey(datamap.get("ecp"),"11");
            tvflow=getKey(datamap.get("ecp"),"13");
        }

        // a3，与电商点击的（文章ID，频道ID，流量）
        if(datamap.get("type").equals("event") && datamap.get("ec").equals("增强型电子商务") && datamap.get("ea").equals("添加到购物车")){
            sdkExposureMessage.setSys("3");//业务（1：曝光，2：详情点击，3：电商点击）
            if(datamap.get("el").startsWith("推荐详情")){
                articleid=datamap.get("el").split("_")[1];
            }else{
                articleid=getKey(datamap.get("ecp"),"4");
            }
            chanalid=getKey(datamap.get("ecp"),"11");
            tvflow=getKey(datamap.get("ecp"),"13");
        }


        return sdkExposureMessage;
    }

    private static String getKey(String ecp, String a) {
        return null;
    }

    private static String getsp(String ecp) {
        return null;
    }

}
