/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dropbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.dao.MailboxRTDMDAO;
import com.liaison.mailbox.rtdm.dao.UploadedFileDAO;
import com.liaison.mailbox.rtdm.model.UploadedFile;
import com.liaison.mailbox.service.dto.GenericSearchFilterDTO;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * This will fetch the uploaded file details. 
 *
 */
public class UploadedFileDAOBase extends GenericDAOBase<UploadedFile> implements UploadedFileDAO, MailboxRTDMDAO {
	
    public UploadedFileDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }

	@Override
	public int getUploadedFilesCountByUserId(String loginId, String fileName) {
		
        EntityManager entityManager = null;
        Long totalItems;
        int count;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            StringBuilder query = new StringBuilder().append("select count(uf.fileName) from UploadedFile uf")
            		.append(" WHERE uf.userId = :")
            		.append(USER_ID)
            		.append(" and uf.expiryDate > :")
                    .append(CURRENT_TIME);
            
            boolean nameIsEmpty = MailBoxUtil.isEmpty(fileName) ? true : false;
            if (!nameIsEmpty) {
                query.append(" and LOWER(uf.fileName) like :")
                .append(FILE_NAME);
            }           
            
            Query uploadedFileCountQuery = entityManager.createQuery(query.toString());            
            
            uploadedFileCountQuery.setParameter(USER_ID, loginId);
            uploadedFileCountQuery.setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()));
            if (!nameIsEmpty) {
               uploadedFileCountQuery.setParameter(FILE_NAME, fileName.toLowerCase() + "%");
            }

            totalItems = (Long) uploadedFileCountQuery.getSingleResult();
            count = totalItems.intValue();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }

        return count;
	}


	@Override
	public List<UploadedFile> findUploadedFiles(String loginId, GenericSearchFilterDTO searchFilter,
			Map<String, Integer> pageOffsetDetails) {
		
	    EntityManager entityManager = null;
	    List<UploadedFile> uploadedFiles = new ArrayList<>();
		
		try{
			
			entityManager = DAOUtil.getEntityManager(persistenceUnitName);
			// get Search Filters
            String sortDirection = searchFilter.getSortDirection();
            int pagingOffset = pageOffsetDetails.get(MailBoxConstants.PAGING_OFFSET);
            int pagingCount = pageOffsetDetails.get(MailBoxConstants.PAGING_COUNT);
            String fileName =  searchFilter.getUploadedFileName();
            
            StringBuilder queryString = new StringBuilder().append("select uf from UploadedFile uf")
            		.append(" WHERE  uf.userId =:")
            		.append(USER_ID)
            		.append(" and uf.expiryDate > :")
                    .append(CURRENT_TIME);
            
            boolean nameIsEmpty = MailBoxUtil.isEmpty(fileName) ? true : false;
            if (!nameIsEmpty) {
                queryString.append(" and LOWER(uf.fileName) like :")
                .append(FILE_NAME);
            }
            
            if (!MailBoxUtil.isEmpty(sortDirection)) {
                sortDirection = sortDirection.toUpperCase();
                queryString.append(" order by uf.fileName ").append(sortDirection);
            } else {
                queryString.append(" order by uf.fileName");
            }
            
            Query query = entityManager.createQuery(queryString.toString());
            query.setParameter(USER_ID, loginId);
            query.setParameter(CURRENT_TIME, new Timestamp(System.currentTimeMillis()));
            if (!nameIsEmpty) {
                query.setParameter(FILE_NAME, fileName.toLowerCase() + "%");
            }
            
            query.setFirstResult(pagingOffset);
            query.setMaxResults(pagingCount);
            
            List<?> files = query.getResultList();

            for (Object uploadedFile : files) {
            	uploadedFiles.add((UploadedFile) uploadedFile);
            }
		
		} finally {
			if (null != entityManager) {
				entityManager.close();
			}
		}
		return uploadedFiles;
	}
    
}
