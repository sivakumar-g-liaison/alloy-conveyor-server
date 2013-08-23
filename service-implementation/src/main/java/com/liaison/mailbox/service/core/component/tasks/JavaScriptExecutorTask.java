/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.component.tasks;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.service.util.JavaScriptExecutor;

/**
 * JavaScriptExecutorTask
 * 
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */

public class JavaScriptExecutorTask implements Task<List<List<java.nio.file.Path>>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptExecutorTask.class);
	
	private List<java.nio.file.Path> files;
	private List<List<Path>> response = null;
	
	public JavaScriptExecutorTask(List<java.nio.file.Path> files) {
		this.files = files;
	}
	@Override
	public void run() {
		process();
		
	}
	
	/**
     *  Create file groups calling Java script - This will always be called for now .After DB integration 
     *  will be optionally called based on the DB configurations. The grouping right now is based on file name patterns. 
     *  This is just to prove the files can be grouped into logical work unit. 
     */
	private void process() {	

        try {
        	
            JavaScriptExecutor jsexecutor = new JavaScriptExecutor();            
            List<List<java.nio.file.Path>> fileGroups = jsexecutor.groupFiles(files); 
            response = fileGroups;
        } catch (Exception e) {
            LOGGER.error("Error in directory sweeping.", e);
        }
	}
	
	@Override
	public List<List<Path>> getResponse() {
		// TODO Auto-generated method stub
		return response;
	}
}
