package com.liaison.mailbox.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;


/**
 * The persistent class for the FOLDERS database table.
 * 
 */
@Entity
@Table(name="FOLDERS")
@NamedQuery(name="Folder.findAll", query="SELECT f FROM Folder f")
public class Folder implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String fldrDesc;
	private String fldrType;
	private String fldrUri;

	private Processor processor;

	public Folder() {
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(unique=true, nullable=false, length=32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name="FLDR_DESC", length=250)
	public String getFldrDesc() {
		return this.fldrDesc;
	}

	public void setFldrDesc(String fldrDesc) {
		this.fldrDesc = fldrDesc;
	}

	@Column(name="FLDR_TYPE", nullable=false, length=50)
	public String getFldrType() {
		return this.fldrType;
	}

	public void setFldrType(String fldrType) {
		this.fldrType = fldrType;
	}

	@Column(name="FLDR_URI", length=50)
	public String getFldrUri() {
		return this.fldrUri;
	}

	public void setFldrUri(String fldrUri) {
		this.fldrUri = fldrUri;
	}

	//bi-directional many-to-one association to Processor
	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSORS_GUID", nullable=false)
	public Processor getProcessor() {
		return this.processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	@Override
	@Transient
	public Object getPrimaryKey() {
		return (Object) getPguid();
	}


	@Override
	@Transient
	public  Class getEntityClass() {
		return this.getClass();
	}

}