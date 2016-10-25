package com.seaky.hamster.core.rpc.registeration;

import java.util.Collection;

// 只在UI管理中使用
public interface RegisterationManageService {

  public Collection<String> getAllServiceNames();

  public Collection<ServiceProviderDescriptor> getAllServiceInstance(String serviceName);

  public Collection<ServiceReferenceDescriptor> getAllReferInstance(String serviceName);

  public Collection<ServiceProviderDescriptor> searchServiceInstance(String app, String serviceName,
      String version, String host, int port, String protocol);

}
