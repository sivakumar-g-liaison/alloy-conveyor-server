/**
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.framework.bootstrap;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.liaison.commons.acl.util.ACLUtil;
import com.liaison.commons.acl.util.RemoteURLPublicKeyVerifier;
import com.liaison.commons.acl.util.SignatureVerifier;
import com.liaison.commons.audit.AuditStatement;
import com.liaison.commons.audit.DefaultAuditStatement;
import com.liaison.commons.jpa.DAOUtil;
import com.liaison.commons.messagebus.common.KafkaTextMessageProcessor;
import com.liaison.commons.messagebus.kafka.LiaisonKafkaConsumer;
import com.liaison.commons.messagebus.kafka.LiaisonKafkaConsumerFactory;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonArchaiusConfiguration;
import com.liaison.health.check.file.FileReadDeleteCheck;
import com.liaison.health.check.jdbc.JdbcConnectionCheck;
import com.liaison.health.core.LiaisonHealthCheckRegistry;
import com.liaison.health.core.management.ThreadBlockedHealthCheck;
import com.liaison.health.core.management.ThreadDeadlockHealthCheck;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.DeploymentType;
import com.liaison.mailbox.service.core.ProcessorExecutionConfigurationService;
import com.liaison.mailbox.service.core.bootstrap.QueueAndTopicProcessInitializer;
import com.liaison.mailbox.service.module.GuiceInjector;
import com.liaison.mailbox.service.queue.kafka.Consumer;
import com.liaison.mailbox.service.queue.kafka.Producer;
import com.liaison.mailbox.service.queue.kafka.processor.FileStageReplicationRetry;
import com.liaison.mailbox.service.queue.kafka.processor.Mailbox;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToDropbox;
import com.liaison.mailbox.service.queue.kafka.processor.ServiceBrokerToMailbox;
import com.liaison.mailbox.service.queue.kafka.processor.UserManagementToRelayDirectory;
import com.liaison.mailbox.service.util.MailBoxUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SERVICE_BROKER_ASYNC_URI;
import static com.liaison.mailbox.MailBoxConstants.CONFIGURATION_SERVICE_BROKER_URI;
import static com.liaison.mailbox.MailBoxConstants.PROPERTY_SKIP_KAFKA_QUEUE;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_MAILBOX_PROCESSOR_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_REPLICATION_FAILOVER_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_DROPBOX_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_SERVICE_BROKER_TO_MAILBOX_ADDITIONAL_TOPIC_SUFFIXES;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_DEFAULT_TOPIC_SUFFIX;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_RECEIVER_ID;
import static com.liaison.mailbox.MailBoxConstants.TOPIC_USER_MANAGEMENT_TO_RELAY_DIRECTORY_ADDITIONAL_TOPIC_SUFFIXES;

@Singleton
@ApplicationPath("/*")
public class RelayApp extends ResourceConfig {

    private static final Logger logger = LogManager.getLogger(RelayApp.class);
    private static final long serialVersionUID = -8418412083748649428L;

    private static final String PROPERTY_SERVICE_NFS_MOUNT = "com.liaison.service.nfs.mount";
    private static final DecryptableConfiguration configuration = LiaisonArchaiusConfiguration.getInstance();

    public static Injector injector;

    private LiaisonKafkaConsumer serviceBrokerToDropboxConsumer;
    private LiaisonKafkaConsumer fileStageReplicationRetryConsumer;
    private LiaisonKafkaConsumer mailboxProcessorConsumer;
    private LiaisonKafkaConsumer serviceBrokerToMailboxConsumer;
    private LiaisonKafkaConsumer userManagementToRelayDirectoryConsumer;

    @Inject
    public RelayApp(final ServiceLocator serviceLocator) {

        logger.info("Registering injectable...");
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        injector = GuiceInjector.getInjector();
        guiceBridge.bridgeGuiceInjector(injector);

        // Set package to look for resources in
        packages("com.liaison.mailbox.service.rest",
                "com.liaison.service.resources.acl",
                "com.liaison.threadmanagement.resources",
                "com.wordnik.swagger.jersey.listing",
                "com.fasterxml.jackson.jaxrs.json",
                "com.fasterxml.jackson.jaxrs.xm");

        //GMB-1064 Making sure the BC is before SUNJCE
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        int result = Security.insertProviderAt(new BouncyCastleProvider(), 5);
        if (result == -1) {
            logger.warn("The provider was already installed at the order {}", result);
        } else {
            logger.info("The provider({}) installed successfully at the order {}", BouncyCastleProvider.PROVIDER_NAME, result);
        }

        String deploymentType = configuration.getString(MailBoxConstants.DEPLOYMENT_TYPE, DeploymentType.RELAY.getValue());

        // nfs health check
        // check only if current service is not dropbox
        if (!DeploymentType.CONVEYOR.getValue().equals(deploymentType)) {
            String[] serviceNfsMount = configuration.getStringArray(PROPERTY_SERVICE_NFS_MOUNT);
            if (serviceNfsMount != null) {
                for (String mount : serviceNfsMount) {
                    LiaisonHealthCheckRegistry.INSTANCE.register(mount + "_read_delete_check",
                            new FileReadDeleteCheck(mount));
                }
            }
        }

        logger.info(new DefaultAuditStatement(AuditStatement.Status.SUCCEED,"initialize", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

        DAOUtil.init();
        // Check stuck processors (ie., processorExecutionState is "PROCESSING") during the application startup.
        // Update the status from "PROCESSING" to "FAILED" for the current node.
        ProcessorExecutionConfigurationService.updateExecutionStateOnInit();

        //Initialize the Kafka Producer and Consumer before the Queue
        if (!configuration.getBoolean(PROPERTY_SKIP_KAFKA_QUEUE, true)
                && !DeploymentType.CONVEYOR.getValue().equals(deploymentType)) {
            new Consumer().consume();
            try {
                Class.forName(Producer.class.getName());
            } catch (ClassNotFoundException e) {
                logger.error("Unable to load Producer class", e);
            }
        }

        //QUEUE and TOPIC consumers initialization
        QueueAndTopicProcessInitializer.initialize();

        // db health check
        LiaisonHealthCheckRegistry.INSTANCE.register("dtdm_db_connection_check",
                new JdbcConnectionCheck("jdbc/UCPPool-DTDM"));
        LiaisonHealthCheckRegistry.INSTANCE.register("rtdm_db_connection_check",
                new JdbcConnectionCheck("jdbc/UCPPool-RTDM"));
        LiaisonHealthCheckRegistry.INSTANCE.register("thread_deadlock_check",
                new ThreadDeadlockHealthCheck(1));
        LiaisonHealthCheckRegistry.INSTANCE.register("thread_blocked_check",
                new ThreadBlockedHealthCheck(10));

        // Set ACL Filter Signature Verifier
        SignatureVerifier aclSignatureVerifier = new RemoteURLPublicKeyVerifier();

        ACLUtil.setSignatureVerifier(aclSignatureVerifier);
        logger.info(new DefaultAuditStatement(AuditStatement.Status.SUCCEED, "ACL Filter Signature Verifier Set: " + aclSignatureVerifier.getClass().getName(), com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));
        logger.info(new DefaultAuditStatement(AuditStatement.Status.SUCCEED, "initialize via InitializationServlet", com.liaison.commons.audit.pci.PCIV20Requirement.PCI10_2_6));

        //Register sb http async host
        String serviceBrokerAsyncUri = configuration.getString(CONFIGURATION_SERVICE_BROKER_ASYNC_URI);
        if (!MailBoxUtil.isEmpty(serviceBrokerAsyncUri)) {
            try {

                URL uri = new URL(serviceBrokerAsyncUri);
                HTTPRequest.registerHostForSeparateConnectionPool(uri.getHost());
                HTTPRequest.registerHealthCheck();
            } catch (Exception e) {
                logger.error("Unable to register http sb async pool", e);
            }
        }

        //Register sb http sync host
        String serviceBrokerRTUri = configuration.getString(CONFIGURATION_SERVICE_BROKER_URI);
        if (!MailBoxUtil.isEmpty(serviceBrokerRTUri)) {

            try {
                URL uri = new URL(serviceBrokerRTUri);
                HTTPRequest.registerHostForSeparateConnectionPool(uri.getHost());
                HTTPRequest.registerHealthCheck();
            } catch (Exception e) {
                logger.error("Unable to register http sb sync pool", e);
            }
        }
    }

    // TODO: Does this really work?
    public void destroy() {

        //Shutdown the kafka producer gracefully
        if (!configuration.getBoolean(PROPERTY_SKIP_KAFKA_QUEUE, true)) {
            Producer.stop();
        }

        if (configuration.getBoolean(MailBoxConstants.CONFIGURATION_QUEUE_SERVICE_ENABLED, false)) {
            serviceBrokerToDropboxConsumer.shutdown();
            fileStageReplicationRetryConsumer.shutdown();
            mailboxProcessorConsumer.shutdown();
            serviceBrokerToMailboxConsumer.shutdown();
            userManagementToRelayDirectoryConsumer.shutdown();
        }
    }
}