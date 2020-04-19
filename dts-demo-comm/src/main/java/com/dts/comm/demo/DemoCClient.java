package com.dts.comm.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value="dts-demo-c")
public interface DemoCClient {


	@RequestMapping("addUser")
	Integer addUser(
			@RequestParam("userId")Integer userId,@RequestParam("total")Integer total,
			@RequestParam("log")String log);
	
}
