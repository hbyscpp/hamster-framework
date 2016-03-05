package com.seaky.hamster.core.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import com.seaky.hamster.core.ClientHelper;
import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.client.Client;
import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.EtcdRegisterationService;

public class BenchmarkTestServiceClient extends BenchmarkClient {

  private BenchmarkTestService service;

  public BenchmarkTestServiceClient(BenchmarkTestService service) {
    this.service = service;
  }

  public static void main(String[] args) throws Exception {
    ClientConfig conf = new ClientConfig();
    EtcdRegisterationService zkcc = new EtcdRegisterationService("hamster", "localhost:2181");
    Client<?, ?> hc =
        ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class)
            .findExtension("hamster").createClient();
    hc.connect(zkcc, conf);

    EndpointConfig sc = new EndpointConfig();
    BenchmarkTestService service =
        ClientHelper.referInterface(hc, BenchmarkTestService.class, sc, null);
    BenchmarkTestServiceClient client = new BenchmarkTestServiceClient(service);
    client.run(args);

  }

  @Override
  public ClientRunnable getClientRunnable(String targetIP, int targetPort, int clientNums,
      int rpcTimeout, int codecType, int requestSize, CyclicBarrier barrier, CountDownLatch latch,
      long endTime, long startTime) {
    return new BenchmarkClientRunnable(service, requestSize, barrier, latch, startTime, endTime,
        codecType);
  }

}
