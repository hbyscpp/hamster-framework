package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;

@SPI("hamster")
public class HamsterProtocolExtensionFactory
    implements ProtocolExtensionFactory<HamsterRequest, HamsterResponse> {
  private static HamsterResponseConvertor repextractor = new HamsterResponseConvertor();
  private static HamsterRequestConvertor extractor = new HamsterRequestConvertor();

  public HamsterServer createServer() {
    HamsterServer server = new HamsterServer();
    return server;
  }

  @Override
  public RequestConvertor<HamsterRequest> getRequestConvertor() {
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

  @Override
  public short protocolMaxVersion() {
    return 0;
  }

  @Override
  public HamsterRequest createHeartbeatRequest() {
    ProtocolRequestHeader header = new ProtocolRequestHeader();
    header.getAttachments().putByte(Constants.MSG_TYPE, Constants.MSG_HEARTBEAT_TYPE);
    header.getAttachments().putShort(Constants.PROTOCOL_VERSION_KEY, (short) 0);
    return extractor.createRequest(header, null);
  }

  @Override
  public HamsterResponse createHeartbeatResponse() {
    ProtocolResponseHeader header = new ProtocolResponseHeader();
    header.getAttachments().putByte(Constants.MSG_TYPE, Constants.MSG_HEARTBEAT_RSP_TYPE);
    header.getAttachments().putShort(Constants.PROTOCOL_VERSION_KEY, (short) 0);
    return repextractor.createResponse(header, null);
  }

  @Override
  public String defaultSerialization() {
    return "kryo";
  }



}
