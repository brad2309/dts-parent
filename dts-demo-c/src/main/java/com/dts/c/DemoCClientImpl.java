package com.dts.c;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dts.client.anno.DTSTransaction;
import com.dts.client.trans.DtsLocalContext;
import com.dts.comm.demo.DemoCClient;

@RestController
public class DemoCClientImpl implements DemoCClient{

	@Autowired
	private UserAccountDAO userAccountDAO;
	@Autowired
	private UserLogDAO userLogDAO;
	

	@DTSTransaction
	@Transactional
	public Integer addUser(Integer userId, Integer total, String log) {
		UserAccount ua = new UserAccount();
		ua.setUserId(userId);
		ua.setTotal(total);
		userAccountDAO.insert(ua);
		UserLog ul = new UserLog();
		ul.setUserId(userId);
		ul.setLog(log);
		userLogDAO.insert(ul);
		JSONObject jo = new JSONObject();
		jo.put("userAccountId", ua.getId());
		jo.put("userLogId", ul.getId());
		DtsLocalContext.UNDO_DATA.set(jo.toJSONString());
		return null;
	}
	
	public void addUserRollback(String undoData){
		JSONObject jo = JSON.parseObject(undoData);
		userAccountDAO.delete(jo.getInteger("userAccountId"));
		userLogDAO.delete(jo.getInteger("userLogId"));
	}

}
