package com.liaison.mailbox.jpa.model;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the "LANGUAGE" database table.
 * 
 */
@Entity
@Table(name="LANGUAGE")
@NamedQuery(name="Language.findAll", query="SELECT l FROM Language l")
public class Language implements com.liaison.commons.jpa.Identifiable {
	private static final long serialVersionUID = 1L;
	private String pguid;
	private String name;
	private Account account;

	public Language() {
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


	//bi-directional one-to-one association to Account
	@OneToOne(mappedBy = "language", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
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