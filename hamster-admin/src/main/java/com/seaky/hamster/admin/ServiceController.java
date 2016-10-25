package com.seaky.hamster.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {


  @Autowired
  private EtcdRegisterationManageService etcdRegisterationManageService;

  // 所有的服务列表
  @RequestMapping("/serviceList")
  public Response serviceList() {
    List<String> datas = etcdRegisterationManageService.getAllServiceNames();
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 依据名字搜索服务
  @RequestMapping("/serviceSearch")
  public Response serviceSearch(@RequestParam("q") String q) {
    List<String> datas = etcdRegisterationManageService.searchService(q);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }


  // 应用列表
  @RequestMapping("/appList")
  public Object appList() {
    List<String> datas = etcdRegisterationManageService.getAllApp();
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  @RequestMapping("/appSearch")
  public Object appSearch(@RequestParam("q") String q) {
    List<String> datas = etcdRegisterationManageService.searchApp(q);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 节点列表
  @RequestMapping("/nodeList")
  public Object nodeList() {
    List<String> datas = etcdRegisterationManageService.getAllNode();
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  @RequestMapping("/nodeSearch")
  public Object nodeSearch(@RequestParam("q") String q) {
    List<String> datas = etcdRegisterationManageService.searchNode(q);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 列出某个服务的所有实例
  @RequestMapping("/serviceInstanceList")
  public Object listAllServiceInstance(@RequestParam String serviceName) {
    List<ServiceInstanceView> datas =
        etcdRegisterationManageService.getAllServiceInstance(serviceName);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 列出某个服务被引用的实例
  @RequestMapping("/referInstanceList")
  public Object listAllReferInstance(@RequestParam String serviceName) {
    List<ReferInstanceView> datas = etcdRegisterationManageService.getAllReferInstance(serviceName);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 列出某个服务所在应用的情况
  @RequestMapping("/serviceAppList")
  public Object serviceAppList(@RequestParam String serviceName) {
    return null;
  }

  // 列出某个应用暴露的服务
  @RequestMapping("/appExportServiceList")
  public Object appExportServiceList(@RequestParam String app) {
    return null;
  }

  // 列出某个应用引用的服务
  @RequestMapping("/appReferServiceList")
  public Object appReferServiceList(@RequestParam String app) {
    return null;
  }

  // 列出某个节点部署的应用
  @RequestMapping("/nodeAppList")
  public Object nodeAppList(@RequestParam String node) {
    return null;
  }



  @RequestMapping("/serviceConfig")
  public Object listServiceConfig(@RequestParam String app, @RequestParam String serviceName,
      @RequestParam String configKey) {

    return null;
  }

  @RequestMapping(value = "/updateServiceConfig")
  public Object listServiceConfig(@RequestParam String app, @RequestParam String serviceName,
      @RequestParam String configKey, @RequestBody Map<String, String> config) {
    return "success";
  }

  @RequestMapping("/referConfig")
  public Object listReferConfig(@RequestParam String app, @RequestParam String serviceName,
      @RequestParam String configKey) {

    return null;
  }

  @RequestMapping("/updateReferConfig")
  public Object listReferConfig(@RequestParam String app, @RequestParam String serviceName,
      @RequestParam String configKey, @RequestBody Map<String, String> config) {


    return "success";
  }

  @RequestMapping("/searchService")
  public Object searchService(@RequestParam(required = false) String app,
      @RequestParam(required = false) String serviceName,
      @RequestParam(required = false) String version, @RequestParam(required = false) String host,
      @RequestParam(required = false, defaultValue = "0") int port,
      @RequestParam(required = false) String protocol) {
    return null;
  }



}
