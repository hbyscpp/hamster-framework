package com.seaky.hamster.core.rpc.protocol;

public class Response {

  private static Object NOT_SET_OBJ = new Object();

  // 客户端使用这个值,请不要随意改变
  private Object result = NOT_SET_OBJ;

  private Attachments attachments = new Attachments();

  /**
   * @return the result
   */
  public Object getResult() {
    if (result == NOT_SET_OBJ)
      return null;
    return result;
  }

  public Throwable getException() {
    if (isException())
      return (Throwable) result;
    return null;
  }

  /**
   * @param result the result to set
   */
  public void setResult(Object result) {
    this.result = result;
  }

  public void clearResult() {
    this.result = NOT_SET_OBJ;
  }

  // 是否成功调用
  public boolean isDone() {
    return result != NOT_SET_OBJ;
  }

  public boolean isException() {
    if (result == null)
      return false;

    return result instanceof Throwable;
  }

  public Attachments getAttachments() {
    return attachments;
  }

  public void setAttachments(Attachments attachments) {
    this.attachments = attachments;
  }



}
