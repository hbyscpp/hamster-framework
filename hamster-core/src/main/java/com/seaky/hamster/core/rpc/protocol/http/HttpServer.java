package com.seaky.hamster.core.rpc.protocol.http;

import com.seaky.hamster.core.rpc.server.Netty4Server;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class HttpServer extends Netty4Server<FullHttpRequest, FullHttpResponse> {
  private static HttpChannelPipelineConfigurator httpChannelPipelineConfigurator =
      new HttpChannelPipelineConfigurator();

  public HttpServer() {
    super(httpChannelPipelineConfigurator,
        ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension("http"));
  }

}
