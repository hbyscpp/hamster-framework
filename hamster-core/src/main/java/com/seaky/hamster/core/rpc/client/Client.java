package com.seaky.hamster.core.rpc.client;

import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.registeration.RegisterationService;
import com.seaky.hamster.core.service.JavaReferenceService;

/**
 * 
 * Client代表某个协议<Req,Rsp>的客户端 通过Client的refer方法来引用远程服务，client通过注册中心来寻找服务，通过配置中心来管理客户端引用的配置
 * 远程服务可以位于1个或者多个机器之上 client提供负载策略，集群容错策略 在一个JVM中针对同一个注册中心和配置中心一个协议只需一个客户端实例
 * 同一个客户端对某个服务的引用的配置需提供下面配置: ReferAppNameConfig 代表引用的所在应用的名字 ServiceNameConfig 代表需引用的服务的名字
 * ServiceVersionConfig代表需引用的服务的版本 ServiceSigConfig服务签名类型的配置 ConfigKeyConfig代表引用的配置的key
 * 
 * ServiceConfig中需提供引用的配置,多个refer实例可以使用相同的配置 serviceName-referApp-配置的key来唯一定位refer配置
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
  void connect(RegisterationService registService, ClientConfig config);

  /**
   * 引用服务ServiceConfig包含 ReferAppNameConfig 代表引用的所在应用的名字 ServiceNameConfig 代表需引用的服务的名字
   * ServiceVersionConfig代表需引用的服务的版本 ServiceSigConfig服务签名类型的配置 ConfigKeyConfig代表引用的配置的key
   * 
   * @param config 服务的配置
   * @return
   */
  JavaReferenceService refer(EndpointConfig config);

  /**
   * 查询引用
   * 
   * @param referApp 引用的app
   * @param serviceName 服务的名字
   * @param version 服务的版本
   * @return 服务的引用
   */
  JavaReferenceService findRefer(String serviceName, String referApp, String version, String group);

  /**
   * 关闭客户端
   */
  void close();

  /**
   * 获取注册中心服务
   * 
   * @return 注册中心服务
   */
  RegisterationService getRegistService();

}
