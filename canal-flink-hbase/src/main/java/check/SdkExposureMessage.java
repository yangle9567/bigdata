package check;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SdkExposureMessage {
    private String key;//用于去重的key
    private String articleid;//文章ID
    private String channel;//频道ID
    private String abtest;//流量
    private String is_repeat;//1：重复，0：未重复
    private String sys;//业务（1：曝光，2：详情点击，3：电商点击）
    private String event;//事件
    private String article_dim;//通过来源数据中的文章ID联查大宽表所得文章属性信息 json 转成格式
    private Map<String,String> datamap;//源头kafka中消费出来的数据键值对儿

    public String getArticle_dim() {
        return article_dim;
    }

    public void setArticle_dim(String article_dim) {
        this.article_dim = article_dim;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getArticleid() {
        return articleid;
    }

    public void setArticleid(String articleid) {
        this.articleid = articleid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAbtest() {
        return abtest;
    }

    public void setAbtest(String abtest) {
        this.abtest = abtest;
    }

    public String getIs_repeat() {
        return is_repeat;
    }

    public void setIs_repeat(String is_repeat) {
        this.is_repeat = is_repeat;
    }

    public String getSys() {
        return sys;
    }

    public void setSys(String sys) {
        this.sys = sys;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }



    public Map<String, String> getDatamap() {
        return datamap;
    }

    public void setDatamap(Map<String, String> datamap) {
        this.datamap = datamap;
    }

    @Override
    public String toString() {
        return "SdkExposureMessage{" +
                "key='" + key + '\'' +
                ", articleid='" + articleid + '\'' +
                ", channel='" + channel + '\'' +
                ", abtest='" + abtest + '\'' +
                ", is_repeat='" + is_repeat + '\'' +
                ", sys='" + sys + '\'' +
                ", event='" + event + '\'' +
                ", article_dim='" + article_dim + '\'' +
                ", datamap=" + datamap +
                '}';
    }

    public String toJsonString(SdkExposureMessage sdkExposureMessage) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //System.out.println("-----tojsonstr1---sdkExposureMessage="+sdkExposureMessage.toString());
        System.out.println("----tojsonstr---articleid="+sdkExposureMessage.getArticleid()+"----sys="+sdkExposureMessage.getSys()+"---event"+sdkExposureMessage.getEvent()+"---is_repeat="+sdkExposureMessage.getIs_repeat());
        Map datamap=sdkExposureMessage.getDatamap();
        //System.out.println("datamap="+datamap);

        HashMap<String,String> map = new HashMap<String,String>();
        map = (HashMap<String, String>) BeanUtils.describe(sdkExposureMessage);
        //System.out.println("map="+map);
        map.putAll(datamap);
        map.remove("datamap");
        map.remove("key");
        map.remove("id");
        map.remove("class");
        String json=JSON.toJSONString(map);

        //System.out.println("-----tojsonstr2----jsonString="+json);
        return json;
    }
}
