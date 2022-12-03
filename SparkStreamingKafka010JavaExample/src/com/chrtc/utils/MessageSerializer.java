package com.chrtc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class MessageSerializer implements Serializer<Message> {
	
	private String encoding = "UTF8";

	

	public void configure(Map<String, ?> configs, boolean isKey) {
        String propertyName = isKey ? "key.serializer.encoding" : "value.serializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if (encodingValue == null)
            encodingValue = configs.get("serializer.encoding");
        if (encodingValue != null && encodingValue instanceof String)
            encoding = (String) encodingValue;
    }

	public byte[] serialize(String topic, Message data) {
		// TODO Auto-generated method stub
		try {
            if (data == null)
                return null;
            else{
            	byte []bt=null;
		    	ByteArrayOutputStream os=new ByteArrayOutputStream();
		        ObjectOutputStream oos=new ObjectOutputStream(os);
		        oos.writeObject(data);
		        bt=os.toByteArray();
                return bt;
            }
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException("Error when serializing string to byte[] due to unsupported encoding " + encoding);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
	
	
	
	public void close() {
		// TODO Auto-generated method stub
		
	}


	
	}
