/*
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

// Other scripts to include (if any needed).
// List of script Aliases
//
var requires = function() {
    return [];
};

//
// Does the work.
//
var process = function(_proc) {

    var processor = _proc.getConfigurationInstance();
    var mailbox = processor.getMailbox();
    var mboxName = mailbox.getMbxName();
    var prcsrName = processor.getProcsrName();

    var mboxInfo = "mailbox:" + mboxName + " processor:" + prcsrName + " sftp_downloader_script :::";

    var processor_properties = JSON.parse(_proc.getPropertiesJson());
    var processor_properties_String = String(_proc.getPropertiesJson());

    var payloadLocation = '';
    var targetLocation = '';
    var g2SftpClient = null;

    try {
        if (processor_properties.debugTranscript) {
            _proc.sendEmail(null, "DEBUG", "processor_properties_String: " + processor_properties_String, "HTML");
        }

        _proc.logInfo(mboxInfo + " Javascript downloader start >>>>");

        // remote payload location
        payloadLocation = String(_proc.getPayloadURI());
        _proc.logInfo(mboxInfo + " remote payload location: " + payloadLocation);
        if (!payloadLocation) {
            _proc.logError(mboxInfo + " missing remote payload configuration");
            throw new Error(mboxInfo + " missing remote payload configuration");
        }

        // local target location
        targetLocation = String(_proc.getWriteResponseURI());
        _proc.logInfo(mboxInfo + "local target location: " + targetLocation);
        if (!targetLocation) {
            _proc.logInfo(mboxInfo +
                " missing target location  configuration");
            throw new Error(mboxInfo +
                " missing target location  configuration");
        }

        // Connect to sftp server
        g2SftpClient = _proc.getClient();
        g2SftpClient.connect();

        if (g2SftpClient.openChannel()) {
            // download files from a directory

            // attempting to change directory here so we get and early error message if that fails
            // _proc.logInfo(mboxInfo + " attempting to change remote directory to " + payloadLocation);
            // g2SftpClient.changeDirectory(payloadLocation);

            var getProfileName = String(_proc.getReqDTO().getProfileName());

            // If this processor is called with the Mock Test Profile we will create a mock file after validation that we can connect and change directories
            if (getProfileName == "QLiaison_Mock.test") {
                _proc.logInfo(mboxInfo + " detected QLiaison_Mock.test process, creating empty mock file.");
                file = new java.io.File(targetLocation + String(java.io.File.separator) + "QLiaison_Mock.test");

                if (!file.exists()) {
                    file.createNewFile();
                }
            } else {
                // main function start
                var files = downloadFiles(g2SftpClient, payloadLocation, targetLocation, _proc, processor_properties, mboxInfo);
                // main function end
                if (processor_properties.directSubmit) {
                	var staticprop = new com.liaison.mailbox.service.dto.SweeperStaticPropertiesDTO();
                	staticprop.setSecuredPayload(processor_properties.securedPayload);
                	staticprop.setLensVisibility(processor_properties.lensVisibility);
                	staticprop.setPipeLineID(processor_properties.pipeLineID);
                	_proc.sweepFiles(files, staticprop);
                }
            }
        }

        _proc.logInfo(mboxInfo + " Javascript downloader end <<<<");

    } catch (e) {
        _proc.logError(mboxInfo + " exception " + e);
        _proc.sendEmail(null, mboxInfo + " Failure",
            " remotePayloadLoc: " + payloadLocation +
            "localTargetLoc: " + targetLocation +
            "\nexception: " + e + " \nProperties:" + processor_properties_String, "HTML");
    } finally {
        if (g2SftpClient) {
            g2SftpClient.disconnect();
        }
    }
};

//
// Download files from remote server
//
var downloadFiles = function (g2SftpClient, payloadLocation, targetLocation, _proc, processor_properties, mboxInfo) {

    var customProperties = _proc.getCustomProperties();
    var fileNamePattern = String(customProperties.getProperty('filename_suffix'));
    var include_filepattern_array = String(customProperties.getProperty('include_filepattern_array'));
    var exclude_filepattern_array = String(customProperties.getProperty('exclude_filepattern_array'));
    var fileLists = [];

    //Validation to check atlease any one this available in the properties
    if (isEmpty(include_filepattern_array) && isEmpty(exclude_filepattern_array)) {

        if (isEmpty(fileNamePattern)) {
            throw new Error("missing file pattern configuration");
        }
    }

    _proc.logInfo(mboxInfo + " download files starts here");

    var fileNames = g2SftpClient.listFiles(payloadLocation).toArray();
    g2SftpClient.changeDirectory(payloadLocation);
    _proc.logInfo(mboxInfo + " Numer of found files / directories is " + fileNames.length.toString());
    for (var i = 0; i < fileNames.length; i++) {

        var fileName = fileNames[i];

        // skip parent directory and the directory itself
        if (fileName == "." || fileName == "..") {
            continue;
        }
        var isDir = g2SftpClient.getNative().stat(fileName).isDir();
        if (isDir) {
            _proc.logInfo(mboxInfo + " It is a directory -- " + fileName);
            continue;
        }

        //Exclude gets more priority.
        if (!isEmpty(exclude_filepattern_array)) {
            if (matchInArray(fileName, exclude_filepattern_array)) {
                _proc.logInfo(" Matching exclude pattern -- " + fileName);
                continue;
            }
        }

        //Includes the file
        if (!isEmpty(include_filepattern_array)) {
            if (!matchInArray(fileName, include_filepattern_array)) {
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


        _proc.logInfo(mboxInfo + " fileNameWithProgressIndicatorStatus is " +
            fileNameWithProgressIndicatorStatus(fileName, progressIndicator));
        var localInProgressFileName = targetLocation + String(java.io.File.separator) +
            fileNameWithProgressIndicatorStatus(fileName, progressIndicator);

        var fos;
        var bos;
        try {

            _proc.logInfo(mboxInfo + " localInProgressFileName is " + localInProgressFileName);


            //creates response directory if the directory is not exist
            _proc.createResponseDirectory(localInProgressFileName);

            fos = new java.io.FileOutputStream(localInProgressFileName);
            bos = new java.io.BufferedOutputStream(fos);

            // retrieve file from remote payload location
            var replyCode = g2SftpClient.getFile(fileName, bos);

            // if file downloaded successfully rename it
            if (replyCode === 0) {

                _proc.logInfo(mboxInfo + " File downloaded successfully");

                // file with file_transfer_status_indicator extension
                var localFileWithStatusExtension = new java.io.File(localInProgressFileName);
                _proc.logInfo(mboxInfo + " local File is " + localFileWithStatusExtension.getAbsolutePath());

                // file streams closed here so that file renaming can be done.
                if (fos !== null) {
                    fos.close();
                }
                if (bos !== null) {
                    bos.close();
                }


                // rename the file to remove file_transfer_status_indicator
                var renameStatus = localFileWithStatusExtension.renameTo(originalFile(targetLocation, fileName));

                _proc.logInfo(mboxInfo + " File Rename status is " + renameStatus);
                if (renameStatus) {
                    _proc.logInfo(mboxInfo + " File renamed successfully");
                    fileLists.push(originalFile(targetLocation, fileName));
                    // for some reason the delete File command duplicates the folder path. trying to go one level higer.
                    //g2SftpClient.changeDirectory("..");
                    // g2SftpClient.changeDirectory(payloadLocation);

                    //SECTION to delete the remote files once it is downloaded successfully
                    g2SftpClient.deleteFile(fileName);

                    if (processor_properties.debugTranscript) {
                        //#SECTION enable this for success email notification
                        _proc.sendEmail(null, mboxInfo + " Success",
                            " remotePayloadLoc: " + payloadLocation +
                            " localTargetLoc: " + targetLocation, "HTML");
                    }

                } else {
                    _proc.logInfo(mboxInfo + " File renaming got failed");
                }
            } else {
                // file downloading got failed
                _proc.logError(mboxInfo + " File downloading failed for file" + fileName);
            }
        } catch (e) {
            _proc.logError(mboxInfo + " Error while downloading file" + e);
            throw e;
        } finally {
            if (fos !== null) {
                fos.close();
            }
            if (bos !== null) {
                bos.close();
            }

        }
    }
    return fileLists;
}

//
// Cleanup any resources used by the script.
//
var cleanup = function(_proc) {
    _proc.logInfo('clean up');
    // remove the private key once processor execution is done
    _proc.cleanup();
};

var isEmpty = function(str) {
    return (!str || 0 === str.length || "null" === str);
};

var fileNameWithProgressIndicatorStatus = function(fileName, progressIndicator ) {
    if (progressIndicator) {
        return (fileName + progressIndicator);
    } else {
        return (fileName + ".prg");
    }
};


var originalFile = function(targetLocation, fileName) {
    return new java.io.File((String(targetLocation) + String(java.io.File.separator) + fileName));
};


// REGX matching
var matchInArray = function(string, expressions) {

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