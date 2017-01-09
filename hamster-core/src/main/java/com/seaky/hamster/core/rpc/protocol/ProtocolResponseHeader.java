package com.seaky.hamster.core.rpc.protocol;

public class ProtocolResponseHeader {

  private boolean isException;

  private Attachments attachments = new Attachments();

  public Attachments getAttachments() {
    return attachments;
  }

  public void setAttachments(Attachments attachments) {
    this.attachments = attachments;
  }

  public boolean isException() {
    return isException;
  }

  public void setException(boolean isException) {
    this.isException = isException;
  }



}
