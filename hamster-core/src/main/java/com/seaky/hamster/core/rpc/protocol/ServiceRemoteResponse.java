package com.seaky.hamster.core.rpc.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since 1.0.0
 * @Date Mar 5, 2016 
 */
public class ServiceRemoteResponse {


	private static Object NOT_SET_OBJ = new Object();

	private Map<String, String> attachments;

	// 客户端使用这个值,请不要随意改变
	private Object result = NOT_SET_OBJ;

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

	public void addAttchment(String key, String value) {
		if (value == null)
			return;
		if (attachments == null)
			attachments = new HashMap<String, String>();
		this.attachments.put(key, value);
	}

	/**
	 * @return the result
	 */
	public Object getResult() {
		if (result == NOT_SET_OBJ)
			return null;
		return result;
	}

	public Throwable getException() {
		if (isException())
			return (Throwable) result;
		return null;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(Object result) {
		this.result = result;
	}

	public void clearResult() {
		this.result = NOT_SET_OBJ;
	}

	// 是否成功调用
	public boolean isDone() {
		return result != NOT_SET_OBJ;
	}

	public boolean isException() {
		if (result == null)
			return false;

		return result instanceof Throwable;
	}


}
