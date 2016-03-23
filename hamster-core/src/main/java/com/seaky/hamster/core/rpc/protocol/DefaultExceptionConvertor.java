package com.seaky.hamster.core.rpc.protocol;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.exception.RpcException;

/**
 * @Description 异常转换
 * @author seaky
 * @since 1.0.0
 * @Date Mar 23, 2016
 */
@SPI("default")
public class DefaultExceptionConvertor implements ExceptionConvertor {
  @Override
  public ExceptionResult convertTo(RpcException e) {
    ExceptionResult res = new ExceptionResult();
    res.setCode(e.getCode());
    StringBuilder sb = new StringBuilder();
    if (e.getMessage() != null)
      sb.append(e.getMessage()).append("\n");
    Throwable root = ExceptionUtils.getRootCause(e);
    String[] stacks = null;
    if (root == null) {
      stacks = ExceptionUtils.getRootCauseStackTrace(e);
    } else {
      stacks = ExceptionUtils.getRootCauseStackTrace(root);
    }
    for (int i = 0; i < 2 && i < stacks.length; ++i) {
      sb.append(stacks[i]).append("\n");
    }
    res.setMsg(sb.toString());
    return res;
  }

  @Override
  public RpcException convertFrom(ExceptionResult result) {
    RpcException e = new RpcException(result.getCode(), result.getMsg());
    return e;
  }

}
