/*
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
function init(processor, logger) {
    logger.info("Javascript executed via custom JS engine");
    var javaImports = new JavaImporter(
		Packages.com.google.api.client.json.jackson2,
        Packages.org.apache.commons.codec.binary,
		Packages.org.codehaus.jackson.map);
	with(javaImports) {
        logger.info("Displaying List of Remote Processor Properties");
        var remoteProcessorProperties = processor.getRemoteProcessorProperties();
        logger.info("url " +remoteProcessorProperties.getUrl());
        logger.info("HTTP Version " +remoteProcessorProperties.getHttpVersion());
        logger.info("HTTP Verb " +remoteProcessorProperties.getHttpVerb());
        logger.info("Retry Attempts " +remoteProcessorProperties.getRetryAttempts());
        logger.info("Socket Timeout " +remoteProcessorProperties.getSocketTimeout());
        logger.info("Connection Timeout " +remoteProcessorProperties.getConnectionTimeout());
        logger.info("Chunked Encoding " +remoteProcessorProperties.isChunkedEncoding());
        logger.info("Content Type " +remoteProcessorProperties.getContentType());
        logger.info("Encoding Format " +remoteProcessorProperties.getEncodingFormat());
        logger.info("Passive " +remoteProcessorProperties.isPassive());
        logger.info("Binary " +remoteProcessorProperties.isBinary());
        logger.info("Retry Interval " +remoteProcessorProperties.getRetryInterval());
        logger.info("Pipeline ID " +remoteProcessorProperties.getPipeLineID());
        logger.info("Displaying Response location" + processor.getWriteResponseURI());
        logger.info("OtherRequest Header " + remoteProcessorProperties.getOtherRequestHeader());
        
        logger.info("Displaying List of Dynamic Processor Properties");
        var properties = processor.getDynamicProperties();
        logger.info("Processed File Location "+ properties.getProperty("processedfilelocation"));
        logger.info("File Rename Format "+ properties.getProperty("filerenameformat"));
        logger.info("Sweeped File Location "+ properties.getProperty("sweepedfilelocation"));
        logger.info("Error File Location "+ properties.getProperty("errorfilelocation"));
              
     }
    
}