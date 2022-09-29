package smzdm.schema;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.otter.canal.protocol.FlatMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FlatMessageSchema implements DeserializationSchema<FlatMessage>, SerializationSchema<FlatMessage> {
    @Override
    public FlatMessage deserialize(byte[] message) throws IOException {
        //System.out.println("------1-------------FlatMessageSchema message="+new String(message));
        FlatMessage flatMessage = new FlatMessage();
        Map map =(Map)JSON.parseObject(new String(message));
        try {
            BeanUtils.populate(flatMessage,map);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //System.out.println("-------2-------------FlatMessageSchema flatMessage="+flatMessage);
        return flatMessage;
    }

    @Override
    public boolean isEndOfStream(FlatMessage nextElement) {
        return false;
    }

    @Override
    public byte[] serialize(FlatMessage element) {
        return element.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public TypeInformation<FlatMessage> getProducedType() {
        return TypeInformation.of(new TypeHint<FlatMessage>() {});

    }
}
