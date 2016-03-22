/**
 * Copyright (c) @2016,重庆云途交通科技有限公司.版权所有
 */
package com.seaky.hamster.core.rpc.protocol;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.exception.RpcException;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 23, 2016 
 */
@SPI("default")
public interface ExceptionConvertor {

	ExceptionResult convertTo(RpcException e);
	
	RpcException convertFrom(ExceptionResult result);
	
}
