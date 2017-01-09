package com.seaky.hamster.core.rpc.protocol.http;

import com.seaky.hamster.core.rpc.client.NettyClient;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class HttpClient extends NettyClient<FullHttpRequest, FullHttpResponse> {

  @SuppressWarnings("unchecked")
  public HttpClient() {
    super(ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension("http"), configurator);
  }

  private static HttpClientChannelPipelineConfigurator configurator =
      new HttpClientChannelPipelineConfigurator();

}
