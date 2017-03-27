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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.MailBoxConfigurationDAO;

/**
 * The persistent class for the MAILBOXES database table.
 * 
 *  @author OFS
 */
@Entity
@Table(name = "MAILBOX")
@NamedQueries({
	@NamedQuery(name = MailBoxConfigurationDAO.GET_MBX,
			query = "SELECT mbx FROM MailBox mbx"
					+ " inner join mbx.mailboxProcessors prcsr"
					+ " inner join prcsr.scheduleProfileProcessors schd_prof_processor"
					+ " inner join schd_prof_processor.scheduleProfilesRef profile"
					+ " where LOWER(mbx.mbxName) like :" + MailBoxConfigurationDAO.MBOX_NAME
					+ " AND mbx.mbxStatus NOT like :" + MailBoxConfigurationDAO.STATUS
					+ " AND profile.schProfName like :" + MailBoxConfigurationDAO.SCHD_PROF_NAME
					+ " order by mbx.mbxName"),
    @NamedQuery(name = MailBoxConfigurationDAO.FIND_BY_MBX_NAME_AND_TENANCY_KEY_NAME, query = "SELECT mbx FROM MailBox mbx "
			        + "WHERE mbx.mbxName =:" + MailBoxConfigurationDAO.MBOX_NAME
			        + " AND mbx.mbxStatus NOT LIKE :" + MailBoxConfigurationDAO.STATUS
			        + " AND mbx.tenancyKey =:" + MailBoxConfigurationDAO.TENANCY_KEYS
			        + " AND mbx.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")"),
	@NamedQuery(name = "MailBox.findAll", query = "SELECT m FROM MailBox m"),
	@NamedQuery(name = MailBoxConfigurationDAO.GET_MBX_BY_NAME,
	        query = "SELECT mbx FROM MailBox mbx"
	                + " WHERE mbx.mbxName =:" +  MailBoxConfigurationDAO.MBOX_NAME
	                + " AND mbx.mbxStatus NOT LIKE :" + MailBoxConfigurationDAO.STATUS
	                + " AND mbx.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")"),
	@NamedQuery(name = MailBoxConfigurationDAO.GET_CLUSTER_TYPE_BY_MAILBOX_GUID, 
			query = "SELECT mbx.clusterType FROM MailBox mbx"
					+ " WHERE mbx.pguid =:" +  MailBoxConfigurationDAO.PGUID)
})

public class MailBox implements Identifiable {

	private static final long serialVersionUID = 1L;

	private String pguid;
	private String mbxDesc;
	private String mbxName;
	private String mbxStatus;
	private String shardKey;
	private Set<MailBoxProperty> mailboxProperties;
	private Set<Processor> mailboxProcessors;
	private Set<MailboxServiceInstance> mailboxServiceInstances;
	private String tenancyKey;
	private String modifiedBy;
	private Date modifiedDate;
    private String originatingDc;
    private String clusterType;

	public MailBox() {
	}
	
	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "DESCRIPTION", length = 512)
	public String getMbxDesc() {
		return this.mbxDesc;
	}

	public void setMbxDesc(String mbxDesc) {
		this.mbxDesc = mbxDesc;
	}

	@Column(name = "NAME", nullable = false, length = 128)
	public String getMbxName() {
		return this.mbxName;
	}

	public void setMbxName(String mbxName) {
		this.mbxName = mbxName;
	}

	@Column(name = "STATUS", nullable = false, length = 16)
	public String getMbxStatus() {
		return this.mbxStatus;
	}

	public void setMbxStatus(String mbxStatus) {
		this.mbxStatus = mbxStatus;
	}

	@Column(name = "SHARD_KEY", length = 512)
	public String getShardKey() {
		return this.shardKey;
	}

	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
	}

	// bi-directional many-to-one association to MailBoxProperty
	@OneToMany(mappedBy = "mailbox", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	@Fetch(FetchMode.JOIN)
	public Set<MailBoxProperty> getMailboxProperties() {
	    if (this.mailboxProperties == null) {
            this.mailboxProperties = new HashSet<>();
        }
		return this.mailboxProperties;
	}

	public void setMailboxProperties(Set<MailBoxProperty> mailboxProperties) {
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

	// bi-directional many-to-one association to MailBoxProcessor
	@OneToMany(mappedBy = "mailbox", orphanRemoval = true, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE, CascadeType.REFRESH })
	public Set<Processor> getMailboxProcessors() {
		return this.mailboxProcessors;
	}

	public void setMailboxProcessors(Set<Processor> mailboxProcessors) {
		this.mailboxProcessors = mailboxProcessors;
	}
	
	@OneToMany(mappedBy = "mailbox", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	public Set<MailboxServiceInstance> getMailboxServiceInstances() {
		return mailboxServiceInstances;
	}

	public void setMailboxServiceInstances(Set<MailboxServiceInstance> mailboxServiceInstances) {
		this.mailboxServiceInstances = mailboxServiceInstances;
	}
	
	@Column(name = "TENANCY_KEY", nullable = false, length = 128)
	public String getTenancyKey() {
		return tenancyKey;
	}

	public void setTenancyKey(String tenancyKey) {
		this.tenancyKey = tenancyKey;
	}
	
	@Column(name = "MODIFIED_BY", length = 128)
	public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED_DATE")
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
	@Column(name = "ORIGINATING_DC", length = 16)
	public String getOriginatingDc() {
		return originatingDc;
	}

	public void setOriginatingDc(String originatingDc) {
		this.originatingDc = originatingDc;
	}
	
    @Column(name = "CLUSTER_TYPE", nullable = false, length = 32)
    public String getClusterType() {
        return clusterType;
    }
    
    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    /**
	 * Method to retrieve the given properties form Mailbox
	 * 
	 * @param propertiesToBeRetrieved - list of property Names to be retrieved
	 * 		possible propertyNames are 'timetopickupfilepostedtomailbox', 'timetopickupfilepostedbymailbox'
	 * 		'emailnotificationids', 'ttl' and 'ttlunit'
	 * @return a Map containing values of given properties having the property Names as keys
	 */
	@Transient
	public Map<String, String> retrieveMailboxProperties(List<String> propertiesToBeRetrieved) {
		
		Set<MailBoxProperty> properties = getMailboxProperties();
		Map <String, String> MailboxProps = new HashMap<String, String>();
		if (null != properties) {
			
			for (MailBoxProperty property : properties) {
				
				String propertyName = property.getMbxPropName();
				if ( propertiesToBeRetrieved.contains(propertyName)) {
					MailboxProps.put(propertyName, property.getMbxPropValue());
				}
			}
		}
		return MailboxProps;
	}
	
	@Transient
	public List<String> getEmailAddress() {

		Set<MailBoxProperty> properties = this.getMailboxProperties();

		if (null != properties) {

			for (MailBoxProperty property : properties) {

				if (MailBoxConstants.MBX_RCVR_PROPERTY.equals(property.getMbxPropName())) {
					String address = property.getMbxPropValue();
					return Arrays.asList(address.split(","));
				}
			}
		}

		return null;

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

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((pguid == null) ? 0 : pguid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MailBox)) {
			return false;
		}
		MailBox other = (MailBox) obj;
		if (pguid == null) {
			if (other.pguid != null) {
				return false;
			}
		} else if (!pguid.equals(other.pguid)) {
			return false;
		}
		return true;
	}

}