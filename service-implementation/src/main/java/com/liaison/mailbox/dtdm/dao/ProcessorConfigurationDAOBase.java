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
import com.liaison.mailbox.dtdm.model.FileWriter;
import com.liaison.mailbox.dtdm.model.HTTPAsyncProcessor;
import com.liaison.mailbox.dtdm.model.HTTPSyncProcessor;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.RemoteUploader;
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

	
	public List<Processor> getAllProcessors(GenericSearchFilterDTO searchFilter, Map <String, Integer> pageOffsetDetails) {
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {
			
			StringBuilder query = new StringBuilder().append("select processor from Processor processor");
			String sortDirection = searchFilter.getSortDirection();
			String sortField=searchFilter.getSortField();

			if(!(StringUtil.isNullOrEmptyAfterTrim(sortField) && StringUtil.isNullOrEmptyAfterTrim(sortDirection))) {
				sortDirection=sortDirection.toUpperCase();
				switch (sortField.toLowerCase()) {
					case "mailboxname":
						query.append(" order by processor.mailbox.mbxName " + sortDirection);
					break;
					case "name":
						query.append(" order by processor.procsrName " + sortDirection);
						break;
					case "type":
						query.append(" order by processor.type " + sortDirection);
						break;
					case "protocol":
						query.append(" order by processor.procsrProtocol " + sortDirection);
						break;
					case "status":
						query.append(" order by processor.procsrStatus " + sortDirection);
						break;
				}
			}else {
				query.append(" order by processor.procsrName");
			}
			List <?> proc = entityManager.createQuery(query.toString())
										 .setFirstResult(pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET))
										 .setMaxResults(pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT))
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
	
	@Override
	public List<Processor> filterProcessors(GenericSearchFilterDTO searchDTO) {
		EntityManager entityManager = DAOUtil.getEntityManager(persistenceUnitName);
		List<Processor> processors = new ArrayList<Processor>();

		try {

			LOG.info("Fetching the processor starts.");
			boolean isWhereAdded = false;
			StringBuilder query = new StringBuilder().append("select processor from Processor processor");
			
			if(null != searchDTO.getMbxName() && searchDTO.getMbxName() != ""){
				isWhereAdded = true;
				query.append(" inner join processor.mailbox mbx");
			}
			if(null != searchDTO.getPipelineId() && searchDTO.getPipelineId() != ""){
				isWhereAdded = true;
				query.append(" ");
			}
			if(null != searchDTO.getFolderPath() && searchDTO.getFolderPath() != ""){
				isWhereAdded = true;
				query.append(" inner join processor.folders folder ");
			}
			if(null != searchDTO.getProfileName() && searchDTO.getProfileName() != ""){
				isWhereAdded = true;
				query.append(" inner join processor.scheduleProfileProcessors schd_prof_processor")
						     .append(" inner join schd_prof_processor.scheduleProfilesRef profile");
			}
			if(isWhereAdded){
				query.append(" where  ");
			}
			if(null != searchDTO.getMbxName() && searchDTO.getMbxName() != ""){
				query.append(" processor.mailbox.name like: %" + searchDTO.getMbxName() + "%");				
			}
			if(null != searchDTO.getPipelineId() && searchDTO.getPipelineId() != ""){
				query.append(" ");
			}
			if(null != searchDTO.getFolderPath() && searchDTO.getFolderPath() != ""){
				query.append(" folder.folderuri like: %" + searchDTO.getFolderPath() + "%");
			}
			if(null != searchDTO.getProfileName() && searchDTO.getProfileName() != ""){
				query.append(" profile.name like: %" + searchDTO.getProfileName() + "%");
			}
			if(null != searchDTO.getProtocol() && searchDTO.getProtocol() != ""){
				query.append(" processor.protocol = :" + searchDTO.getProtocol());
			}
			if(null != searchDTO.getProcessorType() && searchDTO.getProcessorType() != ""){
				query.append(" TYPE(processor) = :" + searchDTO.getProcessorType());
			}

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
}