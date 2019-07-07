/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.dto.remote.uploader;

import com.liaison.fs2.metadata.FS2MetaSnapshot;
import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.rtdm.model.StagedFile;
import com.liaison.mailbox.service.storage.util.StorageUtilities;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Wrapper class of staged file entity which reads the file from storage instead of disk
 */
public class RelayFile {

    private File file;
    private String name;
    private String payloadUri;
    private String globalProcessId;
    private String processorId;
    private long size;
    private String tenancyKey;

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

    public String getTenancyKey() {

        if (StringUtils.isEmpty(tenancyKey) && StringUtils.isNotEmpty(payloadUri)) {
            FS2MetaSnapshot metadata = StorageUtilities.getMetaData(payloadUri);
            return metadata.getHeader(MailBoxConstants.KEY_TENANCY_KEY)[0];
        }
        return tenancyKey;
    }

    public String getPayloadUri() {
        return payloadUri;
    }

    private void setPayloadUri(String payloadUri) {
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

    public void setTenancyKey(String tenancyKey) {
        this.tenancyKey = tenancyKey;
    }

    /**
     * api to delete the file written by filewriter
     *
     * @throws IOException io errors
     */
    public boolean delete() throws IOException {
        return Files.deleteIfExists(file.toPath());
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
