package com.seaky.hamster.admin;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

	// 所有的服务列表
	@RequestMapping("/allServiceNameList")
	public Object listAllServiceName() {

		return ServiceDal.dal.allServiceName();
	}

	@RequestMapping("/serviceInstanceList")
	public Object listAllServiceInstance(@RequestParam String serviceName) {
		return ServiceDal.dal.allServiceInstance(serviceName);
	}

	@RequestMapping("/referInstanceList")
	public Object listAllReferInstance(@RequestParam String serviceName) {
		return ServiceDal.dal.allReferInstance(serviceName);
	}

	@RequestMapping("/serviceConfig")
	public Object listServiceConfig(@RequestParam String app,
			@RequestParam String serviceName, @RequestParam String configKey) {

		return ServiceDal.dal.getServiceConfig(app, serviceName, configKey);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
	@RequestMapping(value="/updateServiceConfig")
	public Object listServiceConfig(@RequestParam String app,
			@RequestParam String serviceName, @RequestParam String configKey,
			@RequestBody Map<String, String> config) {
		ServiceDal.dal.updateServiceConfig(app, serviceName, configKey, config);
		return "success";
	}

	@RequestMapping("/referConfig")
	public Object listReferConfig(@RequestParam String app,
			@RequestParam String serviceName, @RequestParam String configKey) {

		return ServiceDal.dal.getReferConfig(app, serviceName, configKey);
	}

	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@RequestMapping("/updateReferConfig")
	public Object listReferConfig(@RequestParam String app,
			@RequestParam String serviceName, @RequestParam String configKey,
			@RequestBody Map<String, String> config) {

		ServiceDal.dal.updateReferConfig(app, serviceName, configKey, config);

		return "success";
	}

	@RequestMapping("/searchService")
	public Object searchService(@RequestParam(required = false) String app,
			@RequestParam(required = false) String serviceName,
			@RequestParam(required = false) String version,
			@RequestParam(required = false) String host,
			@RequestParam(required = false,defaultValue="0") int port,
			@RequestParam(required = false) String protocol) {
		return ServiceDal.dal.searchService(app, serviceName, version, host, port, protocol);
	}
	
	

}
