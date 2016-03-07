package com.seaky.hamster.core.rpc.protocol;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Attachments {

  private ConcurrentHashMap<String, String> attachments = new ConcurrentHashMap<>();

  public Map<String, String> getAllAttachments() {
    return Collections.unmodifiableMap(attachments);
  }

  public void addAttachment(String key, String value) {
    attachments.put(key, value);
  }

  public void removeAttachment(String key) {
    attachments.remove(key);
  }

  public String getAttachment(String key) {
    return attachments.get(key);
  }
}
