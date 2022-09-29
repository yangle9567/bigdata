package check;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DOM4J 方式解析XML
 */
public class ParseXmlDom4jUtil {

    public static void main(String[] args) {

    }

    public static XmlArgsBean getxmlArgsBean(String xmlargspath) {
        // 解析books.xml文件
        // 创建SAXReader的对象reader
        XmlArgsBean xmlArgsBean = new XmlArgsBean();
        Map<String, Map<String, String>> mapout = new HashMap<>();


        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
            Document document = reader.read(new File(xmlargspath));
            // 通过document对象获取根节点bookstore
            Element root = document.getRootElement();
            xmlArgsBean.setTopic(root.element("tongbu").attributeValue("inputkafkatopic"));
            xmlArgsBean.setTongbutype(root.element("tongbu").attributeValue("type"));
            ////System.out.println(root.element("tongbu").element("inputdbtb").attributeValue("name"));
            List<Element> elements = root.element("tongbu").elements("inputdbtb");
            for (Element element : elements) {
                //System.out.println(element.attributeValue("name"));
                String inputdbtbname = element.attributeValue("name");
                Map<String,String> mapin = new HashMap<String,String>();
                List<Element> elements2 = element.element("inputtbcolus").elements("inputcolu");
                for (Element elem : elements2) {
                    String inputname = elem.attributeValue("inputname");
                    String outputname = elem.attributeValue("outputname");
                    if("" == outputname){
                        outputname=inputname;
                        mapin.put(inputname,outputname);
                    }else{
                        mapin.put(inputname,outputname);
                    }
                }
                mapout.put(inputdbtbname,mapin);
            }
            xmlArgsBean.setAssembleMap(mapout);
            //System.out.println("mapout="+mapout);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return xmlArgsBean;
    }

    private static void getNodes(Element element) {
        //当前节点的名称、文本内容和属性
        //System.out.println("当前节点名称："+element.getName());//当前节点名称
        //System.out.println("当前节点的内容："+element.getTextTrim());//当前节点名称
        List<Attribute> listAttr=element.attributes();//当前节点的所有属性的list
        for(Attribute attr:listAttr){//遍历当前节点的所有属性
            String name=attr.getName();//属性名称
            String value=attr.getValue();//属性的值
            //System.out.println("属性名称："+name+"属性值："+value);
        }

        //递归遍历当前节点所有的子节点
        List<Element> listElement=element.elements();//所有一级子节点的list
        for(Element e:listElement){//遍历所有一级子节点
            getNodes(e);//递归
        }

    }


}
