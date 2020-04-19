package com.dts.client.store;

import java.util.Date;

public class DtsUnit {

	
	private Integer id;
	private String transId;
	private String unit;
	private String clientHost;
	private String undoData;
	private String className;
	private String methodName;
	private Date createTime;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTransId() {
		return transId;
	}
	public void setTransId(String transId) {
		this.transId = transId;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getClientHost() {
		return clientHost;
	}
	public void setClientHost(String clientHost) {
		this.clientHost = clientHost;
	}
	public String getUndoData() {
		return undoData;
	}
	public void setUndoData(String undoData) {
		this.undoData = undoData;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}	
	
}
