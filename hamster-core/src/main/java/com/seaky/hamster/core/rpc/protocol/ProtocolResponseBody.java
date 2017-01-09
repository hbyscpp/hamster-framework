package com.seaky.hamster.core.rpc.protocol;

public class ProtocolResponseBody {

  private static Object NOT_SET_OBJ = new Object();

  // 客户端使用这个值,请不要随意改变
  private Object result = NOT_SET_OBJ;


  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }


  public boolean isDone() {
    return result != NOT_SET_OBJ;
  }
}
