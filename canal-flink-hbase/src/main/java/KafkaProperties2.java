public class KafkaProperties2 {
    public static final String INTOPIC = "legendtopic";
    //public static final String OUTTOPIC = "topic2";
    public static final String KAFKA_SERVER_URL = "hadoop001:6667,hadoop002:6667,hadoop003:6667";
    public static final int KAFKA_SERVER_PORT0 = 6667;
    public static final int KAFKA_PRODUCER_BUFFER_SIZE = 10;
    public static final int CONNECTION_TIMEOUT = 100000;
    public static final String CLIENT_ID = "SimpleConsumerDemoClient";

    private KafkaProperties2() {}

}
