/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;
import com.liaison.mailbox.rtdm.dao.DatacenterDAO;

/**
 * The persistent class for the DATACENTER database table.
 *
 */
@Entity
@Table(name = "DATACENTER")
@NamedQueries({
    @NamedQuery(name = DatacenterDAO.FIND_PROCESSING_DC_BY_NAME,
            query = "SELECT datacenter.processing_Dc FROM Datacenter datacenter" +
                    " WHERE datacenter.name =:" + DatacenterDAO.NAME)
})
public class Datacenter implements Identifiable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String processing_Dc;

    @Id
    @Column(name = "NAME", nullable = false, unique = true, length = 5)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "PROCESSING_DC", nullable = false, unique = true, length = 5)
    public String getProcessing_Dc() {
        return processing_Dc;
    }

    public void setProcessing_Dc(String processing_Dc) {
        this.processing_Dc = processing_Dc;
    }

    @Override
    @Transient
    public Object getPrimaryKey() {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transient
    public Class getEntityClass() {
        return this.getClass();
    }
}
