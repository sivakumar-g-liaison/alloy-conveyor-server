package com.liaison.mailbox.jpa.dao;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.jpa.model.MailBoxComponent;
import com.liaison.mailbox.jpa.model.Processor;

public class ProcessorComponentDAOBase extends GenericDAOBase <Processor> implements ProcessorComponentDAO,MailBoxDAO {

	public ProcessorComponentDAOBase(){
		super(PERSISTENCE_UNIT_NAME);
	}
}
