package com.seaky.hamster.core.rpc.protocol;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Attachments {

  private Map<String, Object> attachments = new ConcurrentHashMap<>();

  public Attachments() {

  }

  public Attachments(Attachments attachments) {
    this.attachments.putAll(attachments.attachments);
  }

  public void putAttachments(Attachments attachments) {
    this.attachments.putAll(attachments.attachments);
  }

  public Map<String, Object> getAllKeyValue() {
    return Collections.unmodifiableMap(attachments);
  }

  public void putInt(String key, int value) {
    attachments.put(key, value);
  }

  public Integer getAsInt(String key) {
    return getAsInt(key, null);
  }

  public Integer getAsInt(String key, Integer deafultValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return deafultValue;

    if (obj instanceof Number) {
      return ((Number) obj).intValue();
    }
    throw new ClassCastException(obj.getClass().getName() + "can not cast to integer");

  }

  public void putLong(String key, long value) {
    attachments.put(key, value);
  }

  public Long getAsLong(String key) {
    return getAsLong(key, null);
  }

  public Long getAsLong(String key, Long defautValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return null;
    if (obj instanceof Number) {
      return ((Number) obj).longValue();
    }
    throw new ClassCastException(obj.getClass().getName() + "can not cast to long");
  }

  public void putShort(String key, short value) {
    attachments.put(key, value);
  }

  public Short getAsShort(String key) {
    return getAsShort(key, null);
  }

  public Short getAsShort(String key, Short defaultValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return defaultValue;
    if (obj instanceof Number) {
      return ((Number) obj).shortValue();
    }
    throw new ClassCastException(obj.getClass().getName() + "can not cast to short");
  }

  public void putString(String key, String value) {
    attachments.put(key, value);
  }

  public String getAsString(String key) {
    return getAsString(key, null);
  }

  public String getAsString(String key, String defaultValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return defaultValue;
    return (String) obj;
  }


  public void putByte(String key, byte value) {
    attachments.put(key, value);
  }

  public Byte getAsByte(String key) {
    return getAsByte(key, null);
  }

  public Byte getAsByte(String key, Byte deaultValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return deaultValue;
    if (obj instanceof Number) {
      return ((Number) obj).byteValue();
    }
    throw new ClassCastException(obj.getClass().getName() + "can not cast to byte");
  }

  public void putBoolean(String key, boolean value) {
    attachments.put(key, value);
  }

  public Boolean getAsBoolean(String key) {
    return getAsBoolean(key, null);
  }

  public Boolean getAsBoolean(String key, Boolean defaultValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return defaultValue;
    return (Boolean) obj;
  }

  public void putDouble(String key, double value) {
    attachments.put(key, value);
  }

  public Double getAsDouble(String key) {
    return getAsDouble(key, null);
  }

  public Double getAsDouble(String key, Double defaultValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return defaultValue;
    if (obj instanceof Number) {
      return ((Number) obj).doubleValue();
    }
    throw new ClassCastException(obj.getClass().getName() + "can not cast to double");
  }

  public void putFloat(String key, float value) {
    attachments.put(key, value);
  }

  public Float getAsFloat(String key) {
    return getAsFloat(key, null);
  }

  public Float getAsFloat(String key, Float defaultValue) {
    Object obj = attachments.get(key);
    if (obj == null)
      return defaultValue;
    if (obj instanceof Number) {
      return ((Number) obj).floatValue();
    }
    throw new ClassCastException(obj.getClass().getName() + "can not cast to float");
  }
}
