/*
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.framework;


import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.AuditStatement.Status;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.audit.log4j2.auditmessage.LogTags;
import com.liaison.commons.audit.pci.PCIV20Requirement;
import com.liaison.framework.exceptions.UnhandledApplicationException;

/**
 * Framework Filter
 * <p/>
 * <P>Filters incoming requests for audit and handling of propagated exceptions
 *
 * @author Robert.Christian
 * @version 1.0
 */

public class FrameworkFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(FrameworkFilter.class);

    protected String initializeAuditID() {
    	return UUID.randomUUID().toString();
    }
    
    public void fishTag(ServletRequest request) {
    	//Initial FishTags
    	ThreadContext.put(LogTags.LOG_ID, initializeAuditID());
    	ThreadContext.put(LogTags.REMOTE_ADDRESS, request.getRemoteAddr()); 
    	ThreadContext.put(LogTags.REMOTE_HOST, request.getRemoteHost()); 
    	ThreadContext.put(LogTags.REMOTE_PORT, Integer.toString(request.getRemotePort())); 
    	ThreadContext.put(LogTags.START, DateFormat.getDateTimeInstance().format(new Date()));
    	
    	if (request instanceof HttpServletRequest) {
    		
    		HttpServletRequest httpRequest = ((HttpServletRequest)request);
    		// logger.info("Fish Tagging Request URL..."+httpRequest.getRequestURL().toString());
   	     	ThreadContext.put(LogTags.RESOURCE, httpRequest.getServletPath());
    	     ThreadContext.put(LogTags.REQUEST_URL, httpRequest.getRequestURL().toString()); 
    	     ThreadContext.put(LogTags.REQUEST_QUERY, httpRequest.getQueryString());
    	     String userPrincipal = (null != httpRequest.getUserPrincipal()) ? httpRequest.getUserPrincipal().getName() : "NOUSER";
    	     ThreadContext.put(LogTags.USER_PRINCIPAL, userPrincipal);    	 
    	     String remoteUser = (null != httpRequest.getRemoteUser()) ? httpRequest.getRemoteUser() : "NOUSER";
    	     ThreadContext.put(LogTags.REMOTE_USER, remoteUser);    	 
    	}
    }
    
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing FrameworkFilter...");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	
    	if (!ThreadContext.isEmpty()) {
    		ThreadContext.clearAll();
    	}
    	
    	fishTag(request);
    	//logger.info(new DefaultAuditStatement(Status.ATTEMPT,"example log for attempted", PCIV20Requirement.PCI10_2_3));
           try {
            // audit
            chain.doFilter(request, response);
               
          } catch (Throwable t) {


        	if (t instanceof AuditStatement) {
            	logger.catching(t);        		
        	}
        	throw logger.throwing(new UnhandledApplicationException(t));


        }
        
// audit sucess and fail logging
// Because Jetty has an oldentimes HttpServletResponse we are unable to use this code block until it's removed.
//      if (response instanceof HttpServletResponse) {
//    	  HttpServletResponse hResponse = (HttpServletResponse)response;
//    	  int status = hResponse.getStatus();
//    	  if (status >= 200 && status < 300) {
//    	    	logger.info(new DefaultAuditStatement(Status.SUCCEED,"response status >= 200 and < 300 so we assume success."));    		  
//    	  } else {
//    		    logger.error(new DefaultAuditStatement(Status.FAILED,"generic potential failure, response status:" + status));
//    	  }
//    	  
//     } else {
//     	logger.info(new DefaultAuditStatement(Status.SUCCEED,"No response status or thrown errors... assuming success"));    	 
//     }
        
    }

    @Override
    public void destroy() {
        logger.debug("FrameworkFilter Destroyed");
    }

}