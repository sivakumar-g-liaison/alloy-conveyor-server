//
//  gitlab:/sftp_uploader_storage_system_read
//
var requires = function () {
    return [];
};

//
// Does the work.
//
var changeDirectory = function (remoteTargetLocation, _proc, mboxInfo, sftp_request) {

    var directories = remoteTargetLocation.split(String(java.io.File.separator));
    var i = 0;
    for (i = 0; i < directories.length; i++) {

        var directory = directories[i];
        _proc.logInfo(mboxInfo + "The directory is " + directory);

        if (!directory) { //For when path starts with /
            continue;
        }

        try {
            sftp_request.getNative().lstat(directory);
            _proc.logInfo(mboxInfo + "The remote directory" + directory + "already exists");
            sftp_request.changeDirectory(directory);
        } catch (ex) {
            try {
                sftp_request.getNative().mkdir(directory);
                _proc.logInfo(mboxInfo + "The remote directory" + directory + " does not exist.So created that.");
                sftp_request.changeDirectory(directory);
            } catch (ex) {
                _proc.logError(mboxInfo + "The mkdir(" + directory + ") failed. Moving on.");
            }
        }
    }
};

var process = function (_proc) {

    var mboxInfo = "";
    var MSGContext = "";
    var remoteTargetLocation = "";
    var arrayLength = 0;
    var actualFileName = "";
    var myStringArray = null;
    var localPayloadLocation = "";

    try {

        //----------------------------------------------------------------------------
        // PART 1 - check all the passed in attributes and fail if their is an issue
        //----------------------------------------------------------------------------

        //local payload location
        localPayloadLocation = _proc.getPayloadURI();

        //remote paylaod location
        remoteTargetLocation = String(_proc.getWriteResponseURI()).trim();
        if (!remoteTargetLocation) {
            _proc.logError(mboxInfo + "The given remote target location URI is Empty.");
            throw new Error("The given remote target location configuration is Empty.");
        }

        _proc.logDebug("Local Payload Location is " + localPayloadLocation);
        if (!localPayloadLocation) {
            _proc.logError(mboxInfo + "The given local payload is Empty.");
            throw new Error("The given local payload configuration is Empty.");
        }

        var mboxName = _proc.getConfigurationInstance().getMailbox().getMbxName();
        var prcsrName = _proc.getConfigurationInstance().getProcsrName();
        mboxInfo = "mailbox:" + mboxName + " processor:" + prcsrName + " sftp_uploader_V4.5 ::: ";

        //----------------------------------------------------------------------------
        // PART 2 - Check if there are files to be processed. stop if not
        //----------------------------------------------------------------------------
        myStringArray = _proc.getRelayFiles(false);
        arrayLength = myStringArray.length;
        MSGContext = " localPayloadLocation=" + localPayloadLocation + " ,remoteTargetLocation=" + remoteTargetLocation;

    } catch (e) {
        _proc.logError(mboxInfo + "exception" + e);
        _proc.sendEmail(null, "Failure", "sftp uploader failed during initialization : " + MSGContext + " " + e, "HTML");

    }

    // check if we even have files to send
    var sftp_request = null;


    if (arrayLength > 0) {

        try {

            //Connect to sftp server
            sftp_request = _proc.getClient();
            sftp_request.connect();

            //----------------------------------------------------------------------------
            // PART 3 - change into remote directory
            //----------------------------------------------------------------------------
            if (sftp_request.openChannel()) {

                _proc.logInfo(mboxInfo + "Remote target location is " + remoteTargetLocation);
                changeDirectory(remoteTargetLocation, _proc, mboxInfo, sftp_request);

                //----------------------------------------------------------------------------
                // PART 4 - Loop over the files and send them
                //----------------------------------------------------------------------------
                for (i = 0; i < arrayLength; i++) {

                    _proc.logInfo(mboxInfo + "filename is " + myStringArray[i].getName());
                    actualFileName = String(myStringArray[i].getName());

                    uploadFileToRemote(sftp_request,
                        localPayloadLocation,
                        remoteTargetLocation,
                        myStringArray[i],
                        _proc,
                        MSGContext,
                        mboxInfo);

                    _proc.logInfo(mboxInfo + localPayloadLocation.substr(11) + " " + actualFileName + " " + String(i + 1) + " of " + String(arrayLength) + " Completed SFTP for: " + actualFileName + " " + MSGContext);

                }
            }

        } catch (e) {
            _proc.logError(mboxInfo + "exception " + e);
            _proc.sendEmail(null, "Failure", "sftp uploader failed because: " + MSGContext + " " + e, "HTML");
            throw e;
        } finally {
            if (sftp_request) {
                sftp_request.disconnect();
            }
        }
    }
};

//
// Upload a file to remote server
//
var uploadFileToRemote = function (sftpRequest, localParentDir, remoteParentDir, _file, _proc, _MSGContext, _mboxInfo) {

    var msg = "";
    var _actualFileName = "";
    var inputStream = null;

    try {

        _actualFileName = _file.getName();
        _proc.logInfo(_mboxInfo + "actual file name is ::" + _actualFileName);

        var progressIndicator = _proc.getCustomProperties().getProperty("file_transfer_status_indicator");
        _proc.logInfo(_mboxInfo + "progressIndicator is ::" + progressIndicator);

        var useTriggerFileEx = String(_proc.getCustomProperties().getProperty('use_trigger_file_extension'));
        _proc.logInfo(_mboxInfo + "use_trigger_file_extension is ::" + useTriggerFileEx);

        var replaceExtnWithTrigger = (String(_proc.getCustomProperties().getProperty('replace_extension_with_trigger')) == "true");
        _proc.logInfo(_mboxInfo + "replace_extension_with_trigger is ::" + replaceExtnWithTrigger);

        var fileNameWithProgressIndicatorStatus = function () {
            if (progressIndicator) {
                return (_actualFileName + progressIndicator);
            } else {
                return (_actualFileName);
            }
        };

        _proc.logInfo(_mboxInfo + "put file - name - " + fileNameWithProgressIndicatorStatus());
        inputStream = _file.getPayloadInputStream();

        //setting the default
        var replyCode = 0;

        if (_actualFileName != 'QLiaison_Mock.test') {
            replyCode = sftpRequest.putFile(fileNameWithProgressIndicatorStatus(), inputStream);
            _proc.logInfo(_mboxInfo + "replyCode : " + replyCode);
        }

        // File Uploading done successfully so rename the file
        if (!isEmpty(useTriggerFileEx)) {
            replyCode = 0;
            var triggerFileName = _actualFileName;
            //Remove the extension if it is set ot true
            if (replaceExtnWithTrigger) {
                triggerFileName = triggerFileName.substring(0, triggerFileName.indexOf('.'));
            }
            replyCode = sftpRequest.putFile(triggerFileName + useTriggerFileEx, inputStream);
        }

        //if (replyCode === 0) {
            /*if (_file['delete']()) {
                _proc.logInfo(_mboxInfo + "deleted the file " + _actualFileName);
            }*/

        //}

        if (_actualFileName == 'QLiaison_Mock.test') {
            msg = "File " + _file.getName() + " (MOCK) uploaded successfully. " + _mboxInfo + _MSGContext;
        } else {
            msg = "File " + _file.getName() + " uploaded successfully. " + _mboxInfo + _MSGContext;
        }

        _proc.logToLens(msg, _file, Packages.com.liaison.mailbox.enums.ExecutionState.COMPLETED);

    } catch (e) {

        _proc.logError(_mboxInfo + "exception in upload " + e);
        if (_file) {
            msg = "File " + _file.getName() + " is failed to upload to the remote location: " + _MSGContext + " " + e;
            _proc.logToLens(msg, _file, Packages.com.liaison.mailbox.enums.ExecutionState.FAILED);
        }

        throw e;
    } finally {
        if (inputStream !== null && typeof inputStream !== 'undefined') {
            inputStream.close();
            _proc.logInfo(_mboxInfo + "closed the inputstream");
        }
    }
};

//
// Cleanup any resources used by the script.
//
var cleanup = function (_proc) {
    _proc.logDebug('clean up');
    _proc.cleanup();
};

var isEmpty = function (str) {
    return (!str || 0 === str.length || "null" === str);
};