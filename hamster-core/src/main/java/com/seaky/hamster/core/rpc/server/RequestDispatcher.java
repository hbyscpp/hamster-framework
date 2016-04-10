package com.seaky.hamster.core.rpc.server;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RejectedExecutionException;

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
import com.seaky.hamster.core.rpc.exception.ServerResourceIsFullException;
import com.seaky.hamster.core.rpc.exception.ServiceProviderConfigNotFoundException;
import com.seaky.hamster.core.rpc.exception.ServiceProviderNotFoundException;
import com.seaky.hamster.core.rpc.exception.ServiceSigMismatchException;
import com.seaky.hamster.core.rpc.exception.UnknownException;
import com.seaky.hamster.core.rpc.executor.ServiceThreadpool;
import com.seaky.hamster.core.rpc.interceptor.ProcessPhase;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.ExceptionConvertor;
import com.seaky.hamster.core.rpc.protocol.ExceptionResult;
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaService;
/**
 * server的请求分发，一个jvm中所有server 共享一个dispatcher线程池
 *dispatcher主要用于协议的解析，ServiceContext的创建
 * 
 * @author seaky
 * @since 1.0.0
 * @Date Apr 6, 2016
 */
public class RequestDispatcher<Req, Rsp> {

  private AbstractServer<Req, Rsp> server;

  private static Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

  // 分发消息
  public void dispatchMessages(List<Req> requests, ServerTransport<Req, Rsp> transport) {
    try {
      if (requests.size() == 1) {
        // 单个消息
        ServerResourceManager.getDispatcherPool()
            .execute(new MessageTask<Req, Rsp>(requests.get(0), this, transport));
      } else {
        ServerResourceManager.getDispatcherPool()
            .execute(new MessagesTask<Req, Rsp>(requests, this, transport));
      }
    } catch (RejectedExecutionException e) {
      for (Req req : requests) {
        ServiceContext sc = initContext(req, transport);
        setException(sc, new ServerResourceIsFullException("dispatcher threadpool"));
        writeResponse(sc, transport);
      }
    }
  }

  public static class MessagesTask<Req, Rsp> implements Runnable {

    private List<Req> requests;

    private RequestDispatcher<Req, Rsp> dispatcher;

    private ServerTransport<Req, Rsp> transport;

    public MessagesTask(List<Req> requests, RequestDispatcher<Req, Rsp> dispatcher,
        ServerTransport<Req, Rsp> transport) {
      this.requests = requests;
      this.dispatcher = dispatcher;
      this.transport = transport;
    }

    @Override
    public void run() {
      if (requests == null || requests.size() == 0)
        return;
      for (Req req : requests) {
        MessageTask<Req, Rsp> subTask = new MessageTask<Req, Rsp>(req, dispatcher, transport);
        try {
          ServerResourceManager.getDispatcherPool().execute(subTask);
        } catch (RejectedExecutionException e) {
          ServiceContext sc = dispatcher.initContext(req, transport);
          dispatcher.setException(sc, new ServerResourceIsFullException("dispatcher threadpool"));
          dispatcher.writeResponse(sc, transport);
        }
      }

    }

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

  // 处理单个消息
  public static class MessageTask<Req, Rsp> implements Runnable {

    private Req request;

    private RequestDispatcher<Req, Rsp> dispatcher;

    private ServerTransport<Req, Rsp> transport;

    public MessageTask(Req request, RequestDispatcher<Req, Rsp> dispatcher,
        ServerTransport<Req, Rsp> transport) {
      this.request = request;
      this.dispatcher = dispatcher;
      this.transport = transport;
    }

    public void run() {
      ServiceContext context = dispatcher.initContext(request, transport);
      if (ServiceContextUtils.getResponse(context).isException()) {
        dispatcher.writeResponse(context, transport);
        return;
      }
      String serviceName = ServiceContextUtils.getServiceName(context);
      String app = ServiceContextUtils.getApp(context);
      String serviceVersion = ServiceContextUtils.getVersion(context);
      String group = ServiceContextUtils.getGroup(context);
      JavaService service = dispatcher.server.findService(serviceName, app, serviceVersion, group);
      List<ServiceInterceptor> interceptors =
          dispatcher.server.getServiceInterceptor(serviceName, app, serviceVersion, group);
      if (service == null) {
        dispatcher.setException(context, new ServiceProviderNotFoundException(serviceName));
        dispatcher.writeResponse(context, transport);
        return;
      }

      EndpointConfig sc = ServiceContextUtils.getProviderConfig(context);
      String[] params = dispatcher.server
          .getServiceDescriptor(app, serviceName, serviceVersion, group).getParamTypes();
      if (!Utils.isMatch(params,
          context.getAttribute(ServiceContext.REQUEST_PARAMS, Object[].class))) {
        dispatcher.setException(context, new ServiceSigMismatchException(serviceName));
        dispatcher.writeResponse(context, transport);
        return;
      }
      // 1判断是否当前线程执行 还是线程池执行
      // 开始执行,当前线程还是线程池
      boolean useDispatcherThread =
          sc.getValueAsBoolean(ConfigConstans.PROVIDER_DISPATCHER_THREAD_EXE, false);
      ServiceRunner<Req, Rsp> sr =
          new ServiceRunner<Req, Rsp>(dispatcher, service, transport, context, interceptors);
      if (!useDispatcherThread) {

        String name = sc.get(ConfigConstans.PROVIDER_THREADPOOL_NAME);
        // 线程池执行
        ServiceThreadpool pool = null;
        if (name == null || ConfigConstans.PROVIDER_THREADPOOL_NAME_DEFAULT.equals(name)) {
          pool = ServerResourceManager.getServiceThreadpoolManager().createDefault();
        } else {
          int maxQueue = sc.getValueAsInt(ConfigConstans.PROVIDER_THREADPOOL_MAXQUEUE,
              ConfigConstans.PROVIDER_THREADPOOL_MAXQUEUE_DEFAULT);

          int maxThread = sc.getValueAsInt(ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE,
              ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE_DEFAULT);

          pool =
              ServerResourceManager.getServiceThreadpoolManager().create(name, maxThread, maxQueue);
        }
        try {
          pool.execute(sr);
        } catch (RejectedExecutionException e) {

          dispatcher.setException(context,
              new ServerResourceIsFullException("service threadpool name is " + pool.getName()));
          dispatcher.writeResponse(context, transport);
        }
      } else {
        // 当前线程执行
        sr.run();
      }

    }
  }

  private void setException(ServiceContext sc, Throwable e) {
    ServiceContextUtils.getResponse(sc).setResult(e);
  }

  // 初始化context
  private ServiceContext initContext(Req request, ServerTransport<Req, Rsp> transport) {

    ServiceContext context = new DefaultServiceContext(ProcessPhase.SERVER_CALL_SERVICE);
    ServiceContextUtils.setServerHost(context,
        transport.getLocalAddress().getAddress().getHostAddress());
    ServiceContextUtils.setServerPort(context, transport.getLocalAddress().getPort());

    ServiceContextUtils.setClientHost(context,
        transport.getRemoteAddress().getAddress().getHostAddress());
    ServiceContextUtils.setClientPort(context, transport.getRemoteAddress().getPort());
    ServiceContextUtils.setResponse(context, new Response());
    ServiceContextUtils.setRequestAttachments(context, new Attachments());

    try {
      server.getProtocolExtensionFactory().getRequestExtractor().extractTo(request, context);
      // 验证所需的参数
      validateRequestInfo(context);
      // 用自身的配置
      EndpointConfig config = server.getServiceConfig(ServiceContextUtils.getApp(context),
          ServiceContextUtils.getServiceName(context), ServiceContextUtils.getVersion(context),
          ServiceContextUtils.getGroup(context));

      if (config == null) {
        throw new ServiceProviderConfigNotFoundException(ServiceContextUtils.getApp(context),
            ServiceContextUtils.getServiceName(context), ServiceContextUtils.getVersion(context),
            ServiceContextUtils.getGroup(context));
      }
      ServiceContextUtils.setProviderConfig(context, new ReadOnlyEndpointConfig(config));
    } catch (Exception e) {
      ServiceContextUtils.getResponse(context).setResult(e);
    }

    return context;

  }

  private void validateRequestInfo(ServiceContext sc) {
    if (StringUtils.isBlank(ServiceContextUtils.getApp(sc)))
      throw new LossReqParamException("app");
    if (StringUtils.isBlank(ServiceContextUtils.getVersion(sc)))
      throw new LossReqParamException("version");
    if (StringUtils.isBlank(ServiceContextUtils.getReferenceApp(sc)))
      throw new LossReqParamException("reference app");
    if (StringUtils.isBlank(ServiceContextUtils.getServiceName(sc)))
      throw new LossReqParamException("service name");
    if (StringUtils.isBlank(ServiceContextUtils.getReferenceVersion(sc)))
      throw new LossReqParamException("reference version");
    if (StringUtils.isBlank(ServiceContextUtils.getGroup(sc)))
      throw new LossReqParamException("group");
    if (StringUtils.isBlank(ServiceContextUtils.getReferenceGroup(sc)))
      throw new LossReqParamException("reference group");
  }

  private void writeResponse(ServiceContext sc, ServerTransport<Req, Rsp> transport) {
    try {
      Response response = ServiceContextUtils.getResponse(sc);
      Attachments requestAttchments = ServiceContextUtils.getRequestAttachments(sc);

      Map<String, String> allAttachment = requestAttchments.getAllAttachments();
      if (allAttachment != null) {
        for (Entry<String, String> attach : allAttachment.entrySet()) {
          ServiceContextUtils.getResponse(sc).getAttachments().addAttachment(attach.getKey(),
              attach.getValue());
        }
      }
      if (!response.isDone())
        setException(sc, new NotSetResultException());
      if (response.isException()) {
        Throwable e = response.getException();

        if (!(e instanceof RpcException)) {
          setException(sc, new UnknownException(Utils.getActualException(e)));

        }
      }
      ResponseConvertor<Rsp> attrWriter =
          server.getProtocolExtensionFactory().getResponseConvertor();
      Response r = ServiceContextUtils.getResponse(sc);
      if (r.isException()) {
        String expCon = ServiceContextUtils.getProviderConfig(sc)
            .get(ConfigConstans.PROVIDER_EXCEPTION_CONVERTOR, "default");
        ExceptionConvertor convert =
            ExtensionLoaderConstants.EXCEPTION_CONVERTOR_EXTENSION.findExtension(expCon);
        ExceptionResult er = convert.convertTo((RpcException) r.getException());
        ServiceContextUtils.getResponse(sc).setResult(er);
      }
      Rsp rsp = attrWriter.convertTo(ServiceContextUtils.getResponse(sc));
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
