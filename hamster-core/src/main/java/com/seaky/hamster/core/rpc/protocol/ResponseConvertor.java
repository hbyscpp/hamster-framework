package com.seaky.hamster.core.rpc.protocol;

public interface ResponseConvertor<Rsp> {

  Response convertFrom(Rsp rsp);

  Rsp convertTo(Response rsp);
}
