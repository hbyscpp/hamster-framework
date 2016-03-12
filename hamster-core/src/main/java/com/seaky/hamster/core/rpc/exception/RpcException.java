package com.seaky.hamster.core.rpc.exception;

//异常
//1调用底层服务出现的异常，不可预知，统一编号
//2框架定义的异常
//3未知异常

@SuppressWarnings("serial")
public class RpcException extends RuntimeException {

	// 服务端未找到服务实例
	public static int SC_NOT_FOUNT = 1;

	// 不匹配的协议
	public static int MISMATCH_PROTOCOL = 2;

	// 服务器资源满了
	public static int RES_ISFULL = 4;

	// 超时
	public static int TIMEOUT = 5;

	// 禁止
	public static int FORBBDEN = 6;

	// 无可路由的服务
	public static int NO_ROUTE_SERVICE = 8;

	//无可用的服务
	public static int NO_SERVICE_AVAILABLE = 9;


	// 服务配置找不到
	public static int SERVICE_CONFIG_NOT_FOUND = 11;

	// 引用配置找不到
	public static int REFER_CONFIG_NOT_FOUND = 12;

	//最大并发
	public static int SERVICE_REACH_MAX_CONCURRENT = 13;

	// 缺乏一些参数
	public static int LOSS_REQ_PARAM = 14;

	//连接被取消
	public static int CONNECT_CANCEL = 15;

	//连接出错
	public static int CONNECT_ERROR = 16;

	//取消发送
	public static int SEND_CANCEL = 17;

	//发送错误
	public static int SEND_ERROR = 18;

	//访问出错
	public static int ACCESS_ERROR = 19;

	//未设置结果
	public static int NOT_SET_RESULT = 20;

	//未知错误
	public static int UNKNOW_EXCEPTION = 21;

	// 服务端反序列化错误
	public static int SERVER_DESER_EXCEPTION = 22;

	// 客户端反序列化错误
	public static int CLIENT_DESER_EXCEPTION = 23;
	
	public static int SIG_MISMATCH_EXCEPTION = 24;
	
	public static int EXTRACTOR_RESPONSE_EXCEPTION=25;

	public static int NO_SERVICE_MATCH=26;
	
	
	private int code;

	public RpcException(int code, String msg) {
		super(msg);
		this.code = code;
	}

	public RpcException(int code, String msg, Throwable e) {
		super(msg, e);
		this.code = code;
	}

	public RpcException(int code, Throwable e) {
		super(e);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
