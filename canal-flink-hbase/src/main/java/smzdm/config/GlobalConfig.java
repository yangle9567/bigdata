package smzdm.config;

import java.io.Serializable;

/**
 * 在生产上一般通过配置中心来管理
 */
public class GlobalConfig implements Serializable {
    /**
     * 数据库driver class
     */
    public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
    /**
     * 数据库jdbc url
     */
    public static final String DB_URL = "jdbc:mysql://10.42.132.50:3306/bi_test?useUnicode=true&characterEncoding=utf8";
    /**
     * 数据库user name
     */
    public static final String USER_MAME = "canal";
    /**
     * 数据库password
     */
    public static final String PASSWORD = "canal";
    /**
     * 批量提交size
     */
    public static final int BATCH_SIZE = 2;

    //HBase相关配置
    public static final String HBASE_ZOOKEEPER_QUORUM = "10.45.4.145,10.45.4.1,10.45.3.107";
    public static final String HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT = "2181";
    public static final String ZOOKEEPER_ZNODE_PARENT = "/hbase";
    public static String HBASE_TABLE_NAME = "dbzdm_youhui.youhui";
}
