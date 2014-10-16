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
* The persistent class for the MAILBOX_SERICEINSTANCE database table.
* 
* @author OFS
*/

@Entity
@DiscriminatorValue(Processor.HTTP_ASYNC)
public class HTTPAsyncProcessor extends Processor {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getEntityClass() {
		return this.getClass();
	}
}
