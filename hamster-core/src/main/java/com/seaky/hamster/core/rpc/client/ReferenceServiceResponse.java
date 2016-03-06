package com.seaky.hamster.core.rpc.client;


/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 5, 2016 
 */
public class ReferenceServiceResponse {

	private static Object NOT_SET_OBJ = new Object();


	// 客户端使用这个值,请不要随意改变
	private Object result = NOT_SET_OBJ;

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
