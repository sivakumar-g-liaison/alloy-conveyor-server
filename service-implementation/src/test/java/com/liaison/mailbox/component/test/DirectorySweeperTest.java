/**
* This software is the confidential and proprietary information of
* Liaison Technologies, Inc. ("Confidential Information").  You shall
* not disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Liaison Technologies.
*/

package com.liaison.mailbox.component.test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.liaison.mailbox.service.core.component.DirectorySweeper;
import com.liaison.mailbox.service.util.MailBoxSweeperConstants;

/**
 * DirectorySweeperTest.java
 *
 *
 * @author veerasamyn
 */
public class DirectorySweeperTest {
    
    private DirectorySweeper sweeper;
    
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

	sweeper = new DirectorySweeper();
	
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

	List<Path> path = sweeper.sweepDirectory(inbox, false, false, null);
	Files.delete(target);
	Assert.assertNotNull(path.get(0));
    }

    /**
     * @throws IOException
     */
    @Test
    public void testSweepDirectory_FileAvailable_ShouldReturnPath() throws IOException {
	
	Path target = Paths.get(inbox + File.separator + "Purchase.txt");
	Files.createFile(target);

	List<Path> path = sweeper.sweepDirectory(inbox, false, false, null);
	Files.delete(target);
	Assert.assertNotNull(path.get(0));
    }

    /**
     * @throws IOException
     */
    @Test
    public void testSweepDirectyory_EmptyDir_ShouldEmpty() throws IOException {

	List<Path> path = sweeper.sweepDirectory(outbox, false, false, null);
	Assert.assertEquals(true, path.isEmpty());
    }
    
    /**
     * @throws IOException
     */
    @Test(expected = NoSuchFileException.class)
    public void testsweepDirectyory_DirDoesNotExist_ShouldThrowException()
	    throws IOException {

	sweeper.sweepDirectory(notExist, false, false, null);
    }
    
    /**
     * @throws IOException
     */
    @Test
    public void testSweepDirectyory_DirOnlyExists_ShouldNotEmpty()
	    throws IOException {

	List<Path> path = sweeper.sweepDirectory(dummy, false, true, null);
	Assert.assertEquals(false, path.isEmpty());
    }
    
    /**
     * @throws IOException
     */
    @Test
    public void testSweepDirectyory_DirOnlyExists_ShouldThrowException()
	    throws IOException {

	List<Path> path = sweeper.sweepDirectory(dummy, false, false, null);
	Assert.assertEquals(true, path.isEmpty());

    }

    /**
     * @throws IOException
     * @throws JSONException 
     */
    @Test(expected=NullPointerException.class)
    public void testMarkAsSweeped_Null_ShouldThrowException() throws IOException, JSONException {
	
	sweeper.markAsSweeped(null);
    }

    /**
     * Test method for {@link com.liaison.g2mailboxservice.core.DirectorySweeper#markAsSweeped(java.util.List)}.
     * @throws IOException 
     * @throws JSONException 
     */
    @Test
    public void testMarkAsSweeped() throws IOException, JSONException {
	
	Path target = Paths.get(dir+ File.separator + "Invoice.txt");
	Files.createFile(target);
	
	List<Path> path = sweeper.sweepDirectory(inbox, false, false, null);
	sweeper.markAsSweeped(path);
	
	String rootPath = inbox + File.separator + MailBoxSweeperConstants.SWEEPED_FOLDER_NAME;
	List<Path> sweepedFiles = sweeper.sweepDirectory(rootPath, false, false, null);
	
	Files.delete(sweepedFiles.get(0).toAbsolutePath());
	Files.delete(Paths.get(rootPath));
	
	Assert.assertNotNull(sweepedFiles.get(0));
    }

}
