package com.liaison.mailbox.jpa.dao;

import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.Processor;

public class ProcessorConfigurationDAOBase extends GenericDAOBase <Processor> implements ProcessorConfigurationDAO,MailBoxDAO {

	public ProcessorConfigurationDAOBase(){
		super(PERSISTENCE_UNIT_NAME);
	}

	@Override
	public void softRemove(String guId) {
		
		Processor processor = this.find(Processor.class, guId);
		
		if (processor != null) {
			
			processor.setProcsrStatus("inactive");
			this.merge(processor);
		}
		
	}
}
