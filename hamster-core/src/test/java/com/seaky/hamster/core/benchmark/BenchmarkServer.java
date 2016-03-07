package com.seaky.hamster.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seaky.hamster.core.ServerHelper;
import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
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
	  
	    EtcdRegisterationService rs = new EtcdRegisterationService("hamster", "http://localhost:2379");
    Server<?, ?> server =
        ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class)
            .findExtension("hamster").createServer();
    ServerConfig sconfig = new ServerConfig();
    sconfig.setPort(12345);
    server.start(rs, sconfig);
    EndpointConfig sc = new EndpointConfig();
    sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_APP, "testapp", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_GROUP, "default", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_VERSION, "1.0.0", true));
    ServerHelper.exportInterface(server, BenchmarkTestService.class,
        new FixSizeResponsebenchmarkTestService(100), sc, null);

  }

}
