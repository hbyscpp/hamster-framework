package com.seaky.hamster.core.rpc.exception;

// 异常
// 1调用底层服务出现的异常，不可预知，统一编号
// 2框架定义的异常
// 3未知异常
// 编号小于0的都是框架内部使用的
@SuppressWarnings("serial")
public class RpcException extends RuntimeException {

	// 服务端调用服务实现抛出的异常
	public static int BUSINESS_EXCEPTION = -1;

	// 未找到任何的服务实例
	public static int SERVICE_PROVIDER_NOT_FOUNT = -2;

	// 客户端和服务端不匹配的协议
	public static int MISMATCH_PROTOCOL = -3;

	// 服务器资源满了
	public static int SERVER_RES_ISFULL = -4;

	// 超时
	public static int TIMEOUT = -5;

	// 访问被禁止
	public static int FORBBDEN = -6;

	// 可选实例中，依据路由选择，无路由的服务
	public static int NO_ROUTE_SERVICE_PROVIDER = -7;

	// 无可用的服务
	public static int NO_SERVICE_PROVIDER_AVAILABLE = -8;

	// 服务配置找不到
	public static int SERVICE_PROVIDER_CONFIG_NOT_FOUND = -9;

	// 引用配置找不到
	public static int SERVICE_REFERENCE_CONFIG_NOT_FOUND = -10;

	// 最大并发
	public static int SERVICE_PROVIDER_REACH_MAX_CONCURRENT = -11;

	// 缺乏一些参数
	public static int LOSS_REQ_PARAM = -12;

	// 连接被取消
	public static int CONNECT_CANCEL = -13;

	// 连接出错
	public static int CONNECT_ERROR = -14;

	// 取消发送
	public static int SEND_CANCEL = -15;

	// 发送错误
	public static int SEND_ERROR = -16;

	// 访问出错
	public static int ACCESS_ERROR = -17;

	// 未设置结果
	public static int NOT_SET_RESULT = -18;

	// 服务端反序列化错误
	public static int SERVER_DESER_EXCEPTION = -19;

	// 客户端反序列化错误
	public static int CLIENT_DESER_EXCEPTION = -20;

	public static int SIG_MISMATCH_EXCEPTION = -21;

	public static int NO_SERVICE_PROVIDER_MATCH = -22;

	public static int UNKNOWN_EXCEPTION = -23;

	private int code;

	public RpcException(int code, String msg) {
		this(code,msg,null);
	}

	public RpcException(int code, String msg, Throwable e) {
		super(msg, e);
		this.code = code;
	}

	public RpcException(int code, Throwable e) {
		this(code,e.getMessage(),e);
	}

	public int getCode() {
		return code;
	}

}
