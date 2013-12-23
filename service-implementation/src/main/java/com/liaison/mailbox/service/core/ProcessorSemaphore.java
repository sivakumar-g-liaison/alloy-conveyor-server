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

import com.liaison.mailbox.jpa.model.Processor;

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

}
