package com.seaky.hamster.core.rpc.exception;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 21, 2016 
 */
@SuppressWarnings("serial")
public class BuisnessException extends RpcException {

	/** 
	* @Description 构造函数 TODO 补充说明
	* @param @param code
	* @param @param e  参数说明 
	* @throws 
	*/
	public BuisnessException(Throwable e) {
		super(BUSINESS_EXCEPTION, e);
	}

}
