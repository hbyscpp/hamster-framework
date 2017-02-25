package com.seaky.hamster.core.rpc.utils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.Nestable;

import com.seaky.hamster.core.rpc.annotation.ServiceInterceptorAnnotation;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptorManager;

public class Utils {

  // from netty4
  private static final boolean HAS_UNSAFE = hasUnsafe0();
  private static final boolean IS_ANDROID = isAndroid0();

  private static String CUR_PID = null;

  static {
    String name = ManagementFactory.getRuntimeMXBean().getName();
    CUR_PID = name.split("@")[0];
  }

  public static boolean isAndroid() {
    return IS_ANDROID;
  }

  private static boolean hasUnsafe0() {

    if (isAndroid()) {
      return false;
    }

    try {
      boolean hasUnsafe = PlatformDependent0.hasUnsafe();
      return hasUnsafe;
    } catch (Throwable t) {
      // Probably failed to initialize PlatformDependent0.
      return false;
    }
  }

  private static boolean isAndroid0() {
    boolean android;
    try {
      Class.forName("android.app.Application", false, getSystemClassLoader());
      android = true;
    } catch (Exception e) {
      // Failed to load the class uniquely available in Android.
      android = false;
    }

    return android;
  }

  public static ClassLoader getSystemClassLoader() {
    return PlatformDependent0.getSystemClassLoader();
  }

  public static boolean hasUnsafe() {
    return HAS_UNSAFE;
  }

  public static void throwException(Throwable t) {
    if (hasUnsafe()) {
      PlatformDependent0.throwException(t);
    } else {
      Utils.<RuntimeException>throwException0(t);
    }
  }

  @SuppressWarnings("unchecked")
  private static <E extends Throwable> void throwException0(Throwable t) throws E {
    throw (E) t;
  }

  // end from netty
  public static void validateStr(String str, char[] forbiddenChars) {
    if (StringUtils.isBlank(str))
      throw new IllegalArgumentException("string must not be null");
    if (StringUtils.containsAny(str, forbiddenChars)) {
      throw new IllegalArgumentException("string contains special char");
    }
  }

  public static String socketAddrToString(InetSocketAddress addr) {
    if (addr == null)
      return null;
    return addr.getAddress().getHostAddress() + ":" + addr.getPort();
  }

  public static InetSocketAddress stringToSocketAddr(String addr) {
    String[] attrs = addr.split(":");
    InetSocketAddress inetAddr = new InetSocketAddress(attrs[0], Integer.valueOf(attrs[1]));
    return inetAddr;
  }

  public static String generateKey(String... params) {
    if (params == null || params.length == 0)
      return null;
    StringBuilder sb = new StringBuilder();
    for (String param : params) {
      sb.append(param).append(Constants.TILDE_LINE);
    }
    return sb.substring(0, sb.length() - 1);
  }

  // 初步检查参数 是否符合要求 只作基本的判断
  public static boolean isMatch(String[] paramTypes, Object[] request)
      throws ClassNotFoundException {

    int leftLength = paramTypes == null ? 0 : paramTypes.length;
    int rightLength = request == null ? 0 : request.length;
    if (leftLength != rightLength)
      return false;
    if (leftLength == 0)
      return true;

    for (int i = 0; i < leftLength; ++i) {
      if (!isCompatibility(paramTypes[i], request[i]))
        return false;
    }

    return true;
  }

  public static Class<?> findClassByName(String typeName) throws ClassNotFoundException {
    Class<?> type = null;
    if ("int".equals(typeName)) {
      type = int.class;

    } else if ("long".equals(typeName)) {
      type = long.class;

    } else if ("byte".equals(typeName)) {
      type = byte.class;

    } else if ("short".equals(typeName)) {
      type = short.class;

    } else if ("double".equals(typeName)) {
      type = double.class;

    } else if ("float".equals(typeName)) {
      type = float.class;

    } else if ("char".equals(typeName)) {
      type = char.class;

    } else if ("void".equals(typeName)) {
      type = void.class;

    } else if ("boolean".equals(typeName)) {
      type = boolean.class;

    } else {
      type = Class.forName(typeName);
    }
    return type;

  }

  private static boolean isCompatibility(String typeName, Object obj)
      throws ClassNotFoundException {

    Class<?> type = findClassByName(typeName);

    if (type.isPrimitive()) {
      if (obj == null)
        return false;
      if (type == int.class)
        return obj.getClass() == Integer.class;

      if (type == long.class)
        return obj.getClass() == Long.class;

      if (type == byte.class)
        return obj.getClass() == Byte.class;

      if (type == short.class)
        return obj.getClass() == Short.class;

      if (type == float.class)
        return obj.getClass() == Float.class;

      if (type == double.class)
        return obj.getClass() == Double.class;

      if (type == char.class)
        return obj.getClass() == Character.class;

      if (type == boolean.class)
        return obj.getClass() == Boolean.class;

      if (type == void.class)
        return obj.getClass() == Void.class;

    }
    if (obj == null)
      return true;
    return type.isInstance(obj);
  }

  private static String versionpattern = "([0-9]+)[\\.]([0-9]+)[\\.](([0-9]+)|([0-9]+-[^\\\\]+))";

  public static void checkVersionFormat(String version) {
    if (version.matches(versionpattern)) {
      return;
    }
    throw new RuntimeException(
        "service version format error,format like X.X.X or X.X.X-YYY . X is [0-9],Y can not be \\");
  }

  // 判断版本是否兼容 right版本是否可以替换left版本
  // left和right主版本必须相同
  // left的次版本必须小于等于right版本
  // 如果left和right次版本相同，则left修订版必须小于等于right版本
  public static boolean isVersionComp(String left, String right) {
    try {

      // 主版本
      int leftpos1 = left.indexOf(".");

      int leftmajorversion = Integer.valueOf(left.substring(0, leftpos1));

      int rightpos1 = right.indexOf(".");

      int rightmajorversion = Integer.valueOf(right.substring(0, rightpos1));

      if (leftmajorversion != rightmajorversion)
        return false;

      // 次版本
      int leftpos2 = left.indexOf(".", leftpos1 + 1);

      int leftminorversion = Integer.valueOf(left.substring(leftpos1 + 1, leftpos2));

      int rightpos2 = right.indexOf(".", rightpos1 + 1);

      int rightminorversion = Integer.valueOf(right.substring(rightpos1 + 1, rightpos2));

      if (leftminorversion < rightminorversion)
        return true;
      if (leftminorversion > rightminorversion)
        return false;

      // 修订版
      int leftpos3 = left.indexOf("-", leftpos2 + 1);
      int leftrevversion = 0;
      if (leftpos3 == -1) {
        leftrevversion = Integer.valueOf(left.substring(leftpos2 + 1));
      } else {
        leftrevversion = Integer.valueOf(left.substring(leftpos2 + 1, leftpos3));
      }

      int rightpos3 = right.indexOf("-", rightpos2 + 1);
      int rightrevversion = 0;
      if (rightpos3 == -1) {
        rightrevversion = Integer.valueOf(right.substring(rightpos2 + 1));
      } else {
        rightrevversion = Integer.valueOf(right.substring(rightpos2 + 1, rightpos3));
      }
      if (leftrevversion > rightrevversion)
        return false;

      return true;

    } catch (Exception e) {
      return false;
    }

  }

  public static boolean isGroupMatch(String left, String right) {
    if (left == null || right == null)
      return false;
    String[] groups = left.split(Constants.COMMA);

    for (String g : groups) {
      if (g.equals(right))
        return true;
    }
    return left.equals(right);
  }

  public static String getCurrentVmPid() {
    return CUR_PID;
  }

  public static boolean isContainPathSeparator(String path) {

    return path.indexOf("/") != -1 || path.indexOf("\\") != -1;
  }

  public static void addDefaultInterceptor(List<ServiceInterceptor> sis, ProcessPhase phase) {
    String[] defaultNames = new String[] {"accesslog", "tracelog"};
    for (String name : defaultNames) {
      ServiceInterceptor si = ServiceInterceptorManager.createServiceInterceptor(name);
      ServiceInterceptorAnnotation anno =
          si.getClass().getAnnotation(ServiceInterceptorAnnotation.class);
      for (ProcessPhase p : anno.phases()) {
        if (p == phase) {
          sis.add(si);
        }
      }
    }
  }

  public static List<ServiceInterceptor> extractByProcessPhase(String interceptors,
      ProcessPhase phase) {
    List<ServiceInterceptor> sis = new ArrayList<ServiceInterceptor>();
    addDefaultInterceptor(sis, phase);
    if (StringUtils.isBlank(interceptors))
      return sis;

    String[] interceptorNames = interceptors.split(Constants.COMMA);
    if (interceptorNames == null || interceptorNames.length == 0)
      return sis;
    for (String name : interceptorNames) {

      if ("accesslog".equals(name)) {
        throw new RuntimeException("can not add access log interceptor");
      }
    }
    int size = interceptorNames.length;
    for (int i = 0; i < size; ++i) {
      ServiceInterceptor si =
          ServiceInterceptorManager.createServiceInterceptor(interceptorNames[i]);
      if (si == null)
        throw new RuntimeException("not found service interceptor " + interceptorNames[i]);
      ServiceInterceptorAnnotation anno =
          si.getClass().getAnnotation(ServiceInterceptorAnnotation.class);
      for (ProcessPhase p : anno.phases()) {
        if (p == phase) {
          sis.add(si);
        }
      }
    }
    return sis;
  }

  public static void shutdownExecutorService(ExecutorService pool, int waitTime) {
    if (pool == null)
      return;

    pool.shutdown();
    try {
      if (!pool.awaitTermination(waitTime, TimeUnit.SECONDS)) {
        pool.shutdownNow();
        pool.awaitTermination(waitTime, TimeUnit.SECONDS);
      }
    } catch (InterruptedException ie) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }

  }

  public static String paramsToString(String[] paramTypes) {
    StringBuilder sb = new StringBuilder();
    if (paramTypes != null && paramTypes.length > 0) {
      for (String p : paramTypes) {
        sb.append(p).append(Constants.COMMA);
      }
    }
    return sb.toString();
  }

  public static String[] getParamTypes(String params) {
    return params.split(Constants.COMMA);
  }

  public static String createServerAndClientAddress(String clientHost, int clientPort,
      String serverHost, int serverPort) {
    StringBuilder sb = new StringBuilder();
    sb.append(clientHost).append(Constants.COLON);
    sb.append(clientPort);
    sb.append(Constants.COMMA);
    sb.append(serverHost).append(Constants.COLON);
    sb.append(serverPort);

    return sb.toString();
  }

  // 从wrapper异常中获取真正的异常,递归获取
  public static Throwable getActualException(Throwable throwable) {
    if (throwable == null)
      return null;
    if (throwable instanceof Nestable) {
      Throwable inner = ((Nestable) throwable).getCause();
      if (inner == null)
        return throwable;
      return getActualException(inner);
    } else if (throwable instanceof SQLException) {
      Throwable inner = ((SQLException) throwable).getNextException();
      if (inner == null)
        return throwable;
      return getActualException(inner);
    } else if (throwable instanceof InvocationTargetException) {
      Throwable inner = ((InvocationTargetException) throwable).getTargetException();
      if (inner == null)
        return throwable;
      return getActualException(inner);
    } else if (throwable instanceof ExecutionException) {
      Throwable inner = ((ExecutionException) throwable).getCause();
      if (inner == null)
        return throwable;
      return getActualException(inner);
    } else {
      return throwable;
    }
  }

}
