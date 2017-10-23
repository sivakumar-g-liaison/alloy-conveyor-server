/*
 * Copyright 2017 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

// Other scripts to include (if any needed).
// List of script Aliases
//
var requires = function () {
    return [];
};

//
// Does the work.
//
var process = function (_proc) {

    var processor = _proc.getConfigurationInstance();
    var mailbox = processor.getMailbox();
    var mboxName = mailbox.getMbxName();
    var prcsrName = processor.getProcsrName();

    var mboxInfo = "mailbox:" + mboxName + " processor:" + prcsrName + " ftp_downloader_script :::";

    var processor_properties = JSON.parse(_proc.getPropertiesJson());
    var processor_properties_String = String(_proc.getPropertiesJson());

    var payloadLocation = '';
    var targetLocation = '';
    var g2FtpClient = null;

    try {

        _proc.logDebug(mboxInfo + " Javascript downloader start <<<<");

        if (processor_properties.debugTranscript) {
            _proc.sendEmail(null, "DEBUG", "processor_properties_String: " + processor_properties_String, "HTML");
        }

        // remote payload location
        payloadLocation = String(_proc.getPayloadURI());
        _proc.logInfo(mboxInfo + " remote payload location: " + payloadLocation);
        if (!payloadLocation) {
            throw new Error(mboxInfo + " missing remote payload configuration");
        }

        // local target location
        targetLocation = String(_proc.getWriteResponseURI());
        _proc.logInfo(mboxInfo + "local target location: " + targetLocation);
        if (!targetLocation) {
            throw new Error(mboxInfo + " missing target location configuration");
        }
        if (java.nio.file.Files.notExists(java.nio.file.Paths.get(targetLocation))) {
            throw new Error(mboxInfo + "the target location is not available : " + targetLocation)
        }

        // Connect to sftp server
        // client disconnect handled in clean up method
        g2FtpClient = _proc.getClient();
        g2FtpClient.connect();
        g2FtpClient.login();
        g2FtpClient.enableSessionReuse(true);

        var getProfileName = String(_proc.getReqDTO().getProfileName());
        // If this processor is called with the Mock Test Profile we will create a mock file after validation that we can connect and change directories
        if (getProfileName === "QLiaison_Mock.test") {
            _proc.logInfo(mboxInfo + " detected QLiaison_Mock.test process, creating empty mock file.");
            file = new java.io.File(targetLocation + String(java.io.File.separator) + "QLiaison_Mock.test");
            file.createNewFile();
        } else {
            //main function
            downloadFiles(g2FtpClient, payloadLocation, targetLocation, _proc, processor_properties, mboxInfo);
        }

        _proc.logDebug(mboxInfo + " Javascript downloader end <<<<");
    } catch (e) {
        _proc.logError(mboxInfo + " exception " + e);
        _proc.sendEmail(null, mboxInfo + " Failure",
            " remotePayloadLoc: " + payloadLocation +
            "localTargetLoc: " + targetLocation +
            "\nexception: " + e + " \nProperties:" + processor_properties_String, "HTML");
        throw e;
    }
};

//
// Cleanup any resources used by the script.
//
var cleanup = function (_proc) {
    _proc.logInfo('clean up');
    _proc.cleanup();
};

var isEmpty = function (str) {
    return (!str || 0 === str.length || "null" === str);
};

//
// Download files from remote server
//
var downloadFiles = function (g2FtpClient, payloadLocation, targetLocation, _proc, processor_properties, mboxInfo) {

    var customProperties = _proc.getCustomProperties();
    var fileNamePattern = String(customProperties.getProperty('filename_suffix'));
    var include_file_pattern_array = String(customProperties.getProperty('include_filepattern_array'));
    var exclude_file_pattern_array = String(customProperties.getProperty('exclude_filepattern_array'));

    //Validation to check any one this available in the properties
    if (isEmpty(include_file_pattern_array) && isEmpty(exclude_file_pattern_array)) {
        if (isEmpty(fileNamePattern)) {
            throw new Error("missing file pattern configuration");
        }
    }

    _proc.logInfo(mboxInfo + " download files starts here");

    //changes to remote directory
    g2FtpClient.changeDirectory(payloadLocation);
    var ftpFileArray = g2FtpClient.getNative().listFiles();

    _proc.logInfo(mboxInfo + " Number of found files / directories is " + ftpFileArray.length.toString());
    for (var i = 0; i < ftpFileArray.length; i++) {

        var ftpFile = ftpFileArray[i];
        var fileName = ftpFile.getName();

        if (ftpFile.isFile()) {

            //Exclude gets more priority.
            if (!isEmpty(exclude_file_pattern_array)) {
                if (matchInArray(fileName, exclude_file_pattern_array)) {
                    _proc.logInfo(" Matching exclude pattern -- " + fileName);
                    continue;
                }
            }

            //Includes the file
            if (!isEmpty(include_file_pattern_array)) {
                if (!matchInArray(fileName, include_file_pattern_array)) {
                    _proc.logInfo(" Matching include pattern -- " + fileName);
                    continue;
                }
            } else if (!isEmpty(fileNamePattern)) {
                if (!matchInArray(fileName, fileNamePattern)) {
                    _proc.logInfo(mboxInfo + " Matching include pattern(single) -- " + fileName);
                    continue;
                }
            }

            if (processor_properties.debugTranscript) {
                _proc.sendEmail(null, "DEBUG", "fileName matched: " + fileName, "HTML");
            }

            _proc.logInfo(mboxInfo + " file name is " + fileName);

            // retrieve file_transfer_status_indicator extension from the processor properties
            var progressIndicator = customProperties.getProperty("file_transfer_status_indicator");

            _proc.logInfo(mboxInfo + " fileNameWithProgressIndicatorStatus is " + fileNameWithProgressIndicatorStatus(fileName, progressIndicator));
            var localInProgressFileName = targetLocation + String(java.io.File.separator) + fileNameWithProgressIndicatorStatus(fileName, progressIndicator);

            var fos = null;
            var bos = null;
            try {

                _proc.logInfo(mboxInfo + " in progress file name is " + localInProgressFileName);

                fos = new java.io.FileOutputStream(localInProgressFileName);
                bos = new java.io.BufferedOutputStream(fos);

                // retrieve file from remote payload location
                var replyCode = g2FtpClient.getFile(fileName, bos);

                // if file downloaded successfully rename it
                _proc.logInfo(mboxInfo + " the reply code is " + replyCode);
                if (226 === replyCode || 250 === replyCode) {

                    _proc.logInfo(mboxInfo + " File downloaded successfully");

                    // file with file_transfer_status_indicator extension
                    var localFileWithStatusExtension = new java.io.File(localInProgressFileName);
                    _proc.logInfo(mboxInfo + " local File is " + localFileWithStatusExtension.getAbsolutePath());

                    // file streams closed here so that file renaming can be done.
                    if (bos !== null) {
                        bos.close();
                    }
                    if (fos !== null) {
                        fos.close();
                    }

                    // rename the file to remove file_transfer_status_indicator
                    var renameStatus = localFileWithStatusExtension.renameTo(originalFile(targetLocation, fileName));

                    if (renameStatus) {
                        _proc.logInfo(mboxInfo + " File renamed successfully");

                        //SECTION to delete the remote files once it is downloaded successfully
                        g2FtpClient.deleteFile(fileName);

                        if (processor_properties.debugTranscript) {
                            //#SECTION enable this for success email notification
                            _proc.sendEmail(null, mboxInfo + " Success",
                                " remotePayloadLoc: " + payloadLocation +
                                " localTargetLoc: " + targetLocation, "HTML");
                        }

                    } else {
                        _proc.logInfo(mboxInfo + " File Rename status is " + renameStatus);
                        _proc.logInfo(mboxInfo + " Failed to rename the file");
                    }
                } else {
                    // file downloading got failed
                    _proc.logError(mboxInfo + " File downloading failed for the file " + fileName);
                }
            } catch (e) {
                throw e;
            } finally {
                if (bos !== null) {
                    bos.close();
                }
                if (fos !== null) {
                    fos.close();
                }
            }

        } else {
            _proc.logInfo(mboxInfo + " It is a directory -- " + fileName);
        }

    }
};

var fileNameWithProgressIndicatorStatus = function (fileName, progressIndicator) {
    if (progressIndicator) {
        return (fileName + progressIndicator);
    } else {
        return (fileName + ".prg");
    }
};


var originalFile = function (targetLocation, fileName) {
    return new java.io.File((String(targetLocation) + String(java.io.File.separator) + fileName));
};


// REGX matching
var matchInArray = function (string, expressions) {

    var regex;
    var array = expressions.split(',');
    var len = array.length;

    for (i = 0; i < len; i++) {

        var Wildcard = "^";
        Wildcard = Wildcard + array[i].trim().replace(".", "\\.");
        Wildcard = Wildcard.replace("*", ".*");
        regex = new RegExp(Wildcard, 'ig');

        if (!string.match(regex)) {
            continue;
        }
        return true;
    }

    return false;
};