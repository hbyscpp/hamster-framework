package com.seaky.hamster.core.rpc.server;

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
import com.seaky.hamster.core.rpc.interceptor.ServerServiceCallContext;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.RequestInfo;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;
import com.seaky.hamster.core.rpc.protocol.ResponseInfo;
import com.seaky.hamster.core.rpc.protocol.ServiceRemoteRequest;
import com.seaky.hamster.core.rpc.protocol.ServiceRemoteResponse;
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
					ServerServiceCallContext sc = initContext(req, transport);
					setException(sc, new ServerResourceIsFullException(
							"dispatcher threadpool"));
					writeResponse(sc, transport);
			}
		}
	}

	public static class MessagesTask<Req, Rsp> implements Runnable {

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
		public void run() {
			if (requests == null || requests.size() == 0)
				return;
			for (Req req : requests) {
				MessageTask<Req, Rsp> subTask = new MessageTask<Req, Rsp>(req,
						dispatcher, transport);
				try {
					ServerResourceManager.getDispatcherPool().execute(subTask);
				} catch (RejectedExecutionException e) {
					ServerServiceCallContext sc = dispatcher.initContext(req,
							transport);
					dispatcher.setException(sc,
							new ServerResourceIsFullException(
									"dispatcher threadpool"));
					dispatcher.writeResponse(sc, transport);
				}
			}

		}

	}

	private static class ServiceRunner<Req, Rsp> implements Runnable {

		private RequestDispatcher<Req, Rsp> dispatcher;

		private JavaService service;

		private ServerTransport<Req, Rsp> transport;

		private ServerServiceCallContext context;


		private List<ServiceInterceptor> interceptors;

		public ServiceRunner(RequestDispatcher<Req, Rsp> dispatcher,
				JavaService service, ServerTransport<Req, Rsp> transport,
				ServerServiceCallContext context,
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
				dispatcher.server.getInterceptorSupportService().process(
						context, service, interceptors);
			} finally {
				dispatcher.writeResponse(context, transport);
			}
		}

	}

	// 处理单个消息
	@SuppressWarnings("serial")
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
			ServerServiceCallContext context = dispatcher.initContext(request,
					transport);
			if (context.getResponse().isException()) {
				dispatcher.writeResponse(context, transport);
				return;
			}
			String serviceName = context.getServiceName();
			String app = context.getApp();
			String serviceVersion = context.getVersion();
			String group = context.getGroup();
			JavaService service = dispatcher.server.findService(serviceName,
					app, serviceVersion, group);
			List<ServiceInterceptor> interceptors = dispatcher.server
					.getServiceInterceptor(serviceName, app, serviceVersion,
							group);
			if (service == null) {
				dispatcher.setException(context, new ServiceNotFoundException(
						context.getServiceName()));
				dispatcher.writeResponse(context, transport);
				return;
			}

			EndpointConfig sc = context.getServiceConfig();

			if (!Utils.isMatch(service.paramTypes(), context.getRequest()
					.getParams())) {
				dispatcher.setException(
						context,
						new ServiceSigMismatchException(context
								.getServiceName()));
				dispatcher.writeResponse(context, transport);
				return;
			}
			// 1判断是否当前线程执行 还是线程池执行
			// 开始执行,当前线程还是线程池
			boolean useDispatcherThread = sc.getValueAsBoolean(
					ConfigConstans.PROVIDER_DISPATCHER_THREAD_EXE, false);
			ServiceRunner<Req, Rsp> sr = new ServiceRunner<Req, Rsp>(
					dispatcher, service, transport, context,
					interceptors);
			if (!useDispatcherThread) {

				String name = sc.get(ConfigConstans.PROVIDER_THREADPOOL_NAME);
				// 线程池执行
				ServiceThreadpool pool = null;
				if (name == null
						|| ConfigConstans.PROVIDER_THREADPOOL_NAME_DEFAULT
								.equals(name)) {
					pool = ServerResourceManager.getServiceThreadpoolManager()
							.createDefault();
				} else {
					int maxQueue = sc
							.getValueAsInt(
									ConfigConstans.PROVIDER_THREADPOOL_MAXQUEUE,
									ConfigConstans.PROVIDER_THREADPOOL_MAXQUEUE_DEFAULT);

					int maxThread = sc.getValueAsInt(
							ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE,
							ConfigConstans.PROVIDER_THREADPOOL_MAXSIZE_DEFAULT);

					pool = ServerResourceManager.getServiceThreadpoolManager()
							.create(name, maxThread, maxQueue);
				}
				try {
					pool.execute(sr);
				} catch (RejectedExecutionException e) {

					dispatcher.setException(
							context,
							new ServerResourceIsFullException(
									"service threadpool name is "
											+ pool.getName()));
					dispatcher.writeResponse(context, transport);
				}
			} else {
				// 当前线程执行
				sr.run();
			}

		}
	}

	private void setException(ServerServiceCallContext sc, Throwable e) {
		sc.getResponse().setResult(e);
	}

	// 初始化context
	private ServiceContext initContext(Req request,
			ServerTransport<Req, Rsp> transport) {

		ServerServiceCallContext context = new ServerServiceCallContext();
		try {
			ServiceRemoteRequest serviceRequest = null;
			serviceRequest = server.getProtocolExtensionFactory()
					.getRequestInfoExtractor().extract(request);

			// 验证所需的参数
			validateRequestInfo(serviceRequest);
			// 用自身的配置
			EndpointConfig config = server.getServiceConfig(
					serviceRequest.getApp(), serviceRequest.getServiceName(),
					serviceRequest.getVersion(), serviceRequest.getGroup());
			if (config == null) {
				throw new ServiceConfigNotFoundException(
						serviceRequest.getApp(),
						serviceRequest.getServiceName(),
						serviceRequest.getVersion(), serviceRequest.getGroup());
			}
		} catch (Exception e) {
			context.getResponse().setResult(e);
		}

		return context;

	}

	private void validateRequestInfo(ServiceRemoteRequest request) {
		if (request == null)
			throw new LossReqParamException("app");
		if (StringUtils.isBlank(request.getApp()))
			throw new LossReqParamException("app");
		if (StringUtils.isBlank(request.getVersion()))
			throw new LossReqParamException("version");
		if (StringUtils.isBlank(request.getReferenceApp()))
			throw new LossReqParamException("reference app");
		if (StringUtils.isBlank(request.getServiceName()))
			throw new LossReqParamException("service name");
		if (StringUtils.isBlank(request.getReferenceVersion()))
			throw new LossReqParamException("reference version");
		if (StringUtils.isBlank(request.getGroup()))
			throw new LossReqParamException("group");
		if (StringUtils.isBlank(request.getReferenceApp()))
			throw new LossReqParamException("reference group");
	}

	private void writeResponse(ServiceContext sc,
			ServerTransport<Req, Rsp> transport) {
		try {
			ResponseInfo response = (ResponseInfo) sc.getAttribute(ServiceContext.RESPONSE_RESULT);
			if (sc.getRequest() != null) {
				response.addAttchment(Constants.SEQ_NUM_KEY, sc.getRequest()
						.getAttachment(Constants.SEQ_NUM_KEY));
			}
			if (!response.isDone())
				setException(sc, new NotSetResultException());
			ResponseConvertor<Req, Rsp> attrWriter = server
					.getProtocolExtensionFactory().getResponseAttrWriter();
			Rsp rsp = attrWriter.convert(response);
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
