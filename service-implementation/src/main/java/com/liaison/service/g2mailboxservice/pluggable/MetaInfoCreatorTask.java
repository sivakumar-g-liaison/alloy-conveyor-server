/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.service.g2mailboxservice.pluggable;

import java.io.IOException;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.service.g2mailboxservice.core.dto.MetaDataDTO;
import com.liaison.service.g2mailboxservice.core.util.MailBoxSweeperUtil;

/**
 * MetaInfoCreatorTask
 * 
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */

public class MetaInfoCreatorTask implements Task {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetaInfoCreatorTask.class);
	private List<List<java.nio.file.Path>> fileGroups;
	private Object response = null;
	
	public MetaInfoCreatorTask(List<List<java.nio.file.Path>> fileGroups) {
		this.fileGroups = fileGroups;
	}
	@Override
	public void run() {
		process();
		
	}
	
	private void process() {	

        try {
        	
            MetaDataDTO metaData = new MetaDataDTO(fileGroups);
            String jsonResponse = MailBoxSweeperUtil.convertObjectToJson(metaData); 
            response = jsonResponse;
            LOGGER.info("Returns json response.{}",new JSONObject(jsonResponse).toString(2));
        } catch (IOException e) {
            LOGGER.error("Error in directory sweeping.", e);
        }catch (Exception e) {
            LOGGER.error("Error in directory sweeping.", e);
        }
	}
	
	@Override
	public Object getResponse() {
		// TODO Auto-generated method stub
		return response;
	}
}
