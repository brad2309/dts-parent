package com.dts.client.trans;

public class DtsLocalContext {

	
	public final static ThreadLocal<String> TRANS_ID = new ThreadLocal<>();

	public final static ThreadLocal<String> UNDO_DATA = new ThreadLocal<>();
	
}
