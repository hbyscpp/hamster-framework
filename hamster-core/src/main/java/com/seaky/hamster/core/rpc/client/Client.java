package com.seaky.hamster.core.rpc.client;

import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.service.JavaService;

/**
 * 
 * Client代表某个协议<Req,Rsp>的客户端 通过Client的reference方法来引用远程服务，client通过注册中心来寻找服务， client提供负载策略，集群容错策略
 * 在一个JVM中针对同一个注册中心和配置中心一个协议只需一个客户端实例
 * 
 * 
 * 
 * @author seaky
 * @version @param <Req>
 * @version @param <Rsp>
 * @since 1.0.0
 */
public interface Client<Req, Rsp> {

  /**
   * 客户端初始化，只需调用一次
   * 
   * @param registService 注册服务
   * @param configService 配置服务
   * @param config 客户端配置
   */
  void connect(RegisterationService registerationService, ClientConfig config);

  /**
   * EndpointConfig中需包含引用所在的app名字，引用服务的名字，引用的版本和引用的group
   * 
   * @param config 服务的配置
   * @return
   */
  JavaService reference(EndpointConfig config);

  /**
   * 查询引用
   * 
   * @param referenceApp 引用的app
   * @param serviceName 服务的名字
   * @param version 服务的版本
   * @return 服务的引用
   */
  JavaService findReferenceService(String serviceName, String referenceApp, String referenceVersion,
      String referenceGroup);

  /**
   * 关闭客户端
   */
  void close();

  /**
   * 获取注册中心服务
   * 
   * @return 注册中心服务
   */
  RegisterationService getRegisterationService();

}
