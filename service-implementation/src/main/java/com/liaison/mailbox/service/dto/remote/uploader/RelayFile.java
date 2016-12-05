/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.remote.uploader;

import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.storage.util.StorageUtilities;

import java.io.File;
import java.io.InputStream;

/**
 * Created by VNagarajan on 11/22/2016.
 */
public class RelayFile {

    private File file;
    private InputStream payloadInputStream;
    private String name;
    private String payloadUri;
    private String globalProcessId;
    private String processorId;
    private long size;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFile(String filePath) {
        this.file = new File(filePath);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getPayloadInputStream() {

        if (null != payloadUri) {
            return StorageUtilities.retrievePayload(payloadUri);
        }

        return null;
    }

    public String getPayloadUri() {
        return payloadUri;
    }

    public void setPayloadUri(String payloadUri) {
        this.payloadUri = payloadUri;
    }

    public String getGlobalProcessId() {
        return globalProcessId;
    }

    public void setGlobalProcessId(String globalProcessId) {
        this.globalProcessId = globalProcessId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getParent() {
        return this.file.getParent();
    }

    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }

    public long length() {
        return this.size;
    }

    public void copy(StagedFile stagedFile) {

        this.setGlobalProcessId(stagedFile.getGlobalProcessId());
        this.setProcessorId(stagedFile.getProcessorId());
        this.setName(stagedFile.getFileName());
        this.setPayloadUri(stagedFile.getSpectrumUri());
        this.setFile(new File(stagedFile.getFilePath() + File.separatorChar + stagedFile.getFileName()));
        this.setSize(Long.parseLong(stagedFile.getFileSize()));
    }
}
