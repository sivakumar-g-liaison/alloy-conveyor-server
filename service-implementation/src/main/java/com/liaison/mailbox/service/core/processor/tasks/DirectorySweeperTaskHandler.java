/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor.tasks;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DirectorySweeperTaskHandler
 *  
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */
public class DirectorySweeperTaskHandler implements Task<String>{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeperTaskHandler.class);
	
	private String response = null;
	private String path = null;
	
	public DirectorySweeperTaskHandler(String path) {
		this.path = path;
	}
	
	private void processTask() {
		
		// Directory Sweeper task start
		Task<List<java.nio.file.Path>> sweepTask = new SweepDirectoryTask(path);
		sweepTask.run();
		List<Path> sweepedFiles = sweepTask.getResponse();		 
		
		
		// JavaScriptExecutor task start
		Task<List<List<java.nio.file.Path>>> jsTask = new JavaScriptExecutorTask(sweepedFiles);
		jsTask.run();
		 List<List<Path>> fileGroups = jsTask.getResponse();
		
		
		// Meta data extraction
		 Task<String> metaDataTask = new MetaDataExtractorTask(fileGroups);
		 metaDataTask.run();
		 String metaData = metaDataTask.getResponse();
		
		
		// File mark task start
		Task<Boolean>fileMarkerTask = new FileMarkerTask(sweepedFiles);
		fileMarkerTask.run();
		// File mark task end
		
		response = metaData;
	}

	@Override
	public void run() {
		processTask();
	}

	@Override
	public String getResponse() {
		// TODO Auto-generated method stub
		return response;
	}		
}
