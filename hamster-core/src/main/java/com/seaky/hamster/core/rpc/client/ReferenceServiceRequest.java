/**
 * Copyright (c) @2016,重庆云途交通科技有限公司.版权所有
 */
package com.seaky.hamster.core.rpc.client;
/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 5, 2016 
 */
public class ReferenceServiceRequest {

	private String serviceName;
	
	private String referenceApp;
	
	private String referenceGroup;
	
	private String referenceVersion;
	
	private Object[] params;
	
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
	 * @return the params
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * @return the referenceApp
	 */
	public String getReferenceApp() {
		return referenceApp;
	}

	/**
	 * @param referenceApp the referenceApp to set
	 */
	public void setReferenceApp(String referenceApp) {
		this.referenceApp = referenceApp;
	}

	/**
	 * @return the referenceGroup
	 */
	public String getReferenceGroup() {
		return referenceGroup;
	}

	/**
	 * @param referenceGroup the referenceGroup to set
	 */
	public void setReferenceGroup(String referenceGroup) {
		this.referenceGroup = referenceGroup;
	}

	/**
	 * @return the referenceVersion
	 */
	public String getReferenceVersion() {
		return referenceVersion;
	}

	/**
	 * @param referenceVersion the referenceVersion to set
	 */
	public void setReferenceVersion(String referenceVersion) {
		this.referenceVersion = referenceVersion;
	}

  public void setParams(Object[] params) {
    this.params = params;
  }

	
}
