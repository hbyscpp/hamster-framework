package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.HashMap;
import java.util.Map;

public class HamsterResponse {



  private byte[] result;

  // 出现异常时设置此类

  private Map<String, String> attachments;


  /**
   * @return the attachments
   */
  public Map<String, String> getAttachments() {
    return attachments;
  }

  /**
   * @param attachments the attachments to set
   */
  public void setAttachments(Map<String, String> attachments) {
    this.attachments = attachments;
  }



  public void addAttachment(String key, String value) {
    if (attachments == null)
      attachments = new HashMap<String, String>();
    attachments.put(key, value);
  }



  /**
   * @return the result
   */
  public byte[] getResult() {
    return result;
  }

  /**
   * @param result the result to set
   */
  public void setResult(byte[] result) {
    this.result = result;
  }



  public static class ObjectHolder {
    private Object obj;

    /**
     * @return the obj
     */
    public Object getObj() {
      return obj;
    }

    /**
     * @param obj the obj to set
     */
    public void setObj(Object obj) {
      this.obj = obj;
    }
  }

}
