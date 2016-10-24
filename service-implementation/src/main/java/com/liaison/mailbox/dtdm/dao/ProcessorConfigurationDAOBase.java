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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.QueryBuilderUtil;

/**
 * Contains the processor fetch informations and  We can retrieve the processor details here.
 * 
 * @author OFS
 */
public class ProcessorConfigurationDAOBase extends GenericDAOBase<Processor> implements ProcessorConfigurationDAO, MailboxDTDMDAO {

	private static final Logger LOG = LogManager.getLogger(ProcessorConfigurationDAOBase.class);

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

		EntityManager entityManager = null;

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
					.append(" inner join processor.scheduleProfileProcessors schd_prof_processor")
					.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
					.append(" inner join processor.mailbox mailbox")
					.append(" where profile.schProfName like :")
					.append(PROF_NAME)
					.append(" and mailbox.mbxStatus = :")
					.append(STATUS)
					.append(" and processor.procsrStatus = :")
					.append(STATUS);

			if (!MailBoxUtil.isEmpty(mbxNamePattern)) {
				query.append(" and mailbox.mbxName not like :").append(MBX_NAME);
			}

			if (!MailBoxUtil.isEmpty(shardKey)) {
				query.append(" and mailbox.shardKey like :").append(SHARD_KEY);
			}

			Query processorQuery = entityManager.createQuery(query.toString())
					.setParameter(PROF_NAME, profileName)
					.setParameter(STATUS, EntityStatus.ACTIVE.value());

			if (!MailBoxUtil.isEmpty(mbxNamePattern)) {
				processorQuery.setParameter(MBX_NAME, mbxNamePattern);
			}

			if (!MailBoxUtil.isEmpty(shardKey)) {
				processorQuery.setParameter(SHARD_KEY, shardKey);
			}

			List<?> proc = processorQuery.getResultList();
			List<Processor> processors = new ArrayList<Processor>();

			Iterator<?> iter = proc.iterator();
			Processor processor;
			while (iter.hasNext()) {
				processor = (Processor) iter.next();
				processors.add(processor);
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
     * @param siid service instance id(name)
     * @param mbxGuid pguid of the mailbox
     * @return boolean
     */
    @Override
    public boolean isMailboxHasProcessor( String mbxGuid, String siid, boolean disableFilter) {

        EntityManager entityManager = null;
        boolean status = false;

        try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            LOG.debug("Fetching the processor count starts.");
            long lStartTime = new Date().getTime(); // start time
            LOG.debug("Start Time of Query Execution : " + lStartTime);

            StringBuilder query = new StringBuilder().append("SELECT count(*)")
                    .append(" FROM Processor processor")
                    .append(" INNER JOIN processor.mailbox mailbox")
                    .append(" INNER JOIN processor.serviceInstance si")
                    .append(" WHERE mailbox.pguid = :")
                    .append(PGUID);

            //SID_CHECK FOR PROCESSOR STATUS
            if (!disableFilter) {
                query.append(" AND si.name = :");
                query.append(SERV_INST_ID);
            }

            Query jpaQuery = entityManager.createQuery(query.toString())
                    .setParameter(PGUID , mbxGuid);
            jpaQuery =  (!disableFilter) ? jpaQuery.setParameter(SERV_INST_ID , siid) : jpaQuery ;
            long count = ((Long) jpaQuery.getSingleResult());

            long lEndTime = new Date().getTime(); // end time
            LOG.debug("End Time of Query Execution : " + lEndTime);
            if (count > 0) {
                status = true;
            }

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }

        LOG.debug("Fetching the processor count ends.");
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
	public Set<Processor> findProcessorByMbxAndServiceInstance(String mbxGuid, String siGuid) {

		EntityManager entityManager = null;
		Set<Processor> processors = new HashSet<Processor>();

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("find processor by mbx and service instance starts.");

			List<?> proc = entityManager.createQuery(PROCESSOR_RETRIEVAL_BY_MAILBOX_AND_SIID.toString())
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

		LOG.debug("find processor by mbx and service instance ends.");
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
	public Set<Processor> findProcessorByMbx(String mbxGuid, boolean activeEntityRequired) {

		EntityManager entityManager = null;
		Set<Processor> processors = new HashSet<Processor>();

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("Fetching the processor count starts.");

			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
					.append(" inner join processor.mailbox mbx")
					.append(" where mbx.pguid = :")
					.append(PGUID);
			if (activeEntityRequired) {
				query.append(" and mbx.mbxStatus = :")
						.append(STATUS)
						.append(" and processor.procsrStatus = :")
						.append(STATUS);
			}

			Query processorQuery = entityManager.createQuery(query.toString())
												.setParameter(PGUID, mbxGuid);
			if (activeEntityRequired) {
				processorQuery.setParameter(STATUS, EntityStatus.ACTIVE.value());
			}		
			
			List<?> proc = processorQuery.getResultList();
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

		EntityManager entityManager = null;
		Processor processor = null;

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("find processor by mbx and processor name starts.");
			
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

		LOG.debug("find processor by mbx and processor name ends.");
		return processor;
	}
	
	@Override
	public List<Processor> findProcessorsByType(List<String> specificProcessorTypes, String mailboxStatus) {
		
		EntityManager entityManager = null;
		List<Processor> processors = new ArrayList<Processor>();

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("Fetching the processor starts.");
			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
						.append(" inner join processor.mailbox mbx")
						.append(" where mbx.mbxStatus = :")
						.append(STATUS)
						.append(" and ( ")
						.append(QueryBuilderUtil.constructSqlStringForTypeOperator(specificProcessorTypes))
						.append(")");

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(STATUS, (MailBoxUtil.isEmpty(mailboxStatus) ? 
												EntityStatus.ACTIVE.name() :
												mailboxStatus.toUpperCase()))
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

		return processors;
	}
	
	@Override
	public List<Processor> findSpecificProcessorTypesOfMbx(String mbxGuid, List<String>specificProcessorTypes) {

		EntityManager entityManager = null;
		List<Processor> processors = new ArrayList<Processor>();

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("Fetching the processor starts.");
			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
						.append(" inner join processor.mailbox mbx")
						.append(" where mbx.pguid = :")
						.append(PGUID)
						.append(" and mbx.mbxStatus = :")
						.append(STATUS)
						.append(" and processor.procsrStatus = :")
						.append(STATUS)
						.append(" and ( ")
						.append(QueryBuilderUtil.constructSqlStringForTypeOperator(specificProcessorTypes))
						.append(")");

			List<?> proc = entityManager.createQuery(query.toString())
					.setParameter(PGUID, mbxGuid)
					.setParameter(STATUS, EntityStatus.ACTIVE.name())
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

		return processors;
	}

	public List<Processor> findProcessorsOfSpecificTypeByProfileAndTenancyKey(String profileId, String tenancyKey, List<String> specificProcessorTypes) {

		EntityManager entityManager = null;
		List<Processor> processors = new ArrayList<Processor>();

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("Fetching the processor by specific type, profile Id and tenancyKey starts.");

			StringBuilder query = new StringBuilder().append("select processor from Processor processor")
						.append(" inner join processor.scheduleProfileProcessors schd_prof_processor")
						.append(" inner join schd_prof_processor.scheduleProfilesRef profile")
						.append(" where profile.pguid = :")
						.append(ProcessorConfigurationDAO.PROFILE_ID)
						.append(" and processor.mailbox.tenancyKey = :")
						.append(ProcessorConfigurationDAO.TENANCY_KEY)
						.append(" and processor.mailbox.mbxStatus = :")
						.append(ProcessorConfigurationDAO.STATUS)
						.append(" and processor.procsrStatus = :")
						.append(ProcessorConfigurationDAO.STATUS)
						.append(" and ( ")
						.append(QueryBuilderUtil.constructSqlStringForTypeOperator(specificProcessorTypes))
						.append(")");

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
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return processors;
	}

	public List<Processor> findAllActiveProcessors() {

		EntityManager entityManager = null;
		List<Processor> processors = new ArrayList<Processor>();

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			List <?> proc = entityManager.createNamedQuery(FIND_ALL_ACTIVE_PROCESSORS)
								.setParameter(STATUS, EntityStatus.ACTIVE.value())
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
		return processors;
	}
	
	private Class<?> getProcessorClass(String processorCode) {

		Class <?> processorClass = null;
		switch(processorCode.toLowerCase()) {

    		case HTTP_SYNC_PRCSR_CLASS:
    			processorClass = HTTPSyncProcessor.class;
    			break;
    		case HTTP_ASYNC_PRCSR_CLASS:
    			processorClass = HTTPAsyncProcessor.class;
    			break;
    		case SWEEPER_CLASS:
    			processorClass = Sweeper.class;
    			break;
    		case REMOTE_UPLAODER_CLASS:
    			processorClass = RemoteUploader.class;
    			break;
    		case FILEWRITER_CLASS:
    			processorClass = FileWriter.class;
    			break;
    		case DROPBOX_PRCSR_CLASS:
    			processorClass = DropBoxProcessor.class;
    			break;
    		case REMOTE_DOWNLAODER_CLASS:
    			processorClass = RemoteDownloader.class;
    			break;
		}
		return processorClass;
	}

	
	public List<Processor> getAllProcessors(GenericSearchFilterDTO searchFilter, Map<String, Integer> pageOffsetDetails) {

		EntityManager entityManager = null;
        List<Processor> processors = new ArrayList<Processor>();
        Processor processor = null;

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			StringBuilder query = new StringBuilder().append("select processor from Processor processor");
			generateQueryBySearchFilters(searchFilter, query);
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
		if (!(StringUtil.isNullOrEmptyAfterTrim(sortField)
				&& StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {

			sortDirection = sortDirection.toUpperCase();
			switch (sortField.toLowerCase()) {
        		case SORT_MAILBOX_NAME:
        			query.append(" order by mailbox.mbxName ")
        			.append(sortDirection);
        		break;
        		case SORT_NAME:
        			query.append(" order by processor.procsrName ")
        			.append(sortDirection);
        			break;
        		case SORT_PROTOCOL:
        			query.append(" order by processor.procsrProtocol ")
        			.append(sortDirection);
        			break;
        		case SORT_STATUS:
        			query.append(" order by processor.procsrStatus ")
        			.append(sortDirection);
        			break;
        		case SORT_MAILBOX_STATUS:
        			query.append(" order by mailbox.mbxStatus ")
        			.append(sortDirection);
        			break;
        	}
        } else {
            query.append(" order by processor.procsrName");
        }
    }

	@SuppressWarnings("unchecked")
	@Override
	public Processor findActiveProcessorById(String id) {

		EntityManager entityManager = null;
		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
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

		EntityManager entityManager = null;
		Long totalItems = null;
		int count = 0;

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("Fetching the processors by filters starts.");

			StringBuilder query = new StringBuilder().append("select count(processor) from Processor processor");
			generateQueryBySearchFilters(searchDTO, query);
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
			 
	        EntityManager entityManager = null;
	        List<MailBox> mailboxNames = new ArrayList<MailBox>();

	        try {

				entityManager = DAOUtil.getEntityManager(persistenceUnitName);
	            StringBuilder query = new StringBuilder().append("SELECT mbx FROM MailBox mbx")
	                    .append(" where LOWER(mbx.mbxName) like :")
	                    .append(MBX_NAME);
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
	public List<Processor> getProcessorNames(GenericSearchFilterDTO searchDTO) {
			 
	        EntityManager entityManager = null;
	        List<Processor> processorNames = new ArrayList<Processor>();

	        try {

				entityManager = DAOUtil.getEntityManager(persistenceUnitName);
	            StringBuilder query = new StringBuilder().append("SELECT proc FROM Processor proc")
	                    .append(" where LOWER(proc.procsrName) like :")
	                    .append(PRCSR_NAME);
	            List <?> proc = entityManager.createQuery(query.toString())
	                    .setParameter(PRCSR_NAME, "%" + searchDTO.getProcessorName().toLowerCase() + "%")
	                    .getResultList();	

	            Iterator<?> iter = proc.iterator();
	            Processor processor;
	 			while (iter.hasNext()) {

	 				processor = (Processor) iter.next();
	 				processorNames.add(processor);	 				
	 			}
	 
	        } finally {
	            if (entityManager != null) {
	                entityManager.close();
	            }
	        }
		return processorNames;
	}
	
	@Override
	public List<ScheduleProfilesRef> getProfileNames(GenericSearchFilterDTO searchDTO) {
			 
	        EntityManager entityManager = null;
	        List<ScheduleProfilesRef> profileNames = new ArrayList<ScheduleProfilesRef>();
	 
	        try {

				entityManager = DAOUtil.getEntityManager(persistenceUnitName);
	            StringBuilder query = new StringBuilder().append("SELECT s FROM ScheduleProfilesRef s")
	                    .append(" where LOWER(s.schProfName) like :")
	                    .append(PROF_NAME);
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

	public void generateQueryBySearchFilters(GenericSearchFilterDTO searchDTO, StringBuilder query) {
		
		List<String> predicateList = new ArrayList<String>();
		boolean isFolderAvailable = false;
		
		if (!MailBoxUtil.isEmpty(searchDTO.getMbxName())) {		
			query.append(" inner join processor.mailbox mailbox ");
//			predicateList.add(" LOWER(mailbox.mbxName) " + searchDTO.getMatchMode() + " :" + MBX_NAME);
			predicateList.add((searchDTO.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE)) ?
					" LOWER(mailbox.mbxName) " + searchDTO.getMatchMode() + " :" + MBX_NAME :
					" mailbox.mbxName " + searchDTO.getMatchMode() + " :" + MBX_NAME);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getFolderPath())) {
			query.append(" inner join processor.folders folder ");
			predicateList.add(" folder.fldrUri " + searchDTO.getMatchMode() + " :" + FOLDER_URI);
			isFolderAvailable = true;
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getPipelineId())) {
			predicateList.add(" processor.procsrProperties " + searchDTO.getMatchMode() + " :" + PIPELINE_ID);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProfileName())) {
            String profileAppender = isFolderAvailable ? " inner join folder.processor folderProcessor inner join folderProcessor.scheduleProfileProcessors schd_prof_processor"
                    : " inner join processor.scheduleProfileProcessors schd_prof_processor";
            query.append(profileAppender).append(" inner join schd_prof_processor.scheduleProfilesRef profile");
            predicateList.add("LOWER(profile.schProfName) " + searchDTO.getMatchMode() + " :" + PROF_NAME);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProtocol())) {
			predicateList.add(" LOWER(processor.procsrProtocol) = :" + PROTOCOL);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProcessorType())) {
		    List<String> list = new ArrayList<String> ();
		    list.add(getProcessorClass(searchDTO.getProcessorType()).getCanonicalName());
			predicateList.add(QueryBuilderUtil.constructSqlStringForTypeOperator(list));
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProcessorName())) {
			predicateList.add(" LOWER(processor.procsrName) = :" + PRCSR_NAME);
		}
		if (!MailBoxUtil.isEmpty(searchDTO.getProcessorGuid())) {
			predicateList.add(" LOWER(processor.pguid) = :" + PGUID);
		}

		for (int i = 0; i < predicateList.size(); i++) {			
            query.append((i == 0) ? " WHERE " : " AND ").append(predicateList.get(i));
		}				
	}

	public Query setParamsForProcessorSearchQuery(GenericSearchFilterDTO searchDTO, Query query) {
		
        if (!MailBoxUtil.isEmpty(searchDTO.getMbxName())) {
            query.setParameter(MBX_NAME, (searchDTO.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE)) ?
					"%" + searchDTO.getMbxName().toLowerCase() + "%" :
					searchDTO.getMbxName());
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getFolderPath())) {
            query.setParameter(FOLDER_URI, (searchDTO.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE)) ?
					"%" + searchDTO.getFolderPath() + "%" :
					searchDTO.getFolderPath());
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getPipelineId())) {
            query.setParameter(PIPELINE_ID, (searchDTO.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE)) ?
					"%" + searchDTO.getPipelineId() + "%" :
					searchDTO.getPipelineId());
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getProfileName())) {
            query.setParameter(PROF_NAME, (searchDTO.getMatchMode().equals(GenericSearchFilterDTO.MATCH_MODE_LIKE)) ?
					"%" + searchDTO.getProfileName().toLowerCase() + "%" :
					searchDTO.getProfileName());
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getProtocol())) {
            query.setParameter(PROTOCOL, searchDTO.getProtocol().toLowerCase());
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getProcessorName())) {
            query.setParameter(PRCSR_NAME, searchDTO.getProcessorName().toLowerCase());
        }
        if (!MailBoxUtil.isEmpty(searchDTO.getProcessorGuid())) {
            query.setParameter(PGUID, searchDTO.getProcessorGuid().toLowerCase());
        }
		return query;		
	}

	@Override
	public List<Processor> findProcessorsByName(String processorName) {
		
		EntityManager entityManager = null;
        List<Processor> processors = new ArrayList<Processor>();
        Processor processor = null;

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            List<?> proc = entityManager.createNamedQuery(FIND_PROCESSOR_BY_NAME)
            		.setParameter(PRCSR_NAME, processorName)
            		.getResultList();
			Iterator<?> iter = proc.iterator();
			while (iter.hasNext()) {

				processor = (Processor) iter.next();
				processors.add(processor);
			}

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return processors;		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findProcessorsByMailboxIdAndProcessorType(String mbxId, String processorType) {

		EntityManager entityManager = null;
		List<Object[]> results = null;

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("Fetching the processor starts.");
			results = entityManager.createNativeQuery(PROCESSOR_RETRIEVAL_BY_TYPE_AND_MBX_ID_QUERY.toString())
						  .setParameter(1, processorType)
						  .setParameter(2, (MailBoxUtil.isEmpty(mbxId) ? "''" : mbxId))
						  .getResultList();

		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return results;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findProcessorsByMailboxNameAndProcessorType(String mbxName, String processorType) {

		EntityManager entityManager = null;
		List<Object[]> results = null;

		try {

			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			LOG.debug("Fetching the processor starts.");
			results = entityManager.createNativeQuery(PROCESSOR_RETRIEVAL_BY_TYPE_AND_MBX_NAME_QUERY.toString())
						  .setParameter(1, processorType)
						  .setParameter(2, (MailBoxUtil.isEmpty(mbxName) ? "''" : mbxName.toLowerCase()))
						  .getResultList();
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}

		return results;
	}
}
