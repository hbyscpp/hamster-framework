package com.seaky.hamster.core.rpc.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录Client调用服务之中抛出的异常
 * 
 * @author seaky
 * @since 1.0.0
 * @Date Mar 12, 2016
 */
public class ClientCallExceptionTrace {


  // 获取远程服务实例集群之前的pre process中发生的异常
  public static String CALL_CLUSTER_SERVICE_PREPROCESS = "CALL_CLUSTER_SERVICE_PREPROCESS";

  //获取远程服务实例集群之后的post process中发生的异常
  public static String CALL_CLUSTER_SERVICE_POSTPROCESS = "CALL_CLUSTER_SERVICE_POSTPROCESS";

  // 选择集群服务中
  public static String SELECT_CLUSTER_SERVICE_PREPROCESS = "SELECT_CLUSTER_SERVICE";

  // 调用
  public static String CALL_CLUSTER_SERVICE = "CALL_CLUSTER_SERVICE";

  public static String CALL_SERVICE_INSTANCE_PREPROCESS = "CALL_SERVICE_INSTANCE_PREPROCESS";

  public static String CALL_SERVICE_INSTANCE_POSTPROCESS = "CALL_SERVICE_INSTANCE_POSTPROCESS";

  public static String CONNECT_SERVICE_INSTANCE = "CONNECT_SERVICE_INSTANCE";

  public static String SEND_SERVICE_INSTANCE = "SEND_SERVICE_INSTANCE";

  private long traceId;

  private List<ExceptionTrace> traces = new ArrayList<ExceptionTrace>();

  private static Logger logger = LoggerFactory.getLogger("hamster_client_exception_trace_log");


  public ClientCallExceptionTrace(long id) {
    this.traceId = id;
  }

  public synchronized void addException(String phase, String msg, Throwable e) {
    ExceptionTrace t = new ExceptionTrace();
    t.e = e;
    t.phase = phase;
    t.msg = msg;
    traces.add(t);
  }

  public void show() {
    if (traces == null || traces.size() == 0)
      return;
    StringBuilder sb = new StringBuilder();
    sb.append("\ntraceId:").append(traceId).append("\n");
    for (ExceptionTrace trace : traces) {
      sb.append("phase:").append(trace.phase).append("\n");
      sb.append("msg:").append(trace.msg).append("\n");
      sb.append("exception:").append(trace.exceptionStr()).append("\n\n");
    }
    logger.info(sb.toString());

  }

  public long getTraceId() {
    return traceId;
  }

  private static class ExceptionTrace {

    public String phase;

    public String msg;

    public Throwable e;

    public String exceptionStr() {

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return sw.toString();
    }
  }
}
