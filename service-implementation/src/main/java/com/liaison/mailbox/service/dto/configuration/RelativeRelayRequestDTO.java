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

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO;

@JsonRootName("relativeRelayRequest")
public class RelativeRelayRequestDTO {

    private File file;
    private SweeperStaticPropertiesDTO staticProp;
    private Processor processor;
    
    @SuppressWarnings("unused")
    public RelativeRelayRequestDTO() {
        super();
    }
    
    public RelativeRelayRequestDTO(File file, SweeperStaticPropertiesDTO staticProp) {
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
}
