
package com.liaison.mailbox.jpa.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.ParallelProcessor;;

public interface ParallelProcessorDAO extends GenericDAO<ParallelProcessor> {
		
	public ParallelProcessor findById(String processorID);
}
