package com.seaky.hamster.admin;

import java.util.Date;

public class ReferInstanceView {

	private String referApp;

	private String serviceName;

	private String referVersion;;
	
	private String referHost;

	private int referPort;

	private String protocol;
	// 服务的地址
	private String serviceHost;
	
	private int servicePort;

	private String configKey;
	
	// 服务端的app
	private String app;
	
	private String serviceVersion;
	
	private Date registTime;
	

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the referApp
	 */
	public String getReferApp() {
		return referApp;
	}

	/**
	 * @param referApp
	 *            the referApp to set
	 */
	public void setReferApp(String referApp) {
		this.referApp = referApp;
	}



	/**
	 * @return the app
	 */
	public String getApp() {
		return app;
	}

	/**
	 * @param app the app to set
	 */
	public void setApp(String app) {
		this.app = app;
	}








	/**
	 * @return the clientHost
	 */
	public String getReferHost() {
		return referHost;
	}

	/**
	 * @param clientHost the clientHost to set
	 */
	public void setReferHost(String clientHost) {
		this.referHost = clientHost;
	}

	/**
	 * @return the clientPort
	 */
	public int getReferPort() {
		return referPort;
	}

	/**
	 * @param clientPort the clientPort to set
	 */
	public void setReferPort(int clientPort) {
		this.referPort = clientPort;
	}

	/**
	 * @return the serverHost
	 */
	public String getServiceHost() {
		return serviceHost;
	}

	/**
	 * @param serverHost the serverHost to set
	 */
	public void setServiceHost(String serverHost) {
		this.serviceHost = serverHost;
	}

	/**
	 * @return the serverPort
	 */
	public int getServicePort() {
		return servicePort;
	}

	/**
	 * @param serverPort the serverPort to set
	 */
	public void setServicePort(int serverPort) {
		this.servicePort = serverPort;
	}


	/**
	 * @return the configKey
	 */
	public String getConfigKey() {
		return configKey;
	}

	/**
	 * @param configKey the configKey to set
	 */
	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}


	/**
	 * @return the registTime
	 */
	public Date getRegistTime() {
		return registTime;
	}

	/**
	 * @param registTime the registTime to set
	 */
	public void setRegistTime(Date registTime) {
		this.registTime = registTime;
	}

	/**
	 * @return the serviceVersion
	 */
	public String getServiceVersion() {
		return serviceVersion;
	}

	/**
	 * @param serviceVersion the serviceVersion to set
	 */
	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	/**
	 * @return the referVersion
	 */
	public String getReferVersion() {
		return referVersion;
	}

	/**
	 * @param referVersion the referVersion to set
	 */
	public void setReferVersion(String referVersion) {
		this.referVersion = referVersion;
	}

}
