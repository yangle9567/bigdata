package check;

import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import smzdm.config.GlobalConfig;
import smzdm.sink.HbaseTemplate;
import smzdm.sink.MyHbaseSyncService;

import java.util.Date;

public class SdketlValueStateFunction2 extends RichMapFunction<SdkExposureMessage, SdkExposureMessage> {
    private transient ValueState<String> valueState;
    private Date date1=null;//定时更新联查大宽表信息的时间标记
    MyHbaseSyncService myHbaseSyncService;
    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<String> valuestate_articleid_tagid = new ValueStateDescriptor<>("valuestate_articleid_tagid", TypeInformation.of(new TypeHint<String>(){}));
        //查询文章对应的tagid初始化setValueState String article_id=parameters.
        valueState = getRuntimeContext().getState(valuestate_articleid_tagid);
        org.apache.hadoop.conf.Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.set("hbase.zookeeper.quorum", GlobalConfig.HBASE_ZOOKEEPER_QUORUM);
        hbaseConfig.set("hbase.zookeeper.property.clientPort", GlobalConfig.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT);
        hbaseConfig.set("zookeeper.znode.parent", GlobalConfig.ZOOKEEPER_ZNODE_PARENT);
        HbaseTemplate hbaseTemplate = new HbaseTemplate(hbaseConfig);
        myHbaseSyncService = new MyHbaseSyncService(hbaseTemplate);
        date1=new Date();
    }

    @Override
    public SdkExposureMessage map(SdkExposureMessage sdkExposureMessage) throws Exception {
        //System.out.println("-------1--------MyValueStateFunction2------------------sdkExposureMessage="+sdkExposureMessage);
        if(null == valueState.value()){
            //System.out.println("--------valueState.value() is null------");
            String articleid=sdkExposureMessage.getArticleid();
            if(null != articleid && StringUtils.isNotBlank(articleid)){
                String bigwidetableinfo = myHbaseSyncService.queryBigwidetable(articleid);
                //System.out.println("--------articleid="+articleid+" bigwidetableinfo="+bigwidetableinfo);
                sdkExposureMessage.setArticle_dim(bigwidetableinfo);
                valueState.update(bigwidetableinfo);
            }
        }else{
            //System.out.println("--------valueState.value() is not null------");
            Date date2=new Date();
            if(differentDaysByMillisecond(date1,date2)>1){
                System.out.println("----before--valueState.value()--"+valueState.value().toString());
                valueState.update(null);//清空之前的联查结果
                System.out.println("----------query again------------");
                String articleid=sdkExposureMessage.getArticleid();
                if(null != articleid && StringUtils.isNotBlank(articleid)){
                    String bigwidetableinfo = myHbaseSyncService.queryBigwidetable(articleid);
                    //System.out.println("--------articleid="+articleid+" bigwidetableinfo="+bigwidetableinfo);
                    sdkExposureMessage.setArticle_dim(bigwidetableinfo);
                    valueState.update(bigwidetableinfo);
                }
                date1=date2;//更新时间标记
                System.out.println("----after-query-again-valueState.value()--"+valueState.value().toString());

            }else{//如果时间差小于等于一天就复用之前联查回来的大宽表信息
                System.out.println("----------copy use------------");
                String bigwidetableinfo = valueState.value();
                sdkExposureMessage.setArticle_dim(bigwidetableinfo);
            }

        }
        //System.out.println("-------2--------MyValueStateFunction2------------------sdkExposureMessage="+sdkExposureMessage);
        return sdkExposureMessage;
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔（天）
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1,Date date2)
    {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
        return days;
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔（分钟）
     * @param date1
     * @param date2
     * @return
     */
    public static int differentSecondsByMillisecond(Date date1,Date date2)
    {
        int seconds = (int) ((date2.getTime() - date1.getTime()) / (1000*60));
        return seconds;
    }

}
