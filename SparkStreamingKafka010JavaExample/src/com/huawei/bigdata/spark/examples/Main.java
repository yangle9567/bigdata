package com.huawei.bigdata.spark.examples;

import java.util.*;

import scala.Tuple2;

import org.apache.hadoop.conf.Configuration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;

import com.huawei.hadoop.security.LoginUtil;

/**
 * Consumes messages from one or more topics in Kafka.
 * <checkPointDir> is the Spark Streaming checkpoint directory.
 * <brokers> is for bootstrapping and the producer will only use it for getting metadata
 * <topics> is a list of one or more kafka topics to consume from
 * <batchTime> is the Spark Streaming batch duration in seconds.
 */
public class Main
{
  public static void main(String[] args) throws Exception {// 本地运行时参数C:\\tmp localhost:9092 messagetopic 100
    JavaStreamingContext ssc = createContext(args);

    //The Streaming system starts.
    ssc.start();
    try {
      ssc.awaitTermination();
    } catch (InterruptedException e) {
    }
  }

  private static JavaStreamingContext createContext(String[] args) throws Exception {
    /*String userPrincipal = "JJtest04";
    String userKeytabPath = "D://workspace5//SparkStreamingKafka010JavaExample//conf//user.keytab";
    String krb5ConfPath = "D://workspace5//SparkStreamingKafka010JavaExample//conf//krb5.conf";
    String userKeytabPath = "/root/conf/user.keytab";
    String krb5ConfPath = "/root/conf/krb5.conf";

    Configuration hadoopConf = new Configuration();
    LoginUtil.login(userPrincipal, userKeytabPath, krb5ConfPath, hadoopConf);*/
    //LoginUtil.setJaasConf("KafkaClient", userPrincipal, userKeytabPath);
	System.setProperty("hadoop.home.dir", "D://hadoop-2.7.3//hadoop-2.7.3");
 
    String checkPointDir = args[0];
    String brokers = args[1];
    String topics = args[2];
    String batchSize = args[3];

    SparkConf sparkConf = new SparkConf().setMaster("local[3]").setAppName("KafkaWordCount");
    JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, new Duration(Long.parseLong(batchSize) * 1));
    ssc.checkpoint(checkPointDir);
    
    String[] topicArr = topics.split(",");
    Set<String> topicSet = new HashSet<String>(Arrays.asList(topicArr));
    Map<String, Object> kafkaParams = new HashMap();
    kafkaParams.put("bootstrap.servers", brokers);
    kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    kafkaParams.put("group.id", "groupId12");

    LocationStrategy locationStrategy = LocationStrategies.PreferConsistent();
    ConsumerStrategy consumerStrategy = ConsumerStrategies.Subscribe(topicSet, kafkaParams);

    JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(ssc, locationStrategy, consumerStrategy);

    JavaDStream<String> lines = messages.map(new Function<ConsumerRecord<String, String>, String>() {
      public String call(ConsumerRecord<String, String> tuple2) throws Exception {
        return tuple2.value();
      }
    });

    
    // print the results
    lines.print();
    return ssc;
  }
}