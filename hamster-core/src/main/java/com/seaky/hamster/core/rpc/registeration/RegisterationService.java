package com.seaky.hamster.core.rpc.registeration;

import java.util.Collection;

/**
 * 
 * Service的provider和reference注册服务 实现此接口需保证如下条件，否则会对框架的正确性和性能造成影响:
 * <br/>
 * 1保证service provider的查询方法的性能<br/>
 * 2保证远程的注册中心断开时可以使用本地缓存的数据<br/>
 * 3当远程注册中心数据发生变更时能够及时反应到注册服务的使用着
 * 
 * 
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
  public void registServiceProvider(ServiceProviderDescriptor sd);

  /**
   * 返回注册中心，注册的服务实例集合
   * 
   * @param name 服务的名字
   * @return 服务实例的集合
   */
  public Collection<ServiceProviderDescriptor> findServiceProviders(String name);


  /**
   * 
   * 返回本地的某个实例
   * 
   * @param TODO 参数含义的说明
   * @return ServiceProviderDescriptor TODO 返回值含义的说明
   * @throws TODO 抛出的异常，如果没有删除此行
   */
  public ServiceProviderDescriptor findServiceProvider(String app, String name, String version,
      String group, String protocol, String host, int port, String pid, long registTime);

  /**
   * 取消注册
   * 
   * @param sd 服务的实例
   */
  public void unregistServiceProvider(ServiceProviderDescriptor sd);


  // 引用

  public Collection<ServiceReferenceDescriptor> findServiceReferences(String name);

  public void registServiceReference(ServiceReferenceDescriptor rd);

  public void unregistServiceReference(ServiceReferenceDescriptor rd);


  public ServiceReferenceDescriptor findServiceReference(String referApp, String name,
      String version, String group, String protocol, String host, String pid, long registTime);

  /**
   * 关闭注册服务
   */
  public void close();

}
