package com.seaky.hamster.admin;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class HeaderFilter implements Filter {
	private final static String XFO = "X-Frame-Options";

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
//		System.out.println("*** doFilter ***");
		
		chain.doFilter(request, response);
		
		HttpServletResponse httpResp = (HttpServletResponse) response;
		if (httpResp.containsHeader(XFO)) {
//			System.out.println("*** containsHeader ***");
			httpResp.setHeader(XFO, "SAMEORIGIN");
		}
		
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
