package com.seaky.hamster.core.rpc.protocol;

public interface RequestConvertor<Req> {

  ProtocolRequestHeader parseProtocolHeader(Req req);

  ProtocolRequestBody parseProtocolBody(Req req, ProtocolRequestHeader header,
      Class<?>[] paramTypes);

  Req createRequest(ProtocolRequestHeader header, ProtocolRequestBody body);

}
