package com.liaison.mailbox.jpa.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.ProcessorForMerge;

public class ProcessorForMergeConfigurationDAOBase extends GenericDAOBase <ProcessorForMerge> implements ProcessorForMergeConfigurationDAO,MailBoxDAO {

	public ProcessorForMergeConfigurationDAOBase(){
		super(PERSISTENCE_UNIT_NAME);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProcessorForMerge> find(String mailboxId, String schedulerProfileId) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<ProcessorForMerge> processorsToMerge;
		
		try {
        	
			List<ProcessorForMerge> processor = entityManager.createNamedQuery(FIND_PROCESSOR_BY_MAILBOX_PROFILE_LINKERID).
        			setParameter(MB_PGUID, mailboxId).setParameter(PROFILE_PGUID, schedulerProfileId).getResultList();
			
			Iterator<ProcessorForMerge> iter = processor.iterator();
			processorsToMerge = new ArrayList<ProcessorForMerge>();
        	
        	while ( iter.hasNext() ){ 
        		   processorsToMerge.add(iter.next());
        		}
           		
        } finally {
            if (entityManager != null) {
                entityManager.clear();
            }
        }
        
		return processorsToMerge;
	}
}
