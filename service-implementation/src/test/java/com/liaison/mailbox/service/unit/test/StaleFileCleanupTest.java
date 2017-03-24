/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.unit.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.liaison.framework.util.ServiceUtils;
import com.liaison.mailbox.dtdm.model.Sweeper;
import com.liaison.mailbox.service.base.test.BaseServiceTest;
import com.liaison.mailbox.service.base.test.InitInitialDualDBContext;
import com.liaison.mailbox.service.core.processor.DirectorySweeper;
import com.liaison.mailbox.dtdm.model.Processor;

public class StaleFileCleanupTest  extends BaseServiceTest {
	
	private String payloadLocation;
    private Path payloadPath;
	private static String PAYLOAD = "PAYLOAD";
	private static String TMP_DIR = "java.io.tmpdir";
	
	   /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
    	
        System.setProperty("com.liaison.mailbox.sweeper.stalefile.ttl", "30");
        InitInitialDualDBContext.init();
        payloadLocation = System.getProperty(TMP_DIR) + File.separator + PAYLOAD;
        payloadPath = Paths.get(payloadLocation);
        if (payloadPath.toFile().exists()) {
            FileUtils.forceDelete(payloadPath.toFile());
        }
		Files.createDirectory(payloadPath);
    }
    
    @Test
    public void testStaleFileCleanup() throws IOException {
    	
        Processor sweeper = new Sweeper();
        sweeper.setProcsrName("TestSweeper");
        sweeper.setProcsrStatus("ACTIVE");
        sweeper.setProcsrProperties(ServiceUtils.readFileFromClassPath("requests/processor/sweeper.json"));
        DirectorySweeper sweeperProcsr = new DirectorySweeper(sweeper);
        
        String fileName = payloadLocation + File.separator + "testFile";
        Path filePath = Paths.get(fileName);
        Files.createFile(filePath);
        File file = filePath.toFile();
        
        // Case 1 no stale files in location 
        List<String> staleFiles = new ArrayList<>();
        sweeperProcsr.deleteStaleFiles(payloadPath, staleFiles);
        // As the file got created just now it should not be considered for deletion by stale file cleanu API
        Assert.assertTrue((staleFiles.isEmpty()));
        
        // Case 2 stale files present in the given location
        file.setLastModified(getLastModifiedTime(true));
        sweeperProcsr.deleteStaleFiles(payloadPath, staleFiles);
        Assert.assertTrue(staleFiles.size() == 1);
        Assert.assertEquals(staleFiles.get(0), file.getName());
        
        // case 3 no files present in the payload location
        staleFiles = new ArrayList<>();
        sweeperProcsr.deleteStaleFiles(payloadPath, staleFiles);
        // As there are no files present, the staleFiles size should be zero
        Assert.assertTrue((staleFiles.size() == 0));
        
        //case 4 payload location contains only folders
        String folderPath = payloadLocation + File.separator + "folder1" + File.separator + "folder2";
        Files.createDirectories(Paths.get(folderPath));
        sweeperProcsr.deleteStaleFiles(payloadPath, staleFiles);
        // As there are no files and only folders, the staleFiles size should be zero
        Assert.assertTrue((staleFiles.size() == 0));
        
        Files.createFile(filePath);
        // Case 5 files and folders present in the given location but files did not expire
        file.setLastModified(getLastModifiedTime(false));
        sweeperProcsr.deleteStaleFiles(payloadPath, staleFiles);
        Assert.assertTrue(staleFiles.size() == 0);
    }
    
    /**
     * Method to get last modified time for the created file
     * we will set the modified time as twice as the value of ttl before or after current time
     * based on the value of the parameter beforecurrentTime
     * 
     * @param beforeCurrentTime
     * @return
     */
    private long getLastModifiedTime(boolean beforeCurrentTime) {
    	
    	Calendar cal = Calendar.getInstance();
    	int ttl = Integer.parseInt(System.getProperty("com.liaison.mailbox.sweeper.stalefile.ttl"));
    	int modifier = (beforeCurrentTime) ? -2 * ttl : 2 * ttl;
    	cal.add(Calendar.DATE, modifier);
    	return cal.getTimeInMillis();    	
    }
    
    /**
     * Method to delete any files present inside the payload location
     * 
     * @throws Exception
     */
    @AfterMethod
    public void cleanUp() throws Exception {
    	
    	File payloadDir = Paths.get(payloadLocation).toFile();
    	try {
    		if (payloadDir.exists()) {
        		FileUtils.forceDelete(payloadDir);
    		}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

}
