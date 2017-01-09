package com.seaky.hamster.core.rpc.protocol.http;

import com.seaky.hamster.core.rpc.client.ClientChannelPipelineConfigurator;
import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.client.Netty4ClientHandler;
import com.seaky.hamster.core.rpc.client.NettyClientTransport;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class HttpClientChannelPipelineConfigurator implements
    ClientChannelPipelineConfigurator<FullHttpRequest, FullHttpResponse, NettyClientTransport<FullHttpRequest, FullHttpResponse>> {

  @SuppressWarnings("unchecked")
  @Override
  public void config(NettyClientTransport<FullHttpRequest, FullHttpResponse> transport,
      ChannelPipeline pipeline, ClientConfig config) {
    pipeline.addLast(new HttpClientCodec());
    pipeline.addLast(new ChunkedWriteHandler());
    pipeline.addLast(new HttpObjectAggregator(1048576));
    pipeline
        .addLast(new IdleStateHandler(config.getHeartbeatIdleTime() + config.getReadTimeout() + 1,
            config.getHeartbeatIdleTime(), 0));
    pipeline.addLast(new Netty4ClientHandler<FullHttpRequest, FullHttpResponse>(transport,
        ExtensionLoaderConstants.PROTOCOLFACTORY_EXTENSION.findExtension("http")));

  }


}
