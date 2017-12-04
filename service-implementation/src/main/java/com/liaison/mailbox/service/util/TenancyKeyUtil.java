/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import static com.liaison.mailbox.MailBoxConstants.PROPERTY_GEM_TENANCY_KEY_VALIDATION_URL;

import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.StringUtil;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tenancy Key Utility
 */
public class TenancyKeyUtil {

    private static final Logger LOGGER = LogManager.getLogger(TenancyKeyUtil.class);
    private static final String JSON_KEY_BOOLEAN = "Boolean";

    /**
     * Method to get all tenancy keys from acl manifest Json
     *
     * @param aclManifestJson manifest json
     * @return list of tenancy keys
     * @throws IOException
     */
    public static List<TenancyKeyDTO> getTenancyKeysFromACLManifest(String aclManifestJson)
            throws IOException {

        List<TenancyKeyDTO> tenancyKeys = new ArrayList<>();

        if (MailBoxUtil.isEmpty(aclManifestJson)) {
            return tenancyKeys;
        }

        List<RoleBasedAccessControl> roleBasedAccessControls = GEMHelper.getDomainsFromACLManifest(aclManifestJson);
        TenancyKeyDTO tenancyKey = null;
        for (RoleBasedAccessControl rbac : roleBasedAccessControls) {

            tenancyKey = new TenancyKeyDTO();
            tenancyKey.setName(rbac.getDomainName());
            // if domainInternalName is not available then exception will be thrown.
            if (StringUtil.isNullOrEmptyAfterTrim(rbac.getDomainInternalName())) {
                throw new MailBoxServicesException(Messages.DOMAIN_INTERNAL_NAME_MISSING_IN_MANIFEST,
                        Response.Status.CONFLICT);
            } else {
                tenancyKey.setGuid(rbac.getDomainInternalName());
            }
            tenancyKeys.add(tenancyKey);
        }

        LOGGER.debug("List of Tenancy keys retrieved are {}", tenancyKeys);
        return tenancyKeys;
    }

    /**
     *  Gets tenancy keys from the manifest
     *
     * @param aclManifestJson manifest details
     * @return list of tenancy key
     * @throws IOException
     */
    public static List<String> getTenancyKeyGuids(String aclManifestJson)
            throws IOException {

        if (MailBoxUtil.isEmpty(aclManifestJson)) {
            return new ArrayList<>();
        }

        return GEMHelper.getDomainsFromACLManifest(aclManifestJson)
                .stream()
                .map(RoleBasedAccessControl::getDomainInternalName)
                .collect(Collectors.toList());
    }

    /**
     * This Method will retrieve the TenancyKey Name from the given guid
     *
     * @param aclManifestJson manifest details
     * @param tenancyKeyGuid tenancy key guid
     * @return tenancy key name(org name)
     * @throws IOException
     */
    public static String getTenancyKeyNameByGuid(String aclManifestJson, String tenancyKeyGuid)
            throws IOException {

        String tenancyKeyDisplayName = null;
        if (!MailBoxUtil.isEmpty(aclManifestJson)) {

            List<RoleBasedAccessControl> roleBasedAccessControls = GEMHelper.getDomainsFromACLManifest(aclManifestJson);
            tenancyKeyDisplayName = roleBasedAccessControls
                    .stream()
                    .filter(rbac -> rbac.getDomainInternalName().equals(tenancyKeyGuid))
                    .map(RoleBasedAccessControl::getDomainName)
                    .findFirst()
                    .orElse(null);
        }

        if (null == tenancyKeyDisplayName) {
            tenancyKeyDisplayName = tenancyKeyGuid;
        }

        return tenancyKeyDisplayName;
    }

    /**
     * This method will validate TenancyKey name with given guid
     * 
     * @param tenancyKeyGuid tenancy key guid
     * @return boolean
     * @throws IOException 
     * @throws LiaisonException 
     * @throws JSONException 
     */
    public static boolean isValidTenancyKeyByGuid(String tenancyKeyGuid) throws IOException, LiaisonException, JSONException {

    	String url = MailBoxUtil.getEnvironmentProperties().getString(PROPERTY_GEM_TENANCY_KEY_VALIDATION_URL);
    	if (url == null) {
    	    throw new RuntimeException(String.format("Property [%s] cannot be null", PROPERTY_GEM_TENANCY_KEY_VALIDATION_URL));
    	}

    	GEMManifestResponse gemManifestFromGEM = GEMHelper.getACLManifest();
    	Map<String, String> headerMap = GEMHelper.getRequestHeaders(gemManifestFromGEM, "application/json");

    	url = url + tenancyKeyGuid;
    	LOGGER.debug("The GEM URL TO VALIDATE TENANCY KEY " + url);
    	String jsonResponse = HTTPClientUtil.getHTTPResponseInString(LOGGER, url, headerMap);
    	JSONObject obj = new JSONObject(jsonResponse);

        return obj.getBoolean(JSON_KEY_BOOLEAN);
    }
    
}
