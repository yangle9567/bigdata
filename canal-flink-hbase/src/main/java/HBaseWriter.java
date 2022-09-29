import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * 写入HBase
 * 继承RichSinkFunction重写父类方法
 *
 * 写入hbase时500条flush一次, 批量插入, 使用的是writeBufferSize
 */
class HBaseWriter extends RichSinkFunction<String> {
    private static final Logger logger = LoggerFactory.getLogger(HBaseWriter.class);

    private static org.apache.hadoop.conf.Configuration configuration;
    private static Connection connection = null;
    private static BufferedMutator mutator;
    private static int count = 0;
    private static int i = 0;

    @Override
    public void open(Configuration parameters) throws Exception {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "hadoop001,hadoop002,hadoop003");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("zookeeper.znode.parent","/hbase-unsecure");
        getRuntimeContext().getUserCodeClassLoader();

        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf("test_yl"));
        params.writeBufferSize(2 * 1024 * 1024);
        mutator = connection.getBufferedMutator(params);
    }

    @Override
    public void close() throws IOException {
        if (mutator != null) {
            mutator.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void invoke(String values, Context context) throws Exception {

        //System.out.println("---invokestart--1--- "+values+" "+values);
        //System.out.println("UUID.randomUUID().hashCode()="+UUID.randomUUID().hashCode());
        String rk = "row"+i;
        Put put = new Put(rk.getBytes());
        i++;
        put.addColumn("c1".getBytes(), "user_name".getBytes(),values.getBytes());

        //System.out.println("---invokend--2--- "+values+" "+values);
        mutator.mutate(put);
        //每满500条刷新一下数据
        if (count >= 5) {
            mutator.flush();
            count = 0;
        }
        count = count + 1;
    }
}