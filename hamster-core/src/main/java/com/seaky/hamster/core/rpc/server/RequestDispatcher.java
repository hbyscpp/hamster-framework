package com.seaky.hamster.core.rpc.server;

import io.netty.util.internal.chmv8.ForkJoinTask;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.exception.LossReqParamException;
import com.seaky.hamster.core.rpc.exception.NotSetResultException;
import com.seaky.hamster.core.rpc.exception.ReferConfigNotFoundException;
import com.seaky.hamster.core.rpc.exception.ServerResourceIsFullException;
import com.seaky.hamster.core.rpc.exception.ServiceConfigNotFoundException;
import com.seaky.hamster.core.rpc.exception.ServiceNotFoundException;
import com.seaky.hamster.core.rpc.exception.ServiceSigMismatchException;
import com.seaky.hamster.core.rpc.exception.UnknowException;
import com.seaky.hamster.core.rpc.executor.ServiceThreadpool;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.RequestInfo;
import com.seaky.hamster.core.rpc.protocol.ResponseAttributeWriter;
import com.seaky.hamster.core.rpc.protocol.ResponseInfo;
import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;
import com.seaky.hamster.core.rpc.utils.Utils;
import com.seaky.hamster.core.service.JavaService;
import com.seaky.hamster.core.service.ServiceContext;

public class RequestDispatcher<Req, Rsp> {

	private AbstractServer<Req, Rsp> server;

	private static Logger logger = LoggerFactory
			.getLogger(RequestDispatcher.class);

	// 分发消息
	public void dispatchMessages(List<Req> requests,
			ServerTransport<Req, Rsp> transport) {
		try {
			if (requests.size() == 1) {
				// 单个消息
				ServerResourceManager.getDispatcherPool().execute(
						new MessageTask<Req, Rsp>(requests.get(0), this,
								transport));
			} else {
				ServerResourceManager.getDispatcherPool().execute(
						new MessagesTask<Req, Rsp>(requests, this, transport));
			}
		} catch (RejectedExecutionException e) {
			for (Req req : requests) {
				ServiceContext sc = initContext(req, transport);
				setException(sc, new ServerResourceIsFullException(
						"dispatcher threadpool"));
				writeResponse(req, sc, transport);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class MessagesTask<Req, Rsp> extends ForkJoinTask<Void> {

		private List<Req> requests;

		private RequestDispatcher<Req, Rsp> dispatcher;

		private ServerTransport<Req, Rsp> transport;

		public MessagesTask(List<Req> requests,
				RequestDispatcher<Req, Rsp> dispatcher,
				ServerTransport<Req, Rsp> transport) {
			this.requests = requests;
			this.dispatcher = dispatcher;
			this.transport = transport;
		}

		@Override
		public Void getRawResult() {
			return null;
		}

		@Override
		protected void setRawResult(Void value) {

		}

		@Override
		protected boolean exec() {
			if (requests == null || requests.size() == 0)
				return true;
			for (Req req : requests) {
				MessageTask<Req, Rsp> subTask = new MessageTask<Req, Rsp>(req,
						dispatcher, transport);
				try {
					subTask.fork();
				} catch (RejectedExecutionException e) {
					ServiceContext sc = dispatcher.initContext(req, transport);
					dispatcher.setException(sc,
							new ServerResourceIsFullException(
									"dispatcher threadpool"));
					dispatcher.writeResponse(req, sc, transport);
				}
			}
			return true;

		}

	}

	private static class ServiceRunner<Req, Rsp> implements Runnable {

		private RequestDispatcher<Req, Rsp> dispatcher;

		private JavaService service;

		private ServerTransport<Req, Rsp> transport;

		private ServiceContext context;

		private Req request;

		private List<ServiceInterceptor> interceptors;

		public ServiceRunner(RequestDispatcher<Req, Rsp> dispatcher,
				JavaService service, ServerTransport<Req, Rsp> transport,
				ServiceContext context, Req request,
				List<ServiceInterceptor> interceptors) {
			this.dispatcher = dispatcher;
			this.service = service;
			this.transport = transport;
			this.context = context;
			this.request = request;
			this.interceptors = interceptors;
		}

		@Override
		public void run() {
			try {
				dispatcher.server.getInterceptorSupportService().process(
						context, service, interceptors);
			} finally {
				dispatcher.writeResponse(request, context, transport);
			}
		}

	}

	// 处理单个消息
	@SuppressWarnings("serial")
	public static class MessageTask<Req, Rsp> extends ForkJoinTask<Void> {

		private Req request;

		private RequestDispatcher<Req, Rsp> dispatcher;

		private ServerTransport<Req, Rsp> transport;

		public MessageTask(Req request, RequestDispatcher<Req, Rsp> dispatcher,
				ServerTransport<Req, Rsp> transport) {
			this.request = request;
			this.dispatcher = dispatcher;
			this.transport = transport;
		}

		@Override
		public Void getRawResult() {
			return null;
		}

		@Override
		protected void setRawResult(Void value) {
		}

		@Override
		protected boolean exec() {
			run();
			return true;
		}

		public void run() {
			ServiceContext context = dispatcher.initContext(request, transport);
			if (context.getResponseInfo().isException()) {
				// 初始化过程中有错误发生
				dispatcher.writeResponse(request, context, transport);
				return;
			}
			String serviceName = context.getRequestInfo().getServiceName();
			String app = context.getRequestInfo().getApp();
			String serviceVersion = context.getRequestInfo().getVersion();
			String group = context.getRequestInfo().getGroup();
			JavaService service = dispatcher.server.findService(serviceName,
					app, serviceVersion, group);
			List<ServiceInterceptor> interceptors = dispatcher.server
					.getServiceInterceptor(serviceName, app, serviceVersion,
							group);
			if (service == null) {
				dispatcher.setException(context, new ServiceNotFoundException(
						context.getServiceName()));
				dispatcher.writeResponse(request, context, transport);
				return;
			}

			EndpointConfig sc = context.getServiceConfig();

			if (!Utils.isMatch(service.paramTypes(), context.getRequestInfo()
					.getParams())) {
				dispatcher.setException(
						context,
						new ServiceSigMismatchException(context
								.getServiceName()));
				dispatcher.writeResponse(request, context, transport);
				return;
			}
			// 1判断是否当前线程执行 还是线程池执行
			// 开始执行,当前线程还是线程池
			boolean useDispatcherThread = sc
					.getValueAsBoolean(ConfigConstans.PROVIDER_DISPATCHER_THREAD_EXE,false);
			ServiceRunner<Req, Rsp> sr = new ServiceRunner<Req, Rsp>(
					dispatcher, service, transport, context, request,interceptors);
			if (!useDispatcherThread) {
				
				String name= sc
					.get(ConfigConstans.PROVIDER_THREADPOOL_NAME);
				// 线程池执行
				ServiceThreadpool pool = null;
				if (name == null || ConfigConstans.PROVIDER_THREADPOOL_NAME_DEFAULT.equals(name)) {
					pool = ServerResourceManager.getServiceThreadpoolManager()
							.createDefault();
				} else {
					int maxQueue= sc
							.getValueAsInt(ConfigConstans.PROVIDER_THREADPOOL_MAXQUEUE,ConfigConstans.PROVIDER_THREADPOOL_MAXQUEUE_DEFAULT);
					
					int maxThread= sc
							.getValueAsInt(ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE,ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE_DEFAULT);
					
					pool = ServerResourceManager.getServiceThreadpoolManager()
							.create(name,
									maxThread,
									maxQueue);
				}
				try {
					pool.execute(sr);
				} catch (RejectedExecutionException e) {

					dispatcher.setException(
							context,
							new ServerResourceIsFullException(
									"service threadpool name is "
											+ pool.getName()));
					dispatcher.writeResponse(request, context, transport);
				}
			} else {
				// 当前线程执行
				sr.run();
			}

		}
	}

	private void setException(ServiceContext sc, Throwable e) {
		if (!sc.getResponseInfo().isDone())
			return;
		sc.getResponseInfo().setResult(e);
	}

	// 初始化context
	private ServiceContext initContext(Req request,
			ServerTransport<Req, Rsp> transport) {
		RequestInfo info = null;
		EndpointConfig config = null;
		EndpointConfig referConfig = null;
		try {
			info = server.getProtocolExtensionFactory()
					.getRequestInfoExtractor().extract(request);

			// 验证所需的参数
			String errorParam = validateRequestInfo(info);
			if (errorParam != null) {
				// 错误的参数
				ServiceContext sc = new ServiceContext(info, null, null,
						transport.getLocalAddress(),
						transport.getRemoteAddress());
				setException(sc, new LossReqParamException(errorParam));
				return sc;
			}
			// 用自身的配置
			config = server.getServiceConfig(info.getApp(),
					info.getServiceName(), info.getVersion(), info.getGroup());
			if (config == null) {
				ServiceContext sc = new ServiceContext(info, null, null,
						transport.getLocalAddress(),
						transport.getRemoteAddress());
				setException(
						sc,
						new ServiceConfigNotFoundException(info.getApp(), info
								.getServiceName(), info.getVersion(), info
								.getGroup()));
				return sc;
			}

			ServiceReferenceDescriptor rd = server.getRegisterationService().findRefer(
					info.getReferApp(), info.getServiceName(),
					info.getReferVersion(), info.getReferGroup(),
					server.getProtocolExtensionFactory().protocolName(),
					transport.getRemoteAddress().getAddress().getHostAddress(),
					transport.getRemoteAddress().getPort(),
					server.getServerConfig().getHost(), server.getServerConfig().getPort());
			if (rd != null)
				referConfig = rd.getConfig();
			if (referConfig == null) {
				ServiceContext sc = new ServiceContext(info, config, null,
						transport.getLocalAddress(),
						transport.getRemoteAddress());
				setException(sc,
						new ReferConfigNotFoundException(info.getReferApp(),
								info.getServiceName(), info.getVersion()));
				return sc;
			}

			ServiceContext context = new ServiceContext(info, config,
					referConfig, transport.getLocalAddress(),
					transport.getRemoteAddress());
			return context;

		} catch (Exception e) {
			logger.error("init context error.", e);
			ServiceContext context = new ServiceContext(info, config,
					referConfig, transport.getLocalAddress(),
					transport.getRemoteAddress());
			setException(context, new UnknowException(e.getMessage()));
			return context;
		}
	}

	private String validateRequestInfo(RequestInfo info) {
		if (info == null)
			return "app";
		if (StringUtils.isBlank(info.getApp()))
			return "app";
		if (StringUtils.isBlank(info.getVersion()))
			return "service version";
		if (StringUtils.isBlank(info.getReferApp()))
			return "referApp";
		if (StringUtils.isBlank(info.getServiceName()))
			return "serviceName";
		if (StringUtils.isBlank(info.getReferVersion()))
			return "refer version";
		if (StringUtils.isBlank(info.getGroup()))
			return "group";
		if (StringUtils.isBlank(info.getReferApp()))
			return "refer group";
		return null;
	}

	private void writeResponse(Req req, ServiceContext sc,
			ServerTransport<Req, Rsp> transport) {
		try {
			ResponseInfo response = sc.getResponseInfo();
			if (sc.getRequestInfo() != null) {
				response.addAttchment(Constants.SEQ_NUM_KEY, sc
						.getRequestInfo().getAttachment(Constants.SEQ_NUM_KEY));
			}
			if (!response.isDone())
				setException(sc, new NotSetResultException());
			ResponseAttributeWriter<Req, Rsp> attrWriter = server
					.getProtocolExtensionFactory().getResponseAttrWriter();
			Rsp rsp = server.getProtocolExtensionFactory().createResponse();
			attrWriter.write(req, rsp, response);
			// TODO 需要统计异常以及成功的次数
			transport.send(rsp, sc);
		} catch (Exception e) {
			logger.error("write Response error:", e);
			return;
		}
	}

	public RequestDispatcher(AbstractServer<Req, Rsp> server) {
		this.server = server;
	}

}
