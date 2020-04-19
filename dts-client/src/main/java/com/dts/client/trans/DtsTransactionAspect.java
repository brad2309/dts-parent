package com.dts.client.trans;

import java.lang.reflect.Method;
import java.net.InetAddress;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dts.client.anno.DTSTransaction;
import com.dts.client.store.DtsManager;
import com.dts.client.store.DtsRedisManager;
import com.dts.client.store.DtsUnit;
import com.dts.client.util.DtsException;

@Aspect
@Component
@Order(1)
public class DtsTransactionAspect {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	String clientHost;
	String unit;
	String managerType;
	DtsManager tm;
	
	@Autowired
	Environment env;
	
	@PostConstruct
	public void init() throws Exception{
		managerType = env.getProperty("dts.managerType");
		clientHost = env.getProperty("dts.client.host");
		unit = env.getProperty("dts.client.unit");
		if(StringUtils.isBlank(managerType)){
			managerType = "redis";
		}
		if(managerType.equals("redis")){
			tm = new DtsRedisManager(env);
		}else{
			throw new DtsException("store type配置错误");
		}
		if(StringUtils.isBlank(clientHost)){
			clientHost = InetAddress.getLocalHost().getHostAddress();
			clientHost = "http://"+clientHost+":"+env.getProperty("server.port")+"/";
		}
		if(StringUtils.isBlank(unit)){
			unit = env.getProperty("spring.application.name");
		}
		logger.info("[dts]事务管理器类型:"+managerType);
		logger.info("[dts]本地回调地址:"+clientHost);
	}
	
	
	
	@Pointcut("@annotation(com.dts.client.anno.DTSTransaction)")
    public void dtsTransactionPointcut() {}
	
    @Around("dtsTransactionPointcut()")
    public Object transactionRunning(ProceedingJoinPoint point) throws Throwable {
    	Method method = ((MethodSignature) point.getSignature()).getMethod();
    	DTSTransaction dts = method.getAnnotation(DTSTransaction.class);
    	if(dts.isStart()){
        	return wrap4Start(point);
    	}else{
        	return wrap4Unit(point);
    	}
    	
    }
    
    public Object wrap4Start(ProceedingJoinPoint point) throws Throwable{
    	logger.info("发起方事务开始");
		String transId = DtsLocalContext.TRANS_ID.get();
		if(transId!=null){
			throw new DtsException("事务发起方已存在事务ID");
		}
		transId = tm.generateTransId();
		DtsLocalContext.TRANS_ID.set(transId);
		try{
			Object obj = point.proceed();
			logger.info("发起方事务结束无异常");
			return obj;
		}catch (Exception e) {
			logger.info("发起方异常，将通知参与方回滚，"+e.getMessage());
			tm.notifyRollback(transId);
			throw e;
		}finally {
			DtsLocalContext.TRANS_ID.remove();
		}
    }
    
    public Object wrap4Unit(ProceedingJoinPoint point) throws Throwable{
    	logger.info("参与方事务开始");
    	Method method = ((MethodSignature) point.getSignature()).getMethod();
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String transId = request.getHeader("DTS_TRANS_ID");//获取事务id
		if(transId==null){
			//TODO
			throw new DtsException("事务参与方不存在事务ID");
		}
		
		Object obj;
		String undoData;
		try{
			obj = point.proceed();
			undoData = DtsLocalContext.UNDO_DATA.get();//获取回滚数据
		}finally {
			DtsLocalContext.UNDO_DATA.remove();
		}
		
		DtsUnit du = new DtsUnit();
		du.setUnit(unit);
		du.setClassName(method.getDeclaringClass().getName());
		du.setMethodName(method.getName());
		du.setClientHost(clientHost);
		du.setUndoData(undoData);
		tm.addUnit(transId, du);
		logger.info("参与方事务结束无异常");
		return obj;
    }

}
