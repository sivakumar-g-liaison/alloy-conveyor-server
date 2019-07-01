/**
 * Copyright 2019 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

import static com.liaison.mailbox.MailBoxConstants.HAZELCAST_CACHE_BACKUP_COUNT;
import static com.liaison.mailbox.MailBoxConstants.HAZELCAST_CACHE_LEGACY_RELAY_CLUSTER_NODES;
import static com.liaison.mailbox.MailBoxConstants.HAZELCAST_CACHE_RELAY_CLUSTER_NODES;
import static com.liaison.mailbox.MailBoxConstants.HAZELCAST_CACHE_SIZE;
import static com.liaison.mailbox.MailBoxConstants.HAZELCAST_CACHE_TTL_IN_SECONDS;

public class HazelcastProvider {

    private static final DecryptableConfiguration CONFIGURATION = LiaisonArchaiusConfiguration.getInstance();
    private static final Logger LOGGER = LogManager.getLogger(HazelcastProvider.class);

    private static IMap<String, String> fileCache;

    private static final String FILE_DELETE_CACHE = "file_delete_cache";

    public static void init() {

        LOGGER.info("hazelcast instance creation process started.");
        Config config = new Config();
        String[] nodes;

        String deploymentType = CONFIGURATION.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());
        if (DeploymentType.RELAY.getValue().equals(deploymentType)) {
            nodes = CONFIGURATION.getStringArray(HAZELCAST_CACHE_RELAY_CLUSTER_NODES);
        } else if (DeploymentType.LOW_SECURE_RELAY.getValue().equals(deploymentType)) {
            nodes = CONFIGURATION.getStringArray(HAZELCAST_CACHE_LEGACY_RELAY_CLUSTER_NODES);
        } else {
            LOGGER.warn("Invalid deployment type {} for HazelcastProvider", deploymentType);
            return;
        }

        List<String> clusterMembers = Arrays.asList(nodes);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        if (!clusterMembers.isEmpty()) {
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).setMembers(clusterMembers);
        }

        config.getMapConfig(FILE_DELETE_CACHE)
                .setBackupCount(CONFIGURATION.getInt(HAZELCAST_CACHE_BACKUP_COUNT, 1))
                .setInMemoryFormat(InMemoryFormat.BINARY).setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizeConfig(new MaxSizeConfig(CONFIGURATION.getInt(HAZELCAST_CACHE_SIZE, 5000), MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE))
                .setTimeToLiveSeconds(CONFIGURATION.getInt(HAZELCAST_CACHE_TTL_IN_SECONDS, 300));

        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
        fileCache = hazelcast.getMap(FILE_DELETE_CACHE);
        LOGGER.info("hazelcast instance created successfully. Caches are {}", FILE_DELETE_CACHE);

    }

    public static void put(String key, String value) {
        fileCache.set(key, value);
    }

    public static String get(String key) {
        return fileCache.get(key);
    }

    public static void remove(String key) {
        fileCache.delete(key);
    }
}