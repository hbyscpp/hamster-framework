package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.HashMap;
import java.util.Map;

public class HamsterResponse {


  private boolean isException = false;



  private byte[] result;

  // 出现异常时设置此类

  private Map<String, Object> attachments = new HashMap<>();


  /**
   * @return the attachments
   */
  public Map<String, Object> getAttachments() {
    return attachments;
  }

  /**
   * @param attachments the attachments to set
   */
  public void setAttachments(Map<String, Object> attachments) {
    this.attachments = attachments;
  }



  public void addAttachment(String key, Object value) {
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

  public boolean isException() {
    return isException;
  }

  public void setException(boolean isException) {
    this.isException = isException;
  }

}
