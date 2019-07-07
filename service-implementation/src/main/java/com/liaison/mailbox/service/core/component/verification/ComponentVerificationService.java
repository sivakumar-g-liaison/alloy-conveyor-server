/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.component.verification;

import com.liaison.Version;
import com.liaison.commons.util.StreamUtil;
import com.liaison.commons.util.bootstrap.BootstrapRemoteKeystore;
import com.liaison.dto.queue.WorkTicket;
import com.liaison.fs2.api.FS2ObjectHeaders;
import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.gem.service.client.GEMHelper;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.dao.ProcessorConfigurationDAOBase;
import com.liaison.mailbox.rtdm.dao.ProcessorExecutionStateDAOBase;
import com.liaison.mailbox.service.dto.configuration.response.ComponentVerificationDTO;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import com.netflix.config.ConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for verifying different components in Mailbox
 */
public class ComponentVerificationService {

    private static final Logger logger = LogManager.getLogger(ComponentVerificationService.class);
    private static final String COMPONENT_NAME_EDM = "DB_DTDM";
    private static final String COMPONENT_NAME_RTDM = "DB_RTDM";
    private static final String SUCCESS = "Success";
    private static final String FAILURE = "Failure";
    private static final String COMPONENT_NAME_FS2 = "FS2";
    private static final String COMPONENT_NAME_ACL = "ACL";
    private static final String COMPONENT_NAME_BOOTSTRAP = "Bootstrap";
    private static final String COMPONENT_NAME_ENVIRONMENT = "Environment";
    private static final String COMPONENT_NAME_VERSION = "Version";
    private static final String COMPONENT_NAME_QUEUE = "Queue";
    private static final String COMPONENT_NAME_THREADS = "Thread Summary";

    private long startTime = 0;
    private long endTime = 0;
    private long elapsedTime = 0;
    private List<ComponentVerificationDTO> componentsList = new ArrayList<>();

    /**
     * Verifies all the components
     *
     * @return List list of component results (ComponentVerificationDTO class)
     * @throws Exception
     */
    public List<ComponentVerificationDTO> verifyComponents() throws Exception {

        verifyDbConfiguration();
        verifyFs2Configuration();
        verifyAclConfiguration();
        verifyBootStrapConfiguration();
        verifyEnvironmentConfiguration();
        verifyVersionConfiguration();
        verifyQueueConfiguration();
        verifyThreadStatus();
        return componentsList;
    }

    /**
     * Verifies the EDM and RTDM DB components
     */
    public void verifyDbConfiguration() {

        // To verify the EDM Configuration
        startTime = System.currentTimeMillis();

        try {

            ProcessorConfigurationDAOBase procConfigDAOBase = new ProcessorConfigurationDAOBase();
            procConfigDAOBase.findAllActiveProcessors();
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_EDM, SUCCESS, "", elapsedTime);
            logger.debug("Component: " + COMPONENT_NAME_EDM + ", Status : " + SUCCESS + ", ElapsedTime in milli seconds : " + elapsedTime);
        } catch (Throwable t) {
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_EDM, FAILURE, t.getMessage(), elapsedTime);
            logger.error("Component: " + COMPONENT_NAME_EDM + ", Status : " + FAILURE + ", ElapsedTime in milli seconds : " + elapsedTime, t);
        }

        // To verify RTDM Configuration
        startTime = System.currentTimeMillis();

        try {

            ProcessorExecutionStateDAOBase procExecDAOBase = new ProcessorExecutionStateDAOBase();
            procExecDAOBase.findNonExecutingProcessors();
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_RTDM, SUCCESS, "", elapsedTime);
            logger.debug("Component: " + COMPONENT_NAME_RTDM + ", Status : " + SUCCESS + ", ElapsedTime in milli seconds : " + elapsedTime);
        } catch (Throwable t) {
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_RTDM, FAILURE, t.getMessage(), elapsedTime);
            logger.error("Component: " + COMPONENT_NAME_RTDM + ", Status : " + FAILURE + ", ElapsedTime in milli seconds : " + elapsedTime, t);
        }
    }


    /**
     * To verify the FS2 configuration by storing and retrieving the payload
     *
     * @throws Exception
     */
    public void verifyFs2Configuration() throws Exception {

        startTime = System.currentTimeMillis();

        try {

            System.setProperty("archaius.deployment.applicationId", "g2mailboxservice");
            System.setProperty("archaius.deployment.environment", "test");

            String input = "This is to verify the FS2 Component";
            InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

            //Dummy headers
            long globalProcessId = System.currentTimeMillis();
            FS2ObjectHeaders fs2Header = new FS2ObjectHeaders();
            fs2Header.addHeader(MailBoxConstants.KEY_GLOBAL_PROCESS_ID, String.valueOf(globalProcessId));
            logger.debug("FS2 Headers set are {}", fs2Header.getHeaders());

            WorkTicket wTicket = new WorkTicket();
            wTicket.setGlobalProcessId(String.valueOf(globalProcessId));
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(MailBoxConstants.PROPERTY_HTTPLISTENER_SECUREDPAYLOAD, String.valueOf(true));

            FS2MetaSnapshot metaSnapshot = StorageUtilities.persistPayload(stream, wTicket, properties, false);
            InputStream is = null;
            try {

                is = StorageUtilities.retrievePayload(metaSnapshot.getURI().toString());
                String payload = new String(StreamUtil.streamToBytes(is));
                logger.debug("The received payload is \"{}\"", payload);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_FS2, SUCCESS, "", elapsedTime);
            logger.debug("Component: " + COMPONENT_NAME_FS2 + ", Status : " + SUCCESS + ", ElapsedTime in milli seconds : " + elapsedTime);
        } catch (Throwable t) {
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_FS2, FAILURE, t.getMessage(), elapsedTime);
            logger.error("Component: " + COMPONENT_NAME_FS2 + ", Status : " + FAILURE + ", ElapsedTime in milli seconds : " + elapsedTime, t);
        }
    }

    /**
     * To verify the ACL configuration by Performing the operation against Key Manager
     */
    public void verifyAclConfiguration() {

        startTime = System.currentTimeMillis();

        try {

            GEMHelper.getACLManifest();
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_ACL, SUCCESS, "", elapsedTime);
            logger.debug("Component: " + COMPONENT_NAME_ACL + ", Status : " + SUCCESS + ", ElapsedTime in milli seconds : " + elapsedTime);
        } catch (Throwable t) {

            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_ACL, FAILURE, t.getMessage(), elapsedTime);
            logger.error("Component: " + COMPONENT_NAME_ACL + ", Status : " + FAILURE + " , ElapsedTime in milli seconds : " + elapsedTime, t);
        }
    }

    /**
     * To verify the Bootstrap configuration
     */
    public void verifyBootStrapConfiguration() {

        startTime = System.currentTimeMillis();

        try {

            // read keystore from Bootstrap
            BootstrapRemoteKeystore.getDecryptedRemoteKeypairPassphrase().toCharArray();
            logger.debug("Loading keystore from Bootstrap");
            BootstrapRemoteKeystore.getRemoteKeyStore();
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_BOOTSTRAP, SUCCESS, "", elapsedTime);
            logger.debug("Component: " + COMPONENT_NAME_BOOTSTRAP + ", Status : " + SUCCESS + ", ElapsedTime in milli seconds : " + elapsedTime);
        } catch (Throwable t) {

            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_BOOTSTRAP, FAILURE, t.getMessage(), elapsedTime);
            logger.error("Component: " + COMPONENT_NAME_BOOTSTRAP + ", Status : " + FAILURE + ", ElapsedTime in milli seconds : " + elapsedTime, t);
        }
    }

    /**
     * To verify the Environment configuration
     */
    public void verifyEnvironmentConfiguration() {

        startTime = System.currentTimeMillis();

        try {

            //get the required deployment configurations
            StringBuilder sb = new StringBuilder().append("ApplicationId:")
                    .append(ConfigurationManager.getDeploymentContext().getApplicationId())
                    .append("--DeploymentEnvironment:")
                    .append(ConfigurationManager.getDeploymentContext().getDeploymentEnvironment())
                    .append("--DeploymentDatacenter:")
                    .append(ConfigurationManager.getDeploymentContext().getDeploymentDatacenter())
                    .append("--DeploymentServerId:")
                    .append(ConfigurationManager.getDeploymentContext().getDeploymentServerId())
                    .append("--DeploymentStack:")
                    .append(ConfigurationManager.getDeploymentContext().getDeploymentStack());

            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_ENVIRONMENT, SUCCESS, sb.toString(), elapsedTime);
            logger.debug("Component: " + COMPONENT_NAME_ENVIRONMENT + ", Status : " + SUCCESS + ", ElapsedTime in milli seconds : " + elapsedTime);
        } catch (Throwable t) {

            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_ENVIRONMENT, FAILURE, t.getMessage(), elapsedTime);
            logger.error("Component: " + COMPONENT_NAME_ENVIRONMENT + ", Status : " + FAILURE + ", ElapsedTime in milli seconds : " + elapsedTime, t);
        }

    }

    /**
     * To verify the Version configuration
     */
    public void verifyVersionConfiguration() {

        startTime = System.currentTimeMillis();

        try {

            Version version = new Version();
            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_VERSION, SUCCESS, version.getBuildId() + "::" + version.getVersionId(), elapsedTime);
            logger.debug("Component: " + COMPONENT_NAME_VERSION + ", Status : " + SUCCESS + ", ElapsedTime in milli seconds : " + elapsedTime);
        } catch (Throwable t) {

            endTime = System.currentTimeMillis();
            elapsedTime = calculateElapsedTime(startTime, endTime);
            constructComponentVerificationDTO(COMPONENT_NAME_VERSION, FAILURE, t.getMessage(), elapsedTime);
            logger.error("Component: " + COMPONENT_NAME_VERSION + ", Status : " + FAILURE + ", ElapsedTime in milli seconds : " + elapsedTime, t);
        }
    }

    /**
     * TODO : To verify the Queue configuration
     */
    public void verifyQueueConfiguration() {

        constructComponentVerificationDTO(COMPONENT_NAME_QUEUE, SUCCESS, "Not yet implemented", 0);
    }

    /**
     * This method is used to construct the Component Verification DTO
     *
     * @param name         - name of the Component
     * @param status       - status of the Component
     * @param errorMessage - errorMessage in case of status failure
     */
    private void constructComponentVerificationDTO(String name, String status, String errorMessage, long elapsedTime) {

        // Constructing DTO
        ComponentVerificationDTO entityResponse = new ComponentVerificationDTO();
        entityResponse.setName(name);
        entityResponse.setStatus(status);
        entityResponse.setMessage(errorMessage);
        entityResponse.setElapsedTime(elapsedTime);
        if (null != entityResponse) {
            componentsList.add(entityResponse);
        }
    }

    /**
     * This method is used to calculate the Elapsed time for each check
     *
     * @param startTime - start time of the process
     * @param endTime   - end time of the process
     */
    private long calculateElapsedTime(long startTime, long endTime) {

        logger.debug("start time - {}", startTime);
        logger.debug("end time - {}", endTime);
        Long elapsedTime = endTime - startTime;
        return elapsedTime;

    }

    /**
     * This method is used to verify the thread status.
     */
    public void verifyThreadStatus() {

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long threadIds[] = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, 0);
        int threadsNew = 0;
        int threadsRunnable = 0;
        int threadsBlocked = 0;
        int threadsWaiting = 0;
        int threadsTimedWaiting = 0;
        int threadsTerminated = 0;

        List<String> runnable = new ArrayList<String>();
        List<String> newThread = new ArrayList<String>();
        List<String> blockedThread = new ArrayList<String>();
        List<String> waitingThread = new ArrayList<String>();
        List<String> timedWaitingThread = new ArrayList<String>();
        List<String> terminatedThread = new ArrayList<String>();

        for (ThreadInfo threadInfo : threadInfos) {

            if (threadInfo == null) {
                continue;
            }

            Thread.State state = threadInfo.getThreadState();
            if (state == State.NEW) {
                threadsNew++;
                newThread.add(threadInfo.getThreadName());
            } else if (state == State.RUNNABLE) {
                threadsRunnable++;
                runnable.add(threadInfo.getThreadName());
            } else if (state == State.BLOCKED) {
                threadsBlocked++;
                blockedThread.add(threadInfo.getThreadName());
            } else if (state == State.WAITING) {
                threadsWaiting++;
                waitingThread.add(threadInfo.getThreadName());
            } else if (state == State.TIMED_WAITING) {
                threadsTimedWaiting++;
                timedWaitingThread.add(threadInfo.getThreadName());
            } else if (state == State.TERMINATED) {
                threadsTerminated++;
                terminatedThread.add(threadInfo.getThreadName());
            }
        }

        String lineSeparator = ",";
        StringBuilder threadStatusInfo = new StringBuilder();
        threadStatusInfo.append("NEW :").append(threadsNew).append(lineSeparator);
        threadStatusInfo.append("RUNNABLE :").append(threadsRunnable).append(lineSeparator);
        threadStatusInfo.append("BLOCKED :").append(threadsBlocked).append(lineSeparator);
        threadStatusInfo.append("WAITING :").append(threadsWaiting).append(lineSeparator);
        threadStatusInfo.append("TIMEDWAITIMG :").append(threadsTimedWaiting).append(lineSeparator);
        threadStatusInfo.append("TERMINATED :").append(threadsTerminated).append(lineSeparator);

        threadStatusInfo.append("RUNNABLE Threads:");
        for (String name : runnable) {
            threadStatusInfo.append(name).append(lineSeparator);
        }
        threadStatusInfo.append("NEW Threads:");
        for (String name : newThread) {
            threadStatusInfo.append(name).append(lineSeparator);
        }
        threadStatusInfo.append("BLOCKED Threads :");
        for (String name : blockedThread) {
            threadStatusInfo.append(name).append(lineSeparator);
        }

        threadStatusInfo.append("WAITING Threads:");
        for (String name : waitingThread) {
            threadStatusInfo.append(name).append(lineSeparator);
        }

        threadStatusInfo.append("TIMEDWAITIMG Threads:");
        for (String name : timedWaitingThread) {
            threadStatusInfo.append(name).append(lineSeparator);
        }

        threadStatusInfo.append("TERMINATED Threads:");
        for (String name : terminatedThread) {
            threadStatusInfo.append(name).append(lineSeparator);
        }

        ComponentVerificationDTO entityResponse = new ComponentVerificationDTO();
        entityResponse.setName(COMPONENT_NAME_THREADS);
        entityResponse.setStatus(SUCCESS);
        entityResponse.setMessage(threadStatusInfo.toString());
        if (null != entityResponse) {
            componentsList.add(entityResponse);
        }
    }

}
