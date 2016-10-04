/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.rtdm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


/**
 * The persistent class for the PROCESSORS database table.
 *
 *  @author OFS
 */
@Entity
@Table(name = "PROCESSORS")
public class RuntimeProcessors {

    private String pguid;
    private String processorId;
    
    @OneToOne
    @JoinColumn(name = "PGUID", nullable = false)
    public String getPguid() {
        return pguid;
    }
    
    public void setPguid(String pguid) {
        this.pguid = pguid;
    }
    
    @Column(name = "PROCESSOR_ID", length = 32)
    public String getProcessorId() {
        return processorId;
    }
    
    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }
}
