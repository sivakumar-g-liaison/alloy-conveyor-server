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
import javax.persistence.Transient;

/**
* The persistent class for the processor type file writer in processor database table.
* 
* @author OFS
*/
@Entity
@DiscriminatorValue(Processor.TYPE_FILE_WRITER)
public class FileWriter extends Processor {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transient
	public  Class getEntityClass() {
		return this.getClass();
	}

}
