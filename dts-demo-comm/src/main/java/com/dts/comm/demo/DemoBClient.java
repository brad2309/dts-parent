package com.dts.comm.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value="dts-demo-b")
public interface DemoBClient {

	@RequestMapping("addUser")
	Integer addUser(
			@RequestParam("username")String username,@RequestParam("password")String password,
			@RequestParam("nickname")String nickname,@RequestParam("gendar")Integer gendar);
	
}
