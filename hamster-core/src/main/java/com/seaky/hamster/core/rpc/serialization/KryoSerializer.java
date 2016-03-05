package com.seaky.hamster.core.rpc.serialization;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Map;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import de.javakaffee.kryoserializers.CopyForIterateCollectionSerializer;
import de.javakaffee.kryoserializers.CopyForIterateMapSerializer;
import de.javakaffee.kryoserializers.DateSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.EnumSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;

public class KryoSerializer implements Serializer {
	private static final ThreadLocal<Kryo> threadLocalKryo = new ThreadLocal<Kryo>() {

		protected Kryo initialValue() {
			Kryo _kryo = new KryoReflectionFactorySupport() {

				@Override
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public com.esotericsoftware.kryo.Serializer<?> getDefaultSerializer(
						final Class type) {
					if (EnumSet.class.isAssignableFrom(type)) {
						return new EnumSetSerializer();
					}
					if (EnumMap.class.isAssignableFrom(type)) {
						return new EnumMapSerializer();
					}
					if (Collection.class.isAssignableFrom(type)) {
						return new CopyForIterateCollectionSerializer();
					}
					if (Map.class.isAssignableFrom(type)) {
						return new CopyForIterateMapSerializer();
					}
					if (Date.class.isAssignableFrom(type)) {
						return new DateSerializer(type);
					}
					return super.getDefaultSerializer(type);
				}
			};
			_kryo.setRegistrationRequired(false);
			_kryo.register(Arrays.asList("").getClass(),
					new ArraysAsListSerializer());
			_kryo.register(Collections.singletonList("").getClass(),
					new CollectionsSingletonListSerializer());
			_kryo.register(Collections.singleton("").getClass(),
					new CollectionsSingletonSetSerializer());
			_kryo.register(Collections.singletonMap("", "").getClass(),
					new CollectionsSingletonMapSerializer());
			_kryo.register(GregorianCalendar.class,
					new GregorianCalendarSerializer());
			_kryo.register(InvocationHandler.class, new JdkProxySerializer());
			_kryo.register(InetSocketAddress.class,
					new InetSocketAddressSerializer());
			UnmodifiableCollectionsSerializer.registerSerializers(_kryo);
			SynchronizedCollectionsSerializer.registerSerializers(_kryo);
			return _kryo;
		}
	};

	@Override
	public <T> byte[] serialize(T obj) {
		if (obj == null)
			return null;
		Output output = new Output(1024, -1);
		threadLocalKryo.get().writeObject(output, obj);
		return output.toBytes();
	}

	@Override
	public <T> T deSerialize(byte[] data, Class<T> cls) {
		if (data == null || data.length == 0)
			return null;
		Input input = new Input(data);
		return threadLocalKryo.get().readObject(input, cls);
	}

	@Override
	public <T> T deSerialize(InputStream data, Class<T> cls) {
		Input input = new Input(data);
		return threadLocalKryo.get().readObject(input, cls);
	}

	@Override
	public <T> void serializer(T obj, OutputStream output) {
		Output outputkryo = new Output(output);

		threadLocalKryo.get().writeObject(outputkryo, obj);

	}

	public static class InetSocketAddressSerializer extends
			com.esotericsoftware.kryo.Serializer<InetSocketAddress> {

		@Override
		public void write(Kryo kryo, Output output, InetSocketAddress obj) {
			output.writeString(obj.getAddress().getHostAddress());
			output.writeInt(obj.getPort(), true);

		}

		@Override
		public InetSocketAddress read(Kryo kryo, Input input,
				Class<InetSocketAddress> type) {
			String host = input.readString();
			int port = input.readInt(true);
			return new InetSocketAddress(host, port);

		}

	}

}
