package com.seaky.hamster.core.rpc.config;

public interface ConfigConstans {

  // 单个响应超时
  public static final String REFERENCE_READ_TIMEOUT = "sr.readtimeout";

  public static final int REFERENCE_READ_TIMEOUT_DEFAULT = 5000;

  // 消费端调用的超时时间 毫秒为单位,此超时包含，网络，容错策略里面的超时
  // 调用的总时常，小于0代表不做限定
  public static final String REFERENCE_CALL_TIMEOUT = "sr.calltimeout";

  public static final int REFERENCE_CALL_TIMEOUT_DEFAULT = -1;

  // 消费方集群容错策略
  public static final String REFERENCE_CLUSTER = "sr.cluster";

  public static final String REFERENCE_CLUSTER_DEFAULT = "failfast";

  public static final String REFERENCE_LOADBALANCER = "sr.loadbalancer";

  public static final String REFERENCE_LOADBALANCER_DEFAULT = "random";

  public static final String REFERENCE_ROUTER = "sr.router";

  public static final String REFERENCE_ROUTER_DEFAULT = "default";

  public static final String REFERENCE_APP = "sr.app";

  public static final String REFERENCE_NAME = "sr.name";

  public static final String REFERENCE_VERSION = "sr.version";

  public static final String REFERENCE_GROUP = "sr.group";

  public static final String REFERENCE_PROTOCOL = "sr.protocol";

  public static final String REFERENCE_REG_TIME = "sr.reg.time";

  public static final String REFERENCE_PID = "sr.pid";

  public static final String REFERENCE_ASYNPOOL_THREAD_EXE = "sr.asynpool.thread.exe";

  public static final String REFERENCE_THREADPOOL_NAME = "sr.threadpool.name";

  public static final String REFERENCE_THREADPOOL_NAME_DEFAULT = "__COMMON__";

  public static final String REFERENCE_THREADPOOL_MAXQUEUE = "sr.threadpool.maxqueue";

  public static final int REFERENCE_THREADPOOL_MAXQUEUE_DEFAULT = 100;

  public static final String REFERENCE_THREADPOOL_MAXSIZE = "sr.threadpool.maxsize";

  public static final int REFERENCE_THREADPOOL_MAXSIZE_DEFAULT = 100;

  public static final String REFERENCE_MAX_CONCURRENT = "sr.maxcucurrent";

  public static final int REFERENCE_MAX_CONCURRENT_DEFAULT = -1;

  public static final String REFERENCE_INTERCEPTORS = "sr.interceptors";

  public static final String REFERENCE_HOST = "sr.host";



  // 引用指定的provider的地址127.0.0.1:-1;127.
  public static final String REFERENCE_SERVICE_PROVIDER_ADDRESSES = "sr.provider.addresses";

  // 引用时是否check
  public static final String REFERENCE_CHECK_SERVICE = "sr.check.service";

  public static final boolean REFERENCE_CHECK_SERVICE_DEFAULT = false;

  public static final String REFERENCE_PARAMS = "sr.params";

  public static final String REFERENCE_RETURN = "sr.return";

  public static final String REFERENCE_EXCEPTION_CONVERTOR = "sr.exception.convertor";

  // 断路器 默认恢复时间10s
  public static final String REFERENCE_CIRCUITBREAKER_HALFOPEN_DELAY =
      "sr.circuitbreaker.halfopen.delay";
  public static final int REFERENCE_CIRCUITBREAKER_HALFOPEN_DELAY_DEFAULT = 10000;

  public static final String REFERENCE_CIRCUITBREAKER_OPEN_TOTAL_NUMBER =
      "sr.circuitbreaker.open.total.number";

  public static final int REFERENCE_CIRCUITBREAKER_OPEN_TOTAL_NUMBER_DEFAULT = 3;

  public static final String REFERENCE_CIRCUITBREAKER_OPEN_FAIL_NUMBER =
      "sr.circuitbreaker.open.fail.number";

  public static final int REFERENCE_CIRCUITBREAKER_OPEN_FAIL_NUMBER_DEFAULT = 3;


  public static final String REFERENCE_CIRCUITBREAKER_CLOSE_TOTAL_NUMBER =
      "sr.circuitbreaker.close.total.number";

  public static final int REFERENCE_CIRCUITBREAKER_CLOSE_TOTAL_NUMBER_DEFAULT = 3;

  public static final String REFERENCE_CIRCUITBREAKER_CLOSE_SUCCESS_NUMBER =
      "sr.circuitbreaker.close.success.number";

  public static final int REFERENCE_CIRCUITBREAKER_CLOSE_SUCCESS_NUMBER_DEFAULT = 3;

  // 协议版本
  public static final String REFERENCE_MAX_PROTOCOL_VERSION = "sr.max.protocol.version";

  public static final short REFERENCE_MAX_PROTOCOL_VERSION_DEFAULT = 0;

  public static final String REFERENCE_FRAMEWORK_VERSION = "sr.framework.version";


  // 服务提供方的配置
  // readonly
  public static final String PROVIDER_NAME = "sp.name";

  public static final String PROVIDER_APP = "sp.app";

  public static final String PROVIDER_GROUP = "sp.group";

  public static final String PROVIDER_VERSION = "sp.version";

  public static final String PROVIDER_PROTOCOL = "sp.protocol";

  public static final String PROVIDER_HOST = "sp.host";

  public static final String PROVIDER_PORT = "sp.port";

  public static final String PROVIDER_PID = "sp.pid";

  public static final String PROVIDER_PARAMS = "sp.params";

  public static final String PROVIDER_RETURN = "sp.return";

  public static final String PROVIDER_REG_TIME = "sp.reg.time";

  public static final String PROVIDER_DISPATCHER_THREAD_EXE = "sp.dispatcher.thread.exe";

  public static final String PROVIDER_THREADPOOL_NAME = "sp.threadpool.name";

  public static final String PROVIDER_THREADPOOL_NAME_DEFAULT = "__COMMON__";

  public static final String PROVIDER_THREADPOOL_MAXQUEUE = "sp.threadpool.maxqueue";

  public static final int PROVIDER_THREADPOOL_MAXQUEUE_DEFAULT = -1;

  public static final String PROVIDER_THREADPOOL_MAXSIZE = "sp.threadpool.maxsize";

  public static final int PROVIDER_THREADPOOL_MAXSIZE_DEFAULT = 30;

  public static final String PROVIDER_MAX_CONCURRENT = "sp.maxconcurrent";

  public static final int PROVIDER_MAX_CONCURRENT_DEFAULT = 100;

  public static final String PROVIDER_WEIGHT = "sp.weight";

  public static final int PROVIDER_WEIGHT_DEFAULT = 1;


  public static final String PROVIDER_INTERCEPTORS = "sp.interceptors";

  public static final String PROVIDER_EXCEPTION_CONVERTOR = "sp.exception.convertor";

  public static final String PROVIDER_HIDDERN = "sp.hidden";

  public static final boolean PROVIDER_HIDDERN_DEFAULT = false;

  public static final String PROVIDER_FORCE_ACCESS = "sp.forceaccess";

  public static final boolean PROVIDER_FORCE_ACCESS_DEFAULT = false;

  public static final String PROVIDER_MAX_PROTOCOL_VERSION = "sp.max.protocol.version";

  public static final short PROVIDER_MAX_PROTOCOL_VERSION_DEFAULT = 0;

  public static final String PROVIDER_FRAMEWORK_VERSION = "sp.framework.version";


}
