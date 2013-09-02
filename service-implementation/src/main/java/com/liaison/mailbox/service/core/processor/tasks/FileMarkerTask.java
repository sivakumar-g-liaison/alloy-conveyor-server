/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.core.processor.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.mailbox.MailBoxConstants;

/**
 * FileMarkerTask
 * 
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */

public class FileMarkerTask implements Task<Boolean> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileMarkerTask.class);
	
	private List<java.nio.file.Path> files;
	private Boolean taskStatus = false;
	
	public FileMarkerTask(List<java.nio.file.Path> files) {
		this.files = files;
	}
	@Override
	public void run() {
		process();
		
	}
	
	private void process() {	

        try {
        	
            List<java.nio.file.Path> sweepList = new ArrayList<>(files); 
            markAsSweeped(sweepList);
            taskStatus = true;
        } catch (IOException e) {
            LOGGER.error("Error in directory sweeping.", e);
        }catch (Exception e) {
            LOGGER.error("Error in directory sweeping.", e);
        }
	}

    /**
     * Method is used to build the json meta data for file groups.Once it extracts the meta data , it renames the files and moves the files to a queueed folder
     *
     * @param fileList Files list.
     * @return String which contains JSON string of the give file groups
     * @throws IOException
     * @throws JSONException
     */
    public void markAsSweeped(List<Path> fileList) throws IOException,
    JSONException {
       
        Path target = null;  
        for (Path file : fileList) {          

            Path targetDirectory =  file.getParent().resolve(MailBoxConstants.SWEEPED_FOLDER_NAME);
            if(!Files.exists(targetDirectory)){
                LOGGER.info("Creating 'sweeped' folder");
                Files.createDirectories(targetDirectory);
            }
            target = targetDirectory.resolve(file.getFileName() + MailBoxConstants.SWEEPED_FILE_EXTN);
            //Adding .queued extension and moving to sweeped folder
            move(file, target);
        }
       
    }

    /**
     * Method is used to move the file to the sweeped folder.
     *
     * @param file The source location
     * @param target The target location
     * @throws IOException
     */
    private void move(Path file, Path target) throws IOException {

        Files.move(file, target, StandardCopyOption.ATOMIC_MOVE);
    }
	@Override
	public  Boolean getResponse() {
		// TODO Auto-generated method stub
		return taskStatus;
	}
}
