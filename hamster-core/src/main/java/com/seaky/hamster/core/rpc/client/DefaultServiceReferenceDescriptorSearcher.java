package com.seaky.hamster.core.rpc.client;

import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;

public class DefaultServiceReferenceDescriptorSearcher
    implements ServiceReferenceDescriptorSearcher {

  private AbstractClient<?, ?> client;

  public DefaultServiceReferenceDescriptorSearcher(AbstractClient<?, ?> client) {
    this.client = client;
  }

  @Override
  public ServiceReferenceDescriptor search(String referApp, String serviceName, String referVersion,
      String referGroup) {
    return client.getServiceReferenceDescriptor(serviceName, referApp, referVersion, referGroup);
  }

}
