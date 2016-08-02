package com.seaky.hamster.core.test;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.exception.RpcException;
import com.seaky.hamster.core.rpc.protocol.DefaultExceptionConvertor;
import com.seaky.hamster.core.rpc.protocol.ExceptionResult;

@SPI("mytestxception")
public class TestExceptionConvertor extends DefaultExceptionConvertor {



  public static class YtExceptionData {

    private Errorcode errorcode;

    private String extraInfo;

    public String getExtraInfo() {
      return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
      this.extraInfo = extraInfo;
    }

    public Errorcode getErrorcode() {
      return errorcode;
    }

    public void setErrorcode(Errorcode errorcode) {
      this.errorcode = errorcode;
    }
  }

  @Override
  public ExceptionResult convertTo(RpcException e) {

    if (e.getCause() != null) {
      Throwable inner = e.getCause();
      if (inner instanceof YtException) {
        ExceptionResult result = super.convertTo(e, inner);
        YtExceptionData data = new YtExceptionData();
        data.setErrorcode(((YtException) inner).getErrorcode());
        data.setExtraInfo(((YtException) inner).getExtraInfo());
        result.setData(data);
        return result;
      }
    }

    return super.convertTo(e);
  }

  @Override
  public RpcException convertFrom(ExceptionResult result) {

    if (result.getData() != null) {

      if (result.getData() instanceof YtExceptionData) {

        YtException exp = new YtException(((YtExceptionData) result.getData()).getErrorcode(),
            ((YtExceptionData) result.getData()).getExtraInfo(), result.getMsg());
        RpcException e = new RpcException(result.getCode(), result.getMsg(), exp);
        return e;
      }
    }

    return super.convertFrom(result);
  }

}
