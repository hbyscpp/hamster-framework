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

  RequestConvertor<Req> getRequestConvertor();

  ResponseConvertor<Rsp> getResponseConvertor();

  Class<Req> getReqClass();

  Class<Rsp> getRspClass();

  Client<Req, Rsp> createClient();

  Server<Req, Rsp> createServer();

  String protocolName();

  // 支持的最大协议版本
  short protocolMaxVersion();

  Req createHeartbeatRequest();

  Rsp createHeartbeatResponse();

  String defaultSerialization();

}
