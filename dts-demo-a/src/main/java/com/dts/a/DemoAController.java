package com.dts.a;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dts.client.anno.DTSTransaction;
import com.dts.comm.demo.DemoBClient;
import com.dts.comm.demo.DemoCClient;

@RestController
public class DemoAController {

	@Autowired
	private DemoBClient demoBClient;
	@Autowired
	private DemoCClient demoCClient;
	
	@DTSTransaction(isStart=true)
	@RequestMapping("addUser")
	public String addUser(){
		Integer userId = demoBClient.addUser("tom", "111", "tom", 1);
		demoCClient.addUser(userId, 80000, "新增用户");
//		System.out.println(1/0);
		return "ok";
	}
	
}
