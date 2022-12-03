package com.chrtc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class MessageProducer extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(MessageProducer.class);
    
    private final KafkaProducer<String, Message> producer;
    
    private final String topic;
    
    private final Boolean isAsync;
    
    private final Properties props = new Properties();
    
    // Broker地址列表
    private final String bootstrapServers = "bootstrap.servers";
    
    // 客户端ID
    private final String clientId = "client.id";
    
    // Key序列化类
    private final String keySerializer = "key.serializer";
    
    // Value序列化类
    private final String valueSerializer = "value.serializer";
 
    //默认发送20条消息
    private final int messageNumToSend = 100;
    
    /**
    * 用户自己申请的机机账号keytab文件名称
   
    /**
    * 用户自己申请的机机账号名称
    */
    
    
    /**
     * 新Producer 构造函数
     * @param topicName Topic名称
     * @param isAsync 是否异步模式发送
     */
    public MessageProducer(String topicName, Boolean asyncEnable)
    {
        
        KafkaProperties kafkaProc = KafkaProperties.getInstance();
        
        // Broker地址列表
        props.put(bootstrapServers, kafkaProc.getValues(bootstrapServers, "192.168.12.88:9092"));
        // 客户端ID
        //props.put(clientId, kafkaProc.getValues(clientId, "DemoProducer"));
        // Key序列化类
        props.put(keySerializer,kafkaProc.getValues(keySerializer, "org.apache.kafka.common.serialization.StringSerializer"));
        // Value序列化类
        props.put(valueSerializer,kafkaProc.getValues(valueSerializer, "com.chrtc.utils.MessageSerializer"));
        
        producer = new KafkaProducer<String, Message>(props);
        topic = topicName;
        isAsync = asyncEnable;
    }
    
    /**
     * 生产者线程执行函数，循环发送消息。
     */
    public void run()
    {
        LOG.info("New Producer: start.");
        int messageNo = 1;
        // 指定发送多少条消息后sleep1秒
        int intervalMessages=0; 
        
        while (true)
        {
            String messageStr = "Message_" + messageNo;
            long startTime = System.currentTimeMillis();
            
            // 构造消息记录
            Message message = new Message();
            message.setDataSetName("testdatasetname1");
            Map map = new HashMap();
            map.put("name", "zhangsan");
            map.put("phone", "13240807667");
            map.put("address", "北京海淀区清华大学");
            message.setMap(map);
            
            
            ProducerRecord<String, Message> record = new ProducerRecord<String, Message>(topic, message);
            
            if (true)
            {
                // 异步发送
            	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	producer.send(record, new DemoCallBack5(startTime, messageNo, messageStr));
                System.out.println("produced one message !");
                LOG.info("[NewProducer], Produced message: (" + record.key() + ", " + record.value().getDataSetName());
            }
            else
            {
                try
                {
                    // 同步发送
                    producer.send(record).get();
                }
                catch (InterruptedException ie)
                {
                    LOG.info("The InterruptedException occured : {}.", ie);
                }
                catch (ExecutionException ee)
                {
                    LOG.info("The ExecutionException occured : {}.", ee);
                }
            }
            messageNo++;
            
        }
        
    }
    
    
    public static Boolean isSecurityModel()
    {
        Boolean isSecurity = false;
        String krbFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "kafkaSecurityMode";
        
        Properties securityProps = new Properties();
        
        // file does not exist.
        if (!isFileExists(krbFilePath))
        {
            return isSecurity;
        }
        
        try 
        {
            securityProps.load(new FileInputStream(krbFilePath));
            if ("yes".equalsIgnoreCase(securityProps.getProperty("kafka.client.security.mode")))
            {
                isSecurity = true;
            }
        }
        catch (Exception e)
        {
            LOG.info("The Exception occured : {}.", e);
        }
        
        return isSecurity;
    }
    
    /*
     * 判断文件是否存在
     */
    private static boolean isFileExists(String fileName)
    {
        File file = new File(fileName);
        
        return file.exists();
    }
    
    public static void main(String[] args)
    {
        if (isSecurityModel())
        {
            LOG.info("Securitymode start.");
            LOG.info("Security prepare success.");
        }
        
        // 是否使用异步发送模式
        final boolean asyncEnable = false;
        MessageProducer producerThread = new MessageProducer("mytopic", asyncEnable);
        producerThread.start();
    }
}

class DemoCallBack5 implements Callback
{
    private static Logger LOG = LoggerFactory.getLogger(DemoCallBack5.class);
    
    private long startTime;
    
    private int key;
    
    private String message;
    
    public DemoCallBack5(long startTime, int key, String message)
    {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }
    
    /**
     * 回调函数，用于处理异步发送模式下，消息发送到服务端后的处理。
     * @param metadata  元数据信息
     * @param exception 发送异常。如果没有错误发生则为Null。
     */
    public void onCompletion(RecordMetadata metadata, Exception exception)
    {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null)
        {
            LOG.info("message(" + key + ", " + message + ") sent to partition(" + metadata.partition() + "), "
                + "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
        }
        else if (exception != null)
        {
            LOG.error("The Exception occured.", exception);
        }
        
    }
}