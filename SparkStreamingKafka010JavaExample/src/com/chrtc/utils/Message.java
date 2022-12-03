package com.chrtc.utils;

import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable{
	private String dataSetName;//数据集名称
	
	private Map map;//数据集内列名与列值映射关系

	public String getDataSetName() {
		return dataSetName;
	}

	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}
	

}
