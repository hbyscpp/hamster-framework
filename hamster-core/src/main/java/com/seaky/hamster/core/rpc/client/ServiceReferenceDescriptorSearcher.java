package com.seaky.hamster.core.rpc.client;

import com.seaky.hamster.core.rpc.registeration.ServiceReferenceDescriptor;

public interface ServiceReferenceDescriptorSearcher {

  ServiceReferenceDescriptor search(String referApp, String serviceName, String referVersion,
      String referGroup);;
}
