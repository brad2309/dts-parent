package com.dts.b;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dts.client.anno.DTSTransaction;
import com.dts.client.trans.DtsLocalContext;
import com.dts.comm.demo.DemoBClient;

@RestController
public class DemoBClientImpl implements DemoBClient{
	
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private UserExtDAO userExtDAO;

	@DTSTransaction
	@Transactional
	public Integer addUser(String username, String password,String nickname,Integer gendar) {
		User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		u.setCreateTime(new Date());
		userDAO.insert(u);
		UserExt ue = new UserExt();
		ue.setUserId(u.getId());
		ue.setGendar(gendar);
		ue.setNickname(nickname);
		userExtDAO.insert(ue);
		JSONObject jo = new JSONObject();
		jo.put("userId", u.getId());
		jo.put("userExtId", ue.getId());
		DtsLocalContext.UNDO_DATA.set(jo.toJSONString());
		return u.getId();
	}
	
	public void addUserRollback(String undoData){
		JSONObject jo = JSON.parseObject(undoData);
		userDAO.delete(jo.getInteger("userId"));
		userExtDAO.delete(jo.getInteger("userExtId"));
	}

}
