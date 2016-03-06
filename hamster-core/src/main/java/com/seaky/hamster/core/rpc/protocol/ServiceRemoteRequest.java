/**
 * Copyright (c) @2016,重庆云途交通科技有限公司.版权所有
 */
package com.seaky.hamster.core.rpc.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 5, 2016
 */
public class ServiceRemoteRequest {

	// 请求服务所在的应用
	private String app;
	// 请求的来源的应用
	private String referenceApp;
	// 服务的名字
	private String serviceName;

	// 想要引用的group
	private String referenceGroup;
	// 实际的group
	private String group;
	// 请求的来源的版本
	private String referenceVersion;
	// 请求服务的版本
	private String version;
	// 附加属性，用于一些特性的实现 比如序列号，监控等,应用应该避免设置
	private Map<String, String> attachments = new ConcurrentHashMap<String, String>();
	// 请求参数
	private Object[] params;


	public void addAttachment(String key, String value) {
		this.attachments.put(key, value);
	}

	public void addAttachment(Map<String, String> otherAttach) {
		if (otherAttach != null) {
			this.attachments.putAll(otherAttach);
		}
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @return the attachments
	 */
	public Map<String, String> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments
	 *            the attachments to set
	 */
	public void setAttachments(Map<String, String> attachments) {
		this.attachments = attachments;
	}

	public String getAttachment(String key) {
		if (attachments == null)
			return null;
		return attachments.get(key);
	}

	/**
	 * @return the app
	 */
	public String getApp() {
		return app;
	}

	/**
	 * @return the params
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * @param params
	 *            the params to set
	 */
	public void setParams(Object[] params) {
		this.params = params;
	}


	/**
	 * @return the serviceVersion
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param serviceVersion
	 *            the serviceVersion to set
	 */
	public void setVersion(String serviceVersion) {
		this.version = serviceVersion;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}


	public void setApp(String app) {
		this.app = app;
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

}
