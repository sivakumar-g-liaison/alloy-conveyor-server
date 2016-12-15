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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the FOLDERS database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "FOLDER")
@NamedQuery(name = "Folder.findAll", query = "SELECT f FROM Folder f")
public class Folder implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String fldrDesc;
	private String fldrType;
	private String fldrUri;	
	private Processor processor;
	private String originatingDc;

	public Folder() {
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "DESCRIPTION", length = 256)
	public String getFldrDesc() {
		return this.fldrDesc;
	}

	public void setFldrDesc(String fldrDesc) {
		this.fldrDesc = fldrDesc;
	}

	@Column(name = "TYPE", nullable = false, length = 64)
	public String getFldrType() {
		return this.fldrType;
	}

	public void setFldrType(String fldrType) {
		this.fldrType = fldrType;
	}

	@Column(name = "URI", length = 256)
	public String getFldrUri() {
		return this.fldrUri;
	}

	public void setFldrUri(String fldrUri) {
		this.fldrUri = fldrUri;
	}

	// bi-directional many-to-one association to Processor
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "PROCESSOR_GUID", nullable = false)
	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	@Column(name = "ORIGINATING_DC", length = 16)
	public String getOriginatingDc() {
		return originatingDc;
	}

	public void setOriginatingDc(String originatingDc) {
		this.originatingDc = originatingDc;
	}
	
	@Override
	@Transient
	public Object getPrimaryKey() {
		return getPguid();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transient
	public Class getEntityClass() {
		return this.getClass();
	}

}