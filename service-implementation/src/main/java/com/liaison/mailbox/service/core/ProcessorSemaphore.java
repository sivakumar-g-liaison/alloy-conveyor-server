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

import java.util.ArrayList;
import java.util.List;

import com.liaison.mailbox.jpa.model.ParallelProcessor;
import com.liaison.mailbox.jpa.model.Processor;
import com.liaison.mailbox.jpa.dao.ParallelProcessorDAO;
import com.liaison.mailbox.jpa.dao.ParallelProcessorDAOBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor Semaphore which ensures processor can be run only once at a time.
 * 
 * @author veerasamyn
 */
public class ProcessorSemaphore {

	/**
	 * List of running processors.
	 */
	private static List<Processor> synchronizedProcessors = new ArrayList<>();
	
	private static final Logger LOG = LoggerFactory.getLogger(ProcessorSemaphore.class);


	/**
	 * 
	 */
	public ProcessorSemaphore() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Check and add the processor to the running list.
	 * 
	 * @param boolean
	 */
	public static synchronized boolean validateProcessorExecution(Processor processor) {

		List<Processor> existingProcessors = new ArrayList<>();
		boolean result = false;
		for (Processor prcsr : synchronizedProcessors) {
				if (prcsr.equals(processor)) {
					existingProcessors.add(processor);
					break;
			}
		}

		if (!existingProcessors.isEmpty()) {
			result = false;
		} else {
			synchronizedProcessors.add(processor);
			result = true;
		}
		return result;
	}

	/**
	 * Check and add the processor to the running list.
	 * 
	 * @param processors
	 */
	public static synchronized void validateProcessorExecution(List<Processor> processors) {

		List<Processor> existingProcessors = new ArrayList<>();

		for (Processor prcsr : synchronizedProcessors) {
			for (Processor inputPrcsr : processors) {
				if (prcsr.equals(inputPrcsr)) {
					existingProcessors.add(inputPrcsr);
				}
			}
		}

		if (!existingProcessors.isEmpty()) {
			processors.removeAll(existingProcessors);
		}

		if (!processors.isEmpty()) {
			synchronizedProcessors.addAll(processors);
		}

	}

	/**
	 * Remove the processors from the running list.
	 * 
	 * @param processors
	 */
	public static synchronized void removeExecutedProcessor(List<Processor> processors) {
		synchronizedProcessors.removeAll(processors);
	}

	/**
	 * Remove the processor from running list
	 * 
	 * @param processor
	 */
	public static synchronized void removeExecutedProcessor(Processor processor) {
		synchronizedProcessors.remove(processor);
	}

	/**
	 * Check and add the processor to the DB.
	 * 
	 * @param processorID
	 */
	public static synchronized void addToProcessorExecutionList(String processorId) {

		ParallelProcessor processorFromDB = null;
		
		ParallelProcessorDAO processorDAO = new ParallelProcessorDAOBase();
		processorFromDB = processorDAO.findById(processorId);	
		// if processor id present in DB then processor is still running
		if (processorFromDB != null) {
			LOG.info("The processor is already in progress , validated via DB."+ processorId);
				
		} else {
			
			ParallelProcessor parallelProcessor = new ParallelProcessor();
			parallelProcessor.setProcessorId(processorId);
			processorDAO.persist(parallelProcessor);
		}
		
	}
	
	/**
	 * Remove the processorId from table ParallelProcessor in DB
	 * 
	 * @param processorId
	 */
	public static synchronized void removeExecutedProcessor(String processorId) {
		
		ParallelProcessorDAO processorDAO = new ParallelProcessorDAOBase();
		
		ParallelProcessor processorToBeRemoved = null;
		
		processorToBeRemoved = processorDAO.findById(processorId);
		
		if(processorToBeRemoved != null) {
			processorDAO.remove(processorToBeRemoved);
		}
		
	}

}
