/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the PROCESSOR_SEMAPHORE database table
 *
 */
@Entity
@Table(name = "PROCESSOR_SEMAPHORE")
@NamedQuery(name = "ParallelProcessor.findAll", query = "SELECT p FROM ParallelProcessor p")
public class ParallelProcessor implements Identifiable {
	
	private static final long serialVersionUID = 1L;

	private String processorId;
		
	public ParallelProcessor() {
		
	}
		
	@Id
	@Column(name = "PROCESSOR_ID", unique = true, nullable = false, length = 32)
	public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	

	@Override
	@Transient
	public Object getPrimaryKey() {
		// TODO Auto-generated method stub
		return getProcessorId();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transient
	public Class getEntityClass() {
		return this.getClass();
	}

}
