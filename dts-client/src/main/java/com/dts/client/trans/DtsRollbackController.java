package com.dts.client.trans;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("dts")
@RestController
public class DtsRollbackController implements ApplicationContextAware{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ApplicationContext context;

	
	@RequestMapping("rollback")
	public void rollback(String className,String methodName,String undoData) throws Exception{
		logger.info("开始回滚:"+undoData);
		Class<?> cls = Class.forName(className);
		Object target = context.getBean(cls);
		Method method = cls.getMethod(methodName+"Rollback", String.class);
		method.invoke(target, undoData);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
	
}
