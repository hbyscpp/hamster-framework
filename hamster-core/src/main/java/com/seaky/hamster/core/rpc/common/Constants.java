package com.seaky.hamster.core.rpc.common;

import java.nio.charset.Charset;

public interface Constants {

  public static String SEQ_NUM_KEY = "_seq_";

  public static String PROTOCOL_VERSION_KEY = "_pv_";

  public static final Charset UTF_8 = Charset.forName("UTF-8");

  public static String DEFAULT_CONFIG_KEY = "DEFAULT";

  public static String DEFAULT_SERVICE_KEY = "DEFAULT";

  public static String DEFAULT_APP = "DEFAULT";

  public static String DEFAULT_REFER_KEY = "DEFAULT";


  // 分隔字符选用下面几种
  public static final String MID_LINE = "-";

  public static final String TILDE_LINE = "~";

  public static final String CARET = "^";

  public static final String LEFT_BRACKETS = "[";

  public static final String RIGHT_BRACKETS = "]";

  public static final String EQUAL = "=";

  public static final String AND = "&";

  public static final String COMMA = ",";

  public static final String COLON = ":";


  // 服务的名字允许的正则表达式
  public static final String SERVICE_NAME_ALLOW_REG = "[a-zA-Z0-9_\\$\\.]+";

  public static final String APP_NAME_ALLOW_REG = "[a-zA-Z0-9_\\$\\.-]+";

  public static final String GROUP_NAME_ALLOW_REG = "[a-zA-Z0-9_,]+";

  public static final String VERSION_NAME_ALLOW_REG = "[0-9.]+";


  public static final String INTERCEPTOR_NAME_ALLOW_REG = "[0-9a-zA-Z]+";


  //
  public static final String CONFIG_KEY_NOT_ALLOW_STR = "\\/" + TILDE_LINE + EQUAL + AND;

  public static final String CONFIG_VALUE_NOT_ALLOW_STR = TILDE_LINE + EQUAL + AND;

  // 序列华类型

  public static final String KRYO_SERIAL = "kryo";

  public static final String JSON_SERIAL = "json";

  public static final String MSGPACK_SERIAL = "msgpack";

  // 消息的类型
  public static final String MSG_TYPE = "__mtype__";

  // 正常消息
  public static final byte MSG_NORMAL_TYPE = 0;

  // 心跳消息
  public static final byte MSG_HEARTBEAT_TYPE = 1;

  // 心跳响应
  public static final byte MSG_HEARTBEAT_RSP_TYPE = 2;



}
