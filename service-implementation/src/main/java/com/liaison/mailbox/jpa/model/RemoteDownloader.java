package com.liaison.mailbox.jpa.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.liaison.mailbox.MailBoxConstants;

@Entity
@DiscriminatorValue(MailBoxConstants.REMOTE_DOWNLOADER)
public class RemoteDownloader extends Processor {

	private static final long serialVersionUID = 1L;

	@Override
	@Transient
	public Class getEntityClass() {
		return this.getClass();
	}

}
