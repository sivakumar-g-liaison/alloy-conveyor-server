package com.liaison.mailbox.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;

/**
 * The persistent class for the MAILBOX_COMPONENT database table.
 * 
 */
@Entity
@Table(name="MAILBOX_COMPONENT")
public class MailBoxComponent implements Identifiable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String url;
	private String profile;
	
	@Override
	@Transient
	public Object getPrimaryKey() {
		return getId();
	}

	@Override
	@Transient
	public Class getEntityClass() {
		
		return this.getClass();
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(unique=true, nullable=false)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name="NAME", nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="URL", nullable=false)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public String getProfile() {
		return profile;
	}
	
	@Column(name="PROFILE", nullable=false)
	public void setProfile(String profile) {
		this.profile = profile;
	}

	@Override
	public String toString() {
		return "MailBoxComponent [id=" + id + ", name=" + name + ", url=" + url
				+ ", profile=" + profile + "]";
	}

}
