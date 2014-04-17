/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.liaison.service.core;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;

import oracle.ucp.UniversalConnectionPoolException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.liaison.commons.acl.util.ACLUtil;
import com.liaison.commons.acl.util.SignatureVerifier;
import com.liaison.commons.acl.util.example.ExampleBase64EncodedSignatureVerifier;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.util.UUIDGen;
import com.netflix.karyon.spi.Component;

/**
 * @author Nitesh Kant
 *  */
@Component
public class HelloworldComponent {
	private static final Logger logger = LogManager.getLogger(HelloworldComponent.class);
	
	@PostConstruct
    public void initialize() throws NamingException, SQLException, UniversalConnectionPoolException {
        // Statements added for deprecating the Initialization Servlet defined in web.xml  
		//OracleDataSource.initOracleDataSource(); // TODO
		  DAOUtil.init(); // TODO This does the work of loading all JAP entity files. We
		      // should change to allow the query string to be passed.
		  UUIDGen.init();
		  
		//Set ACL Filter Signature Verifier
  		SignatureVerifier aclSignatureVerifier = new ExampleBase64EncodedSignatureVerifier();
  		ACLUtil.setSignatureVerifier(aclSignatureVerifier);
		  
		        // Statements added for deprecating the Initialization Servlet defined in web.xml  
		logger.info("HelloworldComponent.initialize()");
    }
}
