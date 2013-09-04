package com.liaison.mailbox.jpa.dao;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.Processor;


public interface ProcessorConfigurationDAO extends GenericDAO <Processor>{
	
public void softRemove(String guId);


}

