package com.liaison.mailbox.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;


/**
 * The persistent class for the ACCOUNT_TYPE database table.
 * 
 */
@Entity
@Table(name="ACCOUNT_TYPE")
@NamedQuery(name="AccountType.findAll", query="SELECT a FROM AccountType a")
public class AccountType implements Identifiable {

	private static final long serialVersionUID = 1L;
	private String pguid;
	private String name;

	public AccountType() {
	}

	@Id
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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