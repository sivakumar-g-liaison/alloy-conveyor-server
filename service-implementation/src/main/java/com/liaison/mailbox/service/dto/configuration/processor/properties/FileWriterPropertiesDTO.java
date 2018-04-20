/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.processor.properties;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.service.validation.PatternValidation;

/**
 * Data Transfer Object for the properties of file writer.
 * 
 * @author OFS
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="staticProperties")
public class FileWriterPropertiesDTO extends StaticProcessorPropertiesDTO {
    private int staleFileTTL;
    private String fileTransferStatusIndicator;

    @PatternValidation(errorMessage = "Invalid value for TTL", type = MailBoxConstants.PROPERTY_STALE_FILE_TTL)
    public int getStaleFileTTL() {
        return staleFileTTL;
    }

    public void setStaleFileTTL(int staleFileTTL) {
        this.staleFileTTL = staleFileTTL;
    }
    
    public String getFileTransferStatusIndicator() {
        return fileTransferStatusIndicator;
    }

    public void setFileTransferStatusIndicator(String fileTransferStatusIndicator) {
        this.fileTransferStatusIndicator = fileTransferStatusIndicator;
    }
}
