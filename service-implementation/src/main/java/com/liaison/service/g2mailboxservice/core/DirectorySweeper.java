/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.service.g2mailboxservice.core;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liaison.service.g2mailboxservice.core.dto.SweepConditions;
import com.liaison.service.g2mailboxservice.core.util.MailBoxSweeperConstants;

/**
 * DirectorySweeper
 *
 * <P>DirectorySweeper sweeps the files from mail box and creates meta data about file and post it to the queue.
 *
 * @author veerasamyn
 */

public class DirectorySweeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectorySweeper.class);

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

            Path targetDirectory =  file.getParent().resolve(MailBoxSweeperConstants.SWEEPED_FOLDER_NAME);
            if(!Files.exists(targetDirectory)){
                LOGGER.info("Creating 'sweeped' folder");
                Files.createDirectories(targetDirectory);
            }
            target = targetDirectory.resolve(file.getFileName() + MailBoxSweeperConstants.SWEEPED_FILE_EXTN);
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

}
