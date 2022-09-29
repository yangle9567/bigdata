import check.SdkExposureMessage;
import check.SdketlValueStateFunction;
import check.SdketlValueStateFunction2;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import smzdm.schema.SdketlMessageSchema;
import smzdm.sink.MyHbaseSyncService;

import java.util.Map;
import java.util.Properties;

public class etl {
    public static void main(String[] args) throws Exception {

        //ParameterTool parameterTool = ParameterTool.fromPropertiesFile("etl.properties");
        /*String sourcetopic=parameterTool.get("sourcetopic");
        String sinktopic=parameterTool.get("sinktopic");*/
        MyHbaseSyncService myHbaseSyncService = null;
        //Map<String,String> map=new

        ParameterTool parameter = ParameterTool.fromArgs(args);
        //String sourcetopic= parameter.get("sourcetopic");
        String sourcetopic= "app-sdk-log";
        //System.out.println("sourcetopic="+sourcetopic);

        String sinktopic="dwd_sdk_log";//源头数据app-sdk-log  // dwd_sdk_log

        Properties props = new Properties();
        /*props.put("bootstrap.servers", parameterTool.get("sourcekafkaipport"));
        props.put("zookeeper.connect", parameterTool.get("sourcekafkaipport"));*/

        //props.put("bootstrap.servers", "10.45.4.146:9092");//
        props.put("bootstrap.servers", "10.45.0.171:9092");
        props.put("zookeeper.connect", "10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181");
        props.put("group.id", "groupx1");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("flink.partition-discovery.interval-millis","30000");
        //获取执行环境
        StreamExecutionEnvironment sEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        //sEnv.registerCachedFile("","");
        //消费kafka数据
        FlinkKafkaConsumer<SdkExposureMessage> flinkKafkaConsumer = new FlinkKafkaConsumer(sourcetopic, new SdketlMessageSchema(),props);
        flinkKafkaConsumer.setStartFromLatest();
        DataStream<SdkExposureMessage> message = sEnv.addSource(flinkKafkaConsumer);
        message.timeWindowAll(Time.milliseconds(500));
        if(message != null){
            message.print();
        }

        //1 通过数据中的文章id联查大宽表信息
        DataStream<SdkExposureMessage> message2 = message.map(new MapFunction<SdkExposureMessage, SdkExposureMessage>() {
            @Override
            public SdkExposureMessage map(SdkExposureMessage sdkExposureMessage) throws Exception {
                //1a 根据列表页取数口径 重装各曝光，详情点击，与电商点击的（文章ID，频道ID，流量）,并重装去重key
                if(null != sdkExposureMessage && null != sdkExposureMessage.getDatamap()){
                    //System.out.println("---reload前-------sdkExposureMessage="+sdkExposureMessage.toString());
                    sdkExposureMessage=reload(sdkExposureMessage);
                    //System.out.println("---reload后-------sdkExposureMessage="+sdkExposureMessage.toString());
                    return sdkExposureMessage;
                }else{
                    return null;
                }
            }
        });
        if(message2 != null){
            message2.print();
        }

        //2 过滤扔掉为空的数据和非首页推荐数据
        DataStream<SdkExposureMessage> message3 = message2.filter(new FilterFunction<SdkExposureMessage>() {
            @Override
            public boolean filter(SdkExposureMessage sdkExposureMessage) throws Exception {
                if(null != sdkExposureMessage && sdkExposureMessage.getSys().equals("home_recomend") && sdkExposureMessage.getDatamap() != null){
                    return true;
                }else{
                    return false;
                }
            }
        });
        if(message3 != null){
            message3.print();
        }


        //3 分流出首页推荐数据
        /**1、定义OutputTag*/
        OutputTag<SdkExposureMessage> sideOutputTag = new OutputTag<SdkExposureMessage>("is_home_recomend"){};

        /**2、在ProcessFunction中处理主流和分流*/
        SingleOutputStreamOperator<SdkExposureMessage> processedStream = message3.process(new ProcessFunction<SdkExposureMessage,SdkExposureMessage>() {
            @Override
            public void processElement(SdkExposureMessage sdkExposureMessage, Context context, Collector<SdkExposureMessage> out) throws Exception {
                //侧流-只输出特定数据
                if(null != sdkExposureMessage){
                    if (sdkExposureMessage.getSys() != null && sdkExposureMessage.getSys().equals("home_recomend") && sdkExposureMessage.getEvent().equals("exposure")) {
                        context.output(sideOutputTag, sdkExposureMessage);
                    }else {
                        //主流
                        out.collect(sdkExposureMessage);
                    }
                }
            }
        });
        if(processedStream != null){
            processedStream.print();
        }

        //4 对首页推荐曝光信息进行按key标记是否重复
        SingleOutputStreamOperator<SdkExposureMessage> message_sdkhomerec_exposure = processedStream.getSideOutput(sideOutputTag).keyBy(new KeySelector<SdkExposureMessage, String>() {
            @Override
            public String getKey(SdkExposureMessage sdkExposureMessage) throws Exception {
                //System.out.println("keyBy sdkExposureMessage="+sdkExposureMessage.toString());
                if (null != sdkExposureMessage && StringUtils.isNotBlank(sdkExposureMessage.getKey())) {
                    return sdkExposureMessage.getKey();
                } else {
                    return "key0";
                }
            }
        }).map(new SdketlValueStateFunction());//对首页推荐数据进行打标记（是否重复）
        if(message_sdkhomerec_exposure != null){
            message_sdkhomerec_exposure.print();
        }

        //5 合并首页推荐数据与其他数据，然后联查大宽表文章属性信息
        SingleOutputStreamOperator<SdkExposureMessage> message4 = processedStream.union(message_sdkhomerec_exposure).keyBy(new KeySelector<SdkExposureMessage, String>() {
            @Override
            public String getKey(SdkExposureMessage sdkExposureMessage) throws Exception {
                //System.out.println("keyBy sdkExposureMessage=" + sdkExposureMessage.toString());
                if (null != sdkExposureMessage && StringUtils.isNotBlank(sdkExposureMessage.getArticleid())) {
                    return sdkExposureMessage.getArticleid();
                } else {
                    return "key0";
                }
            }
        }).map(new SdketlValueStateFunction2());//1b 通过数据里面的文章id联查大宽表信息，缓冲复用联查回的大宽表信息，一天更新一次
        if(message4 != null){
            message4.print();
        }


        DataStream<String> message5 = message4.map(new MapFunction<SdkExposureMessage, String>() {
            @Override
            public String map(SdkExposureMessage sdkExposureMessage) throws Exception {
                //1a 根据列表页取数口径 重装各曝光，详情点击，与电商点击的（文章ID，频道ID，流量）,并重装去重key
                if(null != sdkExposureMessage && null != sdkExposureMessage.getDatamap()){
                   return sdkExposureMessage.toJsonString(sdkExposureMessage);
                }else{
                    return null;
                }
            }
        });
        if(message5 != null){
            message5.print();
        }


        Properties props2 = new Properties();
        props2.put("bootstrap.servers", "10.45.4.146:9092");
        props2.put("zookeeper.connect", "10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181");
        props2.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props2.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props2.put("flink.partition-discovery.interval-millis","30000");
        //6 结果流数据写入kafka数据
        FlinkKafkaProducer<String> flinkKafkaProducer = new FlinkKafkaProducer<String>(sinktopic, new SimpleStringSchema(), props2);
        message5.addSink(flinkKafkaProducer);
        sEnv.execute("SdkEtl");
    }

    private static SdkExposureMessage reload(SdkExposureMessage sdkExposureMessage) {
         String articleid="";
         String chanalid="";
         String abtest="";
        Map<String, String> datamap = sdkExposureMessage.getDatamap();

        String sp=getKey(datamap.get("ecp"),"sp");
        if(datamap.containsKey("type") && datamap.containsKey("ec") && datamap.containsKey("ea")){
            //System.out.println("-----------------------1.0-------------------");

            //a1 曝光的（文章ID，频道ID，流量）
            if(datamap.get("type").equals("show") && datamap.get("ec").equals("01") && datamap.get("ea").equals("01") && "0".equals(sp)){
                sdkExposureMessage.setEvent("exposure");//event事件（1：曝光，2：详情点击，3：电商点击）
                sdkExposureMessage.setSys("home_recomend");//sys 业务 （首页推荐，搜索，首页关注，好价......）
                articleid=getKey(datamap.get("ecp"),"a");
                chanalid=getKey(datamap.get("ecp"),"c");
                abtest=getKey(datamap.get("ecp"),"tv");

                sdkExposureMessage.setArticleid(articleid);
                sdkExposureMessage.setChannel(chanalid);
                sdkExposureMessage.setAbtest(abtest);
                sdkExposureMessage=makekey(datamap,sdkExposureMessage,sp);
                //System.out.println("-----------------------1.1-------------------sdkExposureMessage="+sdkExposureMessage);
                return sdkExposureMessage;
            }

            // a2，详情点击的（文章ID，频道ID，流量）
            if(datamap.get("type").equals("event") && datamap.get("ec").equals("首页") && datamap.get("ea").equals("首页站内文章点击")){
                sdkExposureMessage.setEvent("detailclick");//event事件（1：曝光，2：详情点击，3：电商点击）
                sdkExposureMessage.setSys("home_recomend");//sys 业务 （首页推荐，搜索，首页关注，好价......）
                articleid=datamap.get("el").split("_")[2];
                chanalid=getKey(datamap.get("ecp"),"11");
                abtest=getKey(datamap.get("ecp"),"13");

                sdkExposureMessage.setArticleid(articleid);
                sdkExposureMessage.setChannel(chanalid);
                sdkExposureMessage.setAbtest(abtest);
                sdkExposureMessage=makekey(datamap,sdkExposureMessage, sp);
                //System.out.println("-----------------------1.2-------------------sdkExposureMessage="+sdkExposureMessage);
                return sdkExposureMessage;
            }

            // a3，与电商点击的（文章ID，频道ID，流量）
            if(datamap.get("type").equals("event") && datamap.get("ec").equals("增强型电子商务") && datamap.get("ea").equals("添加到购物车")){
                sdkExposureMessage.setEvent("e-commerceclick");//event事件（1：曝光，2：详情点击，3：电商点击）
                sdkExposureMessage.setSys("home_recomend");//sys 业务 （首页推荐，搜索，首页关注，好价......）
                if(datamap.get("el").startsWith("推荐详情")){
                    articleid=datamap.get("el").split("_")[1];
                }else{
                    articleid=getKey(datamap.get("ecp"),"4");
                }
                chanalid=getKey(datamap.get("ecp"),"11");
                abtest=getKey(datamap.get("ecp"),"13");

                sdkExposureMessage.setArticleid(articleid);
                sdkExposureMessage.setChannel(chanalid);
                sdkExposureMessage.setAbtest(abtest);
                sdkExposureMessage=makekey(datamap,sdkExposureMessage, sp);
                //System.out.println("-----------------------1.3------------------sdkExposureMessage="+sdkExposureMessage);
                return sdkExposureMessage;
            }

            //System.out.println("-----------------------1.4-------------------");
            return sdkExposureMessage;
        }else{
            //System.out.println("-----------------------2.0-------------------");
            return sdkExposureMessage;
        }
    }

    private static SdkExposureMessage makekey(Map<String, String> datamap, SdkExposureMessage sdkExposureMessage, String sp) {
        //a4  组装去重key
        if(null != datamap && datamap.get("type").equals("show") &&  datamap.get("ec").equals("01") && datamap.get("ea").equals("01") && sp.equals("0")){
            //2 组织去重字段key  曝光去重事件 接收到曝光日志 解析出 【"repeat_" + deviceId + "" + itemId + "" + channelId】  itemId=articleid
            String key="repeat_" + datamap.get("did") + "" + sdkExposureMessage.getArticleid() + "" + sdkExposureMessage.getChannel();
            sdkExposureMessage.setKey(key);
            //System.out.println("--------------key="+key);
            //System.out.println("-----------------------1.4-------------------");
        }
        return sdkExposureMessage;
    }

    private static String getKey(String ecp, String a) {
        if(null != ecp && null != a){
            //System.out.println("ecp="+ecp);
            Map<String,String> map = (Map) JSON.parse(ecp);
            //System.out.println("a="+map.get(a));
            return map.get(a);
        }else{
            return null;
        }
    }
}
