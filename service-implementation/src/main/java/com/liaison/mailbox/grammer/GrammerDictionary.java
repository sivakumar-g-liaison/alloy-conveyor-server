/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.grammer;

import java.util.HashMap;
import java.util.Map;

import com.liaison.mailbox.grammer.dto.ProfileConfigurationRequest;
import com.liaison.mailbox.grammer.dto.ProfileConfigurationResponse;

/**
 * This is the class programatically describes the JAXB/JSON model.
 *
 * @author veerasamyn
 */
public class GrammerDictionary {

	static final Map<String, Class<?>> entityMap;
    static final Class<?>[] entityArray;

    static {

        entityMap = new HashMap<String, Class<?>>();
        entityMap.put("ProfileConfigurationRequest", ProfileConfigurationRequest.class);
        entityMap.put("ProfileConfigurationResponse", ProfileConfigurationResponse.class);

        entityArray = entityMap.values().toArray(new Class<?>[]{});
    }
    
    public static Map<String, Class<?>> getEntityMap() {        
        return entityMap;
    }
    
    public static Class<?>[] getEntityArray() {
        return entityArray;
    }

}
