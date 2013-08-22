/**
* This software is the confidential and proprietary information of
* Liaison Technologies, Inc. ("Confidential Information").  You shall
* not disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Liaison Technologies.
*/
package com.liaison.g2mailboxservice.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.liaison.service.g2mailboxservice.core.DirectorySweeper;
import com.liaison.service.g2mailboxservice.core.util.MailBoxSweeperConstants;
import com.liaison.service.g2mailboxservice.tasks.DirectorySweeperTaskHandler;

/**
 * DirectorySweeperTaskHandlerTest
 *  
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */
public class DirectorySweeperTaskHandlerTest {
    
    private DirectorySweeperTaskHandler handler;
    
    private String inbox;
    private String outbox;
    private String notExist;
    private String dummy;
    private String dummyFolder;
    
    private Path dir;
    private Path dirFileNotExist;
    private Path dirWithDirectory;
    private Path dummyDir;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	
	inbox = System.getProperty("java.io.tmpdir")+ File.separator + "INBOX";
	outbox = System.getProperty("java.io.tmpdir")+ File.separator + "OUTBOX";
	dummy = System.getProperty("java.io.tmpdir")+ File.separator + "DUMMY";
	dummyFolder = dummy + File.separator + "EMPTYFOLDER";
	notExist = System.getProperty("java.io.tmpdir")+ File.separator + "NOTEXIST";

	handler = new DirectorySweeperTaskHandler(inbox);
	
	dir = Paths.get(inbox);
	Files.createDirectory(dir);

	dirFileNotExist = Paths.get(outbox);
	Files.createDirectory(dirFileNotExist);
	
	dummyDir = Paths.get(dummy);
	Files.createDirectory(dummyDir);
	
	dirWithDirectory = Paths.get(dummyFolder);
	Files.createDirectory(dirWithDirectory);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
	
	Files.deleteIfExists(dir);
	Files.deleteIfExists(dirFileNotExist);
	Files.deleteIfExists(dirWithDirectory);
	Files.deleteIfExists(dummyDir);
    }

    /**
     * Test method for {@link com.liaison.g2mailboxservice.core.DirectorySweeper#sweepDirectory(java.lang.String, boolean, boolean, com.liaison.g2mailboxservice.core.dto.SweepConditions)}.
     * @throws IOException 
     */
    @Test
    public void testSweepDirectory() throws IOException {

	Path target = Paths.get(inbox + File.separator + "Purchase.txt");
	Files.createFile(target);
	handler = new DirectorySweeperTaskHandler(inbox);
	handler.run();
	Files.delete(target);
	Assert.assertEquals(true, (handler.getResponse().length() > 15));
    }

    /**
     * @throws IOException
     */
    @Test
    public void testSweepDirectory_FileAvailable_ShouldReturnPath() throws IOException {
	
	Path target = Paths.get(inbox + File.separator + "Purchase.txt");
	Files.createFile(target);
	handler = new DirectorySweeperTaskHandler(inbox);
	handler.run();
	Files.delete(target);
	Assert.assertNotNull(handler.getResponse());
    }

    /**
     * @throws IOException
     */
    @Test
    public void testSweepDirectyory_EmptyDir_ShouldEmpty() throws IOException {
    	
	handler = new DirectorySweeperTaskHandler(inbox);
	handler.run();
	Assert.assertEquals(true, (handler.getResponse().length() == 15));
    }
    /**
     * @throws IOException
     */
    @Test
    public void testSweepDirectyory_DirOnlyExists_ShouldNotEmpty()
	    throws IOException {

    	handler = new DirectorySweeperTaskHandler(dummy);
    	handler.run();
    	Assert.assertEquals(false, (handler.getResponse().length() > 15));
    }
}
