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

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailBox;
import com.liaison.mailbox.jpa.model.MailBoxSchedProfile;

public class MailBoxScheduleProfileConfigurationDAOBase extends GenericDAOBase<MailBoxSchedProfile> implements
		MailBoxScheduleProfileConfigurationDAO, MailBoxDAO {

	public MailBoxScheduleProfileConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}
	
	public MailBoxSchedProfile find(String mailboxguid, String profileguid) {
		
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        try {
        	
        	List<MailBoxSchedProfile> mailBox = entityManager.createNamedQuery(GET_MAILBOX_PROFILESCHEDULE).setParameter(MAILBOXPGUID, mailboxguid).setParameter(PROFILEPGUID, profileguid).getResultList();
        	Iterator<MailBoxSchedProfile> iter = mailBox.iterator();
        	
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
