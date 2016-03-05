package com.seaky.hamster.core.rpc.server;

import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.service.JavaService;

/**
 * 
 * Server代表网络上运行一些JavaService服务的容器,我们可以从server中一个server实例上运行的服务使用相同的协议。 Req代表
 * 
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Server<Req, Rsp> {

  /**
   * 启动服务
   * 
   * @param registService 注册中心 不能为null
   * @param config server的配置不能为null
   */
  void start(RegisterationService registService, ServerConfig config);

  /**
   * 在Server中导出服务,服务的配置是serviceConfig，配置必须包含: SericeAppNameConfig代表service所属的应用的名字
   * ServiceNameConfig代表服务的名字 ServiceVersionConfig代表service的版本 ConfigKeyConfig代表service相关的配置的关键字
   * ServiceSigConfig代表服务的签名配置
   * 
   * 绑定成功之后，如果修改service配置，只能通过getConfigService获取配置中心来改变 直接修改serviceConfig不影响配置
   * 
   * @param service 服务的实例
   * @param serviceConfig 服务的配置
   */
  void export(JavaService service, EndpointConfig serviceConfig);

  /**
   * 获取服务的实例
   * 
   * @param app 服务所属的app名字
   * @param serviceName 服务的名字
   * @param version 服务的版本
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
