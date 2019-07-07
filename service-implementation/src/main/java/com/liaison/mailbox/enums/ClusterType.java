/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.enums;


import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Contains the list of cluster type.
 */
public enum ClusterType {

    SECURE("SECURE"),
    LOWSECURE("LOWSECURE");

    private String value;

    private ClusterType(String status) {
        this.setValue(status);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * find ClusterType by name
     *
     * @param name The credential type
     * @return CredentialType
     */
    public static ClusterType findByName(String name) {

        ClusterType found = null;
        for (ClusterType value : ClusterType.values()) {

            if (!MailBoxUtil.isEmpty(name) && name.equalsIgnoreCase(value.name())) {
                found = value;
                break;
            }
        }

        return found;

    }

}
