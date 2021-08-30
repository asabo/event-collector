package com.db.logcollector.app;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.google.common.collect.ImmutableSet;

public final class AuthFilter implements javax.servlet.Filter {
	private final static Logger LOG = Log.getLogger(AuthFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	static Set<String> passThroughs = ImmutableSet.of("/v1/auth", "/v1/results");

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String pathInfo = req.getPathInfo();

		if (passThroughs.contains(pathInfo)) {
			chain.doFilter(request, response);
			return;
		}

 
		String reqAuth = req.getHeader("X-LGC-AUTH");

		try {
			UUID uuid = reqAuth == null ? null : UUID.fromString(reqAuth);
		
			
			LOG.info("Trying to log user with auth: " + reqAuth + " got uuid: " + uuid);
			
			//here needs to perform authorization for specific endpoints requiring it
			
			//if (loggedUserIdForUUID == null) 
			//	throw new NotAuthorizedException(response);
			
			//request.setAttribute("userId", uuid == null ? null : loggedUserIdForUUID);

		} catch (IllegalArgumentException illa) {
			LOG.warn("Illegal arg exception, reqAuth: " + reqAuth, illa);
		} finally {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		
	}

}
