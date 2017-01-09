package com.seaky.hamster.core.rpc.server;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.common.DefaultServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.config.ReadOnlyEndpointConfig;
import com.seaky.hamster.core.rpc.exception.LossReqParamException;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.exception.RpcException;
import com.seaky.hamster.core.rpc.exception.ServiceProviderConfigNotFoundException;
import com.seaky.hamster.core.rpc.exception.ServiceProviderNotFoundException;
import com.seaky.hamster.core.rpc.exception.ServiceSigMismatchException;
import com.seaky.hamster.core.rpc.exception.UnknownException;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.ExceptionConvertor;
import com.seaky.hamster.core.rpc.protocol.ExceptionResult;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaService;

import io.netty.util.concurrent.EventExecutorGroup;

/**
 * server的请求分发，一个jvm中所有server 共享一个dispatcher线程池 dispatcher主要用于协议的解析，ServiceContext的创建
 * 
 * @author seaky
 * @since 1.0.0
 * @Date Apr 6, 2016
 */
public class RequestDispatcher<Req, Rsp> {

  private AbstractServer<Req, Rsp> server;

  private static Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

  // 分发消息
  public void dispatchMessage(Req request, ProtocolRequestHeader header,
      ServerTransport<Req, Rsp> transport) {

    ServiceContext context = initContext(request, header, transport);
    if (ServiceContextUtils.getResponseHeader(context).isException()) {
      // 初始化context有异常
      writeResponse(context, transport);
      return;
    }
    String serviceName = header.getServiceName();
    String app = header.getApp();
    String serviceVersion = header.getVersion();
    String group = header.getGroup();
    JavaService service = server.findService(serviceName, app, serviceVersion, group);

    if (service == null) {
      setException(context, new ServiceProviderNotFoundException(String.format(
          "service %s,app %s,group %s,version %s", serviceName, app, group, serviceVersion)));
      writeResponse(context, transport);
      return;
    }

    String[] params =
        server.getServiceDescriptor(app, serviceName, serviceVersion, group).getParamTypes();
    try {
      if (!Utils.isMatch(params, ServiceContextUtils.getRequestBody(context).getParams())) {
        setException(context, new ServiceSigMismatchException(serviceName));
        writeResponse(context, transport);
        return;
      }
    } catch (ClassNotFoundException e) {
      setException(context, new ServiceSigMismatchException(serviceName + ":" + e.getMessage()));
      writeResponse(context, transport);
      return;
    }
    List<ServiceInterceptor> interceptors =
        server.getServiceInterceptor(serviceName, app, serviceVersion, group);
    ServiceRunner<Req, Rsp> sr =
        new ServiceRunner<Req, Rsp>(this, service, transport, context, interceptors);
    runService(context, server, sr);
  }


  private void runService(ServiceContext context, AbstractServer<Req, Rsp> server,
      ServiceRunner<Req, Rsp> sr) {
    EndpointConfig sc = ServiceContextUtils.getProviderConfig(context);
    boolean useDispatcherThread =
        sc.getValueAsBoolean(ConfigConstans.PROVIDER_DISPATCHER_THREAD_EXE, false);

    if (useDispatcherThread) {
      sr.run();
    } else {
      int maxThread = sc.getValueAsInt(ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE,
          ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE_DEFAULT);
      String threadPoolName = threadPoolName(context, server);
      EventExecutorGroup pool =
          ServerResourceManager.getServiceThreadpoolManager().create(threadPoolName, maxThread);
      pool.execute(sr);
    }
  }

  private String threadPoolName(ServiceContext context, AbstractServer<Req, Rsp> server) {

    ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(context);
    String serviceName = header.getServiceName();
    String app = header.getApp();
    String serviceVersion = header.getVersion();
    String group = header.getGroup();
    String key = Utils.generateKey(serviceName, app, serviceVersion, group);
    return key;
  }

  private static class ServiceRunner<Req, Rsp> implements Runnable {


    private RequestDispatcher<Req, Rsp> dispatcher;

    private JavaService service;

    private ServerTransport<Req, Rsp> transport;

    private ServiceContext context;

    private List<ServiceInterceptor> interceptors;

    public ServiceRunner(RequestDispatcher<Req, Rsp> dispatcher, JavaService service,
        ServerTransport<Req, Rsp> transport, ServiceContext context,
        List<ServiceInterceptor> interceptors) {
      this.dispatcher = dispatcher;
      this.service = service;
      this.transport = transport;
      this.context = context;
      this.interceptors = interceptors;
    }

    @Override
    public void run() {
      try {
        dispatcher.server.getInterceptorSupportService().process(context, service, interceptors);
      } finally {
        dispatcher.writeResponse(context, transport);
      }
    }

  }



  private void setException(ServiceContext sc, Throwable e) {
    ServiceContextUtils.getResponseBody(sc).setResult(e);
    ServiceContextUtils.getResponseHeader(sc).setException(true);
  }

  // 初始化context
  private ServiceContext initContext(Req request, ProtocolRequestHeader header,
      ServerTransport<Req, Rsp> transport) {

    ServiceContext context = new DefaultServiceContext(ProcessPhase.SERVER_CALL_SERVICE);
    ServiceContextUtils.setServerHost(context,
        transport.getLocalAddress().getAddress().getHostAddress());
    ServiceContextUtils.setServerPort(context, transport.getLocalAddress().getPort());

    ServiceContextUtils.setClientHost(context,
        transport.getRemoteAddress().getAddress().getHostAddress());
    ServiceContextUtils.setClientPort(context, transport.getRemoteAddress().getPort());
    ServiceContextUtils.setResponseBody(context, new ProtocolResponseBody());
    ServiceContextUtils.setResponseHeader(context, new ProtocolResponseHeader());
    ServiceContextUtils.setRequestHeader(context, header);

    try {
      RequestConvertor<Req> extractor = server.getProtocolExtensionFactory().getRequestConvertor();

      // 验证所需的参数
      validateRequestInfo(header);
      EndpointConfig config = server.getServiceConfig(header.getApp(), header.getServiceName(),
          header.getVersion(), header.getGroup());
      if (config == null) {
        throw new ServiceProviderConfigNotFoundException(header.getApp(), header.getServiceName(),
            header.getVersion(), header.getGroup());
      }
      ServiceProviderDescriptor sd = server.getServiceDescriptor(header.getApp(),
          header.getServiceName(), header.getVersion(), header.getGroup());
      String[] types = sd.getParamTypes();
      if (types != null && types.length > 0) {
        Class<?>[] typeClss = new Class<?>[types.length];
        int len = types.length;
        for (int i = 0; i < len; ++i) {
          typeClss[i] = Utils.findClassByName(types[i]);
        }
        ProtocolRequestBody reqbody = extractor.parseProtocolBody(request, header, typeClss);
        ServiceContextUtils.setRequestBody(context, reqbody);
      } else {
        ProtocolRequestBody reqbody = extractor.parseProtocolBody(request, header, null);
        ServiceContextUtils.setRequestBody(context, reqbody);
      }
      ServiceContextUtils.setProviderConfig(context, new ReadOnlyEndpointConfig(config));

      // 用自身的配置

    } catch (Exception e) {
      ServiceContextUtils.getResponseBody(context).setResult(e);
      ServiceContextUtils.getResponseHeader(context).setException(true);
    }

    return context;

  }

  private void validateRequestInfo(ProtocolRequestHeader header) {
    if (StringUtils.isBlank(header.getApp()))
      throw new LossReqParamException("app");
    if (StringUtils.isBlank(header.getVersion()))
      throw new LossReqParamException("version");
    if (StringUtils.isBlank(header.getServiceName()))
      throw new LossReqParamException("service name");
    if (StringUtils.isBlank(header.getGroup()))
      throw new LossReqParamException("group");

  }

  private void writeResponse(ServiceContext sc, ServerTransport<Req, Rsp> transport) {
    try {
      ProtocolResponseBody response = ServiceContextUtils.getResponseBody(sc);
      ProtocolRequestHeader requestAttchments = ServiceContextUtils.getRequestHeader(sc);
      // 回传的attachment
      Attachments allAttachment = requestAttchments.getAttachments();
      if (allAttachment != null) {
        Attachments attachments = ServiceContextUtils.getResponseHeader(sc).getAttachments();
        attachments.putAttachments(allAttachment);
      }
      if (!response.isDone())
        setException(sc, new NotSetResultException());
      if (ServiceContextUtils.getResponseHeader(sc).isException()) {
        Throwable e = (Throwable) response.getResult();
        if (!(e instanceof RpcException)) {
          setException(sc, new UnknownException(Utils.getActualException(e)));

        }
      }
      ResponseConvertor<Rsp> attrWriter =
          server.getProtocolExtensionFactory().getResponseConvertor();
      if (ServiceContextUtils.getResponseHeader(sc).isException()) {
        EndpointConfig config = ServiceContextUtils.getProviderConfig(sc);
        String expCon = ((config == null) ? "default"
            : config.get(ConfigConstans.PROVIDER_EXCEPTION_CONVERTOR, "default"));
        ExceptionConvertor convert =
            ExtensionLoaderConstants.EXCEPTION_CONVERTOR_EXTENSION.findExtension(expCon);
        ExceptionResult er = convert.convertTo((RpcException) response.getResult());
        ServiceContextUtils.getResponseBody(sc).setResult(er);
      }
      Rsp rsp = attrWriter.createResponse(ServiceContextUtils.getResponseHeader(sc), response);
      // TODO 需要统计异常以及成功的次数
      transport.send(rsp, sc);
    } catch (Exception e) {
      logger.error("write response error:", e);
      return;
    }
  }

  public RequestDispatcher(AbstractServer<Req, Rsp> server) {
    this.server = server;
  }

}
