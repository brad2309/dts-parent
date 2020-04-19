package com.dts.client.trans;

import org.springframework.stereotype.Component;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class DtsFeignInterceptor implements RequestInterceptor{
	

	@Override
	public void apply(RequestTemplate template) {
		String transId = DtsLocalContext.TRANS_ID.get();
		template.header("DTS_TRANS_ID", transId);
	}

}
