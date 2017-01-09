package com.seaky.hamster.core.rpc.serialization;

import java.io.InputStream;
import java.io.OutputStream;

import com.seaky.hamster.core.annotations.SPI;

@SPI("kryo")
public interface Serializer {

  <T> byte[] serialize(T obj);

  <T> T deSerialize(byte[] data, Class<T> cls);

  <T> T deSerialize(InputStream input, Class<T> cls);

  <T> void serializer(T obj, OutputStream output);

  boolean isText();
}
