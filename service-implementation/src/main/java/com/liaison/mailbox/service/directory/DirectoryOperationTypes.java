/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.directory;

import com.liaison.mailbox.service.util.MailBoxUtil;

public enum DirectoryOperationTypes {
    
    CREATE("CREATE"),
    DELETE("DELETE");
    
    private String operType;
    
    private DirectoryOperationTypes(String operType) {
        this.operType = operType;
    }
    
    public String value() {
        return operType;
    }
    
    public static DirectoryOperationTypes findByType(String type) {
        
        DirectoryOperationTypes found = null;
        for (DirectoryOperationTypes value : DirectoryOperationTypes.values()) {
            
            if (!MailBoxUtil.isEmpty(type) && type.equalsIgnoreCase(value.name())) {
                found = value;
                break;
            }
        }
        return found;
    }

}
