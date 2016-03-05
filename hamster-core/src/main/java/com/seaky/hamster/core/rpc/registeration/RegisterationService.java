package com.seaky.hamster.core.rpc.registeration;

import java.util.Collection;

/**
 * 
 * Service注册服务 实现此接口需保证如下条件，否则会对框架的正确性和性能造成影响: 1保证查询方法的性能 2 保证远程的注册中心断开时可以使用本地缓存的数据
 * 3当远程注册中心数据发生变更时能够及时反应到注册服务的使用着
 * 
 * 某个server如果关闭其对应的注册中心数据也会删除。server关闭时最好记录日志 如果注册中心down掉，本地缓存必须保证可用
 * 
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
public interface RegisterationService {

  /**
   * 向注册中心注册一个服务
   * 
   * @param sd 服务的实例
   */
  public void registService(ServiceProviderDescriptor sd);

  /**
   * 获取服务实例的信息
   * 
   * @param name 服务的名字
   * @return 服务实例的集合
   */
  public Collection<ServiceProviderDescriptor> findServices(String name);


  public ServiceProviderDescriptor findService(String app, String name, String version, String group,
      String protocol, String host, int port);

  /**
   * 取消注册
   * 
   * @param sd 服务的实例
   */
  public void unregistService(ServiceProviderDescriptor sd);

  public void registRefer(ServiceReferenceDescriptor rd);

  public void unregistRefer(ServiceReferenceDescriptor rd);


  public ServiceReferenceDescriptor findRefer(String referApp, String name, String version, String group,
      String protocol, String referHost, int referPort, String host, int port);

  // 获取某个服务的所有引用
  /**
   * 关闭注册服务
   */
  public void close();

}
