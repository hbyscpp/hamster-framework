package com.seaky.hamster.core.rpc.serialization;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

public class SerializerManager {

  private static ConcurrentHashMap<Byte, Serializer> maps = new ConcurrentHashMap<>();

  static {
    Collection<Serializer> serializers =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findAllExtension();
    for (Serializer s : serializers) {
      Serializer pre = maps.putIfAbsent(s.id(), s);
      if (pre != null) {
        throw new RuntimeException("duplicate serialzier id " + s.getClass());
      }
    }
  }

  private SerializerManager() {}

  public static Serializer getById(byte id) {
    return maps.get(id);
  }
}
