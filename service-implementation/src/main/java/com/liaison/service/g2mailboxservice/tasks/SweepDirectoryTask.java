/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.service.g2mailboxservice.tasks;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.service.g2mailboxservice.core.dto.SweepConditions;

/**
 * SweepDirectoryTask
 * 
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */

public class SweepDirectoryTask implements Task<List<java.nio.file.Path>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SweepDirectoryTask.class);
	private String path = null;
	private List<java.nio.file.Path> response = null;
	
	public SweepDirectoryTask(String path) {
		this.path = path;
	}
	
	@Override
	public void run() {
		process();
		
	}
	
	private void process() {	

        try {
        	
           response = sweepDirectory(path,false, false, null);
           
        } catch (IOException e) {
            LOGGER.error("Error in directory sweeping.", e);
        }catch (Exception e) {
            LOGGER.error("Error in directory sweeping.", e);
        }
	}
	
    /**
     * Method is used to retrieve the files from the given mailbox.
     *
     * @return List<Path>
     * @throws IOException 
     */
    public  List<Path> sweepDirectory(String root, boolean includeSubDir, boolean listDirectoryOnly,SweepConditions sweepConditions) throws IOException {

        long startTime = System.currentTimeMillis();
        List<Path> result = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(root), defineFilter(listDirectoryOnly))) {
            for (Path entry : stream) {
                result.add(entry);
            }
        } catch (IOException e) {
            throw e;
        }

        long endTime = System.currentTimeMillis();
        LOGGER.info("Total Time Taken : " + (endTime - startTime));

        return result;

    }

    /**
     * Creates a filter for directories only.
     *
     * @return Object which implements DirectoryStream.Filter interface and that
     *         accepts directories only.
     */
    public  DirectoryStream.Filter<Path> defineFilter(
            final boolean listDirectoryOnly) {

        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

            @Override
            public boolean accept(Path entry) throws IOException {

                return listDirectoryOnly
                        ? Files.isDirectory(entry)
                                : Files.isRegularFile(entry);
            }
        };

        return filter;
    }

	@Override
	public List<Path> getResponse() {
		return response;
	}

	
    
	
}
