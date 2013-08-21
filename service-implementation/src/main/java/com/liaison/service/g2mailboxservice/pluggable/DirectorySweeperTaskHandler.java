/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.service.g2mailboxservice.pluggable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DirectorySweeperTaskHandler
 *  
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */
public class DirectorySweeperTaskHandler implements Task{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeperTaskHandler.class);
	
	private Object response = null;
	private String path = null;
	
	public DirectorySweeperTaskHandler(String path) {
		this.path = path;
	}
	
	private void processTask() {
		
		// Directory Sweeper task start
		SweepDirectoryTask sweepTask = new SweepDirectoryTask(path);
		sweepTask.run();
		Object sweepResponse = sweepTask.getResponse();
		List<java.nio.file.Path> files = (List<java.nio.file.Path>) sweepResponse;
		// Directory Sweeper task end
		
		// JavaScriptExecutor task start
		JavaScriptExecutorTask jsTask = new JavaScriptExecutorTask(files);
		jsTask.run();
		Object jsResponse = jsTask.getResponse();
		List<List<java.nio.file.Path>> fileGroups = (List<List<java.nio.file.Path>>) jsResponse;
		// JavaScriptExecutor task end
		
		// Meta Info task start
		MetaInfoCreatorTask metaInfoTask = new MetaInfoCreatorTask(fileGroups);
		metaInfoTask.run();
		Object metaInfoResponse = metaInfoTask.getResponse();
		// Meta Info task end
		
		// File mark task start
		FileMarkerTask fileMarkerTask = new FileMarkerTask(files);
		fileMarkerTask.run();
		// File mark task end
		
		response = metaInfoResponse;
	}

	@Override
	public void run() {
		processTask();
	}

	@Override
	public Object getResponse() {
		// TODO Auto-generated method stub
		return response;
	}		
}
