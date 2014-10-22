/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.framework.fs2;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.activation.DataSource;

import com.liaison.fs2.api.FlexibleStorageSystem;
import com.liaison.fs2.api.exceptions.FS2PayloadNotFoundException;

/**
 * DataSource implementation backed by FS2.
 *
 * @author Suk Robert Koh (rkoh@liaison.com)
 */
public class FS2DataSource implements DataSource {

    /** TODO: couldn't figure out a way to get fs2 injected here */
//    @Inject
//    @Named("spectrum")
    private FlexibleStorageSystem fs2;

    private URI uri;
    private String name = "";
    private String contentType;

    public FS2DataSource(FlexibleStorageSystem fs2, URI uri, String contentType) {
        this.fs2 = fs2;
        this.uri = uri;
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() {
        try {
            return fs2.getFS2PayloadInputStream(uri);
        } catch (FS2PayloadNotFoundException e) {
            throw new RuntimeException("Payload requested for FS2DataSource does not exist at URI = " + uri, e);
        }
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException("Cannot write to FS2DataSource");
    }
}
