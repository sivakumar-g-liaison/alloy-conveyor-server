/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto;

/**
 * To send properties to abstract sweeper from directory sweeper and conditional sweeper
 */
public class SweeperStaticPropertiesDTO {

    boolean isSecuredPayload;
    boolean isLensVisibility;
    String pipeLineID;
    String ContentType;

    public boolean isSecuredPayload() {
        return isSecuredPayload;
    }

    public void setSecuredPayload(boolean isSecuredPayload) {
        this.isSecuredPayload = isSecuredPayload;
    }

    public boolean isLensVisibility() {
        return isLensVisibility;
    }

    public void setLensVisibility(boolean isLensVisibility) {
        this.isLensVisibility = isLensVisibility;
    }

    public String getPipeLineID() {
        return pipeLineID;
    }

    public void setPipeLineID(String pipeLineID) {
        this.pipeLineID = pipeLineID;
    }

    public String getContentType() {
        return ContentType;
    }

    public void setContentType(String contentType) {
        ContentType = contentType;
    }
}
