package check;

import com.alibaba.otter.canal.protocol.FlatMessage;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.hadoop.hbase.HBaseConfiguration;
import smzdm.config.GlobalConfig;
import smzdm.sink.HbaseTemplate;
import smzdm.sink.MyHbaseSyncService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MyValueStateFunction extends RichMapFunction<FlatMessage, FlatMessage> {
    private transient ValueState<Set<String>> setValueState;
    MyHbaseSyncService myHbaseSyncService;

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<Set<String>> valuestate_articleid_tagid = new ValueStateDescriptor<>("valuestate_articleid_tagid", TypeInformation.of(new TypeHint<Set<String>>(){}));
        //查询文章对应的tagid初始化setValueState String article_id=parameters.
        setValueState = getRuntimeContext().getState(valuestate_articleid_tagid);
        org.apache.hadoop.conf.Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.set("hbase.zookeeper.quorum", GlobalConfig.HBASE_ZOOKEEPER_QUORUM);
        hbaseConfig.set("hbase.zookeeper.property.clientPort", GlobalConfig.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT);
        hbaseConfig.set("zookeeper.znode.parent", GlobalConfig.ZOOKEEPER_ZNODE_PARENT);
        HbaseTemplate hbaseTemplate = new HbaseTemplate(hbaseConfig);
        myHbaseSyncService = new MyHbaseSyncService(hbaseTemplate);
    }

    @Override
    public FlatMessage map(FlatMessage flatMessage) throws Exception {
        //System.out.println("---------------MyValueStateFunction------------------flatMessage="+flatMessage);
        if(null != flatMessage && flatMessage.getDatabase().equals("dbzdm_youhui") && flatMessage.getTable().equals("youhui_tag_type_item")){
            //System.out.println("1 flatMessage="+flatMessage.toString());
            String tag_id = flatMessage.getData().get(0).get("tag_id");
            String  article_id = flatMessage.getData().get(0).get("article_id");
            String id = flatMessage.getData().get(0).get("id");
            //System.out.println("article_id="+article_id);
            //System.out.println("tag_id="+tag_id);
            if(null == setValueState.value()){
                //System.out.println("---------------1 set is null--------------------");
                //程序启动时去hbase查询文章id对应的tagid
                String tagids_str= myHbaseSyncService.query(flatMessage);
                //System.out.println("tagids_str="+tagids_str);
                if(null != tagids_str){
                    //System.out.println("-------1------tagids_str=");
                    HashSet<String> set = new HashSet<>();
                    for (String tagid : tagids_str.replaceAll("\\[","").replaceAll("\\]","").split(",")) {
                        //System.out.println("tagid="+tagid);
                        set.add(tagid);
                    }
                    set.add(tag_id);
                    //System.out.println("-------2------tagids_str=");
                    for (String s : set) {
                        //System.out.println("s="+s);
                    }
                    setValueState.update(set);
                }else{
                    HashSet<String> set2 = new HashSet<>();
                    set2.add(tag_id);
                    setValueState.update(set2);
                }

                //System.out.println("setValueState="+setValueState.value().toString());
                //System.out.println("---------------2 set is null--------------------");
            }
            if(null != tag_id && flatMessage.getType().equals("INSERT")){ // || flatMessage.getType().equals("UPDATE"))){
                if(!setValueState.value().contains(tag_id)){
                    //System.out.println("------------------INSERT-----------------------"+setValueState.value().toString());
                    setValueState.value().add(tag_id);
                    //System.out.println("------------------INSERT-----------------------"+setValueState.value().toString());
                }
            }
            if(null != tag_id && flatMessage.getType().equals("UPDATE")){ // || flatMessage.getType().equals("UPDATE"))){
                HashSet<String> set = new HashSet<>();
                set.remove(getOlderTageid(flatMessage));
                set.add(tag_id);
                //System.out.println("------------------UPDATE-----------------------"+setValueState.value().toString());
                setValueState.update(set);
                //System.out.println("------------------UPDATE-----------------------"+setValueState.value().toString());
            }
            if(null != tag_id && flatMessage.getType().equals("DELETE")){
                //System.out.println("------------------DELETE-----------------------"+setValueState.value().toString());
                setValueState.value().remove(tag_id);
                //System.out.println("------------------DELETE-----------------------"+setValueState.value().toString());
            }
            //System.out.println("1 MyValueStateFunction map flatMessage="+flatMessage.toString());
            flatMessage.getData().get(0).put("tag_id",setValueState.value().toString().replaceAll("\\[","").replaceAll("\\]",""));
            //System.out.println("2 MyValueStateFunction map flatMessage="+flatMessage.toString());
            return flatMessage;
        }else{
            return flatMessage;
        }
    }

    private String getOlderTageid(FlatMessage flatMessage) {
        String olderTagid=flatMessage.getOld().get(0).get("tag_id");
        //System.out.println("olderTagid="+olderTagid);
        return olderTagid;
    }


}
