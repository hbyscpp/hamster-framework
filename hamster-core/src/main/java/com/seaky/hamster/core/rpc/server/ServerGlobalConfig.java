package com.seaky.hamster.core.rpc.server;

// 服务端的全局配置
public class ServerGlobalConfig {

  // 处理连接的线程数目
  private static int acceptorThreadNum = Runtime.getRuntime().availableProcessors();

  // 网络IO的线程数目
  private static int ioThreadNum = 2 * Runtime.getRuntime().availableProcessors();

  // 业务分发的线程数目
  private static int dispatcherThreadNum = Runtime.getRuntime().availableProcessors() * 2;


  /**
   * @return the acceptorThreadNum
   */
  public static int getAcceptorThreadNum() {
    return acceptorThreadNum;
  }

  /**
   * @param acceptorThreadNum the acceptorThreadNum to set
   */
  public static void setAcceptorThreadNum(int acceptorThreadNum) {
    ServerGlobalConfig.acceptorThreadNum = acceptorThreadNum;
  }

  /**
   * @return the ioThreadNum
   */
  public static int getIoThreadNum() {
    return ioThreadNum;
  }

  /**
   * @param ioThreadNum the ioThreadNum to set
   */
  public static void setIoThreadNum(int ioThreadNum) {
    ServerGlobalConfig.ioThreadNum = ioThreadNum;
  }

  /**
   * @return the dispatcherThreadNum
   */
  public static int getDispatcherThreadNum() {
    return dispatcherThreadNum;
  }

  /**
   * @param dispatcherThreadNum the dispatcherThreadNum to set
   */
  public static void setDispatcherThreadNum(int dispatcherThreadNum) {
    ServerGlobalConfig.dispatcherThreadNum = dispatcherThreadNum;
  }

}
