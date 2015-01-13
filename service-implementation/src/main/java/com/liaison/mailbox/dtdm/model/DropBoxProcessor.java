package com.liaison.mailbox.dtdm.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(Processor.TYPE_DROPBOX_PROCESSOR)
public class DropBoxProcessor extends Processor {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public  Class getEntityClass() {
		return this.getClass();
	}
}
