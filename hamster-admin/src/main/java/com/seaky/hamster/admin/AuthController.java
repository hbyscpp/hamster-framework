package com.seaky.hamster.admin;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  @RequestMapping("/checkpwd")
  public Object check(HttpServletRequest request, @RequestParam String username,
      @RequestParam String password) {
    Map<String, Object> map = new HashMap<String, Object>();
    UsernamePasswordToken token = new UsernamePasswordToken(username, password);
    Subject currentUser = SecurityUtils.getSubject();
    currentUser.login(token);
    SavedRequest savedRequest = WebUtils.getSavedRequest(request);
    if (savedRequest == null) {
      map.put("jumpPage", "/index");
    } else {
      map.put("jumpPage", savedRequest.getRequestUrl());
    }
    map.put("jsessionid", request.getSession().getId());
    return new Response();
  }

  @RequestMapping("/logout")
  public Response logout() {
    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated()) {
      subject.logout(); // session 会销毁，在SessionListener监听session销毁，清理权限缓存
    }
    return new Response();
  }

}
