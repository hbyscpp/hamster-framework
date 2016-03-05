package com.seaky.hamster.core.rpc.interceptor;

/**
 * 
 * 服务调用的阶段
 * 
 * @author seaky
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ProcessPhase {
	/**
	 * 服务端调用服务的实现
	 */
	SERVER_CALL_SERVICE,
	/**
	 * 客户端调用远程服务集群，调用之前之后ServiceContext没有服务端的地址和客户端的地址，远程服务的实例的相关信息
	 */
	CLIENT_CALL_CLUSTER_SERVICE,
	/**
	 * 客户端调用远程服务实例，调用之前ServiceContext中没有客户端的地址，调用之后ServiceContext会有客户端的地址
	 */
	CLIENT_CALL_SERVICE_INSTANCE
}
