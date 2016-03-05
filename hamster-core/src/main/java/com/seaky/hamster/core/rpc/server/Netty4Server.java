package com.seaky.hamster.core.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * netty4实现网络服务 负责解析req，提交给执行者执行 Netty4Server所有实例共享一组监听线程，共享一组io线程
 * 
 * @author seaky
 * @version @param <Req> servire请求
 * @version @param <Rsp> service响应
 * @since 1.0.0
 */
public class Netty4Server<Req, Rsp> extends AbstractServer<Req, Rsp> {

	private  Lock lock = new ReentrantLock();

	private ChannelFuture future;

	private static Logger logger = LoggerFactory.getLogger(Netty4Server.class);

	private ServerChannelPipelineConfigurator<Req, Rsp, NettyServerTransport<Req, Rsp>> channelPipelineConfigurator;


	private void init(ServerConfig config) throws InterruptedException {
		ServerBootstrap b = new ServerBootstrap();
		b.option(ChannelOption.SO_BACKLOG, config.getSoBacklog());
		b.option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay());
		b.group(ServerResourceManager.getBossGroup(), ServerResourceManager.getWorkerGroup()).channel(NioServerSocketChannel.class);
		b.childHandler(initChannel());
		b.localAddress(config.getHost(),config.getPort());
		future = b.bind().sync();
	}

	private ChannelInitializer<SocketChannel> initChannel() {

		return new ChildChannelInitializer<Req, Rsp>(this,
				channelPipelineConfigurator);

	}

	private static class ChildChannelInitializer<Req, Rsp> extends
			ChannelInitializer<SocketChannel> {

		private Netty4Server<Req, Rsp> server;

		private ServerChannelPipelineConfigurator<Req, Rsp, NettyServerTransport<Req, Rsp>> channelPipelineConfigurator;

		public ChildChannelInitializer(
				Netty4Server<Req, Rsp> server,
				ServerChannelPipelineConfigurator<Req, Rsp, NettyServerTransport<Req, Rsp>> channelPipelineConfigurator) {
			this.server = server;
			this.channelPipelineConfigurator = channelPipelineConfigurator;
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			NettyServerTransport<Req, Rsp> transport = new NettyServerTransport<Req, Rsp>(
					ch, server);
			channelPipelineConfigurator.config(transport, ch.pipeline());
		}

	}

	public Netty4Server(
			ServerChannelPipelineConfigurator<Req, Rsp, NettyServerTransport<Req, Rsp>> configurator,
			ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory) {
		super(protocolExtensionFactory);
		this.channelPipelineConfigurator = configurator;
	}

	@Override
	public void doStart(ServerConfig config) throws Exception {

		try {
			lock.lock();
			if (isRunning())
				return;
			init(config);
		} catch (InterruptedException e) {
			logger.error("Server bind on {}:{} is being interrupted", config
					.getHost(),config.getPort());
			throw e;
		} finally {
			lock.unlock();
		}

	}

	@Override
	protected void releaseResource() {

		try {
			lock.lock();
			if (future!=null && future.channel()!=null) {
				future.channel().close().sync();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			lock.unlock();
		}

	}

	

}
