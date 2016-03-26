package com.seaky.hamster.core.rpc.client;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.exception.ClientDeserResultException;
import com.seaky.hamster.core.rpc.exception.RpcException;
import com.seaky.hamster.core.rpc.exception.RpcTimeoutException;
import com.seaky.hamster.core.rpc.protocol.ExceptionConvertor;
import com.seaky.hamster.core.rpc.protocol.ExceptionResult;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;
import com.seaky.hamster.core.rpc.utils.NetUtils;
import com.seaky.hamster.core.rpc.utils.Utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.ImmediateEventExecutor;

// 代表和服务的一个连接
public class NettyClientTransport<Req, Rsp> implements ClientTransport<Req, Rsp> {
  private static enum State {
    NOTOPEN/* 未连接的状态 */, CONNECTING/* 正在连接 */, CONNECTED/* 已经连接 */, CLOSING/* 正在关闭 */, CLOSED/* 已经关闭 */
  }

  private static Logger logger = LoggerFactory.getLogger(NettyClientTransport.class);

  private Bootstrap bootstrap;

  private Channel ch;

  private ConcurrentHashMap<String, SettableFuture<Rsp>> pendingReqs =
      new ConcurrentHashMap<String, SettableFuture<Rsp>>();

  private ConcurrentHashMap<String, ServiceContext> pendingContexts =
      new ConcurrentHashMap<String, ServiceContext>();

  private Lock lock = new ReentrantLock();

  private AtomicReference<State> status = new AtomicReference<State>(State.NOTOPEN);

  private InetSocketAddress addr;

  private ClientConfig config;

  private ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> clientChannelPipelineConfigurator;

  private InetSocketAddress localAddr;

  private ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private AbstractClient<Req, Rsp> client;

  private volatile ChannelFuture cf;


  public InetSocketAddress getLocalAddress() {
    return localAddr;
  }

  public InetSocketAddress getRemoteAddress() {
    return addr;
  }

  public NettyClientTransport(ServiceProviderDescriptor sd, ClientConfig config,
      ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> clientChannelPipelineConfigurator,
      ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory,
      AbstractClient<Req, Rsp> client) {
    this.clientChannelPipelineConfigurator = clientChannelPipelineConfigurator;
    this.protocolExtensionFactory = protocolExtensionFactory;
    this.addr = new InetSocketAddress(sd.getHost(), sd.getPort());
    this.config = config;
    this.client = client;
  }

  private ChannelInitializer<SocketChannel> initChannel() {
    return new ClientChannelInitializer<Req, Rsp>(this, clientChannelPipelineConfigurator);
  }

  private static class ClientChannelInitializer<Req, Rsp>
      extends ChannelInitializer<SocketChannel> {

    public ClientChannelInitializer(NettyClientTransport<Req, Rsp> transport,
        ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> clientChannelPipelineConfigurator) {
      this.transport = transport;
      this.clientChannelPipelineConfigurator = clientChannelPipelineConfigurator;
    }

    private NettyClientTransport<Req, Rsp> transport;
    private ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> clientChannelPipelineConfigurator;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
      clientChannelPipelineConfigurator.config(transport, ch.pipeline());
    }

  }

  private void setFuture(ChannelFuture cf, SettableFuture<?> resultFuture, ServiceContext sc) {
    if (cf.isCancelled()) {
      // TODO why false not true
      resultFuture.cancel(false);
      return;
    }
    if (cf.cause() != null) {
      resultFuture.setException(cf.cause());
      return;
    }
    if (cf.channel() == null || !cf.channel().isActive()) {
      resultFuture.setException(new ClosedChannelException());
      return;
    }
    if (cf.isSuccess()) {
      InetSocketAddress caddr = (InetSocketAddress) cf.channel().localAddress();
      ServiceContextUtils.setClientHost(sc, caddr.getAddress().getHostAddress());
      ServiceContextUtils.setClientPort(sc, caddr.getPort());
      // 连接成功之后更新RD TODO 避免更新继续优化
      resultFuture.set(null);
    }
  }


  private static class ConnectChannelFutureListener<Req, Rsp> implements ChannelFutureListener {


    private ServiceContext sc;

    private SettableFuture<Void> result;

    private NettyClientTransport<Req, Rsp> client;

    public ConnectChannelFutureListener(NettyClientTransport<Req, Rsp> client, ServiceContext sc,
        SettableFuture<Void> result) {
      this.client = client;
      this.sc = sc;
      this.result = result;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
      client.setFuture(future, result, sc);
    }

  }

  // 调用完之后，一定要close 获取连接的future
  public SettableFuture<Void> connect(final ServiceContext sc) {
    final SettableFuture<Void> resultFuture = SettableFuture.create();
    State s = status.get();
    if (s == State.CLOSED || s == State.CLOSING) {
      // 调用的瞬间 刚好连接被关闭，TODO 加入重连机制
      resultFuture.setException(new ClosedChannelException());
      return resultFuture;
    }
    if (cf != null) {
      if (cf.isDone()) {
        setFuture(cf, resultFuture, sc);
      } else {
        cf.addListener(new ConnectChannelFutureListener<Req, Rsp>(this, sc, resultFuture));
      }
      return resultFuture;
    }
    try

    {
      lock.lock();// 保证cf不为空
      if (status.get() == State.NOTOPEN) {
        status.set(State.CONNECTING);
        bootstrap = new Bootstrap();
        bootstrap.group(ClientResourceManager.getIoGroup());
        bootstrap.channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
            config.getConnectTimeout() * 1000);
        bootstrap.handler(initChannel());
        bootstrap.remoteAddress(addr);
        cf = bootstrap.connect().addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isCancelled()) {
              status.set(State.CLOSED);
              return;
            }
            if (future.cause() != null) {
              status.set(State.CLOSED);
              return;
            }
            if (future.isSuccess()) {
              ch = future.channel();
              localAddr = (InetSocketAddress) ch.localAddress();
              if (NetUtils.isInvalidLocalHost(localAddr.getAddress().getHostAddress())) {
                logger.warn("client has not valid local host {} ",
                    localAddr.getAddress().getHostAddress());
              }
              status.set(State.CONNECTED);

              ch.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                  status.set(State.CLOSING);
                  closePending();
                  status.set(State.CLOSED);
                  client.removeReference(addr, localAddr);
                }
              });
            }
          }
        });
      }
      cf.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          setFuture(future, resultFuture, sc);
        }
      });
      return resultFuture;
    } finally {
      lock.unlock();
    }

  }

  // 如果发生网络异常则future.get()抛出异常
  public SettableFuture<Rsp> send(final Req req, final ServiceContext sc) {

    final SettableFuture<Rsp> resultFuture = SettableFuture.create();
    State s = status.get();
    if (s == State.CLOSED || s == State.CLOSING || s == State.NOTOPEN || cf == null) {
      resultFuture.setException(new ClosedChannelException());
      return resultFuture;
    }
    // 有可能是connecting状态
    if (ch == null) {
      cf.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          // 检查状态
          if (future.isCancelled()) {
            // 连接已经被取消
            resultFuture.cancel(false);
            return;
          }
          if (future.cause() != null) {
            // 连接发生异常
            resultFuture.setException(future.cause());
            return;
          }
          if (ch == null || !ch.isActive()) {
            resultFuture.setException(new ClosedChannelException());
            return;
          }
          setFutureListener(sc, req, resultFuture);
        }
      });
    } else {
      setFutureListener(sc, req, resultFuture);

    }

    return resultFuture;
  }

  private static class SendDoneChannelFutureListener<Rsp> implements ChannelFutureListener {

    private ServiceContext sc;

    private SettableFuture<Rsp> resultFuture;

    public SendDoneChannelFutureListener(ServiceContext sc, SettableFuture<Rsp> resultFuture) {
      this.sc = sc;
      this.resultFuture = resultFuture;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {

      if (future.isCancelled()) {
        // 未发送成功
        resultFuture.cancel(false);
        return;
      }
      if (future.cause() != null) {
        // 未发送成功
        resultFuture.setException(future.cause());
        return;
      }
      if (future.isSuccess()) {
        // 发送成功，设置异步超时
        int seconds = ServiceContextUtils.getReferenceConfig(sc).getValueAsInt(
            ConfigConstans.REFERENCE_READ_TIMEOUT, ConfigConstans.REFERENCE_READ_TIMEOUT_DEFAULT);

        ClientResourceManager.getHashedWheelTimer().newTimeout(new TimerTask() {
          @Override
          public void run(Timeout timeout) throws Exception {
            resultFuture.setException(new RpcTimeoutException(
                Utils.generateKey(ServiceContextUtils.getServerHost(sc),
                    String.valueOf(ServiceContextUtils.getServerPort(sc))),
                ServiceContextUtils.getServiceName(sc)));
          }
        }, seconds, TimeUnit.MILLISECONDS);

      }


    }

  }

  private void setFutureListener(final ServiceContext sc, Req req,
      final SettableFuture<Rsp> resultFuture) {

    final String seqNum =
        ServiceContextUtils.getRequestAttachments(sc).getAttachment(Constants.SEQ_NUM_KEY);
    // 发送之前更新下
    ChannelFuture writeFuture = ch.writeAndFlush(req);
    writeFuture.addListener(new SendDoneChannelFutureListener<Rsp>(sc, resultFuture));
    pendingContexts.put(seqNum, sc);
    pendingReqs.put(seqNum, resultFuture);
    resultFuture.addListener(new Runnable() {
      @Override
      public void run() {
        pendingReqs.remove(seqNum);
        pendingContexts.remove(seqNum);
      }
    }, ImmediateEventExecutor.INSTANCE);

  }

  public void setResponse(List<Rsp> responses) {
    if (responses == null)
      return;
    for (Rsp rsp : responses) {
      setResponse(rsp);
    }
  }

  public void setResponse(Rsp rsp) {
    if (rsp == null)
      return;
    Response response = protocolExtensionFactory.getResponseConvertor().convertFrom(rsp);
    String seqNum = response.getAttachments().getAttachment(Constants.SEQ_NUM_KEY);
    if (StringUtils.isBlank(seqNum)) {
      logger.error("not found seq number");
      return;
    }
    SettableFuture<Rsp> rspFutrue = pendingReqs.get(seqNum);
    ServiceContext sc = pendingContexts.get(seqNum);
    if (sc == null || rspFutrue == null) {
      return;
    }
    if (response.getResult() instanceof ExceptionResult) {
      String expCon = ServiceContextUtils.getReferenceConfig(sc)
          .get(ConfigConstans.REFERENCE_EXCEPTION_CONVERTOR, "default");
      ExceptionConvertor convert =
          ExtensionLoaderConstants.EXCEPTION_CONVERTOR_EXTENSION.findExtension(expCon);
      RpcException e = convert.convertFrom(((ExceptionResult) response.getResult()));
      response.setResult(e);
    }
    ServiceContextUtils.setResponse(sc, response);
    if (response.isException()) {
      Throwable e = response.getException();
      if (e instanceof ClientDeserResultException) {
        logger.error("call remote {}:{} service: {},return exception {}",
            ServiceContextUtils.getServerHost(sc),
            String.valueOf(ServiceContextUtils.getServerPort(sc)),
            ServiceContextUtils.getServiceName(sc), e);
      } else {
        logger.debug("call remote {}:{} service: {},return exception {}",
            ServiceContextUtils.getServerHost(sc),
            String.valueOf(ServiceContextUtils.getServerPort(sc)),
            ServiceContextUtils.getServiceName(sc), response.getException());
      }
      rspFutrue.setException(response.getException());
    } else {
      rspFutrue.set(rsp);
    }

  }

  private void closePending() {
    for (Entry<String, SettableFuture<Rsp>> entry : pendingReqs.entrySet()) {
      SettableFuture<Rsp> f = entry.getValue();
      f.setException(new TimeoutException());
    }
  }

  // 调用这个方法来关闭,
  public void close() {
    try {
      lock.lock();
      State s = status.get();
      if (s == State.CLOSING || s == State.NOTOPEN || s == State.CLOSED)
        return;
      status.set(State.CLOSING);
      if (ch != null)
        // TODO 异步??
        ch.close().sync();
    } catch (InterruptedException e) {
      logger.error("close client transport interrupt");
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("close client transport error.", e);
    } finally {
      lock.unlock();
    }

  }

  @Override
  public boolean isConnected() {
    return status.get() == State.CONNECTED && ch.isActive();
  }

  @Override
  public boolean isClosed() {
    return status.get() == State.CLOSING || status.get() == State.CLOSED;
  }

}
