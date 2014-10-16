/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * 
 *  @author OFS
 */
@Entity
@DiscriminatorValue(value = Processor.TYPE_REMOTEDOWNLOADER)
public class RemoteDownloader extends Processor {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getEntityClass() {
		return this.getClass();
	}

}
