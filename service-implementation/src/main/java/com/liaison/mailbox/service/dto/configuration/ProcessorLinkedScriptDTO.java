/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;

import java.io.IOException;
import java.io.Serializable;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.util.ProcessorPropertyJsonMapper;
import com.wordnik.swagger.annotations.ApiModel;

/**
 * Data Transfer Object for ProcessorsScript details.
 *
 */
@ApiModel(value = "processors")
public class ProcessorLinkedScriptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String guid;
    private String name;
    private boolean isScriptExecutionEnabled;
    private String scriptName;
    private String mailboxId;

    public String getMailboxId() {
        return mailboxId;
    }

    public void setMailboxId(String mailboxId) {
        this.mailboxId = mailboxId;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Copies the values from Entity to DTO.
     *
     * @param processor
     * @throws IOException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public void copyFromEntity(Processor processor) throws IllegalAccessException, NoSuchFieldException, IOException {

        this.setGuid(processor.getPguid());
        this.setName(processor.getProcsrName());
        this.setScriptName(processor.getJavaScriptUri());
        this.setMailboxId(processor.getMailbox().getPguid());
        this.setScriptExecutionEnabled(ProcessorPropertyJsonMapper.getHydratedUIPropertyTemplate(processor.getProcsrProperties(), processor).isHandOverExecutionToJavaScript());

    }

    public boolean isScriptExecutionEnabled() {
        return isScriptExecutionEnabled;
    }

    public void setScriptExecutionEnabled(boolean isScriptExecutionEnabled) {
        this.isScriptExecutionEnabled = isScriptExecutionEnabled;
    }

}