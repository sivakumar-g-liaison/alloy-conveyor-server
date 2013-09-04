package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.Processor;

@NamedQueries({
    @NamedQuery(name=ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PGUID,
            query="SELECT processor FROM Processor processor WHERE processor.pguid = :" + ProcessorConfigurationDAO.PGUID)
})

public interface ProcessorConfigurationDAO extends GenericDAO <Processor>{
	
	public static final String FIND_PROCESSOR_BY_PGUID = "findProcessorByPguid";
	public static final String PGUID = "pguid";
	
	public Processor find(String guid);
	public void softRemove(String guId);

}

