package com.chrtc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import kafka.serializer.Decoder;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class MessageDeserializer implements Deserializer<Message> {
	private String encoding = "UTF8";

	public void configure(Map<String, ?> configs, boolean isKey) {
		// TODO Auto-generated method stub
		
	}

	public Message deserialize(String topic, byte[] data) {
		try {
			if (data == null)
				return null;
			else{

				ByteArrayInputStream os=new ByteArrayInputStream(data);
				ObjectInputStream oos=new ObjectInputStream(os);
				Message resultData= (Message)oos.readObject();
				return resultData;
			}
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException("Error when deserializing byte[] to string due to unsupported encoding null");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	
}
