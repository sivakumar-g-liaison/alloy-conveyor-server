/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import java.util.List;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.rtdm.model.ProcessorExecutionState;

/**
 * @author OFS
 *
 */
@NamedQueries({ @NamedQuery(name = ProcessorExecutionStateDAO.FIND_BY_PROCESSOR_ID,
			query = "SELECT executionState FROM ProcessorExecutionState executionState WHERE executionState.processorId = :" + ProcessorExecutionStateDAO.PROCESSOR_ID),
@NamedQuery(name = ProcessorExecutionStateDAO.FIND_NON_EXECUTING_PROCESSORS,
			query = "SELECT executionState.processorId FROM ProcessorExecutionState executionState WHERE executionState.executionStatus not like :" + ProcessorExecutionStateDAO.EXEC_STATUS),
@NamedQuery(name = ProcessorExecutionStateDAO.FIND_EXECUTING_PROCESSORS,
			query = "SELECT executionState.processorId FROM ProcessorExecutionState executionState WHERE executionState.executionStatus like :" + ProcessorExecutionStateDAO.EXEC_STATUS)
})

public interface ProcessorExecutionStateDAO extends GenericDAO<ProcessorExecutionState> {

	public static final String FIND_BY_PROCESSOR_ID = "findByProcessorId";
	public static final String FIND_NON_EXECUTING_PROCESSORS = "findNonExecutingProcessors";
	public static final String FIND_EXECUTING_PROCESSORS = "findExecutingProcessors";
	public static final String PROCESSOR_ID = "processorId";
	public static final String EXEC_STATUS = "exec_status";

	public ProcessorExecutionState findByProcessorId(String processorId);

	public void addProcessorExecutionState(String processorId, String executionStatus);

	public List <String> findNonExecutingProcessors();
	
	public List <String> findExecutingProcessors();

}
