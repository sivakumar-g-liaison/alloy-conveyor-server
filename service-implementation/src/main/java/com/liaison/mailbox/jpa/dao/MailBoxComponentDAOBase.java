package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailBoxComponent;

public class MailBoxComponentDAOBase extends GenericDAOBase <MailBoxComponent> implements MailBoxComponentDAO,MailBoxDAO {

	public MailBoxComponentDAOBase(){
		super(PERSISTENCE_UNIT_NAME);
	}
	
	public MailBoxComponent find(String profile) { 
        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        try {
        	
        	List<MailBoxComponent> mailBoxComponent = entityManager.createNamedQuery(FIND_MAILBOX_COMP_BY_PROFILE).setParameter(PROFILE, profile).getResultList();
        	Iterator<MailBoxComponent> iter = mailBoxComponent.iterator();
        	
        	while ( iter.hasNext() ){ 
        		   return iter.next();
        		}
           		
        } finally {
            if (entityManager != null) {
                entityManager.clear();
            }
        }
		return null ;
    }

}
