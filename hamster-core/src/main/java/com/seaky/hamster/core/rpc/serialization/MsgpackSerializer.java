package com.seaky.hamster.core.rpc.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaky.hamster.core.annotations.SPI;

@SPI("msgpack")
public class MsgpackSerializer implements Serializer {

  private static ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());

  @Override
  public <T> byte[] serialize(T obj) {
    try {
      return msgpackMapper.writeValueAsBytes(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T deSerialize(byte[] data, Class<T> cls) {
    try {
      T obj = msgpackMapper.readValue(data, cls);
      return obj;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T deSerialize(InputStream input, Class<T> cls) {
    try {
      T obj = msgpackMapper.readValue(input, cls);
      return obj;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> void serializer(T obj, OutputStream output) {
    try {
      msgpackMapper.writeValue(output, obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public boolean isText() {
    return false;
  }

  @Override
  public byte id() {
    return 2;
  }

}
