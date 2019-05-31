/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.dtdm.model.ProcessorProperty;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;

@JsonRootName("sweeperEventRequest")
public class SweeperEventRequestDTO {

    private File file;
    private SweeperStaticPropertiesDTO staticProp;
    private Processor processor;
    private String mailBoxId;
    private Map<String, String> ttlMap;
    private Set<ProcessorProperty> dynamicProperties;
    
    @SuppressWarnings("unused")
    public SweeperEventRequestDTO() {
        super();
    }
    
    public SweeperEventRequestDTO(File file, SweeperStaticPropertiesDTO staticProp) {
        this.file = file;
        this.staticProp = staticProp;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public SweeperStaticPropertiesDTO getStaticProp() {
        return staticProp;
    }

    public void setStaticProp(SweeperStaticPropertiesDTO staticProp) {
        this.staticProp = staticProp;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public String getMailBoxId() {
        return mailBoxId;
    }

    public void setMailBoxId(String mailBoxId) {
        this.mailBoxId = mailBoxId;
    }

    public Map<String, String> getTtlMap() {
        return ttlMap;
    }

    public void setTtlMap(Map<String, String> ttlMap) {
        this.ttlMap = ttlMap;
    }

    public Set<ProcessorProperty> getDynamicProperties() {
        return dynamicProperties;
    }

    public void setDynamicProperties(Set<ProcessorProperty> dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
    }
    
}
