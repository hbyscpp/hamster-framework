package com.seaky.hamster.core.rpc.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class PlatformDependent0 {

  private static final Unsafe UNSAFE;

  static {
    boolean directBufferFreeable = false;
    try {
      Class<?> cls =
          Class.forName("sun.nio.ch.DirectBuffer", false, getClassLoader(PlatformDependent0.class));
      Method method = cls.getMethod("cleaner");
      if ("sun.misc.Cleaner".equals(method.getReturnType().getName())) {
        directBufferFreeable = true;
      }
    } catch (Throwable t) {
      // We don't have sun.nio.ch.DirectBuffer.cleaner().
    }

    Field addressField;
    try {
      addressField = Buffer.class.getDeclaredField("address");
      addressField.setAccessible(true);
      if (addressField.getLong(ByteBuffer.allocate(1)) != 0) {
        // A heap buffer must have 0 address.
        addressField = null;
      } else {
        ByteBuffer direct = ByteBuffer.allocateDirect(1);
        if (addressField.getLong(direct) == 0) {
          // A direct buffer must have non-zero address.
          addressField = null;
        }
      }
    } catch (Throwable t) {
      // Failed to access the address field.
      addressField = null;
    }

    Unsafe unsafe;
    if (addressField != null && directBufferFreeable) {
      try {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        unsafe = (Unsafe) unsafeField.get(null);

        // Ensure the unsafe supports all necessary methods to work
        // around the mistake in the latest OpenJDK.
        // https://github.com/netty/netty/issues/1061
        // http://www.mail-archive.com/jdk6-dev@openjdk.java.net/msg00698.html
        try {
          unsafe.getClass().getDeclaredMethod("copyMemory",
              new Class[] {Object.class, long.class, Object.class, long.class, long.class});

        } catch (NoSuchMethodError t) {
          throw t;
        } catch (NoSuchMethodException e) {
          throw e;
        }
      } catch (Throwable cause) {
        // Unsafe.copyMemory(Object, long, Object, long, long)
        // unavailable.
        unsafe = null;
      }
    } else {
      // If we cannot access the address of a direct buffer, there's no
      // point of using unsafe.
      // Let's just pretend unsafe is unavailable for overall simplicity.
      unsafe = null;
    }

    UNSAFE = unsafe;

  }

  static boolean hasUnsafe() {
    return UNSAFE != null;
  }

  static void throwException(Throwable t) {
    UNSAFE.throwException(t);
  }

  static ClassLoader getClassLoader(final Class<?> clazz) {
    if (System.getSecurityManager() == null) {
      return clazz.getClassLoader();
    } else {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
        @Override
        public ClassLoader run() {
          return clazz.getClassLoader();
        }
      });
    }
  }

  static ClassLoader getContextClassLoader() {
    if (System.getSecurityManager() == null) {
      return Thread.currentThread().getContextClassLoader();
    } else {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
        @Override
        public ClassLoader run() {
          return Thread.currentThread().getContextClassLoader();
        }
      });
    }
  }

  static ClassLoader getSystemClassLoader() {
    if (System.getSecurityManager() == null) {
      return ClassLoader.getSystemClassLoader();
    } else {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
        @Override
        public ClassLoader run() {
          return ClassLoader.getSystemClassLoader();
        }
      });
    }
  }

  private PlatformDependent0() {}

}
