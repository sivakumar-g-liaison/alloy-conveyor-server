/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.jpa.dao;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.ParallelProcessor;

public class ParallelProcessorDAOBase extends GenericDAOBase<ParallelProcessor> implements ParallelProcessorDAO, MailBoxDAO {

	public ParallelProcessorDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}	
		
	public ParallelProcessor findById(String processorID) {
		
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);		
		try {
			
			ParallelProcessor parallelProcessor = null;						
			Object parallelProc = this.find(ParallelProcessor.class, processorID);
			if (parallelProc != null) {
				parallelProcessor = (ParallelProcessor)parallelProc;
			}
			return parallelProcessor;			
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}		
	}
}
 