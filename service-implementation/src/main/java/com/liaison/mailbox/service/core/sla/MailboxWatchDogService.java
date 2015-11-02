/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.sla;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.message.glass.dom.StatusType;
import com.liaison.mailbox.enums.EntityStatus;
import com.liaison.mailbox.enums.ExecutionState;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.dao.StagedFileDAO;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.util.GlassMessage;
import com.liaison.mailbox.service.util.MailBoxUtil;
import com.liaison.mailbox.service.util.TransactionVisibilityClient;

/**
 * Updates LENS status for the customer picked up the files
 * 
 * @author OFS
 */
public class MailboxWatchDogService {

	private static final Logger LOGGER = LogManager.getLogger(MailboxWatchDogService.class);

	protected static final String logPrefix = "WatchDog ";
	protected static final String seperator = " :";

	private String constructMessage(String... messages) {

		StringBuilder msgBuf = new StringBuilder()
				.append(logPrefix)
				.append(seperator);
        for (String str : messages) {
            msgBuf.append(str).append(seperator);
        }
        return msgBuf.toString();
	}

	/**
	 * Poll and update LENS status for the customer picked up files
	 *
	 */
	@SuppressWarnings("unchecked")
	public void pollAndUpdateStatus() {

		String uniqueId = MailBoxUtil.getGUID();
		String filePath = null;
		String fileName = null;

		EntityTransaction tx = null;
		EntityManager em = null;
		List<StagedFile> updatedStatusList = new ArrayList<>();
		TransactionVisibilityClient transactionVisibilityClient = null;
		GlassMessage glassMessage = null;

		try {

			// Getting the mailbox.
			em = DAOUtil.getEntityManager(MailboxRTDMDAO.PERSISTENCE_UNIT_NAME);
			tx = em.getTransaction();
			tx.begin();

			// query
			StringBuilder queryString = new StringBuilder().append("select sf from StagedFile sf")
					.append(" where sf.processorType =:")
					.append(StagedFileDAO.TYPE)
					.append(" and sf.stagedFileStatus =:")
					.append(StagedFileDAO.STATUS);

			List<StagedFile> stagedFiles = em.createQuery(queryString.toString())
					.setParameter(StagedFileDAO.STATUS, EntityStatus.ACTIVE.value())
					.setParameter(StagedFileDAO.TYPE, ProcessorType.FILEWRITER.name())
					.getResultList();
			if (stagedFiles == null || stagedFiles.isEmpty()) {
				LOGGER.info(constructMessage(uniqueId, "No active files found"));
				return;
			}

			for (StagedFile stagedFile : stagedFiles) {

				filePath = stagedFile.getFilePath();
				fileName = stagedFile.getFileName();

				if (MailBoxUtil.isEmpty(filePath)) {
					//This may be old staged file
					continue;
				}

				if (Files.exists(Paths.get(filePath + File.separatorChar + fileName), LinkOption.NOFOLLOW_LINKS)) {
					LOGGER.info(constructMessage(uniqueId, "File {} is exists at the location {}"), fileName, filePath);
					continue;
				}
				LOGGER.info(constructMessage(uniqueId, "File {} is not exist at the location {}"), fileName, filePath);

				transactionVisibilityClient = new TransactionVisibilityClient();
				glassMessage = new GlassMessage();
				glassMessage.setGlobalPId(stagedFile.getGlobalProcessId());

				glassMessage.setStatus(ExecutionState.COMPLETED);
				glassMessage.setOutAgent(stagedFile.getFilePath());
				glassMessage.setOutboundFileName(stagedFile.getFileName());
				glassMessage.logProcessingStatus(StatusType.SUCCESS, "File is picked up by the customer or other process", null, null);

				//TVAPI
				transactionVisibilityClient.logToGlass(glassMessage);
				LOGGER.info(constructMessage(uniqueId, "Updated LENS status for the file {} and location is {}"), fileName, filePath);

				// Inactivate the stagedFile
				stagedFile.setStagedFileStatus(EntityStatus.INACTIVE.value());
				stagedFile.setModifiedDate(MailBoxUtil.getTimestamp());
				updatedStatusList.add(stagedFile);
			}

			for (StagedFile updatedFile : updatedStatusList) {
	            em.merge(updatedFile);
	        }

			tx.commit();

		} catch (Exception e) {
			LOGGER.error(constructMessage(uniqueId, "Error occured in watchdog service" , e.getMessage()));
			if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
		} finally {
            if (em != null) {
                em.close();
            }
        }

	}

}
