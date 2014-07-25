package com.liaison.mailbox.jpa.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
* The persistent class for the MAILBOX_SERICEINSTANCE database table.
* 
* @author OFS
*/

@Entity
@DiscriminatorValue(Processor.HTTP_SYNC)
public class HTTPSyncProcessor extends Processor {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getEntityClass() {
		return this.getClass();
	}
}
