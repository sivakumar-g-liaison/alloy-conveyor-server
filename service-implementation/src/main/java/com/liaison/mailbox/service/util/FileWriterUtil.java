/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.storage.util.StorageUtilities;

/**
 * Utility for file operations
 */
public class FileWriterUtil {

    private static final Logger LOG = LogManager.getLogger(FileWriterUtil.class);

    /**
     * Writes the trigger file
     * 
     * @param triggerFilePath
     * @param fileName
     * @param messageUri
     * @throws IOException
     */
    public static void writeTriggerFile(String triggerFilePath, String fileName, String messageUri) throws IOException {

        String path = triggerFilePath + File.separatorChar + fileName;
        File triggerFile = new File(path);

        // write the trigger file
        if (StorageUtilities.getPayloadSize(messageUri) == 0) {

            if (triggerFile.exists()) {
                boolean deleted = triggerFile.delete();
                LOG.info("deleted the existing file {}", deleted);
            }
            boolean created = triggerFile.createNewFile();
            LOG.debug("created the trigger file {} and the path is {}", created, triggerFilePath);
        } else {

            InputStream triggerFilePayload = null;
            FileOutputStream outputStream = null;
            try {
                triggerFilePayload = StorageUtilities.retrievePayload(messageUri);
                outputStream = new FileOutputStream(triggerFile);
                IOUtils.copy(triggerFilePayload, outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {

                if (outputStream != null) {
                    outputStream.close();
                }
                if (triggerFilePayload != null) {
                    triggerFilePayload.close();
                }
            }
        }
    }
}
