package com.seaky.hamster.core.rpc.client;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

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
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.trace.ClientCallExceptionTrace;
import com.seaky.hamster.core.rpc.utils.Utils;

public class ClientInterceptorService<Req, Rsp> extends
		InterceptorSupportService<Req, Rsp> {

	public ClientInterceptorService(
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
		super(protocolExtensionFactory);
	}

	// 真正调用远程服务
	public SettableFuture<Object> process(final ServiceContext sc,
			final ClientTransport<Req, Rsp> transport, final Executor executor,
			final List<ServiceInterceptor> interceptors) {
		final SettableFuture<Object> result = SettableFuture.create();
		if (interceptors != null && interceptors.size() > 0) {
			if (preProcess(sc, interceptors)) {
				setFuture(sc, result);
				return result;
			}
		}
		try {
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
			addException(sc, ClientCallExceptionTrace.CONNECT_SERVICE_INSTANCE,
					ServiceContextUtils.getServerHost(sc) + ":"
							+ ServiceContextUtils.getServerPort(sc), e);
			Response rsp = ServiceContextUtils.getResponse(sc);
			rsp.setResult(e);
			postProcess(sc, interceptors);
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
		} catch (InterruptedException | CancellationException e) {
			// 连接请求被中断，需要用户自行处理，非业务异常
			String addr = Utils
					.socketAddrToString(transport.getRemoteAddress());
			CancelConnectToRemoteServerException proex = new CancelConnectToRemoteServerException(
					ServiceContextUtils.getServiceName(sc), addr, e);
			addException(sc, ClientCallExceptionTrace.CONNECT_SERVICE_INSTANCE,
					addr, e);
			ServiceContextUtils.getResponse(sc).setResult(proex);
			setFuture(sc, result);
			if (e instanceof InterruptedException)
				Thread.currentThread().interrupt();
			return;
		} catch (ExecutionException e) {
			// 连接请求被中断，需要用户自行处理，非业务异常
			String addr = Utils
					.socketAddrToString(transport.getRemoteAddress());
			ErrorConnectRemoteServerException proex = new ErrorConnectRemoteServerException(
					ServiceContextUtils.getServiceName(sc), addr, e.getCause());
			addException(sc, ClientCallExceptionTrace.CONNECT_SERVICE_INSTANCE,
					addr, e.getCause());
			ServiceContextUtils.getResponse(sc).setResult(proex);
			setFuture(sc, result);
			return;
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
			addException(sc, ClientCallExceptionTrace.SEND_SERVICE_INSTANCE,
					Utils.socketAddrToString(transport.getRemoteAddress()),
					e.getCause());
			ServiceContextUtils.getResponse(sc).setResult(e);
			postProcess(sc, interceptors);
			setFuture(sc, result);
		}

	}

	private void sendHandler(final ServiceContext sc,
			final List<ServiceInterceptor> interceptors,
			final SettableFuture<Object> result, final SettableFuture<Rsp> r) {
		// 查看是否存在网络异常,或者超时
		try {
			r.get();
		} catch (CancellationException | InterruptedException e) {
			CancelSendToRemoteServer e1 = new CancelSendToRemoteServer(
					ServiceContextUtils.getServiceName(sc), Utils.generateKey(
							ServiceContextUtils.getServerHost(sc), String
									.valueOf(ServiceContextUtils
											.getServerPort(sc))), e);
			addException(sc, ClientCallExceptionTrace.SEND_SERVICE_INSTANCE,
					Utils.generateKey(ServiceContextUtils.getServerHost(sc),
							String.valueOf(ServiceContextUtils
									.getServerPort(sc))), e);
			ServiceContextUtils.getResponse(sc).setResult(e1);
			if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
			}
		} catch (ExecutionException e2) {
			Throwable innere = e2.getCause();
			addException(sc, ClientCallExceptionTrace.SEND_SERVICE_INSTANCE,
					Utils.generateKey(ServiceContextUtils.getServerHost(sc),
							String.valueOf(ServiceContextUtils
									.getServerPort(sc))), innere);
			if (innere instanceof RpcException) {
				ServiceContextUtils.getResponse(sc).setResult(innere);
			} else {
				ErrorSendToRemoteServerException e1 = new ErrorSendToRemoteServerException(
						ServiceContextUtils.getServiceName(sc),
						Utils.generateKey(
								ServiceContextUtils.getServerHost(sc), String
										.valueOf(ServiceContextUtils
												.getServerPort(sc))), innere);
				ServiceContextUtils.getResponse(sc).setResult(e1);
			}
		} finally {
			postProcess(sc, interceptors);
			setFuture(sc, result);
		}

	}

}
