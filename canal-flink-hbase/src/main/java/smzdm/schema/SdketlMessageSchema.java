package smzdm.schema;

import check.SdkExposureMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.FlatMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SdketlMessageSchema implements  DeserializationSchema<SdkExposureMessage>, SerializationSchema<SdkExposureMessage> {


    @Override
    public SdkExposureMessage deserialize(byte[] message) throws IOException {
        String jsonStr=new String(message);
        if(null != message && StringUtils.isNotBlank(jsonStr) && isjson(jsonStr)){
            //System.out.println("------1-------------SdkExposureMessage message="+new String(message));
            SdkExposureMessage sdkExposureMessage = new SdkExposureMessage();
            Map map = null;
            try {
                map = (Map) JSON.parseObject(jsonStr);
                if(map.containsKey(null) || map.containsKey("")){
                    //System.out.println("-----------fuck---------");
                    map.remove(null);
                    map.remove("");
                }
            } catch (Exception e) {
                //System.out.println("--------deserialize------error2 format-------message="+new String(message));
                e.printStackTrace();
            } finally {
                sdkExposureMessage.setDatamap(map);
                //System.out.println("-------2-------------SdketlMessageSchema sdkExposureMessage="+sdkExposureMessage.toString());
                return sdkExposureMessage;
            }

        }else{
            return new SdkExposureMessage();
        }

    }

    private static boolean isjson(String s4) {
        try {
            JSONObject jsonStr= JSONObject.parseObject(s4);
            return  true;
        } catch (Exception e) {
            return false;
        }
    }



    @Override
    public boolean isEndOfStream(SdkExposureMessage sdkExposureMessage) {
        return false;
    }

    @Override
    public byte[] serialize(SdkExposureMessage sdkExposureMessage) {
        if(null != sdkExposureMessage && null != sdkExposureMessage.getDatamap()){
            return sdkExposureMessage.toString().getBytes(StandardCharsets.UTF_8);
        }else{
            return null;
        }

    }

    @Override
    public TypeInformation<SdkExposureMessage> getProducedType() {
        return TypeInformation.of(new TypeHint<SdkExposureMessage>() {});
    }
}
