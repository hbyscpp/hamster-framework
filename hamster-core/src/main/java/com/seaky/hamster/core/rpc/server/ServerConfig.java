package com.seaky.hamster.core.rpc.server;


public class ServerConfig {

  private String host;

  private int port;

  private boolean isTcpNoDelay;

  private int soBacklog = 1024;

  // 扫描的开始端口
  private int startScanPort = 20000;
  // 扫描的结束端口
  private int endScanPort = 40000;

  /**
   * @return the isTcpNoDelay
   */
  public boolean isTcpNoDelay() {
    return isTcpNoDelay;
  }

  /**
   * @param isTcpNoDelay the isTcpNoDelay to set
   */
  public void setTcpNoDelay(boolean isTcpNoDelay) {
    this.isTcpNoDelay = isTcpNoDelay;
  }


  /**
   * @return the so_backlog
   */
  public int getSoBacklog() {
    return soBacklog;
  }

  /**
   * @param soBacklog the soBacklog to set
   */
  public void setSoBacklog(int soBacklog) {
    this.soBacklog = soBacklog;
  }

  /**
   * @return the startScanPort
   */
  public int getStartScanPort() {
    return startScanPort;
  }

  /**
   * @param startScanPort the startScanPort to set
   */
  public void setStartScanPort(int startScanPort) {
    this.startScanPort = startScanPort;
  }

  /**
   * @return the endScanPort
   */
  public int getEndScanPort() {
    return endScanPort;
  }

  /**
   * @param endScanPort the endScanPort to set
   */
  public void setEndScanPort(int endScanPort) {
    this.endScanPort = endScanPort;
  }

  /**
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the host to set
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port the port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

}
