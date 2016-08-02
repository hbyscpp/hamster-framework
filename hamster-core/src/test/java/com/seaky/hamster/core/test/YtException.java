package com.seaky.hamster.core.test;


@SuppressWarnings("serial")
public class YtException extends RuntimeException {

  private Errorcode errorcode;

  private String extraInfo;

  private static String format =
      "errorcode:%d,typeid:%d,appid:%d,code:%d,errormsg:%s \n extrainfo:%s";

  private static String formatTrace =
      "errorcode:%d,typeid:%d,appid:%d,code:%d,errormsg:%s \n extrainfo:%s \n exception trace:%s";

  public YtException(Errorcode errorcode) {
    this.errorcode = errorcode;
  }

  public YtException(Errorcode errorcode, String extraInfo) {
    super(formatMsg(errorcode, extraInfo));
    this.errorcode = errorcode;
    this.extraInfo = extraInfo;
  }

  public YtException(Errorcode errorcode, String extraInfo, String stackTrace) {
    super(formatMsg(errorcode, extraInfo, stackTrace));
    this.errorcode = errorcode;
    this.extraInfo = extraInfo;
  }

  public String getExtraInfo() {
    return extraInfo;
  }

  public Errorcode getErrorcode() {
    return errorcode;
  }

  private static String formatMsg(Errorcode errorcode, String extraInfo) {


    return String.format(format, errorcode.getErrorcode(), errorcode.getTypeId(),
        errorcode.getAppId(), errorcode.getCode(), errorcode.getDesc(), extraInfo);
  }

  private static String formatMsg(Errorcode errorcode, String extraInfo, String trace) {


    return String.format(formatTrace, errorcode.getErrorcode(), errorcode.getTypeId(),
        errorcode.getAppId(), errorcode.getCode(), errorcode.getDesc(), extraInfo, trace);
  }



}
