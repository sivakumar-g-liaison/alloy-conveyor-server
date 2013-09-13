/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import java.util.Comparator;

import com.liaison.mailbox.jpa.model.Processor;

/**
 * @author karthikeyanm
 *
 */
public class ProcessorExecutionOrderComparator 
implements Comparator<Processor> {

	@Override
	public int compare(Processor o1, Processor o2) {
		
		Integer one = o1.getExecutionOrder();
		Integer two = o2.getExecutionOrder();
		
		return one.compareTo(two);
	}

}
