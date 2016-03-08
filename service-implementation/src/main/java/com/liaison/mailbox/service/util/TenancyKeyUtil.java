/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.util.client.sftp.StringUtil;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;

/**
 * Utilities for MailBox.
 *
 * @author veerasamyn
 */
public class TenancyKeyUtil {

	private static final Logger LOGGER = LogManager.getLogger(TenancyKeyUtil.class);

	/**
     * Method to get all tenancy keys from acl manifest Json
     *
     * @param String
     *            - aclManifestJson
     * @return list of tenancy keys
     * @throws IOException
     */
    public static List<TenancyKeyDTO> getTenancyKeysFromACLManifest(String aclManifestJson)
            throws IOException {

        List<TenancyKeyDTO> tenancyKeys = new ArrayList<TenancyKeyDTO>();

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

        LOGGER.info("List of Tenancy keys retrieved are {}", tenancyKeys);
        return tenancyKeys;
    }

    public static List<String> getTenancyKeyGuids(String aclManifestJson)
            throws IOException {

        List<String> tenancyKeyGuids = new ArrayList<String>();
        List<RoleBasedAccessControl> roleBasedAccessControls = GEMHelper.getDomainsFromACLManifest(aclManifestJson);

        for (RoleBasedAccessControl rbac : roleBasedAccessControls) {
            tenancyKeyGuids.add(rbac.getDomainInternalName());
        }
        return tenancyKeyGuids;

    }

    /**
     * This Method will retrieve the TenancyKey Name from the given guid
     *
     * @param tenancyKeyGuid
     * @param tenancyKeys
     * @return
     * @throws IOException
     */
    public static String getTenancyKeyNameByGuid(String aclManifestJson, String tenancyKeyGuid)
            throws IOException {

        String tenancyKeyDisplayName = null;
        List<RoleBasedAccessControl> roleBasedAccessControls = GEMHelper.getDomainsFromACLManifest(aclManifestJson);

        for (RoleBasedAccessControl rbac : roleBasedAccessControls) {

            if (rbac.getDomainInternalName().equals(tenancyKeyGuid)) {
                tenancyKeyDisplayName = rbac.getDomainName();
                break;
            }
        }

        return tenancyKeyDisplayName;
    }

}
