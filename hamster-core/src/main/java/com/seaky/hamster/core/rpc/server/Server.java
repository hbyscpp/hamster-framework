package com.seaky.hamster.core.rpc.server;

import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.service.JavaService;

/**
 * 
 * 一个server实例代表在某个网络地址上运行JavaService服务的容器,这些服务使用相同的协议进行通信。 
 * Req代表协议的请求
 * Rsp代表协议的响应
 * server中的服务都会向RegisterationService注册
 * 
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Server<Req, Rsp> {

  /**
   * 启动一个服务
  * @param registerationService 注册中心服务
  * @param config 服务的配置
  * @return void   
   */
  void start(RegisterationService registerationService, ServerConfig config);

/**
 * server中添加一个服务
 * 服务的配置中必须包含 应用名字，服务的名字，服务的版本和服务的分组
 * 具体的配置参见ConfigConstants
* @param service  服务
* @param serviceConfig 服务的配置
 */
  void export(JavaService service, EndpointConfig serviceConfig);

  /**
   * 获取服务的实例
   * 
   * @param serviceName 服务的名字
   * @param app 服务所属的app名字
   * @param version 服务的版本
   * @param group 服务的分组
   * @return 服务的实例
   */
  JavaService findService(String serviceName, String app, String version, String group);

  /**
   * 关闭server
   */
  void close();

  /**
   * 服务是否在运行
   * 
   * @return server处于运行状态返回true否则返回false
   */
  boolean isRunning();

  /**
   * 获取server的配置
   * 
   * @return 当前server的配置
   */

  ServerConfig getServerConfig();

  /**
   * 获取注册服务
   * 
   * @return 注册服务
   */
  RegisterationService getRegisterationService();

}
