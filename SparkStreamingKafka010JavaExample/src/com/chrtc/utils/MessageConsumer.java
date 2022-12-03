package com.chrtc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import kafka.utils.ShutdownableThread;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageConsumer extends ShutdownableThread
{
    private static final Logger LOG = LoggerFactory.getLogger(MessageConsumer.class);
    
    private final KafkaConsumer<String, Message> consumer;
    
    private final String topic;
    
    // 一次请求的最大等待时间
    private final int waitTime = 1000;
    
    // Broker连接地址
    private final String bootstrapServers = "bootstrap.servers";
    // Group id
    private final String groupId = "group.id";
    // 消息内容使用的反序列化类
    private final String valueDeserializer = "value.deserializer";
    // 消息Key值使用的反序列化类
    private final String keyDeserializer = "key.deserializer";
    /*// 协议类型:当前支持配置为SASL_PLAINTEXT或者PLAINTEXT
    private final String securityProtocol = "security.protocol";
    // 服务名
    private final String saslKerberosServiceName = "sasl.kerberos.service.name";
    // 域名
    private final String kerberosDomainName = "kerberos.domain.name";*/
    // 是否自动提交offset
    private final String enableAutoCommit = "enable.auto.commit";
    // 自动提交offset的时间间隔
    private final String autoCommitIntervalMs = "auto.commit.interval.ms";
    
    // 会话超时时间
    private final String sessionTimeoutMs = "session.timeout.ms";
    
    /**
     * 用户自己申请的机机账号keytab文件名称
     */
    /*private static final String USER_KEYTAB_FILE = "user.keytab";*/
    
    /**
    * 用户自己申请的机机账号名称
    */
    /*private static final String USER_PRINCIPAL = "JJtest01";*/
    
    /**
     * NewConsumer构造函数
     * @param topic 订阅的Topic名称
     */
    public MessageConsumer(String topic)
    {
		super("KafkaConsumerExample", false);
        Properties props = new Properties();
        
        KafkaProperties kafkaProc = KafkaProperties.getInstance();
        // Broker连接地址
        props.put(bootstrapServers,
            kafkaProc.getValues(bootstrapServers, "192.168.12.88:21007"));
        // Group id
        props.put(groupId, "DemoConsumer");
        // 是否自动提交offset
        props.put(enableAutoCommit, "true");
        // 自动提交offset的时间间隔
        props.put(autoCommitIntervalMs, "10");
        // 会话超时时间
        props.put(sessionTimeoutMs, "30000");
        // 消息Key值使用的反序列化类
        props.put(keyDeserializer,"org.apache.kafka.common.serialization.StringDeserializer");
        // 消息内容使用的反序列化类
        props.put(valueDeserializer,"com.chrtc.utils.MessageDeserializer");
        /*// 安全协议类型
        props.put(securityProtocol, kafkaProc.getValues(securityProtocol, "SASL_PLAINTEXT"));
        // 服务名
        props.put(saslKerberosServiceName, "kafka");
        // 域名
        props.put(kerberosDomainName, kafkaProc.getValues(kerberosDomainName, "hadoop.hadoop.com"));*/
        consumer = new KafkaConsumer<String, Message>(props);
        this.topic = "messagetopic";
    }
    
    /**
     * 订阅Topic的消息处理函数
     */
    public void doWork()
    {
        // 订阅
        consumer.subscribe(Collections.singletonList(this.topic));
        // 消息消费请求
        ConsumerRecords<String, Message> records = consumer.poll(3000);
        System.out.println(records.count());
        // 消息处理
        for (ConsumerRecord<String, Message> record : records)
        {
        	System.out.println("-----  1 ------");
            LOG.info("[NewConsumerExample], Received message: (" + record.key() + ", " + record.value().getDataSetName()+"   "+record.value().getMap().get("name")+ ") at offset " + record.offset());
            System.out.println("-----  2 ------");
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
        
        MessageConsumer consumerThread = new MessageConsumer(KafkaProperties.TOPIC);
        consumerThread.start();
        
    }
    
}
