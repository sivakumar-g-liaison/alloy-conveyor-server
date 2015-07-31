/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.DropBoxProcessor;
import com.liaison.mailbox.dtdm.model.FileWriter;
import com.liaison.mailbox.dtdm.model.HTTPAsyncProcessor;
import com.liaison.mailbox.dtdm.model.HTTPSyncProcessor;
import com.liaison.mailbox.dtdm.model.MailBox;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.RemoteDownloader;
import com.liaison.mailbox.dtdm.model.RemoteUploader;
import com.liaison.mailbox.dtdm.model.ScheduleProfilesRef;
import com.liaison.mailbox.dtdm.model.Sweeper;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.QueryBuilderUtil;

/**
 * @author OFS
 *
 */
public class ProcessorConfigurationDAOBase extends GenericDAOBase<Processor> implements ProcessorConfigurationDAO, MailboxDTDMDAO {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessorConfigurationDAOBase.class);

	public ProcessorConfigurationDAOBase() {
		super(PERSISTENCE_UNIT_NAME);
	}

	/**
	 * Fetches all the Processor from PROCESSOR database table by profileName and mailbox name pattern.
	 *
	 * @param profileName
	 *            The profile name.
	 * @param mbxNamePattern
	 *            The MailBox name pattern to exclude
	 * @return The list of processors.
	 */
	@Override
	public List<Processor> findByProfileAndMbxNamePattern(String profileName, String mbxNamePattern, String shardKey) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {

			List<?> proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_PROFILE_AND_MBX_NAME_PATTERN)
					.setParameter(PROF_NAME, profileName)
					.setParameter(STATUS, EntityStatus.ACTIVE.value())
					.setParameter(MBX_NAME, (MailBoxUtil.isEmpty(mbxNamePattern) ? "''" : mbxNamePattern + "%"))
					.setParameter(SHARD_KEY, (MailBoxUtil.isEmpty(shardKey) ? "%%" : shardKey))
					.getResultList();

			List<Processor> processors = new ArrayList<Processor>();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus());
			}

			return processors;
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

	}

	/**
     * Checks the mailbox has the processor or not.
     *
     *
     * @param siid service instance id(name)
     * @param mbxGuid pguid of the mailbox
     * @return boolean
     */
    @Override
    public boolean isMailboxHasProcessor( String mbxGuid, String siid) {

        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        boolean status = false;

        try {

            LOG.info("Fetching the processor count starts.");
            long lStartTime = new Date().getTime(); // start time
            LOG.info("Start Time of Query Execution : " + lStartTime);
            StringBuilder query = new StringBuilder().append("select count(processor) from Processor processor")
                    .append(" inner join processor.mailbox mbx")
                    .append(" where mbx.pguid = :" + PGUID)
                    .append(" and processor.serviceInstance.name like :" + SERV_INST_ID);

            long count = (long) entityManager.createQuery(query.toString())
                    .setParameter(PGUID , mbxGuid)
                    .setParameter(SERV_INST_ID, siid)
                    .getSingleResult();
            long lEndTime = new Date().getTime(); // end time
            LOG.info("End Time of Query Execution : " + lEndTime);
            if (count > 0) {
                status = true;
            }

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }

        LOG.info("Fetching the processor count ends.");
        return status;
    }

	/**
	 * Retrieves the list of processor from the given mailbox guid and service instance guid(name).
	 *
	 * @param mbxGuid pguid of the mailbox
	 * @param siGuid service instance id(name)
	 * @return list of processor
	 */
	@Override
	public List<Processor> findProcessorByMbxAndServiceInstance(String mbxGuid, String siGuid) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("find processor by mbx and service instacne starts.");

			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
					.append(" inner join processor.mailbox mbx")
					.append(" where mbx.pguid = :" + PGUID)
					.append(" and processor.pguid in (select prcsr.pguid from Processor prcsr")
					.append(" inner join prcsr.serviceInstance si")
					.append(" where si.name like :" + SERV_INST_ID + ")");

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(PGUID, mbxGuid)
					.setParameter(SERV_INST_ID, siGuid)
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		LOG.info("find processor by mbx and service instacne ends.");
		return processors;
	}

	/**
	 * Retrieves list of processor from the given mailbox guid
	 *
	 * @param mbxGuid the mailbox guid
	 * @param activeEntityRequired if true active processors linked with active mailbox is only retrieved. 
	 * @return list of processor
	 */
	@Override
	public List<Processor> findProcessorByMbx(String mbxGuid, boolean activeEntityRequired) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("Fetching the processor count starts.");

			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
					.append(" inner join processor.mailbox mbx")
					.append(" where mbx.pguid = :" + PGUID);
			if(activeEntityRequired) {
				query.append(" and mbx.mbxStatus = :" + STATUS)
					 .append(" and processor.procsrStatus = :" + STATUS);
			}
			
			Query processorQuery = entityManager.createQuery(query.toString())
												.setParameter(PGUID, mbxGuid);
			if(activeEntityRequired) {
				processorQuery.setParameter(STATUS, EntityStatus.ACTIVE.value());
			}		
			
			List<?> proc = processorQuery.getResultList();
			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}, Type : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus(), processor.getProcessorType());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return processors;
	}
	
	/**
	 * Retrieves processors from the given mailbox guid and processor name
	 *
	 * @param mbxGuid the mailbox guid
	 * @param procName the processor name
	 * @return processor
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Processor findProcessorByNameAndMbx(String mbxGuid, String procName) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Processor processor = null;

		try {

			LOG.info("find processor by mbx and processor name starts.");
			
			List<Processor> proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_NAME_AND_MBX)
					.setParameter(PGUID,  (MailBoxUtil.isEmpty(mbxGuid) ? "''" : mbxGuid))
					.setParameter(PRCSR_NAME, (MailBoxUtil.isEmpty(procName) ? "''" : procName))
					.getResultList();
			
			if ((proc != null) && (proc.size() > 0)) {
                processor =  proc.get(0);
            }
			
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		LOG.info("find processor by mbx and processor name ends.");
		return processor;
	}
	
	@Override
	public List<Processor> findProcessorsByType(List<String> specificProcessorTypes) {
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("Fetching the processor starts.");
			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
						.append(" inner join processor.mailbox mbx")
						.append(" where processor.procsrStatus = :" + STATUS)
						.append(" and mbx.mbxStatus = :" + STATUS)
						.append(" and ( " + QueryBuilderUtil.constructSqlStringForTypeOperator(specificProcessorTypes) + ")");

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(STATUS, EntityStatus.ACTIVE.name())
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}, Type : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus(), processor.getProcessorType());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return processors;
	}
	
	@Override
	public List<Processor> findSpecificProcessorTypesOfMbx(String mbxGuid, List<String>specificProcessorTypes) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("Fetching the processor starts.");
			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
						.append(" inner join processor.mailbox mbx")
						.append(" where mbx.pguid = :" + PGUID)
						.append(" and mbx.mbxStatus = :" + STATUS)
						.append(" and processor.procsrStatus = :" + STATUS)
						.append(" and ( " + QueryBuilderUtil.constructSqlStringForTypeOperator(specificProcessorTypes) + ")");

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(PGUID, mbxGuid)
					.setParameter(STATUS, EntityStatus.ACTIVE.name())
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}, Type : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus(), processor.getProcessorType());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return processors;
	}

	public List<Processor> findProcessorsOfSpecificTypeByProfileAndTenancyKey(String profileId, String tenancyKey, List<String> specificProcessorTypes) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {
			LOG.info("Fetching the processor by specific type, profile Id and tenancyKey starts.");

			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
						.append(" inner join processor.scheduleProfileProcessors schd_prof_processor")
						.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
						.append(" where profile.pguid = :" + ProcessorConfigurationDAO.PROFILE_ID)
						.append(" and processor.mailbox.tenancyKey = :" + ProcessorConfigurationDAO.TENANCY_KEY)
						.append(" and processor.mailbox.mbxStatus = :" + ProcessorConfigurationDAO.STATUS)
						.append(" and processor.procsrStatus = :" + ProcessorConfigurationDAO.STATUS)
						.append(" and ( " + QueryBuilderUtil.constructSqlStringForTypeOperator(specificProcessorTypes) + ")");

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(ProcessorConfigurationDAO.PROFILE_ID, profileId)
					.setParameter(ProcessorConfigurationDAO.TENANCY_KEY, tenancyKey)
					.setParameter(STATUS, EntityStatus.ACTIVE.name())
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}, Type : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus(), processor.getProcessorType());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return processors;
	}

	private Class<?> getProcessorClass(ProcessorType processorType) {

		Class <?> processorClass = null;
		switch(processorType.getCode().toLowerCase()) {

		case "httpsyncprocessor":
			processorClass = HTTPSyncProcessor.class;
			break;
		case "httpasyncprocessor":
			processorClass = HTTPAsyncProcessor.class;
			break;
		case  "sweeper":
			processorClass = Sweeper.class;
			break;
		case "remoteuploader":
			processorClass = RemoteUploader.class;
			break;
		case "filewriter":
			processorClass = FileWriter.class;
			break;
		}
		return processorClass;
	}

	public List<Processor> findProcessorByType(ProcessorType type) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("Fetching the processor count starts.");

			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
					.append(" inner join processor.mailbox mbx")
					.append(" where TYPE(processor) = :" + PROCESSOR_TYPE)
					.append(" and processor.procsrStatus = :" + STATUS)
					.append(" and mbx.mbxStatus = :" + STATUS);
			Class <?> processorType = getProcessorClass(type);

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(PROCESSOR_TYPE, processorType)
					.setParameter(STATUS, EntityStatus.ACTIVE.value())
					.getResultList();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return processors;
	}

	public List<Processor> findAllActiveProcessors() {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			List <?> proc = entityManager.createNamedQuery(FIND_ALL_ACTIVE_PROCESSORS)
								.setParameter(STATUS, EntityStatus.ACTIVE.value())
								.getResultList();
			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return processors;
	}
	
	private Class<?> getProcessorClass(String processorCode) {

		Class <?> processorClass = null;
		switch(processorCode.toLowerCase()) {

    		case "httpsyncprocessor":
    			processorClass = HTTPSyncProcessor.class;
    			break;
    		case "httpasyncprocessor":
    			processorClass = HTTPAsyncProcessor.class;
    			break;
    		case  "sweeper":
    			processorClass = Sweeper.class;
    			break;
    		case "remoteuploader":
    			processorClass = RemoteUploader.class;
    			break;
    		case "filewriter":
    			processorClass = FileWriter.class;
    			break;
    		case "dropboxprocessor":
    			processorClass = DropBoxProcessor.class;
    			break;
    		case "remotedownloader":
    			processorClass = RemoteDownloader.class;
    			break;
		}
		return processorClass;
	}

	
	public List<Processor> getAllProcessors(GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
        List<Processor> processors = new ArrayList<Processor>();
        Processor processor = null;

		try {

			StringBuilder query = new StringBuilder().append("select processor from Processor processor");
			genearteQueryBySearchFilters(searchFilter, query);
			addSortDirections(searchFilter, query);

			Query processorSearchQuery = entityManager.createQuery(query.toString());
			processorSearchQuery = setParamsForProcessorSearchQuery(searchFilter, processorSearchQuery);

            List<?> proc = processorSearchQuery.setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
                    .setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
                    .getResultList();
			Iterator<?> iter = proc.iterator();
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
				LOG.info("Processor Configuration -Pguid : {}, JavaScriptUri : {}, Desc: {}, Properties : {}, Status : {}",
						processor.getPrimaryKey(), processor.getJavaScriptUri(), processor.getProcsrDesc(),
						processor.getProcsrProperties(), processor.getProcsrStatus());
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return processors;		
	}

    private void addSortDirections(GenericSearchFilterDTO searchFilter, StringBuilder query) {

        String sortDirection = searchFilter.getSortDirection();
        String sortField = searchFilter.getSortField();
        if(!(StringUtil.isNullOrEmptyAfterTrim(sortField)
                && StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {

        	sortDirection=sortDirection.toUpperCase();
        	switch (sortField.toLowerCase()) {
        		case "mailboxname":
        			query.append(" order by processor.mailbox.mbxName " + sortDirection);
        		break;
        		case "name":
        			query.append(" order by processor.procsrName " + sortDirection);
        			break;					
        		case "protocol":
        			query.append(" order by processor.procsrProtocol " + sortDirection);
        			break;
        		case "status":
        			query.append(" order by processor.procsrStatus " + sortDirection);
        			break;
        	}
        } else {
            query.append(" order by processor.procsrName");
        }
    }

	@Override
	public int getAllProcessorsCount() {
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Long totalItems = null;
		int count = 0;

		try {

			StringBuilder query = new StringBuilder().append("select count(processor) from Processor processor");

			totalItems = (Long)entityManager.createQuery(query.toString()).getSingleResult();
			count = totalItems.intValue();
		} finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
		return count;	
	}

	@SuppressWarnings("unchecked")
	@Override
	public Processor findActiveProcessorById(String id) {
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);

		try {
			List <Processor> proc = entityManager.createNamedQuery(FIND_ACTIVE_PROCESSOR_BY_ID)
								.setParameter(STATUS, EntityStatus.ACTIVE.value())
								.setParameter(PGUID, id)
								.getResultList();
			return (proc.isEmpty()) ? null : proc.get(0);
			
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}
	
	@Override
	public int getFilteredProcessorsCount(GenericSearchFilterDTO searchDTO) {

		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		Long totalItems = null;
		int count = 0;

		try {

			LOG.info("Fetching the processors by filters starts.");

			StringBuilder query = new StringBuilder().append("select count(processor) from Processor processor");
			genearteQueryBySearchFilters(searchDTO, query);
			Query processorSearchQuery = entityManager.createQuery(query.toString());

			processorSearchQuery = setParamsForProcessorSearchQuery(searchDTO, processorSearchQuery);

			totalItems = (Long) processorSearchQuery.getSingleResult();

			count = totalItems.intValue();
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return count;
	}
	
	@Override
	public List<MailBox> getMailboxNames(GenericSearchFilterDTO searchDTO) {
			 
	        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
	        List<MailBox> mailboxNames = new ArrayList<MailBox>();

	        try {

	            StringBuilder query = new StringBuilder().append("SELECT mbx FROM MailBox mbx")
	                    .append(" where LOWER(mbx.mbxName) like :" + MBX_NAME);
	            List <?> proc = entityManager.createQuery(query.toString())
	                    .setParameter(MBX_NAME, "%" + searchDTO.getMbxName().toLowerCase() + "%")
	                    .getResultList();	

	            Iterator<?> iter = proc.iterator();
	            MailBox mailbox;
	 			while (iter.hasNext()) {

	 				mailbox = (MailBox) iter.next();
	 				mailboxNames.add(mailbox);	 				
	 			}
	 
	        } finally {
	            if (entityManager != null) {
	                entityManager.close();
	            }
	        }
		return mailboxNames;
	}
	
	@Override
	public List<ScheduleProfilesRef> getProfileNames(GenericSearchFilterDTO searchDTO) {
			 
	        EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
	        List<ScheduleProfilesRef> profileNames = new ArrayList<ScheduleProfilesRef>();
	 
	        try {
	 
	            StringBuilder query = new StringBuilder().append("SELECT s FROM ScheduleProfilesRef s")
	                    .append(" where LOWER(s.schProfName) like :" + PROF_NAME);
	            List <?> proc = entityManager.createQuery(query.toString())
	                    .setParameter(PROF_NAME, "%" + searchDTO.getProfileName().toLowerCase() + "%")
	                    .getResultList();	
	             
	            Iterator<?> iter = proc.iterator();
	            ScheduleProfilesRef profile;
	 			while (iter.hasNext()) {

	 				profile = (ScheduleProfilesRef) iter.next();
	 				profileNames.add(profile);	 				
	 			}
	 
	        } finally {
	            if (entityManager != null) {
	                entityManager.close();
	            }
	        }
		return profileNames;
	}

	public void genearteQueryBySearchFilters(GenericSearchFilterDTO searchDTO, StringBuilder query) {
		
		List<String> predicateList = new ArrayList<String>();
		boolean isFolderAvailable = false;
		
		if (!MailBoxUtil.isEmpty(searchDTO.getMbxName())) {			
			predicateList.add(" processor.mailbox.mbxName like :" + MBX_NAME);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getFolderPath())) {
			query.append(" inner join processor.folders folder ");
			predicateList.add(" folder.fldrUri like :" + FOLDER_URI);
			isFolderAvailable = true;
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getPipelineId())) {
			predicateList.add(" processor.procsrProperties like :" + PIPELINE_ID);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProfileName())) {
            String profileAppender = isFolderAvailable ? " inner join folder.processor folderProcessor inner join folderProcessor.scheduleProfileProcessors schd_prof_processor"
                    : " inner join processor.scheduleProfileProcessors schd_prof_processor";
            query.append(profileAppender).append(" inner join schd_prof_processor.scheduleProfilesRef profile");
            predicateList.add("LOWER(profile.schProfName) like :" + PROF_NAME);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProtocol())) {
			predicateList.add(" LOWER(processor.procsrProtocol) = :" + PROTOCOL);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProcessorType())) {
			predicateList.add(" TYPE(processor) = :" + PROCESSOR_TYPE);
		}

		for (int i = 0; i < predicateList.size(); i++) {			
            if (i == 0) {
				query.append(" WHERE ");				
				query.append(predicateList.get(i));
			} else {
				query.append(" AND ");
				query.append(predicateList.get(i));
			}
		}				
	}

	public Query setParamsForProcessorSearchQuery(GenericSearchFilterDTO searchDTO, Query query) {
		
        if (!MailBoxUtil.isEmpty(searchDTO.getProcessorType())) {
            query.setParameter(PROCESSOR_TYPE, getProcessorClass(searchDTO.getProcessorType()));
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getMbxName())) {

            query.setParameter(MBX_NAME, "%" + searchDTO.getMbxName() + "%");
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getFolderPath())) {

            query.setParameter(FOLDER_URI, "%" + searchDTO.getFolderPath() + "%");
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getPipelineId())) {
            query.setParameter(PIPELINE_ID, "%" + searchDTO.getPipelineId() + "%");
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getProfileName())) {

            query.setParameter(PROF_NAME, "%" + searchDTO.getProfileName().toLowerCase() + "%");
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getProtocol())) {
            query.setParameter(PROTOCOL, searchDTO.getProtocol().toLowerCase());
        }	
		return query;		
	}
}
