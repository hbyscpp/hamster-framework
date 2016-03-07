package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.RequestExtractor;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;

@SPI("hamster")
public class HamsterProtocolExtensionFactory
    implements ProtocolExtensionFactory<HamsterRequest, HamsterResponse> {
  private static HamsterResponseExtractor repextractor = new HamsterResponseExtractor();
  private static HamsterRequestExtractor extractor = new HamsterRequestExtractor();

  public HamsterServer createServer() {
    HamsterServer server = new HamsterServer();
    return server;
  }

  @Override
  public RequestExtractor<HamsterRequest> getRequestExtractor() {
    return extractor;
  }

  @Override
  public Class<HamsterResponse> getRspClass() {
    return HamsterResponse.class;
  }

  @Override
  public ResponseConvertor<HamsterResponse> getResponseConvertor() {
    return repextractor;
  }

  @Override
  public Class<HamsterRequest> getReqClass() {
    return HamsterRequest.class;
  }

  @Override
  public HamsterClient createClient() {
    return new HamsterClient();
  }

  @Override
  public String protocolName() {
    return "hamster";
  }
}
