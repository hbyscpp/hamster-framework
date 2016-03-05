package com.seaky.hamster.admin;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ServiceInstanceView {

	private String app;
	
	private String version;
	
	private String configKey;
	
	private String[] paramTypes;
	
	private String returnType;
	
	private String host;
	
	private String protocol;
	
	private int port;
	
	private String serviceName;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date registTime;

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
	 * @return the paramTypes
	 */
	public String[] getParamTypes() {
		return paramTypes;
	}

	/**
	 * @param paramTypes the paramTypes to set
	 */
	public void setParamTypes(String[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	/**
	 * @return the returnType
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}


	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
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
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param serviceName the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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

}
