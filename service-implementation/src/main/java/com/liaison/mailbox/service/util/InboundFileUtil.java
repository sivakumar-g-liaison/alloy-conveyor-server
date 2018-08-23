/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.util;

import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;

/**
 * Utilities for Inbound File.
 */
public class InboundFileUtil {

    /**
     * Validates the file pattern with path matcher. if doesn't match with file path returns false. else return true.
     *
     * @param url
     * @param errorItems
     * @param errorResponse
     * @return
     */
    public static boolean checkFilePath(String url, List<String> errorItems, Map<String, String> errorResponse) {

        File fileDirectory = new File(url.trim());
        FileSystem fileSystem = FileSystems.getDefault();
        PathMatcher pathMatcher = fileSystem.getPathMatcher(MailBoxUtil.DATA_FOLDER_PATTERN);
        java.nio.file.Path filePathToCreate = fileDirectory.toPath();

        if (!pathMatcher.matches(filePathToCreate)) {
            errorItems.add(url);
            errorResponse.put(url, " is incorrect filepath");
            return false;
        }
        return true;
    }

    /**
     * This method will construct response either success or failed messages by given errer items.
     *
     * @param errorItems
     * @param listOfUrls
     * @param errorResponse
     * @return
     */
    public static Response constructResponse(List<String> errorItems, String[] listOfUrls, Map<String, String> errorResponse) {

        String responseMsg;
        if (errorItems.isEmpty()) {
            responseMsg = "Payload messages were created inbound files successfully.";
            return Response.ok().entity(responseMsg).build();
        } else if (errorItems.size() == 1 && listOfUrls.length == 1) {
            responseMsg = errorItems.get(0) + errorResponse.get(errorItems.get(0));
            return Response.serverError().entity(responseMsg).build();
        } else if (errorItems.size() == listOfUrls.length) {
            responseMsg = "No inbound files were created for Payload message; Either filepath incorrect or inbound files creation was failed.";
            return Response.serverError().entity(responseMsg).build();
        } else {
            StringBuilder sb = new StringBuilder();
            errorItems.forEach((errorItem) -> {
                sb.append(errorItem).append(" ").append(errorResponse.get(errorItem)).append("\n");
            });
            responseMsg = "Payload messages were created inbound files partially.\nUnable to create file for below Payload(s)\n" + sb.toString();
            return Response.accepted().entity(responseMsg).build();
        }
    }
}