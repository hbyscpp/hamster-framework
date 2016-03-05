package com.seaky.hamster.core.rpc.serialization;

import java.util.HashMap;
import java.util.Map;

import com.seaky.hamster.core.rpc.common.Constants;

public class SerializerFactory {

	private static Map<Integer, Serializer> serializerMap = new HashMap<Integer, Serializer>();

	static {
		registerSerializer(Constants.KRYO_SERIAL, new KryoSerializer());
	}

	public static synchronized void registerSerializer(int type,
			Serializer serializer) {
		if (serializerMap.containsKey(type))
			throw new RuntimeException(type + " has registed");
		serializerMap.put(type, serializer);
	}

	public static Serializer getSerializer(int type) {
		return serializerMap.get(type);
	}

}
