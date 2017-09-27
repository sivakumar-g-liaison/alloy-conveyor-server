/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import com.liaison.commons.acl.manifest.dto.RoleBasedAccessControl;
import com.liaison.commons.util.StringUtil;
import com.liaison.gem.service.client.GEMACLClient;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.dto.configuration.TenancyKeyDTO;
import com.liaison.mailbox.service.exception.MailBoxServicesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tenancy Key Utility
 */
public class TenancyKeyUtil {

    private static final Logger LOGGER = LogManager.getLogger(TenancyKeyUtil.class);

    private static GEMACLClient gemClient = new GEMACLClient();

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

        List<RoleBasedAccessControl> roleBasedAccessControls = gemClient.getDomainsFromACLManifest(aclManifestJson);
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

        return gemClient.getDomainsFromACLManifest(aclManifestJson)
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

            List<RoleBasedAccessControl> roleBasedAccessControls = gemClient.getDomainsFromACLManifest(aclManifestJson);
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
}
