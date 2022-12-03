package com.chrtc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.avro.io.Decoder;
import org.apache.avro.util.Utf8;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class MapDescode  extends Decoder {
	private String encoding = "UTF8";

	public void configure(Map<String, ?> configs, boolean isKey) {
		String propertyName = isKey ? "key.deserializer.encoding" : "value.deserializer.encoding";
		Object encodingValue = configs.get(propertyName);
		if (encodingValue == null)
			encodingValue = configs.get("deserializer.encoding");
		if (encodingValue != null && encodingValue instanceof String)
			encoding = (String) encodingValue;
	}

	public Map deserialize(String topic, byte[] data) {
		try {
			if (data == null)
				return null;
			else{

				ByteArrayInputStream os=new ByteArrayInputStream(data);
				ObjectInputStream oos=new ObjectInputStream(os);
				Map resultData= (Map)oos.readObject();

				return resultData;
			}
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException("Error when deserializing byte[] to string due to unsupported encoding " + encoding);
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
		// nothing to do
	}

	@Override
	public long arrayNext() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long mapNext() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readArrayStart() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean readBoolean() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ByteBuffer readBytes(ByteBuffer arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double readDouble() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readEnum() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void readFixed(byte[] arg0, int arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float readFloat() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readIndex() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readInt() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readLong() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readMapStart() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void readNull() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String readString() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Utf8 readString(Utf8 arg0) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long skipArray() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void skipBytes() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skipFixed(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long skipMap() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void skipString() throws IOException {
		// TODO Auto-generated method stub
		
	}
}
