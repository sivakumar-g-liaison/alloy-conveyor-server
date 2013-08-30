package com.liaison.mailbox.jpa.model;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;


/**
 * The persistent class for the MAILBOXES database table.
 * 
 */
@Entity
@Table(name="MAILBOXES")
@NamedQuery(name="MailBox.findAll", query="SELECT m FROM MailBox m")
public class MailBox implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String mbxDesc;
	private String mbxName;
	private String mbxStatus;
	private BigDecimal serviceInstId;
	private String shardKey;

	private List<MailBoxProperty> mailboxProperties;
	private List<MailboxSchedProfile> mailboxSchedProfiles;

	public MailBox() {
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

	@Column(name="MBX_DESC", length=1024)
	public String getMbxDesc() {
		return this.mbxDesc;
	}

	public void setMbxDesc(String mbxDesc) {
		this.mbxDesc = mbxDesc;
	}

	@Column(name="MBX_NAME", nullable=false, length=128)
	public String getMbxName() {
		return this.mbxName;
	}

	public void setMbxName(String mbxName) {
		this.mbxName = mbxName;
	}

	@Column(name="MBX_STATUS", nullable=false, length=128)
	public String getMbxStatus() {
		return this.mbxStatus;
	}

	public void setMbxStatus(String mbxStatus) {
		this.mbxStatus = mbxStatus;
	}

	@Column(name="SERVICE_INST_ID")
	public BigDecimal getServiceInstId() {
		return this.serviceInstId;
	}

	public void setServiceInstId(BigDecimal serviceInstId) {
		this.serviceInstId = serviceInstId;
	}

	@Column(name="SHARD_KEY", length=512)
	public String getShardKey() {
		return this.shardKey;
	}

	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
	}

	//bi-directional many-to-one association to MailboxProperty
	@OneToMany(mappedBy="mailbox", cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
	public List<MailBoxProperty> getMailboxProperties() {
		return this.mailboxProperties;
	}

	public void setMailboxProperties(List<MailBoxProperty> mailboxProperties) {
		this.mailboxProperties = mailboxProperties;
	}

	public MailBoxProperty addMailboxProperty(MailBoxProperty mailboxProperty) {
		getMailboxProperties().add(mailboxProperty);
		mailboxProperty.setMailbox(this);

		return mailboxProperty;
	}

	public MailBoxProperty removeMailboxProperty(MailBoxProperty mailboxProperty) {
		getMailboxProperties().remove(mailboxProperty);
		mailboxProperty.setMailbox(null);

		return mailboxProperty;
	}


	//bi-directional many-to-one association to MailboxSchedProfile
	@OneToMany(mappedBy="mailbox", cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
	public List<MailboxSchedProfile> getMailboxSchedProfiles() {
		return this.mailboxSchedProfiles;
	}

	public void setMailboxSchedProfiles(List<MailboxSchedProfile> mailboxSchedProfiles) {
		this.mailboxSchedProfiles = mailboxSchedProfiles;
	}

	public MailboxSchedProfile addMailboxSchedProfile(MailboxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().add(mailboxSchedProfile);
		mailboxSchedProfile.setMailbox(this);

		return mailboxSchedProfile;
	}

	public MailboxSchedProfile removeMailboxSchedProfile(MailboxSchedProfile mailboxSchedProfile) {
		getMailboxSchedProfiles().remove(mailboxSchedProfile);
		mailboxSchedProfile.setMailbox(null);

		return mailboxSchedProfile;
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