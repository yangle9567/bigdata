package check;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;

public class SdketlValueStateFunction extends RichMapFunction<SdkExposureMessage, SdkExposureMessage> {
    private transient ValueState<String> valueState;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ValueStateDescriptor<String> valuestate_articleid_tagid = new ValueStateDescriptor<>("valuestate_articleid_tagid", TypeInformation.of(new TypeHint<String>(){}));
        //查询文章对应的tagid初始化setValueState String article_id=parameters.
        valueState = getRuntimeContext().getState(valuestate_articleid_tagid);
    }

    @Override
    public SdkExposureMessage map(SdkExposureMessage sdkExposureMessage) throws Exception {
        if(null == valueState.value()){
            valueState.update("0");//1：重复，0：未重复
            sdkExposureMessage.setIs_repeat("0");
        }else{
            sdkExposureMessage.setIs_repeat("1");
        }
        return sdkExposureMessage;
    }

}
