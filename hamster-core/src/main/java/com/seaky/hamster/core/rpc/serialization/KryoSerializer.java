package com.seaky.hamster.core.rpc.serialization;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.seaky.hamster.core.annotations.SPI;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import de.javakaffee.kryoserializers.DateSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.EnumSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;

@SPI("kryo")
@SuppressWarnings("unchecked")
public class KryoSerializer implements Serializer {


  private static final ThreadLocal<Kryo> threadLocalKryo = new ThreadLocal<Kryo>() {


    protected Kryo initialValue() {
      Kryo _kryo = new KryoReflectionFactorySupport() {

        @Override
        @SuppressWarnings({"rawtypes"})
        public com.esotericsoftware.kryo.Serializer<?> getDefaultSerializer(final Class type) {
          if (EnumSet.class.isAssignableFrom(type)) {
            return new EnumSetSerializer();
          }
          if (EnumMap.class.isAssignableFrom(type)) {
            return new EnumMapSerializer();
          }
          if (Set.class.isAssignableFrom(type)) {
            return new SetSerializer();
          }
          if (Collection.class.isAssignableFrom(type)) {
            return new CollectionSerializer();
          }
          if (Map.class.isAssignableFrom(type)) {
            return new MapSerializer();
          }
          if (Date.class.isAssignableFrom(type)) {
            return new DateSerializer(type);
          }
          return super.getDefaultSerializer(type);
        }
      };
      _kryo.setRegistrationRequired(false);
      _kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
      _kryo.register(Collections.singletonList("").getClass(),
          new CollectionsSingletonListSerializer());
      _kryo.register(Collections.singleton("").getClass(), new CollectionsSingletonSetSerializer());
      _kryo.register(Collections.singletonMap("", "").getClass(),
          new CollectionsSingletonMapSerializer());
      _kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
      _kryo.register(InvocationHandler.class, new JdkProxySerializer());
      _kryo.register(InetSocketAddress.class, new InetSocketAddressSerializer());
      UnmodifiableCollectionsSerializer.registerSerializers(_kryo);
      SynchronizedCollectionsSerializer.registerSerializers(_kryo);
      _kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
      return _kryo;
    }
  };



  public <T> byte[] serialize(T obj) {
    if (obj == null)
      return null;
    Output output = new Output(1024, -1);
    threadLocalKryo.get().writeObject(output, obj);
    byte[] ret = output.toBytes();
    output.close();
    return ret;
  }

  public <T> T deSerialize(byte[] data, Class<T> cls) {
    if (data == null || data.length == 0)
      return null;
    Input input = new Input(data);
    T ret = threadLocalKryo.get().readObject(input, cls);
    input.close();
    return ret;
  }

  public <T> T deSerialize(InputStream data, Class<T> cls) {
    Input input = new Input(data);
    T ret = threadLocalKryo.get().readObject(input, cls);
    input.close();
    return ret;
  }

  public <T> void serializer(T obj, OutputStream output) {
    Output outputkryo = new Output(output);
    threadLocalKryo.get().writeObject(outputkryo, obj);
    outputkryo.close();

  }


  public static class InetSocketAddressSerializer
      extends com.esotericsoftware.kryo.Serializer<InetSocketAddress> {

    @Override
    public void write(Kryo kryo, Output output, InetSocketAddress obj) {
      output.writeString(obj.getAddress().getHostAddress());
      output.writeInt(obj.getPort(), true);

    }

    @Override
    public InetSocketAddress read(Kryo kryo, Input input, Class<InetSocketAddress> type) {
      String host = input.readString();
      int port = input.readInt(true);
      return new InetSocketAddress(host, port);
    }

  }

  @Override
  public boolean isText() {
    return false;
  }

  @Override
  public byte id() {
    return 1;
  }


  // Map接口

  @SuppressWarnings("rawtypes")
  public static class CollectionSerializer
      extends com.esotericsoftware.kryo.Serializer<Collection> {

    private boolean elementsCanBeNull = true;
    private com.esotericsoftware.kryo.Serializer serializer;
    private Class elementClass;
    private Class genericType;

    public CollectionSerializer() {}

    /** @see #setElementClass(Class, Serializer) */
    public CollectionSerializer(Class elementClass,
        com.esotericsoftware.kryo.Serializer serializer) {
      setElementClass(elementClass, serializer);
    }

    /**
     * @see #setElementClass(Class, Serializer)
     * @see #setElementsCanBeNull(boolean)
     */
    public CollectionSerializer(Class elementClass, com.esotericsoftware.kryo.Serializer serializer,
        boolean elementsCanBeNull) {
      setElementClass(elementClass, serializer);
      this.elementsCanBeNull = elementsCanBeNull;
    }

    /**
     * @param elementsCanBeNull False if all elements are not null. This saves 1 byte per element if
     *        elementClass is set. True if it is not known (default).
     */
    public void setElementsCanBeNull(boolean elementsCanBeNull) {
      this.elementsCanBeNull = elementsCanBeNull;
    }

    /**
     * @param elementClass The concrete class of each element. This saves 1-2 bytes per element. Set
     *        to null if the class is not known or varies per element (default).
     * @param serializer The serializer to use for each element.
     */
    public void setElementClass(Class elementClass,
        com.esotericsoftware.kryo.Serializer serializer) {
      this.elementClass = elementClass;
      this.serializer = serializer;
    }

    public void setGenerics(Kryo kryo, Class[] generics) {
      genericType = null;
      if (generics != null && generics.length > 0) {
        if (kryo.isFinal(generics[0]))
          genericType = generics[0];
      }
    }

    public void write(Kryo kryo, Output output, Collection collection) {
      writeInternal(kryo, output, new ArrayList<Object>(collection));
    }

    private void writeInternal(Kryo kryo, Output output, Collection collection) {
      int length = collection.size();
      output.writeVarInt(length, true);
      com.esotericsoftware.kryo.Serializer serializer = this.serializer;
      if (genericType != null) {
        if (serializer == null)
          serializer = kryo.getSerializer(genericType);
        genericType = null;
      }
      if (serializer != null) {
        if (elementsCanBeNull) {
          for (Object element : collection)
            kryo.writeObjectOrNull(output, element, serializer);
        } else {
          for (Object element : collection)
            kryo.writeObject(output, element, serializer);
        }
      } else {
        for (Object element : collection)
          kryo.writeClassAndObject(output, element);
      }
    }


    public Collection read(Kryo kryo, Input input, Class<Collection> type) {
      Collection collection = kryo.newInstance(ArrayList.class);
      kryo.reference(collection);
      int length = input.readVarInt(true);
      if (collection instanceof ArrayList)
        ((ArrayList) collection).ensureCapacity(length);
      Class elementClass = this.elementClass;
      com.esotericsoftware.kryo.Serializer serializer = this.serializer;
      if (genericType != null) {
        if (serializer == null) {
          elementClass = genericType;
          serializer = kryo.getSerializer(genericType);
        }
        genericType = null;
      }
      if (serializer != null) {
        if (elementsCanBeNull) {
          for (int i = 0; i < length; i++)
            collection.add(kryo.readObjectOrNull(input, elementClass, serializer));
        } else {
          for (int i = 0; i < length; i++)
            collection.add(kryo.readObject(input, elementClass, serializer));
        }
      } else {
        for (int i = 0; i < length; i++)
          collection.add(kryo.readClassAndObject(input));
      }
      return collection;
    }

    /**
     * Used by {@link #copy(Kryo, Collection)} to create the new object. This can be overridden to
     * customize object creation, eg to call a constructor with arguments. The default
     * implementation uses {@link Kryo#newInstance(Class)}.
     */
    protected Collection createCopy(Kryo kryo, Collection original) {
      return kryo.newInstance(original.getClass());
    }

    public Collection copy(Kryo kryo, Collection original) {
      Collection copy = createCopy(kryo, original);
      kryo.reference(copy);
      for (Object element : original)
        copy.add(kryo.copy(element));
      return copy;
    }



  }

  @SuppressWarnings("rawtypes")
  public static class SetSerializer extends com.esotericsoftware.kryo.Serializer<Set> {

    private boolean elementsCanBeNull = true;
    private com.esotericsoftware.kryo.Serializer serializer;
    private Class elementClass;
    private Class genericType;

    public SetSerializer() {}

    /** @see #setElementClass(Class, Serializer) */
    public SetSerializer(Class elementClass, com.esotericsoftware.kryo.Serializer serializer) {
      setElementClass(elementClass, serializer);
    }

    /**
     * @see #setElementClass(Class, Serializer)
     * @see #setElementsCanBeNull(boolean)
     */
    public SetSerializer(Class elementClass, com.esotericsoftware.kryo.Serializer serializer,
        boolean elementsCanBeNull) {
      setElementClass(elementClass, serializer);
      this.elementsCanBeNull = elementsCanBeNull;
    }

    /**
     * @param elementsCanBeNull False if all elements are not null. This saves 1 byte per element if
     *        elementClass is set. True if it is not known (default).
     */
    public void setElementsCanBeNull(boolean elementsCanBeNull) {
      this.elementsCanBeNull = elementsCanBeNull;
    }

    /**
     * @param elementClass The concrete class of each element. This saves 1-2 bytes per element. Set
     *        to null if the class is not known or varies per element (default).
     * @param serializer The serializer to use for each element.
     */
    public void setElementClass(Class elementClass,
        com.esotericsoftware.kryo.Serializer serializer) {
      this.elementClass = elementClass;
      this.serializer = serializer;
    }

    public void setGenerics(Kryo kryo, Class[] generics) {
      genericType = null;
      if (generics != null && generics.length > 0) {
        if (kryo.isFinal(generics[0]))
          genericType = generics[0];
      }
    }

    public void write(Kryo kryo, Output output, Set collection) {
      writeInternal(kryo, output, new HashSet<Object>(collection));
    }

    private void writeInternal(Kryo kryo, Output output, Set collection) {
      int length = collection.size();
      output.writeVarInt(length, true);
      com.esotericsoftware.kryo.Serializer serializer = this.serializer;
      if (genericType != null) {
        if (serializer == null)
          serializer = kryo.getSerializer(genericType);
        genericType = null;
      }
      if (serializer != null) {
        if (elementsCanBeNull) {
          for (Object element : collection)
            kryo.writeObjectOrNull(output, element, serializer);
        } else {
          for (Object element : collection)
            kryo.writeObject(output, element, serializer);
        }
      } else {
        for (Object element : collection)
          kryo.writeClassAndObject(output, element);
      }
    }


    public Set read(Kryo kryo, Input input, Class<Set> type) {
      Set collection = kryo.newInstance(HashSet.class);
      kryo.reference(collection);
      int length = input.readVarInt(true);
      Class elementClass = this.elementClass;
      com.esotericsoftware.kryo.Serializer serializer = this.serializer;
      if (genericType != null) {
        if (serializer == null) {
          elementClass = genericType;
          serializer = kryo.getSerializer(genericType);
        }
        genericType = null;
      }
      if (serializer != null) {
        if (elementsCanBeNull) {
          for (int i = 0; i < length; i++)
            collection.add(kryo.readObjectOrNull(input, elementClass, serializer));
        } else {
          for (int i = 0; i < length; i++)
            collection.add(kryo.readObject(input, elementClass, serializer));
        }
      } else {
        for (int i = 0; i < length; i++)
          collection.add(kryo.readClassAndObject(input));
      }
      return collection;
    }

    /**
     * Used by {@link #copy(Kryo, Collection)} to create the new object. This can be overridden to
     * customize object creation, eg to call a constructor with arguments. The default
     * implementation uses {@link Kryo#newInstance(Class)}.
     */
    protected Collection createCopy(Kryo kryo, Collection original) {
      return kryo.newInstance(original.getClass());
    }

    public Collection copy(Kryo kryo, Collection original) {
      Collection copy = createCopy(kryo, original);
      kryo.reference(copy);
      for (Object element : original)
        copy.add(kryo.copy(element));
      return copy;
    }



  }
  // List接口
  @SuppressWarnings("rawtypes")
  public static class MapSerializer extends com.esotericsoftware.kryo.Serializer<Map> {
    private Class keyClass, valueClass;
    private com.esotericsoftware.kryo.Serializer keySerializer, valueSerializer;
    private boolean keysCanBeNull = true, valuesCanBeNull = true;
    private Class keyGenericType, valueGenericType;

    /**
     * @param keysCanBeNull False if all keys are not null. This saves 1 byte per key if keyClass is
     *        set. True if it is not known (default).
     */
    public void setKeysCanBeNull(boolean keysCanBeNull) {
      this.keysCanBeNull = keysCanBeNull;
    }

    /**
     * @param keyClass The concrete class of each key. This saves 1 byte per key. Set to null if the
     *        class is not known or varies per key (default).
     * @param keySerializer The serializer to use for each key.
     */
    public void setKeyClass(Class keyClass, com.esotericsoftware.kryo.Serializer keySerializer) {
      this.keyClass = keyClass;
      this.keySerializer = keySerializer;
    }

    /**
     * @param valueClass The concrete class of each value. This saves 1 byte per value. Set to null
     *        if the class is not known or varies per value (default).
     * @param valueSerializer The serializer to use for each value.
     */
    public void setValueClass(Class valueClass,
        com.esotericsoftware.kryo.Serializer valueSerializer) {
      this.valueClass = valueClass;
      this.valueSerializer = valueSerializer;
    }

    /**
     * @param valuesCanBeNull True if values are not null. This saves 1 byte per value if keyClass
     *        is set. False if it is not known (default).
     */
    public void setValuesCanBeNull(boolean valuesCanBeNull) {
      this.valuesCanBeNull = valuesCanBeNull;
    }

    public void setGenerics(Kryo kryo, Class[] generics) {
      keyGenericType = null;
      valueGenericType = null;

      if (generics != null && generics.length > 0) {
        if (generics[0] != null && kryo.isFinal(generics[0]))
          keyGenericType = generics[0];
        if (generics.length > 1 && generics[1] != null && kryo.isFinal(generics[1]))
          valueGenericType = generics[1];
      }
    }

    public void write(Kryo kryo, Output output, Map object) {

      final Map<?, ?> map = new HashMap<Object, Object>(object);;
      writeInternal(kryo, output, map);

    }

    public void writeInternal(Kryo kryo, Output output, Map map) {
      int length = map.size();
      output.writeInt(length, true);

      com.esotericsoftware.kryo.Serializer keySerializer = this.keySerializer;
      if (keyGenericType != null) {
        if (keySerializer == null)
          keySerializer = kryo.getSerializer(keyGenericType);
        keyGenericType = null;
      }
      com.esotericsoftware.kryo.Serializer valueSerializer = this.valueSerializer;
      if (valueGenericType != null) {
        if (valueSerializer == null)
          valueSerializer = kryo.getSerializer(valueGenericType);
        valueGenericType = null;
      }

      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
        Entry entry = (Entry) iter.next();
        if (keySerializer != null) {
          if (keysCanBeNull)
            kryo.writeObjectOrNull(output, entry.getKey(), keySerializer);
          else
            kryo.writeObject(output, entry.getKey(), keySerializer);
        } else
          kryo.writeClassAndObject(output, entry.getKey());
        if (valueSerializer != null) {
          if (valuesCanBeNull)
            kryo.writeObjectOrNull(output, entry.getValue(), valueSerializer);
          else
            kryo.writeObject(output, entry.getValue(), valueSerializer);
        } else
          kryo.writeClassAndObject(output, entry.getValue());
      }
    }


    public Map read(Kryo kryo, Input input, Class<Map> type) {
      Map map = kryo.newInstance(HashMap.class);
      int length = input.readInt(true);

      Class keyClass = this.keyClass;
      Class valueClass = this.valueClass;

      com.esotericsoftware.kryo.Serializer keySerializer = this.keySerializer;
      if (keyGenericType != null) {
        keyClass = keyGenericType;
        if (keySerializer == null)
          keySerializer = kryo.getSerializer(keyClass);
        keyGenericType = null;
      }
      com.esotericsoftware.kryo.Serializer valueSerializer = this.valueSerializer;
      if (valueGenericType != null) {
        valueClass = valueGenericType;
        if (valueSerializer == null)
          valueSerializer = kryo.getSerializer(valueClass);
        valueGenericType = null;
      }

      kryo.reference(map);

      for (int i = 0; i < length; i++) {
        Object key;
        if (keySerializer != null) {
          if (keysCanBeNull)
            key = kryo.readObjectOrNull(input, keyClass, keySerializer);
          else
            key = kryo.readObject(input, keyClass, keySerializer);
        } else
          key = kryo.readClassAndObject(input);
        Object value;
        if (valueSerializer != null) {
          if (valuesCanBeNull)
            value = kryo.readObjectOrNull(input, valueClass, valueSerializer);
          else
            value = kryo.readObject(input, valueClass, valueSerializer);
        } else
          value = kryo.readClassAndObject(input);
        map.put(key, value);
      }
      return map;
    }

    protected Map createCopy(Kryo kryo, Map original) {
      return kryo.newInstance(original.getClass());
    }

    public Map copy(Kryo kryo, Map original) {
      Map copy = createCopy(kryo, original);
      for (Iterator iter = original.entrySet().iterator(); iter.hasNext();) {
        Entry entry = (Entry) iter.next();
        copy.put(kryo.copy(entry.getKey()), kryo.copy(entry.getValue()));
      }
      return copy;
    }

  }

}
