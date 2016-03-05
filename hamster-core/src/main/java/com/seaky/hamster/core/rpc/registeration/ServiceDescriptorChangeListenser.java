package com.seaky.hamster.core.rpc.registeration;

public interface ServiceDescriptorChangeListenser {

  public void delete(ServiceProviderDescriptor sd);

  public void update(ServiceProviderDescriptor sd);


}
