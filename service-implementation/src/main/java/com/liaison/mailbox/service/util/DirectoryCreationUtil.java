/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.enums.Messages;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;

/**
 * To create local folders and assign permissions to the folders.
 */
public class DirectoryCreationUtil {

    private static final Logger LOGGER = LogManager.getLogger(DirectoryCreationUtil.class);
    
    private static final String INBOX = "inbox";
    private static final String OUTBOX = "outbox";
    private static final String FOLDER_PERMISSION = "rwxrwx---";
            
    /**
     * This Method create local folders if not available.
     *
     * * @param processorDTO it have details of processor
     *
     * @throws IOException
     */
    public static void createPathIfNotAvailable(String localPath) throws IOException {

        if (MailBoxUtil.isEmpty(localPath)) {
            LOGGER.debug("Given path is empty, so not creating folders..");
            return;
        }

        File fileDirectory = new File(localPath);
        if (fileDirectory.exists()) {
            LOGGER.debug("Not creating folders..");
            return;
        }

        Path filePathToCreate = fileDirectory.toPath();
        LOGGER.debug("Setting on to create - {}", filePathToCreate);
        FileSystem fileSystem = FileSystems.getDefault();
        PathMatcher pathMatcher = fileSystem.getPathMatcher(MailBoxUtil.DATA_FOLDER_PATTERN);
        if (!pathMatcher.matches(filePathToCreate)) {
            throw new MailBoxConfigurationServicesException(Messages.FOLDER_DOESNT_MATCH_PATTERN, MailBoxUtil.DATA_FOLDER_PATTERN.substring(5), Response.Status.BAD_REQUEST);
        }

        //check availability of /data/*/* folder
        if (!Files.exists(filePathToCreate.subpath(0, 3))) {
            throw new MailBoxConfigurationServicesException(Messages.HOME_FOLDER_DOESNT_EXIST_ALREADY, filePathToCreate.subpath(0, 3).toString(), Response.Status.BAD_REQUEST);
        }

        createFoldersAndAssignProperPermissionsV2(filePathToCreate);
    }

    /**
     * Method to create the given path and assign proper group and permissions to the created folders
     *
     * @param filePathToCreate - file Path which is to be created
     * @throws IOException
     */
    private static void createFoldersAndAssignProperPermissions(Path filePathToCreate) throws IOException {

        FileSystem fileSystem = FileSystems.getDefault();
        Files.createDirectories(filePathToCreate);
        LOGGER.debug("Folders {} created.Starting with Group change.", filePathToCreate);
        UserPrincipalLookupService lookupService = fileSystem.getUserPrincipalLookupService();
        String group = getGroupFor(filePathToCreate.getName(1).toString());
        LOGGER.debug("group  name - {}", group);
        GroupPrincipal fileGroup = lookupService.lookupPrincipalByGroupName(group);

        // skip when reaching inbox/outbox
        String pathToCreate = filePathToCreate.getFileName().toString();
        while (!(INBOX.equals(pathToCreate) ||
                OUTBOX.equals(pathToCreate))) {

            LOGGER.debug("setting the group of  {} to {}", filePathToCreate, group);
            Files.getFileAttributeView(filePathToCreate, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(fileGroup);
            Files.setPosixFilePermissions(filePathToCreate, PosixFilePermissions.fromString(FOLDER_PERMISSION));

            // if it is PROCESSED/ERROR Folder then skip assigning permissions to parent folders.
            if ((MailBoxConstants.PROCESSED_FOLDER.equals(pathToCreate) ||
                    MailBoxConstants.ERROR_FOLDER.equals(pathToCreate))) {
                LOGGER.debug("setting file permissions of PROCESSED/ERROR Folder is done. Skipping permission setting for parent folders as it is not needed.");
                break;
            }

            filePathToCreate = filePathToCreate.getParent();
            pathToCreate = filePathToCreate.getFileName().toString();
        }

        LOGGER.debug("Done setting group");
    }

    private static String getGroupFor(String protocol) {
        return MailBoxUtil.getEnvironmentProperties().getString(protocol + ".group.name");
    }

    /**
     * Method to create the given path and assign proper group and permissions to the created folders
     *
     * @param filePathToCreate - file Path which is to be created
     * @throws IOException
     */
    public static void createFoldersAndAssignProperPermissionsV2(Path filePathToCreate) throws IOException {

        FileSystem fileSystem = FileSystems.getDefault();
        UserPrincipalLookupService lookupService = fileSystem.getUserPrincipalLookupService();
        String group = getGroupFor(filePathToCreate.getName(1).toString());
        LOGGER.debug("group  name - {}", group);
        GroupPrincipal fileGroup = lookupService.lookupPrincipalByGroupName(group);

        // skip when reaching inbox/outbox
        Path subPath = getSubPath(filePathToCreate);
        if (null != subPath) {
            Path parent = filePathToCreate.subpath(0, 4);
            for (int i = 0; i < subPath.getNameCount(); i++) {
                parent = Paths.get(parent.toString(), subPath.getName(i).toString());
                if (!Files.exists(parent)) {
                    LOGGER.info("creating directory {}", parent.getFileName());
                    Files.createDirectories(parent);
                    LOGGER.info("setting file attribute and permissions {}", parent.getFileName());
                    Files.getFileAttributeView(parent, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(fileGroup);
                    Files.setPosixFilePermissions(parent, PosixFilePermissions.fromString(FOLDER_PERMISSION));
                    LOGGER.info("done setting file attribute and permissions {}", parent.getFileName());
                }
            }
        }

        LOGGER.info("Done setting group");
    }

    private static Path getSubPath(Path path) {
        if (path.getNameCount() > 4) {
            return path.subpath(4, path.getNameCount());
        }
        return null;
    }
}
