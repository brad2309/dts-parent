package com.dts.client.store;

import java.util.UUID;

public abstract class DtsManager {

	
	public String generateTransId(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	public abstract void notifyRollback(String transId);
	public abstract void addUnit(String transId,DtsUnit du);
	
}
