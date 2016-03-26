package com.seaky.hamster.core.rpc.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.utils.Utils;

@SPI("json")
public class JsonSerializer implements Serializer {

  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public <T> byte[] serialize(T obj) {
    try {
      return mapper.writeValueAsBytes(obj);
    } catch (JsonProcessingException e) {
      Utils.throwException(e);
    }
    return null;
  }

  @Override
  public <T> T deSerialize(byte[] data, Class<T> cls) {

    try {
      return mapper.readValue(data, cls);
    } catch (IOException e) {
      Utils.throwException(e);
    }
    return null;
  }

  @Override
  public <T> T deSerialize(InputStream input, Class<T> cls) {


    try {
      return mapper.readValue(input, cls);
    } catch (IOException e) {
      Utils.throwException(e);
    }
    return null;
  }

  @Override
  public <T> void serializer(T obj, OutputStream output) {
    try {
      mapper.writeValue(output, obj);
    } catch (IOException e) {
      Utils.throwException(e);
    }
  }

}
