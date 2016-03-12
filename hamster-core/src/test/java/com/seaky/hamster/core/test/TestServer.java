package com.seaky.hamster.core.test;


import com.seaky.hamster.core.ServerHelper;
import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.EtcdRegisterationService;
import com.seaky.hamster.core.rpc.server.Server;
import com.seaky.hamster.core.rpc.server.ServerConfig;

public class TestServer {

  public static void main(String[] args) throws InterruptedException {


    // 注册中心
    EtcdRegisterationService rs = new EtcdRegisterationService("hamster", "http://localhost:2379");

    Server<?, ?> server = ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class)
        .findExtension("hamster").createServer();

    // 服务的配置
    ServerConfig sconfig = new ServerConfig();
    sconfig.setHost("127.0.0.1");
    sconfig.setPort(12345);
    server.start(rs, sconfig);
    EndpointConfig sc = new EndpointConfig();
    sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_APP, "testapp", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_GROUP, "default", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.PROVIDER_VERSION, "1.0.0", true));
    ServerHelper.exportInterface(server, Math.class, new MathImpl1(), sc);
    // ServerHelper.bindInterface("app","key2", server, Math.class,
    // new MathImpl2(), configs);

    // server.close();
    // zkcc.close();
    // rs.close();
    // ServerResourceManager.stop();

  }

}
