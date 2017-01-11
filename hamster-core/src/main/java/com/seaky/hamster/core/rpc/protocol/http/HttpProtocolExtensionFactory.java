package com.seaky.hamster.core.rpc.protocol.http;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

@SPI("http")
public class HttpProtocolExtensionFactory
    implements ProtocolExtensionFactory<FullHttpRequest, FullHttpResponse> {
  private static HttpResponseConvertor repextractor = new HttpResponseConvertor();
  private static HttpRequestConvertor extractor = new HttpRequestConvertor();


  public HttpServer createServer() {
    HttpServer server = new HttpServer();
    return server;
  }

  @Override
  public RequestConvertor<FullHttpRequest> getRequestConvertor() {
    return extractor;
  }

  @Override
  public Class<FullHttpResponse> getRspClass() {
    return FullHttpResponse.class;
  }

  @Override
  public ResponseConvertor<FullHttpResponse> getResponseConvertor() {
    return repextractor;
  }

  @Override
  public Class<FullHttpRequest> getReqClass() {
    return FullHttpRequest.class;
  }

  @Override
  public HttpClient createClient() {
    return new HttpClient();
  }

  @Override
  public String protocolName() {
    return "http";
  }

  @Override
  public short protocolMaxVersion() {
    return 0;
  }

  @Override
  public FullHttpRequest createHeartbeatRequest() {
    return null;
  }

  @Override
  public FullHttpResponse createHeartbeatResponse() {
    return null;
  }

  @Override
  public String defaultSerialization() {
    return "msgpack";
  }

}
