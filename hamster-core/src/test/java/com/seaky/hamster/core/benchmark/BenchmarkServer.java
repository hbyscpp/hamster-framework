package com.seaky.hamster.core.benchmark;

import com.seaky.hamster.core.ServerHelper;
import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.EtcdRegisterationService;
import com.seaky.hamster.core.rpc.server.Server;
import com.seaky.hamster.core.rpc.server.ServerConfig;

public class BenchmarkServer {

  /**
   * @param args
   */
  public static void main(String[] args) {
    EtcdRegisterationService rs = new EtcdRegisterationService("hamster", "localhost:2181");
    Server<?, ?> server =
        ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class)
            .findExtension("hamster").createServer();
    ServerConfig sconfig = new ServerConfig();
    sconfig.setPort(12345);
    server.start(rs, sconfig);
    EndpointConfig sc = new EndpointConfig();
    ServerHelper.exportInterface(server, BenchmarkTestService.class,
        new FixSizeResponsebenchmarkTestService(100), sc, null);

  }

}
