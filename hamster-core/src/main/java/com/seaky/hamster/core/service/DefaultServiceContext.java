/**
 * Copyright (c) @2016,重庆云途交通科技有限公司.版权所有
 */
package com.seaky.hamster.core.service;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;

/**
 * @Description TODO 描述类的用途
 * @author seaky
 * @since TODO 从哪个项目版本开始创建
 * @Date Mar 6, 2016
 */
public class DefaultServiceContext implements ServiceContext {
	
	
	private ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public void setAttribute(String key, Object obj) {
		attributes.put(key, obj);
	}

	@Override
	public void removeAttribute(String key) {
		attributes.removeKey(key);
	}

}
