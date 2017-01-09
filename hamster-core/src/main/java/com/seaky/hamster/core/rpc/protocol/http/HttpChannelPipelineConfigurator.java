package com.seaky.hamster.core.rpc.protocol.http;

import com.seaky.hamster.core.rpc.server.NettyServerHandler;
import com.seaky.hamster.core.rpc.server.NettyServerTransport;
import com.seaky.hamster.core.rpc.server.ServerChannelPipelineConfigurator;
import com.seaky.hamster.core.rpc.server.ServerConfig;
import com.seaky.hamster.core.rpc.server.ServerResourceManager;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

public class HttpChannelPipelineConfigurator implements
    ServerChannelPipelineConfigurator<FullHttpRequest, FullHttpResponse, NettyServerTransport<FullHttpRequest, FullHttpResponse>> {

  @Override
  public void config(NettyServerTransport<FullHttpRequest, FullHttpResponse> transport,
      ChannelPipeline pipeline, ServerConfig config) {
    pipeline.addLast(new HttpServerCodec());
    pipeline.addLast(new HttpObjectAggregator(1048576));
    pipeline.addLast(new IdleStateHandler(config.getIdleCloseTime(), 0, 0));
    pipeline.addLast(ServerResourceManager.getDispatcherThreadPool(),
        new NettyServerHandler<FullHttpRequest, FullHttpResponse>(transport));
  }

}
