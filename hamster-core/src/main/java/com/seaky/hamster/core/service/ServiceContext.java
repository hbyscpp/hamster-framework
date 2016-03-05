package com.seaky.hamster.core.service;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.RequestInfo;
import com.seaky.hamster.core.rpc.protocol.ResponseInfo;

/**
 * 
 * ServiceContext是服务调用的上下文，对于服务端 ServiceContext中需包含
 * 
 * 
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
public class ServiceContext {

  // 服务端的地址
  private String serverHost;
  // 服务端的端口
  private int serverPort;
  // 客户端的地址
  private String clientHost;
  // 客户端的端口
  private int clientPort;

  // 服务的配置
  private EndpointConfig serviceConfig;

  // 引用的配置
  private EndpointConfig referConfig;

  // 请求的信息
  private RequestInfo requestInfo;

  // 服务的响应
  private ResponseInfo responseInfo;

  // 是否是服务端，true代表是服务端，false是客户端
  private boolean isServer;

  // 本次调用的attr
  private Map<String, Object> attrs = new ConcurrentHashMap<String, Object>();

  public ServiceContext(RequestInfo requestInfo, EndpointConfig referConfig) {
    this.requestInfo = requestInfo;
    this.referConfig = referConfig;
    this.responseInfo = new ResponseInfo();
    this.isServer = false;
  }

  public ServiceContext(RequestInfo requestInfo, EndpointConfig referConfig,
      EndpointConfig serviceConfig, String serverHost, int serverPort) {
    this.requestInfo = requestInfo;
    this.referConfig = referConfig;
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    this.serviceConfig = serviceConfig;
    this.responseInfo = new ResponseInfo();
    this.isServer = false;
  }

  public ServiceContext(RequestInfo reqInfo, EndpointConfig serviceConfig,
      EndpointConfig referConfig, InetSocketAddress serverAddress, InetSocketAddress clientAddress) {
    this.serviceConfig = serviceConfig;
    this.referConfig = referConfig;
    this.serverHost = serverAddress.getAddress().getHostAddress();
    this.serverPort = serverAddress.getPort();
    this.clientHost = clientAddress.getAddress().getHostAddress();
    this.clientPort = clientAddress.getPort();
    this.requestInfo = reqInfo;
    this.responseInfo = new ResponseInfo();
    this.isServer = true;
  }

  public EndpointConfig getReferConfig() {
    return referConfig;
  }

  public EndpointConfig getServiceConfig() {
    return serviceConfig;
  }

  public void setServiceConfig(EndpointConfig config) {
    this.serviceConfig = config;
  }



  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return requestInfo.getServiceName();
  }

  /**
   * @return the isServer
   */
  public boolean isServer() {
    return isServer;
  }

  public boolean isClient() {
    return !isServer;
  }

  /**
   * @return the requsetInfo
   */
  public RequestInfo getRequestInfo() {
    return requestInfo;
  }

  /**
   * @return the responseInfo
   */
  public ResponseInfo getResponseInfo() {
    return responseInfo;
  }

  // 依据当前是服务端还是客户端返回不同的配置参数
  public String getConfigItem(String key) {
    if (isServer)
      return serviceConfig.get(key);
    else
      return referConfig.get(key);
  }

  public String getApp() {
    if (requestInfo == null)
      return null;
    return requestInfo.getApp();
  }

  public String getReferApp() {
    return requestInfo.getReferApp();
  }

  public void setResponseInfo(ResponseInfo info) {
    if (info == null)
      throw new RuntimeException("can not add null response");
    this.responseInfo = info;
  }

  /**
   * @return the serverHost
   */
  public String getServerHost() {
    return serverHost;
  }

  /**
   * @param serverHost the serverHost to set
   */
  public void setServerHost(String serverHost) {
    this.serverHost = serverHost;
  }

  /**
   * @return the serverPort
   */
  public int getServerPort() {
    return serverPort;
  }

  /**
   * @param serverPort the serverPort to set
   */
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  /**
   * @return the clientHost
   */
  public String getClientHost() {
    return clientHost;
  }

  /**
   * @param clientHost the clientHost to set
   */
  public void setClientHost(String clientHost) {
    this.clientHost = clientHost;
  }

  /**
   * @return the clientPort
   */
  public int getClientPort() {
    return clientPort;
  }

  /**
   * @param clientPort the clientPort to set
   */
  public void setClientPort(int clientPort) {
    this.clientPort = clientPort;
  }

  public String getServiceVersion() {
    if (requestInfo == null)
      return null;
    return requestInfo.getReferVersion();
  }

  public String getReferVersion() {
    if (requestInfo == null)
      return null;
    return requestInfo.getReferVersion();
  }

  public String getGroup() {
    if (requestInfo == null)
      return null;
    return requestInfo.getGroup();
  }

  public String getRefergroup() {
    if (requestInfo == null)
      return null;
    return requestInfo.getReferGroup();
  }

  public void setAtrr(String key, Object obj) {
    attrs.put(key, obj);
  }

  public Object getAttr(String key) {
    return attrs.get(key);
  }


}
