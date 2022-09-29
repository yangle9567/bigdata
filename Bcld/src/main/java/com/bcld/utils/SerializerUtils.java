package com.bcld.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SerializerUtils {

    private static Logger log = LoggerFactory.getLogger(SerializerUtils.class);

    public static String writeObjectToString(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
        } catch (Exception e) {
            log.error("Object转为String时出错", e);
        }
        return Base64.encodeBase64String(baos.toByteArray());
    }

    public static <T> T readObjectFromString(String string) {
        Object object = null;
        try {
            byte[] data = Base64.decodeBase64(string);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            object = ois.readObject();
            ois.close();
        } catch (Exception e) {
            log.error("String转为Object时出错", e);
        }
        return (T) object;
    }

    public static File writeObjectToFile(Object object, File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
            oos.writeObject(object);
            oos.close();
        } catch (Exception e) {
            log.error("Object转为String时出错", e);
        }
        return file;
    }

    public static Object readObjectFromFile(File file) {
        Object object = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fileInputStream);
            object = ois.readObject();
            ois.close();
        } catch (Exception e) {
            log.error("String转为Object时出错", e);
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneObject(T object) {
        return SerializerUtils.<T> readObjectFromString(SerializerUtils.writeObjectToString(object));
    }

    public static String writeObjectToXml(Object object) {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias(ClassUtils.getShortClassName(object.getClass()), object.getClass());
        return xstream.toXML(object);
    }

    public static <T> T readObjectFromXml(String xml, Class type) {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias(ClassUtils.getShortClassName(type), type);
        return (T) xstream.fromXML(xml);
    }
}
