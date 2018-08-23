/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.dao;

import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.jpa.GenericDAOBase;
import com.liaison.mailbox.rtdm.model.Datacenter;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * DAO base class for Datacenter.
 * 
 */
public class DatacenterDAOBase extends GenericDAOBase<Datacenter> implements DatacenterDAO, MailboxRTDMDAO {

    public DatacenterDAOBase() {
        super(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public void updateDatacenter(Map<String, String> datacenterMap) {

        EntityManager entityManager = null;
        EntityTransaction tx = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            tx = entityManager.getTransaction();
            tx.begin();

            Query query = entityManager.createNativeQuery(UPDATE_PROCESSING_DATACENTRE);
            for (Entry<String, String> datacenter : datacenterMap.entrySet()) {

                query.setParameter(PROCESING_DC, datacenter.getValue())
                     .setParameter(NAME, datacenter.getKey())
                     .executeUpdate();
            }

            //commits the transaction
            tx.commit();
        } catch (Exception e) {
            if (null != tx && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public void createDatacenter(Map<String, String> datacenterMap) {

        EntityManager entityManager = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);

            List dcList = datacenterMap.entrySet()
                    .stream()
                    .filter(this::checkIfDatacenterExist)
                    .collect(Collectors.toList());
            if (!dcList.isEmpty()) {
                throw new RuntimeException("Unable to create datacenter: Datacenter already exists");
            }

            for (Entry<String, String> datacenterEntry : datacenterMap.entrySet()) {

                Datacenter datacenter = new Datacenter();
                datacenter.setName(datacenterEntry.getKey());
                datacenter.setProcessing_Dc(datacenterEntry.getValue());
                persist(datacenter);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (null != entityManager) {
                entityManager.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String findProcessingDatacenterByName(String name) {

        EntityManager entityManager = null;
        String processing_Dc = null;

        try {

            entityManager = DAOUtil.getEntityManager(persistenceUnitName);
            processing_Dc = (String) entityManager.createNamedQuery(FIND_PROCESSING_DC_BY_NAME)
                    .setParameter(DatacenterDAO.NAME, name)
                    .getResultList().stream().findFirst().orElse(null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return processing_Dc;
    }

    /**
     * Helper method to check the datacenter existence
     * 
     * @param datacenterEntry
     * @return boolean 
     */
    private boolean checkIfDatacenterExist(Entry<String, String> datacenterEntry) {

        DatacenterDAO dao = new DatacenterDAOBase();
        String datacenter = dao.findProcessingDatacenterByName(datacenterEntry.getKey());
        return null != datacenter;
    }
}
