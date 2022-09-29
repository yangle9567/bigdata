import check.SdkExposureMessage;
import check.SdketlValueStateFunction;
import check.SdketlValueStateFunction2;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
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

public class sdketl {
    public static void main(String[] args) throws Exception {

        //ParameterTool parameterTool = ParameterTool.fromPropertiesFile("etl.properties");
        /*String sourcetopic=parameterTool.get("sourcetopic");
        String sinktopic=parameterTool.get("sinktopic");*/
        MyHbaseSyncService myHbaseSyncService = null;
        //Map<String,String> map=new

        ParameterTool parameter = ParameterTool.fromArgs(args);
        //String sourcetopic= parameter.get("sourcetopic");
        String groupid= parameter.get("groupid");
        String sourcetopic= "app-sdk-log";
        //System.out.println("sourcetopic="+sourcetopic);
        //System.out.println("groupid="+groupid);

        String sinktopic="dwd_sdk_log";//源头数据app-sdk-log  // dwd_sdk_log

        Properties props = new Properties();
        /*props.put("bootstrap.servers", parameterTool.get("sourcekafkaipport"));
        props.put("zookeeper.connect", parameterTool.get("sourcekafkaipport"));*/

        //props.put("bootstrap.servers", "10.45.4.146:9092");
        props.put("bootstrap.servers", "10.45.0.171:9092");
        props.put("zookeeper.connect", "10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181");
        props.put("group.id", groupid);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("flink.partition-discovery.interval-millis","30000");
        //获取执行环境
        StreamExecutionEnvironment sEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        //sEnv.registerCachedFile("","");
        //消费kafka数据
        FlinkKafkaConsumer<SdkExposureMessage> flinkKafkaConsumer = new FlinkKafkaConsumer(sourcetopic, new SdketlMessageSchema(),props);
        flinkKafkaConsumer.setStartFromGroupOffsets();
        //flinkKafkaConsumer.setStartFromLatest();
        DataStream<SdkExposureMessage> message = sEnv.addSource(flinkKafkaConsumer);
        sEnv.setRestartStrategy(RestartStrategies.failureRateRestart(
                10, // 每个测量时间间隔最大失败次数
                org.apache.flink.api.common.time.Time.minutes(5), //失败率测量的时间间隔
                org.apache.flink.api.common.time.Time.seconds(10) // 两次连续重启尝试的时间间隔
        ));
        // 每隔1000 ms进行启动一个检查点
        sEnv.enableCheckpointing(1000*60*5);
        // 高级选项：
        // 设置模式为exactly-once （默认）
        sEnv.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        // 确保检查点之间有至少500 ms的间隔
        sEnv.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);

        // 检查点必须在一分钟内完成，没有完成就被丢弃。
        sEnv.getCheckpointConfig().setCheckpointTimeout(60000);

        // 同一时间只允许进行一个检查点
        sEnv.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

        // 表示一旦Flink处理程序被cancel后，会保留Checkpoint数据，checkpoint有多个，可以根据实际需要恢复到指定的Checkpoint。
        sEnv.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        //当有新的保存点时，允许作业恢复回退到检查点(这个配置是1.9的flink新增的，我现在代码里面是1.8的，所以用不了这个，想测试的可以改一下版本，我就说怎么没有，尴尬..)
        sEnv.getCheckpointConfig().setPreferCheckpointForRecovery(true);

        sEnv.setStateBackend(new FsStateBackend("hdfs://HDFS80727/bi/tmp/sdketl/checkpoint"));
        message.timeWindowAll(Time.milliseconds(500));
       /* if(message != null){
            message.print();
        }*/

        //1 通过数据中的文章id联查大宽表信息
        DataStream<SdkExposureMessage> message2 = message.map(new MapFunction<SdkExposureMessage, SdkExposureMessage>() {
            @Override
            public SdkExposureMessage map(SdkExposureMessage sdkExposureMessage) throws Exception {
                //1a 根据列表页取数口径 重装各曝光，详情点击，与电商点击的（文章ID，频道ID，流量）,并重装去重key
                if(null != sdkExposureMessage && null != sdkExposureMessage.getDatamap()){
                    //System.out.println("---reload前-------sdkExposureMessage="+sdkExposureMessage.toString());
                    sdkExposureMessage=reload(sdkExposureMessage);
                    if(sdkExposureMessage != null){
                        //System.out.println("---reload后-------sdkExposureMessage="+sdkExposureMessage.toString());
                        return sdkExposureMessage;
                    }else{
                        return new SdkExposureMessage();
                    }
                }else{
                    return new SdkExposureMessage();
                }
            }
        });
        /*if(message2 != null){
            message2.print();
        }*/

        //2 过滤扔掉为空的数据
        DataStream<SdkExposureMessage> message3 = message2.filter(new FilterFunction<SdkExposureMessage>() {
            @Override
            public boolean filter(SdkExposureMessage sdkExposureMessage) throws Exception {
                if("home_recomend".equals(sdkExposureMessage.getSys())){
                    return true;
                }else{
                    return false;
                }
            }
        });
        /*if(message3 != null){
            message3.print();
        }*/


        //3 分流出首页推荐曝光数据
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
        /*if(processedStream != null){
            processedStream.print();
        }*/

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
        /*if(message_sdkhomerec_exposure != null){
            message_sdkhomerec_exposure.print();
        }*/

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
        /*if(message4 != null){
            message4.print();
        }*/


        DataStream<String> message5 = message4.map(new MapFunction<SdkExposureMessage, String>() {
            @Override
            public String map(SdkExposureMessage sdkExposureMessage) throws Exception {
                //SdkExposureMessage 对象转jsonstring
                if(null != sdkExposureMessage && null != sdkExposureMessage.getDatamap()){
                   return sdkExposureMessage.toJsonString(sdkExposureMessage);
                }else{
                    return null;
                }
            }
        });
        /*if(message5 != null){
            message5.print();
        }*/

        Properties props2 = new Properties();
        props2.put("bootstrap.servers", "10.45.4.146:9092");
        props2.put("zookeeper.connect", "10.45.4.145:2181,10.45.4.1:2181,10.45.3.107:2181");
        props2.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props2.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props2.put("flink.partition-discovery.interval-millis","30000");
        props2.setProperty("compression.type", "snappy");
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
        if(datamap!= null && datamap.containsKey("type") && datamap.containsKey("ec") && datamap.containsKey("ea")){
            //System.out.println("-----------------------1.0-------------------");

            //a1 曝光的（文章ID，频道ID，流量）
            if("show".equals(datamap.get("type")) && "01".equals(datamap.get("ec")) && "01".equals(datamap.get("ea")) && "0".equals(sp)){
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
            if("event".equals(datamap.get("type")) && "首页".equals(datamap.get("ec")) && "首页站内文章点击".equals(datamap.get("ea"))){
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
            if("event".equals(datamap.get("type")) && "增强型电子商务".equals(datamap.get("ec")) && "添加到购物车".equals(datamap.get("ea"))){
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
            return new SdkExposureMessage();
        }else{
            //System.out.println("-----------------------2.0-------------------");
            return new SdkExposureMessage();
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
        if(null != ecp && null != a && isjson(ecp)){
            //System.out.println("ecp="+ecp);
            Map<String,String> map = (Map) JSON.parse(ecp);
            //System.out.println("a="+map.get(a));
            return map.get(a);
        }else{
            return null;
        }
    }


    private static boolean isjson(String s4) {
        try {
            JSONObject jsonStr= JSONObject.parseObject(s4);
            return  true;
        } catch (Exception e) {
            //System.out.println("errorformatjsonstr="+s4);
            return false;
        }
    }
}
