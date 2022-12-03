package com.chrtc.utils;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息生产者
 * @author legend 2017/3/22 14:27
 */
public class MsgProducer extends Thread {

    private final KafkaProducer<String, Map> producer;
    private final String topic;
    private final boolean isAsync;

    public MsgProducer(String topic, boolean isAsync) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "h1:9092,h2:9092,h3:9092");//broker 集群地址
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "producer");//自定义客户端id
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");//key 序列号方式
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "com.chrtc.utils.MapSerializer");//value 序列号方式
        //properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");//value 序列号方式
        //properties.put(ProducerConfig.spark.serializer,"org.apache.spark.serializer.kryoSerializer");
        //properties.put(ProducerConfig,"avro");//value 序列号方式
//        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,CustomPartitioner.class.getCanonicalName());//自定义分区函数

//        properties.load("properties配置文件");

        this.producer = new KafkaProducer<String, Map>(properties);
        this.topic = topic;
        this.isAsync = isAsync;
    }

    
    /*conf.set("spark.serializer","org.apache.spark.serializer.kryoSerializer")
    //conf.set("spark.kryo.registrator",tokryoRegistrator.class.getName())
    conf.registratorKyroClasses(Array(classOf[MyCLASS1],CLASSOF[MyCLASS2]))*/
    @Override
    public void run() {
    	while(true){
    		String datasetname = "communication1";
    		Map<String,List<Map<String, String>>> map = getColumDataMap();
    		List<Map<String, String>> listmap = map.get(datasetname);
    		for(Map map1 : listmap){
    			Map<String, String> columandvaluemap = map1;//每个map都是一行数据
    			if (isAsync) {//异步
    				Map map2=new HashMap();
    				List list=new ArrayList();
    				list.add(columandvaluemap);
    				map2.put(datasetname, list);
    				producer.send(new ProducerRecord<String, Map>(this.topic,map2));
    			}
    		}
    	}
    	
    	
    	/*producer.send(new ProducerRecord<String, Map>(this.topic,"map2", null));*/
    	/*int msgNo = 0;
        int keyid = 0;
        int i=0;
        Map data =new HashMap<>();
        while (true) {
        	String dataSetId1="communication1";
        	try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String msg = "Msg: " + msgNo;
            String key = msgNo + "";
            if (isAsync) {//异步
            	Map<String,Object> dataDetailMap=new HashMap<String,Object>();
                dataDetailMap.put("col1","c-1");
                dataDetailMap.put("col2","1895412451-1");
                dataDetailMap.put("col3","");
                dataDetailMap.put("col4","cardid-1");
                dataDetailMap.put("col5"," 6215-1");
                dataDetailMap.put("col6","");
                dataDetailMap.put("col7","englinsh name-1");
                dataDetailMap.put("col8","LINUX-1");
                dataDetailMap.put("col9","");
                List  dataDetailList=new ArrayList();
                dataDetailList.add(dataDetailMap);
                data.put(dataSetId1,dataDetailList);
                producer.send(new ProducerRecord<String, Map>(this.topic,data));
                System.out.println("well done ");
                i++;
                msgNo++;
                keyid++;
//                producer.send(new ProducerRecord<String, String>(this.topic, key, msg));
            } else {//同步
                producer.send(new ProducerRecord<String, String>(this.topic, key, msg),
                        new MsgProducerCallback(System.currentTimeMillis(), key, msg));
                msgNo++;
            }
        }*/
    }

    private Map<String,List<Map<String, String>>> getColumDataMap() {
		// TODO Auto-generated method stub
    	Map<String,List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
    	List<Map<String, String>> listmap = new ArrayList<Map<String, String>>();
    	String datasetname = "communication1";
		String columnames="Z002000	Z002001	Z002007	Z002824	Z002003	Z002005	Z002004	Z002095	Z002098	Z002099	Z002100	Z0026LN	Z002600	H010018	H010005	F020004	F020006	F020010	F020012	F020005	F020007	F020011	F020013	Z002606	H010014	Z002700	H010033	H010031	Z002101	B020007	F030002	Z002504	Z002505	Z002604	Z002605	C050001	C020005	Z002603	B020001	H010006	H010007	G020013	G020014	B040021	F030013	F030014	F010008	H010002	B040022	B050016	H040002	C040002	H010013	C040004	C040003	B030001	B030004	B030005	G020004	F010001	F010002	B030020	B030021	H010034	I010009	F020001	B030002	B010001	H010032	F010009	H010035	H010036	H010037	F040007	G020036	F020016	F020017	C020017	C020011	C020009	H070003	C020006	C020007	C020021	C020022	H010041	H010042	C020004	F040002	Z002557	Z002201	Z002558	Z002442	500030B	200040C	230010H	220040B	300040C	101200Z	755200Z	Z002534	Z002570	Z002050	Z002051	Z002052	Z002053	Z002054	Z002055	Z002056	Z002057	F030017	F030020	Z002060	Z002061	Z002064	Z002065	Z0025A4	Z002091	Z002896	Z0021L3	Z002619	100050C	700020B	200030F	Z0020A1	Z0020A2	Z0020A3	Z0020A4	F020020	I070011	F030011	G020020	I070005	110030F	H010001	Z002701	G010002	G010003	G010001	B040002	B040005	B050004	B050009	H030004	H030005	H030001	H010019	Z002720	H010020	Z002713	H010003	Z002714	B020005	Z002103	B040023	B040003	G010004	H030002	H030003	H010021	H010028	H010038	C020027	H030006	H030007	H030008	B040014	H040001	Z002732	Z002733	Z002734	G020006	Z002666	Z002667	200040B	400050B	900050B	100010G	200030H	300030H	400030H	500030H	500020B	320040B	200010G	301200Z	H030009	H030010	H010029	B040033	I050008	Z002020	Z002021	Z002022	Z002711	H100002	B050005	B050010	H040003	Z002062	Z002063	Z0020A0	Z0020A5	Z002066	Z002067	Z002068	Z002069	Z002070	Z002071	Z002072	Z002073	Z002265	Z002266	Z002267	Z002268	270200Z	370200Z";
		File f = new File("E://w//dev//data//mydata//111-746736751-110000-110000-1502122065-02742//111-630000-1502122104-10053-WA_SOURCE_0002-0.bcp");
		
		String[] columnamesarray = columnames.split("\t");
		BufferedReader reader = null;
		
		try {
			String tempString = null;
			int line = 1;
			reader = new BufferedReader(new FileReader(f));
			while((tempString = reader.readLine()) != null){
				//System.out.println("line : "+line+"  ="+tempString);
				String[] columvaluesarray = tempString.split("\t",300);
				System.out.println(tempString);
				//System.out.println("columnamesarray.length ="+columnamesarray.length+" columvaluesarray.length = "+columvaluesarray.length);
				if(columnamesarray.length == columvaluesarray.length){
					Map<String, String> columandvaluemap = new HashMap<String, String>();
					for(int i=0;i < columnamesarray.length; i++){
						columandvaluemap.put(columnamesarray[i], columvaluesarray[i]);
						System.out.println("columnamesarray[i] = "+columnamesarray[i]+"  columvaluesarray[i] = "+columvaluesarray[i]);
					}
					listmap.add(columandvaluemap);
				}
				line++; 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(reader != null ){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		map.put(datasetname, listmap);
		return map;
	}


	private String getStringFromObject(Map data) throws IOException {
		// TODO Auto-generated method stub
    	
    	if (data == null)
            return null;
        else{
	    	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	        ObjectOutputStream oos=new ObjectOutputStream(baos);
	        oos.writeObject(data);
	        byte[] str=baos.toByteArray();
	        String s=new String(str,"ISO-8859-1");
	        return s;
        }
	}

	/**
     * 消息发送后的回调函数
     */
    class MsgProducerCallback implements Callback {

        private final long startTime;
        private final String key;
        private final String msg;

        public MsgProducerCallback(long startTime, String key, String msg) {
            this.startTime = startTime;
            this.key = key;
            this.msg = msg;
        }

        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (recordMetadata != null) {
                System.out.println(msg + " be sended to partition no : " + recordMetadata.partition());
            }
        }
    }

    public static void main(String args[]) {
        new MsgProducer("legendtpoic",true).start();//开始发送消息
    }
}