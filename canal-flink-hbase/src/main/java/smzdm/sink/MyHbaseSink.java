package smzdm.sink;

import com.alibaba.otter.canal.protocol.FlatMessage;
import smzdm.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.hadoop.hbase.HBaseConfiguration;


@Slf4j
public class MyHbaseSink extends RichSinkFunction<FlatMessage> {

    private MyHbaseSyncService myHbaseSyncService;
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        org.apache.hadoop.conf.Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.set("hbase.zookeeper.quorum", GlobalConfig.HBASE_ZOOKEEPER_QUORUM);
        hbaseConfig.set("hbase.zookeeper.property.clientPort", GlobalConfig.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT);
        hbaseConfig.set("zookeeper.znode.parent", GlobalConfig.ZOOKEEPER_ZNODE_PARENT);
        HbaseTemplate hbaseTemplate = new HbaseTemplate(hbaseConfig);
        myHbaseSyncService = new MyHbaseSyncService(hbaseTemplate);
    }

    @Override
    public void invoke(FlatMessage value, Context context) throws Exception {
        myHbaseSyncService.sync(value);
    }
    @Override
    public void close() throws Exception {
        super.close();
    }


}
