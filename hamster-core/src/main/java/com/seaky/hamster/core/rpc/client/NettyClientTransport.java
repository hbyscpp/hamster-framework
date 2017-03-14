package com.seaky.hamster.core.rpc.client;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.registeration.ServiceProviderDescriptor;
import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;
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

/**
 * 
 * 代表客户端和服务端的传输通道包括连接的管理以及响应请求的管理
 * 
 * @author seaky
 * @since 1.0.0
 * @Date 2016年11月14日
 */
public class NettyClientTransport<Req, Rsp> implements ClientTransport<Req, Rsp> {
  private static enum State {
    NOTOPEN/* 未连接的状态 */, CONNECTING/* 正在连接 */, CONNECTED/* 已经连接 */, CLOSED/* 已经关闭 */
  }

  private static Logger logger = LoggerFactory.getLogger(NettyClientTransport.class);

  private Bootstrap bootstrap;

  private Channel ch;

  private ConcurrentHashMap<Long, SettableFuture<Rsp>> pendingReqs = new ConcurrentHashMap<>();

  private ConcurrentHashMap<Long, ServiceContext> pendingContexts = new ConcurrentHashMap<>();

  private Lock lock = new ReentrantLock();

  private AtomicReference<State> status = new AtomicReference<State>(State.NOTOPEN);

  private InetSocketAddress addr;

  private ClientConfig config;

  private ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> clientChannelPipelineConfigurator;

  private InetSocketAddress localAddr;

  private ProtocolExtensionFactory<Req, Rsp> protocolExtensionFactory;

  private AbstractClient<Req, Rsp> client;

  private volatile ChannelFuture cf;


  // 当前重连次数
  private volatile int reconnectNum = 0;

  // 是否需要重新连接
  private volatile boolean isNeedReconnect = true;

  // 是否在重新连接之中
  private volatile boolean isInReconnect = false;


  public InetSocketAddress getLocalAddress() {
    return localAddr;
  }

  public InetSocketAddress getRemoteAddress() {
    return addr;
  }

  public void reconnect(final Executor exe) {
    if (!isNeedReconnect)
      return;
    try {
      lock.lock();
      isInReconnect = true;
      if (reconnectNum < config.getMaxReconnectNum()) {
        closePending();
        localAddr = null;
        cf = null;
        status.set(State.NOTOPEN);
        ClientResourceManager.getHashedWheelTimer().newTimeout(new TimerTask() {
          @Override
          public void run(Timeout timeout) throws Exception {
            SettableFuture<Void> result = connect();
            result.addListener(new Runnable() {
              @Override
              public void run() {
                ++reconnectNum;
                if (status.get() == State.CONNECTED) {
                  reconnectNum = 0;
                  isNeedReconnect = true;
                  isInReconnect = false;
                } else {
                  ClientResourceManager.getHashedWheelTimer().newTimeout(new TimerTask() {
                    @Override
                    public void run(Timeout timeout) throws Exception {
                      reconnect(exe);
                    }
                  }, config.getReconnectInterval(), TimeUnit.SECONDS);
                }
              }
            }, exe);
          }
        }, config.getReconnectInterval(), TimeUnit.SECONDS);
      } else {
        isNeedReconnect = false;
        isInReconnect = false;
        status.set(State.CLOSED);
      }
    } finally {
      lock.unlock();
    }
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
    return new ClientChannelInitializer<Req, Rsp>(this, clientChannelPipelineConfigurator, config);
  }

  private static class ClientChannelInitializer<Req, Rsp>
      extends ChannelInitializer<SocketChannel> {

    public ClientChannelInitializer(NettyClientTransport<Req, Rsp> transport,
        ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> clientChannelPipelineConfigurator,
        ClientConfig config) {
      this.transport = transport;
      this.clientChannelPipelineConfigurator = clientChannelPipelineConfigurator;
      this.config = config;
    }

    private NettyClientTransport<Req, Rsp> transport;
    private ClientChannelPipelineConfigurator<Req, Rsp, NettyClientTransport<Req, Rsp>> clientChannelPipelineConfigurator;

    private ClientConfig config;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
      clientChannelPipelineConfigurator.config(transport, ch.pipeline(), config);
    }

  }

  private void setFuture(ChannelFuture cf, SettableFuture<?> resultFuture) {
    if (cf.isCancelled()) {
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
      resultFuture.set(null);
    }
  }


  private static class ConnectChannelFutureListenser<Req, Rsp> implements ChannelFutureListener {



    private SettableFuture<Void> result;

    private NettyClientTransport<Req, Rsp> client;

    public ConnectChannelFutureListenser(NettyClientTransport<Req, Rsp> client,
        SettableFuture<Void> result) {
      this.client = client;
      this.result = result;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
      client.setFuture(future, result);
    }

  }

  // 调用完之后，一定要close 获取连接的future
  public SettableFuture<Void> connect() {
    final SettableFuture<Void> resultFuture = SettableFuture.create();
    if (cf != null) {
      if (cf.isDone()) {
        // 已经完成
        setFuture(cf, resultFuture);
      } else {
        // 正在连接回调
        cf.addListener(new ConnectChannelFutureListenser<Req, Rsp>(this, resultFuture));
      }
      return resultFuture;
    }
    try {
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
              if (isNeedReconnect) {
                if (!isInReconnect)
                  reconnect(ImmediateEventExecutor.INSTANCE);
              } else {
                client.removeReference(addr, localAddr);
                status.set(State.CLOSED);
              }
              return;
            }
            if (future.cause() != null) {
              if (isNeedReconnect) {
                if (!isInReconnect)
                  reconnect(ImmediateEventExecutor.INSTANCE);
              } else {
                client.removeReference(addr, localAddr);
                status.set(State.CLOSED);
              }
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
              reconnectNum = 0;
              isNeedReconnect = true;
              ch.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                  closePending();
                  if (isNeedReconnect) {
                    if (!isInReconnect)
                      reconnect(ImmediateEventExecutor.INSTANCE);
                  } else {
                    client.removeReference(addr, localAddr);
                    status.set(State.CLOSED);
                  }
                }
              });
            }
          }
        });
      }
      cf.addListener(new ConnectChannelFutureListenser<Req, Rsp>(this, resultFuture));
      return resultFuture;
    } finally {
      lock.unlock();
    }

  }

  // 如果发生网络异常则future.get()抛出异常
  public SettableFuture<Rsp> send(final Req req, final ServiceContext sc) {

    final SettableFuture<Rsp> resultFuture = SettableFuture.create();
    State s = status.get();
    if (s == State.CLOSED || s == State.NOTOPEN || cf == null) {
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
        ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(sc);
        ClientResourceManager.getHashedWheelTimer().newTimeout(new TimerTask() {
          @Override
          public void run(Timeout timeout) throws Exception {
            ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(sc);
            resultFuture.setException(new RpcTimeoutException(String.format(
                "call time out service %s,reference app %s,reference group %s,reference version %s,loacl host %s,port %d,remote app %s,group %s,version %s,host %s,port %d",
                header.getServiceName(), header.getReferenceApp(), header.getReferenceGroup(),
                header.getReferenceVersion(), ServiceContextUtils.getClientHost(sc),
                ServiceContextUtils.getClientPort(sc), header.getApp(), header.getGroup(),
                header.getVersion(), ServiceContextUtils.getServerHost(sc),
                ServiceContextUtils.getServerPort(sc))));
          }
        }, seconds, TimeUnit.MILLISECONDS);

      }


    }

  }

  private void setFutureListener(final ServiceContext sc, Req req,
      final SettableFuture<Rsp> resultFuture) {

    final Long seqNum =
        ServiceContextUtils.getRequestHeader(sc).getAttachments().getAsLong(Constants.SEQ_NUM_KEY);
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


  public void setResponse(Rsp rsp, ProtocolResponseHeader header) {
    if (rsp == null)
      return;
    Long seqNum = header.getAttachments().getAsLong(Constants.SEQ_NUM_KEY);

    if (seqNum == null) {
      logger.error("not found seq number");
      return;
    }
    SettableFuture<Rsp> rspFutrue = pendingReqs.get(seqNum);
    ServiceContext sc = pendingContexts.get(seqNum);
    if (sc == null || rspFutrue == null) {
      return;
    }
    ProtocolRequestHeader reqHeader = ServiceContextUtils.getRequestHeader(sc);
    ServiceReferenceDescriptor rd = client.getSearcher().search(reqHeader.getReferenceApp(),
        reqHeader.getServiceName(), reqHeader.getReferenceVersion(), reqHeader.getReferenceGroup());
    ProtocolResponseBody response;
    try {
      if (!header.isException()) {
        response = protocolExtensionFactory.getResponseConvertor().parseBody(rsp, header,
            Utils.findClassByName(rd.getReturnType()));
      } else {
        response = protocolExtensionFactory.getResponseConvertor().parseBody(rsp, header,
            ExceptionResult.class);
        String expCon = ServiceContextUtils.getReferenceConfig(sc)
            .get(ConfigConstans.REFERENCE_EXCEPTION_CONVERTOR, "default");
        ExceptionConvertor convert =
            ExtensionLoaderConstants.EXCEPTION_CONVERTOR_EXTENSION.findExtension(expCon);
        RpcException e = convert.convertFrom(((ExceptionResult) response.getResult()));
        response.setResult(e);
      }
    } catch (ClassNotFoundException e1) {
      response = new ProtocolResponseBody();
      response.setResult(e1);
      header.setException(true);
    }

    ServiceContextUtils.setResponseHeader(sc, header);
    ServiceContextUtils.setResponseBody(sc, response);
    if (header.isException()) {
      Throwable e = (Throwable) response.getResult();
      if (e instanceof ClientDeserResultException) {
        logger.error("call remote {}:{} service: {},return exception ",
            ServiceContextUtils.getServerHost(sc),
            String.valueOf(ServiceContextUtils.getServerPort(sc)), reqHeader.getServiceName(), e);
      } else {
        logger.debug("call remote {}:{} service: {},return exception ",
            ServiceContextUtils.getServerHost(sc),
            String.valueOf(ServiceContextUtils.getServerPort(sc)), reqHeader.getServiceName(),
            response.getResult());
      }
      rspFutrue.setException((Throwable) response.getResult());
    } else {
      rspFutrue.set(rsp);
    }

  }

  private void closePending() {
    for (Entry<Long, SettableFuture<Rsp>> entry : pendingReqs.entrySet()) {
      SettableFuture<Rsp> f = entry.getValue();
      ServiceContext sc = pendingContexts.get(entry.getKey());
      ProtocolRequestHeader header = ServiceContextUtils.getRequestHeader(sc);
      f.setException(new RpcTimeoutException(String.format(
          "connection close service %s,reference app %s,reference group %s,reference version %s,loacl host %s,port %d,remote app %s,group %s,version %s,host %s,port %d",
          header.getServiceName(), header.getReferenceApp(), header.getReferenceGroup(),
          header.getReferenceVersion(), ServiceContextUtils.getClientHost(sc),
          ServiceContextUtils.getClientPort(sc), header.getApp(), header.getGroup(),
          header.getVersion(), ServiceContextUtils.getServerHost(sc),
          ServiceContextUtils.getServerPort(sc))));
    }
  }

  // 调用这个方法来关闭,主动关闭不会重新连接
  public void close() {
    close(false);
  }

  public void close(boolean reconnect) {
    try {
      lock.lock();
      isNeedReconnect = reconnect;
      if (ch != null)
        ch.close().sync();
    } catch (InterruptedException e) {
      logger.error("close client transport interrupt");
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("close client transport error.", e);
    } finally {
      // 主动关闭无需重新连接
      if (!reconnect)
        status.set(State.CLOSED);
      lock.unlock();
    }

  }



  @Override
  public boolean isConnected() {
    return status.get() == State.CONNECTED && ch.isActive();
  }

  @Override
  public boolean isClosed() {
    return status.get() == State.CLOSED;
  }

}
