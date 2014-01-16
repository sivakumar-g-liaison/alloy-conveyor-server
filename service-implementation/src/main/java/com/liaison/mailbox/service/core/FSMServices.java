/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core;

import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.jpa.dao.FSMStateDAO;
import com.liaison.mailbox.jpa.dao.FSMStateDAOBase;
import com.liaison.mailbox.jpa.dao.FSMStateValueDAO;
import com.liaison.mailbox.jpa.dao.FSMStateValueDAOBase;
import com.liaison.mailbox.jpa.model.FSMState;
import com.liaison.mailbox.jpa.model.FSMStateValue;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class FSMServices {

	private static final Logger LOG = LoggerFactory.getLogger(MailBoxConfigurationService.class);

	/**
	 * Creates FSMState.
	 * 
	 * @param request
	 *            The request DTO.
	 * @return The responseDTO.
	 */
	public void createFSMState(String stateName) {

		try {

			if (stateName == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			// persisting the fsm state
			FSMStateDAO fsmStateDao = new FSMStateDAOBase();
			FSMState state = fsmStateDao.find(stateName);

			if (null != state) {
				throw new MailBoxConfigurationServicesException("FSMState already exists.");
			}
						
			FSMState fsmState = new FSMState();
			fsmState.setPguid(MailBoxUtility.getGUID());
			fsmState.setName(stateName);

			fsmStateDao.persist(fsmState);

		} catch (MailBoxConfigurationServicesException e) {

			LOG.info("FSMState Creation Failed");
			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
		}
	}

	/**
	 * Reads FSMState.
	 * 
	 * @param request
	 *            The request DTO.
	 * @return The responseDTO.
	 */

	public void readFSMState(String stateName) {

		try {

			if (stateName == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			// persisting the fsm state
			FSMStateDAO fsmStateDao = new FSMStateDAOBase();
			FSMState state = fsmStateDao.find(stateName);

			if (null == state) {
				throw new MailBoxConfigurationServicesException("Can't read FSMState");
			}
			
			LOG.info("FSMState successfully read.");

		} catch (MailBoxConfigurationServicesException e) {

			LOG.info("Read FSMState Failed");
			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
		}
	}

	public void createFSMStateValue(String stateName, String value) {

		try {

			if (stateName == null || value == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}
			
			// retrieving the fsm state value
			FSMStateDAO fsmStateDao = new FSMStateDAOBase();
			FSMState state = fsmStateDao.find(stateName);

			if (null == state) {
				throw new MailBoxConfigurationServicesException("FSMState not exists.");
			}

			FSMStateValueDAO fsmStateValDao = new FSMStateValueDAOBase();
			FSMStateValue stateVal = fsmStateValDao.find(value);

			if (null != stateVal) {
				throw new MailBoxConfigurationServicesException("FSMStateValue value already exists.");
			}


			FSMStateValue fsmStateVal = new FSMStateValue();
			fsmStateVal.setPguid(MailBoxUtility.getGUID());
			Date date = new Date();
			Timestamp timeStramp = new Timestamp(date.getTime());
			fsmStateVal.setCreatedDate(timeStramp);
			fsmStateVal.setValue(value);
			fsmStateVal.setFsmState(state);

			// persisting the fsm state value
			fsmStateValDao.persist(fsmStateVal);

		} catch (MailBoxConfigurationServicesException e) {

			LOG.info("FSMStateValue Creation Failed");
			LOG.error(Messages.CREATE_OPERATION_FAILED.name(), e);
		}
	}

	public void readFSMStateValue(String value) {

		try {

			if (value == null) {
				throw new MailBoxConfigurationServicesException(Messages.INVALID_REQUEST);
			}

			// retrieving the fsm state value
			FSMStateValueDAO fsmStateValDao = new FSMStateValueDAOBase();
			FSMStateValue stateVal = fsmStateValDao.find(value);

			if (null == stateVal) {
				throw new MailBoxConfigurationServicesException(Messages.READ_OPERATION_FAILED, "FSM-stateVal");
			}
			
			LOG.info("FSMStateValue successfully read.");

		} catch (MailBoxConfigurationServicesException e) {

			LOG.info("Read FSMStateValue Failed");
			LOG.error(Messages.READ_OPERATION_FAILED.name(), e);
		}
	}
}
