package com.liaison.mailbox.service.util;

import com.liaison.common.log4j2.markers.GlassMessageMarkers;
import com.liaison.commons.message.glass.dom.TimeStamp;
import com.liaison.commons.message.glass.dom.TimeStampAPI;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class TimestampUtil {

    //TimestampLogger
    public static final String DEFAULT_FIRST_CORNER_NAME = "FIRST CORNER";
    public static final String PROPERTY_FIRST_CORNER_NAME = "com.liaison.firstcorner.name";
    public static final String DEFAULT_SECOND_CORNER_NAME = "SECOND CORNER";
    public static final String PROPERTY_SECOND_CORNER_NAME = "com.liaison.secondcorner.name";
    public static final String DEFAULT_THIRD_CORNER_NAME = "THIRD CORNER";
    public static final String PROPERTY_THIRD_CORNER_NAME = "com.liaison.thirdcorner.name";
    public static final String DEFAULT_FOURTH_CORNER_NAME = "FOURTH CORNER";
    public static final String PROPERTY_FOURTH_CORNER_NAME = "com.liaison.fourthcorner.name";

    private transient ExecutionTimestamp firstCornerTimestamp;
    private transient ExecutionTimestamp thirdCornerTimestamp;

    private static final Logger logger = LogManager.getLogger(TimestampUtil.class);

    DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();

    private String globalProcessID;
    private String processID;
    private String pipelineID;

    public TimestampUtil(String globalProcessId, String pipelineId, String processId) {
        this.globalProcessID = globalProcessId;
        this.processID = processId;
        this.pipelineID = pipelineId;
    }

    public String getGlobalProcessID() {
        return globalProcessID;
    }

    public String getPipelineProcessID() {
        return pipelineID;
    }

    public String getProcessID() {
        return processID;
    }

    public static void logTimestamp(Logger logger, String message, Object... objects) {
        if (logger != null) {
            logger.info(String.format("[TIME] %s | %s", new Date(System.currentTimeMillis()), String.format(message, objects)));
        }
    }

    public void logFirstCornerTimestamp() {
        firstCornerTimestamp = logBeginTimestamp(config.getString(PROPERTY_FIRST_CORNER_NAME, DEFAULT_FIRST_CORNER_NAME));
    }

    public void logSecondCornerTimestamp() {
        logEndTimestamp(config.getString(DEFAULT_SECOND_CORNER_NAME, PROPERTY_SECOND_CORNER_NAME));
    }

    public void logThirdCrnerTimestamp() {
        thirdCornerTimestamp = logBeginTimestamp(config.getString(DEFAULT_THIRD_CORNER_NAME, PROPERTY_THIRD_CORNER_NAME));
    }

    public void logFourthCornerTimestamp() {
        logEndTimestamp(config.getString(PROPERTY_FOURTH_CORNER_NAME, DEFAULT_FOURTH_CORNER_NAME));
    }

    public ExecutionTimestamp logBeginTimestamp(String name) {
        ExecutionTimestamp timeStamp = ExecutionTimestamp.beginTimestamp(name);
        logTimeStamp(timeStamp);
        return timeStamp;
    }

    public void logEndTimestamp(String name, String sessionId) {
        ExecutionTimestamp timeStamp = ExecutionTimestamp.endTimestamp(name,sessionId);
        logTimeStamp(timeStamp);
    }

    public void logEndTimestamp(String name) {
        ExecutionTimestamp timeStamp = ExecutionTimestamp.endTimestamp(name);
        logTimeStamp(timeStamp);
    }

    private void logTimeStamp(ExecutionTimestamp timestamp) {
        logger.info(GlassMessageMarkers.GLASS_MESSAGE_MARKER, constructTimeStampAPI(timestamp));
    }

    private TimeStampAPI constructTimeStampAPI(ExecutionTimestamp timestamp) {
        return constructTimeStampAPI(ExecutionTimestamp.buildGlassTimeStamp(timestamp));
    }

    private TimeStampAPI constructTimeStampAPI(TimeStamp glassTimeStamp) {

        TimeStampAPI timeStampAPI = new TimeStampAPI();
        timeStampAPI.setProcessId(getProcessID());
        timeStampAPI.setGlobalId(getGlobalProcessID());
        timeStampAPI.setPipelineId(getPipelineProcessID());
        timeStampAPI.getTimeStamps().add(glassTimeStamp);

        return timeStampAPI;
    }

}
