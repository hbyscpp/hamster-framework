package com.seaky.hamster.core.rpc.client;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.exception.CancelConnectToRemoteServerException;
import com.seaky.hamster.core.rpc.exception.CancelSendToRemoteServer;
import com.seaky.hamster.core.rpc.exception.ErrorConnectRemoteServerException;
import com.seaky.hamster.core.rpc.exception.ErrorSendToRemoteServerException;
import com.seaky.hamster.core.rpc.exception.RpcException;
import com.seaky.hamster.core.rpc.interceptor.InterceptorSupportService;
import com.seaky.hamster.core.rpc.interceptor.ServiceInterceptor;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.RequestExtractor;
import com.seaky.hamster.core.rpc.utils.Utils;

public class ClientInterceptorService<Req, Rsp> extends
		InterceptorSupportService<Req, Rsp> {

	private static Logger logger = LoggerFactory
			.getLogger(ClientInterceptorService.class);

	public ClientInterceptorService(
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
		super(protocolExtensionFactory);
	}

	// 真正调用远程服务
	public SettableFuture<Object> process(final ServiceContext sc,
			final ClientTransport<Req, Rsp> transport, final Executor executor,
			final List<ServiceInterceptor> interceptors) {
		final SettableFuture<Object> result = SettableFuture.create();
		
		try {
			// 连接
			final SettableFuture<Void> connectFuture = transport.connect(sc);
			if (connectFuture.isDone()) {
				connectHandler(result, connectFuture, transport, executor, sc,
						interceptors);
			} else {
				connectFuture.addListener(new Runnable() {

					@Override
					public void run() {
						connectHandler(result, connectFuture, transport,
								executor, sc, interceptors);
					}
				}, executor);
			}

			return result;
		} catch (Exception e) {
			postProcess(sc, interceptors, interceptors.size(), e);
			setFuture(sc, result);
			return result;
		}

	}

	private void connectHandler(final SettableFuture<Object> result,
			final SettableFuture<Void> connectFuture,
			final ClientTransport<Req, Rsp> transport, final Executor executor,
			final ServiceContext sc, final List<ServiceInterceptor> interceptors) {
		try {
			connectFuture.get();
		} catch (InterruptedException e) {
			// 连接请求被中断，需要用户自行处理，非业务异常
			CancelConnectToRemoteServerException proex = new CancelConnectToRemoteServerException(
					sc.getAttribute(ServiceContext.SERVICENAME, String.class),
					Utils.socketAddrToString(transport.getRemoteAddress()), e);
			ServiceContextUtils.getResponse(sc).setResult(proex);
			setFuture(sc, result);
			Thread.currentThread().interrupt();
			return;
		} catch (CancellationException e) {
			// 连接请求被中断，需要用户自行处理，非业务异常
			CancelConnectToRemoteServerException proex = new CancelConnectToRemoteServerException(
					sc.getAttribute(ServiceContext.SERVICENAME, String.class),
					Utils.socketAddrToString(transport.getRemoteAddress()), e);
			ServiceContextUtils.getResponse(sc).setResult(proex);
			setFuture(sc, result);
			return;
		} catch (ExecutionException e) {
			// 连接请求被中断，需要用户自行处理，非业务异常
			logger.error("connect remote server error ", e.getCause());
			ErrorConnectRemoteServerException proex = new ErrorConnectRemoteServerException(
					sc.getAttribute(ServiceContext.SERVICENAME, String.class),
					Utils.socketAddrToString(transport.getRemoteAddress()),
					e.getCause());
			ServiceContextUtils.getResponse(sc).setResult(proex);
			setFuture(sc, result);
			return;
		}
		if (interceptors != null && interceptors.size() > 0) {
			if (!preProcess(sc, interceptors)) {
				setFuture(sc, result);
				return;
			}
		}
		// 连接成功了,写网络
		try {
			RequestExtractor<Req> reqWriter = protocolExtensionFactory
					.getRequestExtractor();
			Req req = reqWriter.extractFrom(sc);
			final SettableFuture<Rsp> r = transport.send(req, sc);

			r.addListener(new Runnable() {
				@Override
				public void run() {
					sendHandler(sc, interceptors, result, r);
				}
			}, executor);
		} catch (Exception e) {
			postProcess(sc, interceptors, e);
			setFuture(sc, result);
		}

	}

	private void sendHandler(final ServiceContext sc,
			final List<ServiceInterceptor> interceptors,
			final SettableFuture<Object> result, final SettableFuture<Rsp> r) {
		// 查看是否存在网络异常,或者超时
		try {
			r.get();
			// 代表访问成功
			postProcess(sc, interceptors, null);
			setFuture(sc, result);
		} catch (CancellationException e) {
			CancelSendToRemoteServer e1 = new CancelSendToRemoteServer(
					sc.getAttribute(ServiceContext.SERVICENAME, String.class),
					Utils.generateKey(sc.getAttribute(
							ServiceContext.SERVER_HOST, String.class), String
							.valueOf(sc.getAttribute(
									ServiceContext.SERVER_PORT, int.class))), e);
			postProcess(sc, interceptors, e1);
			setFuture(sc, result);
		} catch (InterruptedException e2) {
			CancelSendToRemoteServer e1 = new CancelSendToRemoteServer(
					sc.getAttribute(ServiceContext.SERVICENAME, String.class),
					Utils.generateKey(sc.getAttribute(
							ServiceContext.SERVER_HOST, String.class), String
							.valueOf(sc.getAttribute(
									ServiceContext.SERVER_PORT, int.class))),
					e2);
			postProcess(sc, interceptors, e1);
			setFuture(sc, result);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e2) {
			Throwable innere = e2.getCause();
			if (innere instanceof RpcException) {
				postProcess(sc, interceptors, innere);
			} else {
				ErrorSendToRemoteServerException e1 = new ErrorSendToRemoteServerException(
						sc.getAttribute(ServiceContext.SERVICENAME,
								String.class), Utils.generateKey(sc
								.getAttribute(ServiceContext.SERVER_HOST,
										String.class), String.valueOf(sc
								.getAttribute(ServiceContext.SERVER_PORT,
										int.class))), innere);
				postProcess(sc, interceptors, e1);
			}
			setFuture(sc, result);
		}

	}

}
