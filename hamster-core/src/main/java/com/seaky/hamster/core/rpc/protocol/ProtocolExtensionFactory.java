package com.seaky.hamster.core.rpc.protocol;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.client.Client;
import com.seaky.hamster.core.rpc.server.Server;
/**
 * 
 * 协议扩展必须实现此类
 *  
 * @author seaky 
 * @version @param <Req> 协议请求的类
 * @version @param <Rsp> 协议响应的类
 * @since 1.0.0
 */
@SPI("hamster")
public interface ProtocolExtensionFactory<Req, Rsp> {

	RequestInfoExtractor<Req> getRequestInfoExtractor();

	ResponseInfoExtractor<Rsp> getResponsetInfoExtractor();
	
	ResponseConvertor<Req,Rsp> getResponseAttrWriter();
	
	RequestConvertor<Req> getRequestConvertor();
	
	Class<Req> getReqClass();

	Class<Rsp> getRspClass();
	
	Client<Req, Rsp> createClient();
	
	Server<Req,Rsp> createServer();
	
	String protocolName();
	
	Req createRequest();
	
	Rsp createResponse();
	
	
}
