package com.dts.c;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableEurekaClient
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages="com.dts")
@EnableFeignClients(basePackages="com.dts")
public class DtsCApplication {

	public static void main(String[] args) {
		System.out.println("--------------------------开始启动!--------------------------");
		ApplicationContext ac = SpringApplication.run(DtsCApplication.class,args);
		System.out.println("--------------------------启动成功!--------------------------"+ac.getEnvironment().getProperty("server.port"));
	}
	
}
