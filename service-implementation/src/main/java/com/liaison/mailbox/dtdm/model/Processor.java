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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.IndexColumn;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAO;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * The persistent class for the PROCESSORS database table.
 *
 *  @author OFS
 */
@Entity
@Table(name = "PROCESSOR")
@NamedQueries({
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_ALL_ACTIVE_PROCESSORS,
                query = "SELECT processor FROM Processor processor"
                        + " WHERE processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS
                        + " AND processor.clusterType = :" + MailBoxConstants.CLUSTER_TYPE),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSORS_BY_DATACENTER,
                query = "SELECT processor FROM Processor processor"
                        + " WHERE processor.processDc = :" + ProcessorConfigurationDAO.DATACENTER_NAME
                        + " AND processor.procsrStatus <> 'DELETED' "
                        + " AND processor.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")"),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_SWEEPER_PROCESSORS_FOR_FILE_INGRESS,
                query = "SELECT processor FROM Processor processor"
                        + " WHERE processor.class IN (:" + ProcessorConfigurationDAO.PROCESSOR_TYPE + ")"
                        + " AND processor.procsrStatus <> 'DELETED' "
                        + " AND processor.clusterType = :" + MailBoxConstants.CLUSTER_TYPE), 
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_NAME_AND_MBX,
                query = "SELECT processor FROM Processor processor"
                        + " INNER JOIN processor.mailbox mbx" + " WHERE mbx.pguid = :"
                        + ProcessorConfigurationDAO.PGUID
                        + " AND processor.procsrName LIKE :"
                        + ProcessorConfigurationDAO.PRCSR_NAME
                        + " AND processor.procsrStatus <> :"
                        + ProcessorConfigurationDAO.STATUS_DELETE
                        + " AND processor.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")"),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_ACTIVE_PROCESSOR_BY_ID,
                query = "SELECT processor FROM Processor processor"
                        + " INNER JOIN processor.mailbox mbx"
                        + " WHERE processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS
                        + " AND processor.pguid = :" + ProcessorConfigurationDAO.PGUID
                        + " AND mbx.mbxStatus = :" + ProcessorConfigurationDAO.STATUS
                        + " AND mbx.mbxStatus = :" + ProcessorConfigurationDAO.STATUS
                        + " AND processor.clusterType = :" + MailBoxConstants.CLUSTER_TYPE),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_NAME,
                query = "SELECT processor FROM Processor processor"
                        + " WHERE processor.procsrName = :" + ProcessorConfigurationDAO.PRCSR_NAME
                        + " AND processor.procsrStatus <> :" + ProcessorConfigurationDAO.STATUS_DELETE
                        + " AND processor.clusterType IN (:" + MailBoxConstants.CLUSTER_TYPE + ")"),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_PROFILE_AND_TENANCY,
                query = "SELECT processor FROM Processor processor" +
                        " INNER JOIN processor.scheduleProfileProcessors schd_prof_processor" +
                        " INNER JOIN schd_prof_processor.scheduleProfilesRef profile" +
                        " WHERE profile.pguid = :" + ProcessorConfigurationDAO.PROFILE_ID +
                        " AND processor.mailbox.tenancyKey = :" + ProcessorConfigurationDAO.TENANCY_KEY +
                        " AND processor.mailbox.mbxStatus = :" + ProcessorConfigurationDAO.STATUS +
                        " AND processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS +
                        " AND processor.class = :" + ProcessorConfigurationDAO.PROCESSOR_TYPE +
                        " AND processor.clusterType = :" + MailBoxConstants.CLUSTER_TYPE),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSORS_BY_TYPE_AND_MBX_STATUS,
                query = "SELECT processor FROM Processor processor" +
                        " INNER JOIN processor.mailbox mbx" +
                        " WHERE mbx.mbxStatus = :" + ProcessorConfigurationDAO.STATUS +
                        " AND processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS +
                        " AND processor.class in (:" + ProcessorConfigurationDAO.PROCESSOR_TYPE + ")" +
                        " AND processor.clusterType = :" + MailBoxConstants.CLUSTER_TYPE),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSORS_BY_TYPE_AND_STATUS,
                query = "SELECT processor FROM Processor processor" +
                        " INNER JOIN processor.mailbox mbx" +
                        " WHERE mbx.pguid = :" + ProcessorConfigurationDAO.PGUID +
                        " AND mbx.mbxStatus = :" + ProcessorConfigurationDAO.STATUS +
                        " AND processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS +
                        " AND processor.class IN (:" + ProcessorConfigurationDAO.PROCESSOR_TYPE + ")" +
                        " AND processor.clusterType = :" + MailBoxConstants.CLUSTER_TYPE),
        @NamedQuery(name = ProcessorConfigurationDAO.GET_CLUSTER_TYPE_BY_PROCESSOR_GUID,
                query = "SELECT processor.clusterType FROM Processor processor" +
                        " WHERE processor.pguid =:" + ProcessorConfigurationDAO.PGUID),
        @NamedQuery(name = ProcessorConfigurationDAO.FIND_PROCESSOR_BY_TYPE_AND_FOLDER_URI,
                query = "SELECT processor FROM Processor processor" +
                        " INNER JOIN processor.folders folder" +
                        " WHERE processor.class IN (:" + ProcessorConfigurationDAO.PROCESSOR_TYPE + ")" +
                        " AND folder.fldrUri LIKE :" + ProcessorConfigurationDAO.FOLDER_URI +
                        " AND processor.procsrStatus <> :" + ProcessorConfigurationDAO.STATUS_DELETE)
})
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING, length = 128)
public class Processor implements Identifiable {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_REMOTEDOWNLOADER = "REMOTEDOWNLOADER";
	public static final String TYPE_REMOTEUPLOADER = "REMOTEUPLOADER";
	public static final String TYPE_SWEEPER = "SWEEPER";
	public static final String TYPE_CONDITIONAL_SWEEPER = "CONDITIONALSWEEPER";
	public static final String HTTP_ASYNC = "HTTPASYNCPROCESSOR";
	public static final String HTTP_SYNC = "HTTPSYNCPROCESSOR";
	public static final String LITE_HTTP_SYNC = "LITEHTTPSYNCPROCESSOR";
	public static final String TYPE_FILE_WRITER = "FILEWRITER";
	public static final String TYPE_DROPBOX_PROCESSOR = "DROPBOXPROCESSOR";

	private String pguid;
	private String javaScriptUri;
	private String procsrDesc;
	private String procsrProperties;
	private String procsrStatus;
	private String procsrName;
	private String procsrProtocol;
	private MailBox mailbox;
	private ServiceInstance serviceInstance;
	private String modifiedBy;
	private Date modifiedDate;
	private String originatingDc;
    private String clusterType;
    private String processDc;

	private Set<Credential> credentials;
	private Set<Folder> folders;
	private Set<ProcessorProperty> dynamicProperties;
	private Set<ScheduleProfileProcessor> scheduleProfileProcessors;

	public Processor() {
	}

	// bi-directional many-to-one association to ProcessorProperty
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	@Fetch(FetchMode.JOIN)
	@IndexColumn(name = "PGUID")
	public Set<ProcessorProperty> getDynamicProperties() {
	    if (this.dynamicProperties == null) {
            this.dynamicProperties = new HashSet<>();
        }
		return dynamicProperties;
	}

	public void setDynamicProperties(Set<ProcessorProperty> processorProperties) {
		this.dynamicProperties = processorProperties;
	}

	@Id
	@Column(unique = true, nullable = false, length = 32)
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name = "JAVASCRIPT_URI", length = 512)
	public String getJavaScriptUri() {
		return this.javaScriptUri;
	}

	public void setJavaScriptUri(String javaScriptUri) {
		this.javaScriptUri = javaScriptUri;
	}

	@Column(name = "DESCRIPTION", length = 512)
	public String getProcsrDesc() {
		return this.procsrDesc;
	}

	public void setProcsrDesc(String procsrDesc) {
		this.procsrDesc = procsrDesc;
	}

	@Column(name = "PROPERTIES", length = 2048)
	public String getProcsrProperties() {
		return this.procsrProperties;
	}

	public void setProcsrProperties(String procsrProperties) {
		this.procsrProperties = procsrProperties;
	}

	@Column(name = "STATUS", nullable = false, length = 128)
	public String getProcsrStatus() {
		return this.procsrStatus;
	}

	public void setProcsrStatus(String procsrStatus) {
		this.procsrStatus = procsrStatus;
	}

	@Column(name = "NAME", nullable = false, length = 512)
	public String getProcsrName() {
		return procsrName;
	}

	public void setProcsrName(String procsrName) {
		this.procsrName = procsrName;
	}

	@Column(name = "PROTOCOL", nullable = false, length = 128)
	public String getProcsrProtocol() {
		return procsrProtocol;
	}

	public void setProcsrProtocol(String procsrProtocol) {
		this.procsrProtocol = procsrProtocol;
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

    // bi-directional many-to-one association to Credential
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE,
			CascadeType.REFRESH })
	@Fetch(FetchMode.JOIN)
	public Set<Credential> getCredentials() {
	    if (this.credentials == null) {
            this.credentials = new HashSet<>();
        }
		return this.credentials;
	}

	public void setCredentials(Set<Credential> credentials) {
		this.credentials = credentials;
	}

	public Credential addCredential(Credential credential) {
		getCredentials().add(credential);
		credential.setProcessor(this);

		return credential;
	}

	public Credential removeCredential(Credential credential) {
		getCredentials().remove(credential);
		credential.setProcessor(null);

		return credential;
	}

	// bi-directional many-to-one association to Folder
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE,
			CascadeType.REFRESH })
	@Fetch(FetchMode.JOIN)
	public Set<Folder> getFolders() {
	    if (this.folders == null) {
	        this.folders = new HashSet<>();
	    }
		return this.folders;
	}

	public void setFolders(Set<Folder> folders) {
		this.folders = folders;
	}

	public Folder addFolder(Folder folder) {
		getFolders().add(folder);
		folder.setProcessor(this);

		return folder;
	}

	public Folder removeFolder(Folder folder) {
		getFolders().remove(folder);
		folder.setProcessor(null);

		return folder;
	}

	// bi-directional many-to-one association to MailBox
	@ManyToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "MAILBOX_GUID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public MailBox getMailbox() {
		return this.mailbox;
	}

	public void setMailbox(MailBox mailbox) {
		this.mailbox = mailbox;
	}

	// bi-directional many-to-one association to Service instance id
	@ManyToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "SERVICE_INSTANCE_GUID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

	// bi-directional many-to-one association to ScheduleProfileProcessor
	@OneToMany(mappedBy = "processor", fetch = FetchType.EAGER, orphanRemoval = true, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
	@Fetch(FetchMode.SELECT)
	public Set<ScheduleProfileProcessor> getScheduleProfileProcessors() {
	    if (this.scheduleProfileProcessors == null) {
            this.scheduleProfileProcessors = new HashSet<>();
        }
		return this.scheduleProfileProcessors;
	}

	public void setScheduleProfileProcessors(Set<ScheduleProfileProcessor> scheduleProfileProcessors) {
		this.scheduleProfileProcessors = scheduleProfileProcessors;
	}

	public ScheduleProfileProcessor addScheduleProfileProcessor(ScheduleProfileProcessor scheduleProfileProcessor) {
		getScheduleProfileProcessors().add(scheduleProfileProcessor);
		scheduleProfileProcessor.setProcessor(this);

		return scheduleProfileProcessor;
	}

	public ScheduleProfileProcessor removeScheduleProfileProcessor(ScheduleProfileProcessor scheduleProfileProcessor) {
		getScheduleProfileProcessors().remove(scheduleProfileProcessor);
		scheduleProfileProcessor.setProcessor(null);

		return scheduleProfileProcessor;
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
    
    @Column(name = "PROCESS_DC", length = 16)
    public String getProcessDc() {
        return processDc;
    }
	
    public void setProcessDc(String processDc) {
        this.processDc = processDc;
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


	/**
	 * Method returns the processor type from the discriminator value.
	 *
	 * @return The Processor type
	 */
	@Transient
	public ProcessorType getProcessorType() {

		DiscriminatorValue val = this.getClass().getAnnotation(DiscriminatorValue.class);
		String code = val.value();
		return ProcessorType.findByCode(code);
	}

	/**
	 * Factory method returns Processor instance corresponding to the input value.
	 *
	 * @param processorType
	 *            enumeration indicating the type of processor type.
	 * @return a new instance of Processor of the give type.
	 */
	@Transient
    public static Processor processorInstanceFactory(ProcessorType processorType) {

        Processor processor = null;
        switch (processorType) {
            case REMOTEDOWNLOADER:
                processor = new RemoteDownloader();
                break;
            case REMOTEUPLOADER:
                processor = new RemoteUploader();
                break;
            case HTTPASYNCPROCESSOR:
                processor = new HTTPAsyncProcessor();
                break;
			case HTTPSYNCPROCESSOR:
				processor = new HTTPSyncProcessor();
				break;
			case LITEHTTPSYNCPROCESSOR:
				processor = new LiteHTTPSyncProcessor();
				break;
            case FILEWRITER:
                processor = new FileWriter();
                break;
            case DROPBOXPROCESSOR:
                processor = new DropBoxProcessor();
                break;
            case CONDITIONALSWEEPER:
                processor = new ConditionalSweeper();
                break;
            default:
                processor = new Sweeper();
                break;
        }

        return processor;
    }

	/**
	 * Gets the configured email receivers from the mailbox for the processor.
	 *
	 * @return List of receivers
	 */
	@Transient
	public List<String> getEmailAddress() {
		return getMailbox().getEmailAddress();
	}
	
	/**
	 * Get the configured TTL unit and the value from Mailbox Properties
	 * 
	 * @return Map which contains TTL unit and value
	 */
	@Transient
	public Map<String,String> getTTLUnitAndTTLNumber() {

		Map<String,String> map = new HashMap<String,String>();
		Set<MailBoxProperty> properties = getMailbox().getMailboxProperties();
		for (MailBoxProperty mbp : properties) {
			if (mbp.getMbxPropName().equals(MailBoxConstants.TTL) && !MailBoxUtil.isEmpty(mbp.getMbxPropValue())) {
				map.put(MailBoxConstants.TTL_NUMBER, mbp.getMbxPropValue());
			}
			if (mbp.getMbxPropName().equals(MailBoxConstants.TTL_UNIT) && !MailBoxUtil.isEmpty(mbp.getMbxPropValue())) {
				map.put(MailBoxConstants.CUSTOM_TTL_UNIT, mbp.getMbxPropValue());
			}
		}
		return map;
	}
	
    /**
     *  Get the configured TTL value from Processor Properties
     *
     *  @return TTL value
     */
    @Transient
    public int getStaleFileTTLDays() {
        return MailBoxUtil.getStaleFileTTLValue(getProcsrProperties());
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
		return getMailbox().retrieveMailboxProperties(propertiesToBeRetrieved);
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
		if (!(obj instanceof Processor)) {
			return false;
		}
		Processor other = (Processor) obj;
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