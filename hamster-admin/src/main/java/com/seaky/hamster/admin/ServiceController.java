package com.seaky.hamster.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {


  @Autowired
  private EtcdRegisterationManageService etcdRegisterationManageService;

  // 依据名字搜索服务
  @RequestMapping("/serviceSearch")
  public Response serviceSearch(@RequestParam(required = false, value = "q") String q) {
    List<String> datas = etcdRegisterationManageService.searchService(q);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }


  @RequestMapping("/appSearch")
  public Object appSearch(@RequestParam(required = false, value = "q") String q) {
    List<String> datas = etcdRegisterationManageService.searchApp(q);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  @RequestMapping("/nodeSearch")
  public Object nodeSearch(@RequestParam(required = false, value = "q") String q) {
    List<String> datas = etcdRegisterationManageService.searchNode(q);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 列出某个服务的所有实例
  @RequestMapping("/serviceInstanceList")
  public Object listAllServiceInstance(@RequestParam String serviceName) {
    Map<String, List<ServiceInstanceView>> datas =
        etcdRegisterationManageService.getAllServiceInstance(serviceName);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 列出某个服务被引用的实例
  @RequestMapping("/referInstanceList")
  public Object listAllReferInstance(@RequestParam String serviceName) {
    Map<String, List<ReferInstanceView>> datas =
        etcdRegisterationManageService.getAllReferInstance(serviceName);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }


  // 列出某个应用暴露的服务
  @RequestMapping("/appExportServiceList")
  public Object appExportServiceList(@RequestParam String app) {
    Map<String, List<ServiceInstanceView>> datas =
        etcdRegisterationManageService.getAppServiceList(app);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 列出某个应用引用的服务
  @RequestMapping("/appReferServiceList")
  public Object appReferServiceList(@RequestParam String app) {
    Map<String, List<ReferInstanceView>> datas =
        etcdRegisterationManageService.getAppReferList(app);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  // 列出某个节点部署的应用
  @RequestMapping("/nodeExportServiceList")
  public Object nodeExportServiceList(@RequestParam String node) {

    Map<String, List<ServiceInstanceView>> datas =
        etcdRegisterationManageService.nodeExportServiceList(node);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;

  }

  @RequestMapping("/nodeReferServiceList")
  public Object nodeReferServiceList(@RequestParam String node) {
    Map<String, List<ReferInstanceView>> datas =
        etcdRegisterationManageService.nodeReferServiceList(node);
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;


  }

  @RequestMapping("/appDependencyGraph")
  public Object appDependencyGraph() {
    GraphView datas = etcdRegisterationManageService.appDependencyGraph();
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  @RequestMapping("/nodeDependencyGraph")
  public Object nodeDependencyGraph() {
    GraphView datas = etcdRegisterationManageService.nodeDependencyGraph();
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

  @RequestMapping("/configDesc")
  public Object configDesc() {
    Map<String, String> datas = HamsterConfigEnum.getConfigMaps();
    Response rsp = new Response();
    rsp.setData(datas);
    return rsp;
  }

}
