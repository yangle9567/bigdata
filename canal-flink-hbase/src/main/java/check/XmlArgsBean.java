package check;

import java.util.Map;

public class XmlArgsBean {
    private String topic;

    private String tongbutype;

    private Map<String, Map<String,String>> assembleMap;//外层map的key为库.表名,内层map的key为输入表的列名，内层map的value为对应的输出表的列名

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTongbutype() {
        return tongbutype;
    }

    public void setTongbutype(String tongbutype) {
        this.tongbutype = tongbutype;
    }

    public Map<String, Map<String, String>> getAssembleMap() {
        return assembleMap;
    }

    public void setAssembleMap(Map<String, Map<String, String>> assembleMap) {
        this.assembleMap = assembleMap;
    }

    @Override
    public String toString() {
        return "XmlArgsBean{" +
                "topic='" + topic + '\'' +
                ", tongbutype='" + tongbutype + '\'' +
                ", assembleMap=" + assembleMap +
                '}';
    }
}

