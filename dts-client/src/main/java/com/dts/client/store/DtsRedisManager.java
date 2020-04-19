package com.dts.client.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.alibaba.fastjson.JSON;
import com.dts.client.util.HttpClientUtil;

import redis.clients.jedis.Jedis;

public class DtsRedisManager extends DtsManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	Environment env;
	Jedis jedis;

	public DtsRedisManager(Environment env) {
		this.env = env;
		initRedisTemplate();
	}

	@Override
	public void notifyRollback(String transId) {
		List<String> list = jedis.lrange("dts:unit:" + transId, 0, Integer.MAX_VALUE);
		if (list == null || list.size() == 0) {
			return;
		}
		for (String json : list) {
			DtsUnit du = JSON.parseObject(json, DtsUnit.class);
			Map<String, String> params = new HashMap<>();
			params.put("className", du.getClassName());
			params.put("methodName", du.getMethodName());
			params.put("undoData", du.getUndoData());
			logger.info("向参与方请求回滚：" + du.getUnit());
			HttpClientUtil.post(du.getClientHost() + "dts/rollback", params);
		}
	}

	@Override
	public void addUnit(String transId, DtsUnit du) {
		du.setTransId(transId);
		jedis.lpush("dts:unit:" + transId, JSON.toJSONString(du));
	}

	private void initRedisTemplate() {
		jedis = new Jedis(env.getProperty("dts.redis.host"), Integer.valueOf(env.getProperty("dts.redis.port")));
		String pwd = env.getProperty("dts.redis.password");
		if(StringUtils.isNotBlank(pwd))
			jedis.auth(pwd);
		jedis.select(Integer.valueOf(env.getProperty("dts.redis.database")));
		jedis.ping();
		logger.info("DTS加载redis完成");
	}

}
