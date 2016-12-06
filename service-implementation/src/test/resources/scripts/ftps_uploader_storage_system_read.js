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

    var mboxInfo = "";
    var MSGContext = "";
    var remoteTargetLocation = "";
    var arrayLength = 0;
    var actualFile = "";
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

        var mboxName = _proc.getConfigurationInstance().getMailbox().getMbxName();
        var prcsrName = _proc.getConfigurationInstance().getProcsrName();
        mboxInfo = "mailbox:" + mboxName + " processor:" + prcsrName + " ftp_uploader ::: ";

        //----------------------------------------------------------------------------
        // PART 2 - Check if there are files to be processed. stop if not
        //----------------------------------------------------------------------------

        myStringArray = _proc.getRelayFiles(false);
        arrayLength = myStringArray.length;
        MSGContext = " localPayloadLocation=" + localPayloadLocation + " ,remoteTargetLocation=" + remoteTargetLocation;

    } catch (e) {
        _proc.logError(mboxInfo + "exception" + e);
        _proc.sendEmail(null, "Failure", "ftp uploader failed during initialization : " + MSGContext + " " + e, "HTML");
        throw e;
    }

    // check if we even have files to send
    var ftp_request = null;

    if (arrayLength > 0) {

        try {

            //Connect to ftp server
            ftp_request = _proc.getClient();
            ftp_request.connect();
            ftp_request.login();

            var binary = _proc.getProperties().isBinary();
            var passive = _proc.getProperties().isPassive();
            ftp_request.setBinary(binary);
            ftp_request.setPassive(passive);

            //----------------------------------------------------------------------------
            // PART 3 - change into remote directory
            //----------------------------------------------------------------------------
            changeDirectory(_proc, ftp_request, mboxInfo, remoteTargetLocation)

            //----------------------------------------------------------------------------
            // PART 4 - Loop over the files and send them
            //----------------------------------------------------------------------------

            for (i = 0; i < arrayLength; i++) {

                actualFile = myStringArray[i];

                uploadFileToRemote(ftp_request, remoteTargetLocation, actualFile, _proc, MSGContext, mboxInfo);
                _proc.logInfo(mboxInfo + localPayloadLocation.substr(11) + " " + actualFile.getName() + " " + String(i + 1) + " of " + String(arrayLength) + " Completed FTPS for: " + actualFile + " " + MSGContext);

            }


        } catch (e) {
            _proc.logError(mboxInfo + "exception " + e);
            _proc.sendEmail(null, "Failure", "ftp uploader failed because: " + MSGContext + " " + e, "HTML");

            // getting the very first file from File List so we have a context for the error message back to lens
            // open if we should take all or just one
            var file =  myStringArray[0];
            var msg = "File " + file.getName() + " is failed to upload to the remote location, " + MSGContext + " " + e;
            _proc.logToLens(msg, file, Packages.com.liaison.mailbox.enums.ExecutionState.FAILED);
            throw e;
        }
    }
};


//
// Upload a file to remote server
//
var uploadFileToRemote = function(ftpRequest, remoteParentDir, _actualFile, _proc, _MSGContext, _mboxInfo) {

    var msg = "";
    var inputStream = null;

    try {

        _proc.logInfo(_mboxInfo + "actual file name is ::" + _actualFile.getName());

        var progressIndicator = _proc.getCustomProperties().getProperty("file_transfer_status_indicator");
        _proc.logInfo(_mboxInfo + "progressIndicator is ::" + progressIndicator);

        var useTriggerFileEx = String(_proc.getCustomProperties().getProperty('use_trigger_file_extension'));
        _proc.logInfo(_mboxInfo + "use_trigger_file_extension is ::" + useTriggerFileEx);

        var replaceExtnWithTrigger = (String(_proc.getCustomProperties().getProperty('replace_extension_with_trigger')) == "true");
        _proc.logInfo(_mboxInfo + "replace_extension_with_trigger is ::" + replaceExtnWithTrigger);

        var fileNameWithProgressIndicatorStatus = function() {
            if (progressIndicator) {
                return (_actualFile.getName() + progressIndicator);
            } else {
                return (_actualFile.getName());
            }
        };

        _proc.logInfo("put file - name - " + fileNameWithProgressIndicatorStatus());

        inputStream = _actualFile.getPayloadInputStream();

        //setting the default
        replyCode = 0;

        if (_actualFile.getName() != 'QLiaison_Mock.test') {
            replyCode = ftpRequest.putFile(fileNameWithProgressIndicatorStatus(), inputStream);
            _proc.logInfo(_mboxInfo + "replyCode : " + replyCode);
        }

        if (!isEmpty(useTriggerFileEx)) {
            replyCode = 0;
            var triggerFileName = _actualFile.getName();
            //Remove the extension if it is set to true
            if (replaceExtnWithTrigger) {
                triggerFileName = triggerFileName.substring(0, triggerFileName.indexOf('.'));
            }
            replyCode = ftpRequest.putFile(triggerFileName + useTriggerFileEx, inputStream);
        }

        //not needed
        if (replyCode === 226 || replyCode === 250) {
            if (_actualFile['delete']()) {
                _proc.logDebug("deleted the file " + _actualFile.getName());
            } else {
                _proc.logDebug("file doesn't exist " + _actualFile.getName());
            }
        }

        if (_actualFile.getName() == 'QLiaison_Mock.test') {
            msg = "File " + _actualFile.getName() + " (MOCK) uploaded successfully. " + _mboxInfo + _MSGContext;
        } else {
            msg = "File " + _actualFile.getName() + " uploaded successfully. " + _mboxInfo + _MSGContext;
        }

        _proc.logToLens(msg, _actualFile, Packages.com.liaison.mailbox.enums.ExecutionState.COMPLETED);

    } catch (e) {

        _proc.logError(_mboxInfo + "exception in upload " + e);
        msg = "File " + _actualFile.getName() + " is failed to upload to the remote location: " + _MSGContext + " " + e;
        _proc.logToLens(msg, _actualFile, Packages.com.liaison.mailbox.enums.ExecutionState.FAILED);

        throw e;
    } finally {
        if (inputStream !== null && typeof inputStream !== 'undefined') {
            inputStream.close();
        }
    }
};

/**
 * Change remote directory
 *
 * @param _proc processor instance
 * @param ftp_request ftp request
 * @param mboxInfo mailbox log info
 * @param remoteTargetLocation remote target location
 */
var changeDirectory = function(_proc, ftp_request, mboxInfo, remoteTargetLocation) {

    _proc.logInfo(mboxInfo + "Remote target location is " + remoteTargetLocation);
    var directories = remoteTargetLocation.split(String(java.io.File.separator));
    var i = 0;
    for (i = 0; i < directories.length; i++) {
        var directory = directories[i];
        _proc.logInfo(mboxInfo + "The directory is " + directory);

        if (!directory) { //For when path starts with /
            continue;
        }

        var dirExists = ftp_request.getNative().changeWorkingDirectory(remoteTargetLocation);
        if(!dirExists ) {
            var isCreated = ftp_request.getNative().makeDirectory(remoteTargetLocation);
            if (isCreated) {
                _proc.logInfo(mboxInfo + "The remote directory " + remoteTargetLocation + " does not exist.So created that.");
                ftp_request.getNative().changeWorkingDirectory(remoteTargetLocation);
            } else {
                _proc.logError(mboxInfo + "Unable to create remote directory " + remoteTargetLocation);
            }
        }
        ftp_request.changeDirectory(remoteTargetLocation);
    }
};

//
// Cleanup any resources used by the script.
//
var cleanup = function(_proc) {
    _proc.logDebug('clean up');
    _proc.cleanup();
};

var isEmpty = function(str) {
    return (!str || 0 === str.length || "null" === str);
};