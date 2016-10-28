package com.seaky.hamster.admin;

import org.apache.shiro.authc.AccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;



@RestController
@ControllerAdvice
public class ExceptionHandlerController {

  private static Logger logger = LoggerFactory.getLogger(ExceptionHandlerController.class);

  @ExceptionHandler(Exception.class)
  public Response exceptionHandler(Exception e) {
    Response br = new Response();
    logger.error("", e);
    br.setCode(-1);
    br.setMsg("inner error");
    return br;
  }

  @ExceptionHandler(AccountException.class)
  public Response exceptionHandlerAcc(AccountException e) {
    Response br = new Response();
    br.setCode(1);
    br.setMsg("帐号认证失败");
    return br;
  }

}
