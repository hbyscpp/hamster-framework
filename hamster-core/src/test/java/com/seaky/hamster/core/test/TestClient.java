package com.seaky.hamster.core.test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
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
        .findExtension("http").createClient();
    ClientConfig conf = new ClientConfig();
    conf.setReadTimeout(5);
    cc.connect(lrs, conf);
    // 引用客户端

    EndpointConfig sc = new EndpointConfig();
    sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_APP, "testapp", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_GROUP, "default", true));
    sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_VERSION, "1.0.0", true));
    // sc.addConfigItem(
    // new ConfigItem(ConfigConstans.REFERENCE_EXCEPTION_CONVERTOR, "mytestxception", true));
    // sc.addConfigItem(new ConfigItem(ConfigConstans.REFERENCE_ASYNPOOL_THREAD_EXE, "true", true));
    final TestServiceReactive hello = ClientHelper.referReactiveInterface(cc, TestService.class,
        TestServiceReactive.class, sc, null);

    // testCB(hello);
    // testComp(hello);
    testOk(hello, 1);

    /**
     * Thread.sleep(10000); ClientResourceManager.stop(); cc.close(); zkcc.close(); lrs.close();
     **/
    Thread.currentThread().join();
  }

  private static void testComp(TestServiceReactive hello) throws InterruptedException {
    // System.out.println(hello.sub(123, (short) 456).toBlocking().first());
    // System.out.println(hello.ping().toBlocking().first());
    // System.out.println(hello.add(new Integer(2), 4).toBlocking().first());
    // hello.testVoid().toBlocking().first();
    // hello.testException().toBlocking().first();

  }

  private static void testOk(TestServiceReactive hello, int n) throws InterruptedException {

    CountDownLatch latch = new CountDownLatch(n);

    for (int i = 0; i < n; ++i) {
      new Thread(new ValidRunner(hello, latch)).start();
    }

  }

  private static void testCB(TestServiceReactive hello) {
    for (int i = 0; i < 6; ++i) {
      try {
        System.out.println(i);
        hello.testCircuitbreaker(10000).toBlocking().single();

      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    }
    for (int i = 0; i < 5; ++i) {
      try {
        System.out.println(6 + i);
        hello.testCircuitbreaker(100).toBlocking().single();
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }

  private static class ValidRunner implements Runnable {

    private TestServiceReactive hello;

    private CountDownLatch latch;

    public ValidRunner(TestServiceReactive hello, CountDownLatch latch) {

      this.hello = hello;
      this.latch = latch;
    }

    @Override
    public void run() {
      latch.countDown();
      int x = new Random().nextInt(300);
      int y = new Random().nextInt(300);
      int z = hello.add(x, y).toBlocking().single();
      if (z != (x + y)) {
        System.out.println("error");
      } else {
        System.out.println(z);
      }
    }

  }

}
