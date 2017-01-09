package com.seaky.hamster.core.rpc.protocol;

public interface ResponseConvertor<Rsp> {

  ProtocolResponseHeader parseHeader(Rsp rsp);

  ProtocolResponseBody parseBody(Rsp rsp, ProtocolResponseHeader header, Class<?> type);

  Rsp createResponse(ProtocolResponseHeader header, ProtocolResponseBody body);
}
