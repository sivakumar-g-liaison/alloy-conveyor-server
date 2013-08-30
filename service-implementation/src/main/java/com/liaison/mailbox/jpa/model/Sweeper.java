package com.liaison.mailbox.jpa.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.liaison.mailbox.MailBoxConstants;

@Entity
@DiscriminatorValue(MailBoxConstants.SWEEPER)
public class Sweeper extends Processor {

	private static final long serialVersionUID = 1L;

	@Override
	public Class getEntityClass() {
		return this.getClass();
	}

}
