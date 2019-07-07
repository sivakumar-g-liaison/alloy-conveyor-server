/*
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

//
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

    var mboxName =
        _proc.getConfigurationInstance().getMailbox().getMbxName();
    var prcsrName =
        _proc.getConfigurationInstance().getProcsrName();
    var mboxInfo = "mailbox: " + mboxName + " processor: " + prcsrName;

    var localPayloadLocation = '';
    var remoteTargetLocation = '';
    var sftp_request = null;

    try {
        _proc.logInfo(mboxInfo + " Javascript uploader start");

        // local payload location
        localPayloadLocation = _proc.getPayloadURI();
        _proc.logInfo(mboxInfo + " local payload location: " +
            localPayloadLocation);
        if (!localPayloadLocation) {
            _proc.logError(mboxInfo + " missing local payload configuration.");
            throw new Error(mboxInfo + " missing local payload configuration.");
        }

        //remote paylaod location
        remoteTargetLocation = String(_proc.getWriteResponseURI()).trim();
        _proc.logInfo(mboxInfo + " remote target location URI: " +
            remoteTargetLocation);
        if (!remoteTargetLocation) {
            _proc.logError(mboxInfo + " missing remote target location URI.");
            throw new Error(mboxInfo + " missing remote target location URI.");
        }

        // Don't even bother if there are no files in the local directory
        var localDir = new java.io.File(localPayloadLocation);
        var subFiles = localDir.listFiles();
        if (subFiles == null || subFiles.length == 0) {
            _proc.logInfo(mboxInfo + " No local files to upload, exiting.");
            return;
        }

        // Connect to sftp server
        sftp_request = _proc.getClient();
        sftp_request.connect();

        if (sftp_request.openChannel()) {
            var direcotries = remoteTargetLocation.split(String(java.io.File.separator));
            for (var i = 0; i < direcotries.length; i++) {

                var directory = direcotries[i];
                _proc.logInfo(mboxInfo + " directory: " + directory);

                if (!directory) { //For when path starts with /
                    continue;
                }

                try {
                    sftp_request.getNative().lstat(directory);
                    _proc.logInfo(mboxInfo + "  remote directory '" + directory + "' already exists");
                    sftp_request.changeDirectory(directory);
                } catch (ex) {
                    sftp_request.getNative().mkdir(directory);
                    _proc.logInfo(mboxInfo + "  remote directory '" + directory + "' does not exist. Created it.");
                    sftp_request.changeDirectory(directory);
                }
            }

            uploadFileToRemote(sftp_request, localPayloadLocation, remoteTargetLocation, _proc, mboxInfo);
        }

        _proc.logInfo(mboxInfo + " Javascript uploader end");

    } catch (e) {
        _proc.logError(mboxInfo + " exception: " + e);
        _proc.sendEmail(null, mboxInfo + " Failure",
            "localPayloadLoc: " + localPayloadLocation +
            " remoteTargetLoc: " + remoteTargetLocation +
            "\nexception : " + e, "HTML");
        throw e;
    } finally {
        if (sftp_request) {
            sftp_request.disconnect();
        }
    }
};


//
// Upload a file to remote server
//
var uploadFileToRemote = function(sftpRequest, localParentDir, remoteParentDir, _proc, mboxInfo) {

    _proc.logInfo(mboxInfo + " upload files to remote starts");

    var subFiles = _proc.getFilesToUpload(false);
    _proc.logInfo(mboxInfo + " subfiles  : " + subFiles);

    // variable to hold the status of file upload request execution
    var replyCode = -1;

    if (subFiles != null && subFiles.length > 0) {

        var customProperties = _proc.getCustomProperties();
        var inputStream = null;
        var msg;

        for (var i = 0; i < subFiles.length; i++) {

            var file = subFiles[i];
            var actualFileName = file.getName();

            _proc.logInfo(mboxInfo + " actual file name is ::" + actualFileName);

            // upload a file
            try {

                var progressIndicator = customProperties.getProperty("file_transfer_status_indicator");
                _proc.logInfo(mboxInfo + " progressIndicator is ::" + progressIndicator);

                var fileNameWithProgressIndicatorStatus = function() {
                    if (progressIndicator) {
                        return (actualFileName + progressIndicator);
                    } else {
                        return (actualFileName + ".prg");
                    }
                };

                _proc.logInfo(mboxInfo + " put file - name - " + fileNameWithProgressIndicatorStatus());

                inputStream = new java.io.FileInputStream(file);
                replyCode = sftpRequest.putFile(fileNameWithProgressIndicatorStatus(), inputStream);
                _proc.logInfo(mboxInfo + " replyCode : " + replyCode);

                // File Uploading done successfully so rename the file
                if (replyCode == 0) {

                    _proc.logInfo(mboxInfo + " file uploaded successfully.");
                    var responseCode = sftpRequest.renameFile(fileNameWithProgressIndicatorStatus(), actualFileName);
                    _proc.logInfo(mboxInfo + " file rename responseCode is :: " + responseCode);

                    //delete a file once it is renamed
                    if (file['delete']()) {
                        _proc.logInfo(mboxInfo + " deleted the file " + file.getName());
                    }

                     msg = "File " + file.getName() + " uploaded successfully. " + mboxInfo;
                    _proc.logToLens(msg, file, Packages.com.liaison.mailbox.enums.ExecutionState.COMPLETED);
                }
            } catch (e) {

                _proc.logInfo(mboxInfo + " exception in upload " + e);
                msg = "File " + file.getName() + " is failed to upload to the remote location: " + e;
                _proc.logToLens(msg, file, Packages.com.liaison.mailbox.enums.ExecutionState.FAILED);
                throw e;
            } finally {
                if (inputStream !== null && typeof inputStream !== 'undefined') {
                    inputStream.close();
                }
            }

        }
    }
};

//
// Cleanup any resources used by the script.
//
var cleanup = function(_proc) {
    _proc.cleanup();
};
