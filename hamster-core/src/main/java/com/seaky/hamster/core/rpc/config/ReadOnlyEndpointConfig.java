/**
 */
package com.seaky.hamster.core.rpc.config;

import org.apache.commons.lang.NotImplementedException;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 16, 2016
 */
public class ReadOnlyEndpointConfig extends EndpointConfig {

	public ReadOnlyEndpointConfig(EndpointConfig config) {
		super(config);
	}

	public void addConfigItem(ConfigItem item) {
		throw new NotImplementedException();
	}

	public boolean addConfigItem(ConfigItem item, boolean isOverride) {
		throw new NotImplementedException();
	}

	public void remove(String key) {
		throw new NotImplementedException();
	}

	public void remove(ConfigItem item) {
		throw new NotImplementedException();
	}
}
