package com.seaky.hamster.core.rpc.client;

// 客户端默认的配置
// 从应用的角度无需关心底层的连接超时和读取超时，只需关心方法调用的超时，对于实时的调用，连接超时3s和读取超时3s完全足够了
public class ClientConfig {

  // 和服务端连接超时s
  private int connectTimeout = 1;


  private int readTimeout = 3;

  /**
   * @return the connectTimeout
   */
  public int getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * @param connectTimeout the connectTimeout to set
   */
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  /**
   * @return the readTimeout
   */
  public int getReadTimeout() {
    return readTimeout;
  }

  /**
   * @param readTimeout the readTimeout to set
   */
  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }


}
