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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the PROCESSOR_PROPERTIES database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "PROCESSOR_PROPERTY")
@NamedQuery(name = "ProcessorProperty.findAll", query = "SELECT p FROM ProcessorProperty p")
public class ProcessorProperty implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String procsrPropName;
	private String procsrPropValue;

	private Processor processor;

	public ProcessorProperty() {
	}

	// bi-directional many-to-one association to Processor
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinColumn(name = "PROCESSOR_GUID", nullable = false)
	public Processor getProcessor() {
		return processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "NAME", nullable = false, length = 128)
	public String getProcsrPropName() {
		return this.procsrPropName;
	}

	public void setProcsrPropName(String procsrPropName) {
		this.procsrPropName = procsrPropName;
	}

	@Column(name = "VALUE", length = 4000)
	public String getProcsrPropValue() {
		return this.procsrPropValue;
	}

	public void setProcsrPropValue(String procsrPropValue) {
		this.procsrPropValue = procsrPropValue;
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