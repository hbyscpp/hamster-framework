package com.seaky.hamster.core.test;

import java.util.concurrent.ExecutionException;

import com.seaky.hamster.core.ClientHelper;
import com.seaky.hamster.core.extension.ExtensionLoader;
import com.seaky.hamster.core.rpc.client.Client;
import com.seaky.hamster.core.rpc.client.ClientConfig;
import com.seaky.hamster.core.rpc.config.ConfigConstans;
import com.seaky.hamster.core.rpc.config.ConfigItem;
import com.seaky.hamster.core.rpc.config.EndpointConfig;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.registeration.EtcdRegisterationService;

import rx.Observable;
import rx.functions.Action1;

public class TestClient {

  /**
   * @param args
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public static void main(String[] args) throws InterruptedException, ExecutionException {

    // 注册中心和配置中心
    EtcdRegisterationService lrs = new EtcdRegisterationService("hamster", "http://localhost:2379");
    // 连接客户端
    Client<?, ?> cc = ExtensionLoader.getExtensionLoaders(ProtocolExtensionFactory.class)
        .findExtension("hamster").createClient();
    ClientConfig conf = new ClientConfig();
    cc.connect(lrs, conf);
    // 引用客户端

    EndpointConfig sc = new EndpointConfig();
    sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_APP, "testapp", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_GROUP, "default", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_VERSION, "1.0.0", true));


    for (int i = 0; i < 100; ++i) {
      final MathReactive hello =
          ClientHelper.referReactiveInterface(cc, Math.class, MathReactive.class, sc, null);
      // 关闭
      Observable<String> result = hello.hello(String.valueOf(i));
      result.subscribe(new Action1<String>() {

        @Override
        public void call(String t1) {
          System.out.println("result is:" + t1);

        }
      }, new Action1<Throwable>() {

        @Override
        public void call(Throwable t1) {

          System.out.println(t1);
        }
      });
      // Thread.sleep(2000);
    }

    /**
     * Thread.sleep(10000); ClientResourceManager.stop(); cc.close(); zkcc.close(); lrs.close();
     **/
  }

}
