/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.grammer.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import com.liaison.mailbox.grammer.DataTransferObject;

/**
 * Data Transfer Object that implements fields required for
 * configuration request. 
 *
 * @author veerasamyn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace = "http://mailbox.liaison.com/request")
public class ProfileConfigurationRequest {

	private String id;
	private String name;
	private String url;
	private String profile;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
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
