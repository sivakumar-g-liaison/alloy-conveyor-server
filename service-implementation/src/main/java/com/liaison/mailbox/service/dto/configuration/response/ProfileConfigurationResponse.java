/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import com.liaison.mailbox.grammer.DataTransferObject;

/**
 * A Data Transfer Object that implements fields required for configuration response. 
 *
 * @author veerasamyn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace = "http://mailbox.liaison.com/response")
public class ProfileConfigurationResponse {

	private String id;

	@XmlElementRef
	private DataTransferObject dataTransferObject;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the dataTransferObject
	 */
	public DataTransferObject getDataTransferObject() {
		return dataTransferObject;
	}

	/**
	 * @param dataTransferObject the dataTransferObject to set
	 */
	public void setDataTransferObject(DataTransferObject dataTransferObject) {
		this.dataTransferObject = dataTransferObject;
	}

}
