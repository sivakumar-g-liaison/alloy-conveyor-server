//
//  gitlab:/sftp_uploader_V4.4
//
//  V1    Baseline from Veera
//  V2    added better error messages and details.                              CBO 7/23/2015
//  V3    added Lens Logging                                                    CBO 8/28/2015
//  V4    added additional try catch to dir create                              CBO 8/28/2015
//  V4.1  change logdebug to log info                                           CBO 10/12/2015
//  V4.2  change logdebug to log info                                           CBO 10/23/2015
//  V4.3  adding trigger file support                                           CBO 12/04/2015
//        added parameter: use_trigger_file_extension
//        cleaned up logging
//	V4.4  replace extension with trigger
//  V4.5  added validateLastModifiedTolerance to avoid grabbing a file that's still beeing written to.
//  V4.6  added finally catch block for SFTP client                              CBO 03/04/2016
//        removed sendEmail function for success
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
var process              = function(_proc) {

    var mboxInfo             = "";
    var MSGContext           = "";
    var remoteTargetLocation = "";
    var arrayLength          = 0;
    var actualFileName       = "";
    var myStringArray        = null;
    var localPayloadLocation = "";

    try {

        //----------------------------------------------------------------------------
        // PART 1 - check all the passed in attributes and fail if ther is an issue
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

        var mboxName =
            _proc.getConfigurationInstance().getMailbox().getMbxName();
        var prcsrName =
            _proc.getConfigurationInstance().getProcsrName();
        mboxInfo = "mailbox:" + mboxName + " processor:" + prcsrName + " sftp_uploader_V4.5 ::: ";

        //----------------------------------------------------------------------------
        // PART 2 - Check if there are files to be processed. stop if not
        //----------------------------------------------------------------------------

        myStringArray = GetLocalFileList(localPayloadLocation, _proc);
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

            //Connecto to sftp server
            sftp_request = _proc.getClient();
            sftp_request.connect();


            //----------------------------------------------------------------------------
            // PART 3 - change into remote directory
            //----------------------------------------------------------------------------

            if (sftp_request.openChannel()) {
                var homeDirectory = sftp_request.currentWorkingDirectory();
                _proc.logInfo(mboxInfo + "Home directory is " + homeDirectory);

                _proc.logInfo(mboxInfo + "Remote target location is " + remoteTargetLocation);

                var direcotries = remoteTargetLocation.split(String(java.io.File.separator));
                var i = 0;
                for (i = 0; i < direcotries.length; i++) {

                    var directory = direcotries[i];
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


                //----------------------------------------------------------------------------
                // PART 4 - Loop over the files and send them
                //----------------------------------------------------------------------------

                for (i = 0; i < arrayLength; i++) {

                    _proc.logInfo(mboxInfo + "filename is " +  myStringArray[i]);

                    actualFileName = String(myStringArray[i]);

                    uploadFileToRemote(sftp_request, homeDirectory, localPayloadLocation, remoteTargetLocation, actualFileName, _proc, MSGContext, mboxInfo);

                    _proc.logInfo(mboxInfo + localPayloadLocation.substr(11) + " " + actualFileName + " " + String(i + 1) + " of " + String(arrayLength) + " Completed SFTP for: " + actualFileName + " " + MSGContext);

                    // removed 03/04/2016 - KIBANA is reliable now
                    //_proc.sendEmail(null, localPayloadLocation.substr(11) + " " + actualFileName + " " + String(i + 1) + " of " + String(arrayLength), "Starting SFTP for: " + actualFileName + " " + MSGContext, "HTML");
                }
            }


        } catch (e) {
            _proc.logError(mboxInfo + "exception " + e);
            _proc.sendEmail(null, "Failure", "sftp uploader failed because: " + MSGContext + " " + e, "HTML");

            // getting the very first file from File List so we have a context for the error message back to lens
            // open if we should take all or just one
            var file = new java.io.File(localPayloadLocation + "/" + myStringArray[0]);
            var msg = "File " + file.getName() + " is failed to upload to the remote location, " + MSGContext + " " + e;
            _proc.logToLens(msg, file, Packages.com.liaison.mailbox.enums.ExecutionState.FAILED);
        } finally {
            if (sftp_request) {
                sftp_request.disconnect();
            }
        }
    }
};



var GetLocalFileList = function(localParentDir, _proc) {

    var localDir = new java.io.File(localParentDir);
    _proc.logInfo("localDir : " + localDir);

    var subFiles = localDir.listFiles();

    var subFilesClean = [];

    // variable to hold the status of file upload request execution
    var replyCode = -1;
    var ix = 0;

    if (subFiles !== null && subFiles.length > 0) {

        for (var i = 0; i < subFiles.length; i++) {

            var file = subFiles[i];
            // skip parent directory and the directory itself
            if (file.getName() == "." || file.getName() == "..") {
                continue;
            }

            //skip the file is directory
            if (file.isDirectory()) {
                continue;
            }
            subFilesClean.push(file.getName());
        }
    }

    return subFilesClean;
};


//
// Upload a file to remote server
//
var uploadFileToRemote = function(sftpRequest, homeDirectory, localParentDir, remoteParentDir, _actualFileName, _proc, _MSGContext, _mboxInfo) {

    var msg = "";
    var file = null;
    var inputStream = null;
    var remoteExtendedFolder = "";

    try {


        sftpRequest.changeDirectory(homeDirectory);


        // re,moving the leading / as this is an issue with changeDirectory
        remoteParentDir = (remoteParentDir.trim() === '/' ? '' : remoteParentDir.trim());


        if (_actualFileName.includes('_FSHR_FCS-US-PA_')) {
            remoteExtendedFolder = remoteParentDir.concat('/Allentown');
        }
        if (_actualFileName.includes('Ahmedabad')) {
            remoteExtendedFolder = remoteParentDir.concat('/Ahmedabad');
        }
        if (_actualFileName.includes('ARGENTINA')) {
            remoteExtendedFolder = remoteParentDir.concat('/Argentina');
        }
        if (_actualFileName.includes('Bangalore')) {
            remoteExtendedFolder = remoteParentDir.concat('/Bangalore');
        }
        if (_actualFileName.includes('_FSHR_FCS-CH_')) {
            remoteExtendedFolder = remoteParentDir.concat('/Basel');
        }
        if (_actualFileName.includes('CHINA')) {
            remoteExtendedFolder = remoteParentDir.concat('/Beijing');
        }
        if (_actualFileName.includes('BRA')) {
            remoteExtendedFolder = remoteParentDir.concat('/Brazil');
        }
        if (_actualFileName.includes('NOVOFARMA')) {
            remoteExtendedFolder = remoteParentDir.concat('/Chile');
        }
        if (_actualFileName.includes('QUINTILES')) {
            remoteExtendedFolder = remoteParentDir.concat('/Columbia');
        }
        if (_actualFileName.includes('_FSHR_FCS-UK_')) {
            remoteExtendedFolder = remoteParentDir.concat('/Horsham');
        }
        if (_actualFileName.includes('MEXICO')) {
            remoteExtendedFolder = remoteParentDir.concat('/Mexico');
        }
        if (_actualFileName.includes('Russia')) {
            remoteExtendedFolder = remoteParentDir.concat('/Moscow');
        }
        if (_actualFileName.includes('HERSIL')) {
            remoteExtendedFolder = remoteParentDir.concat('/Peru');
        }
        if (_actualFileName.includes('Africa')) {
            remoteExtendedFolder = remoteParentDir.concat('/Pretoria');
        }
        if (_actualFileName.includes('Singapore')) {
            remoteExtendedFolder = remoteParentDir.concat('/Singapore');
        }

        _proc.logInfo(_mboxInfo + "remoteExtendedFolder is :: " + remoteExtendedFolder);
        _proc.logInfo(_mboxInfo + "actual file name is :: " + _actualFileName);
        sftpRequest.changeDirectory(remoteExtendedFolder);

        var progressIndicator = _proc.getCustomProperties().getProperty("file_transfer_status_indicator");
        _proc.logInfo(_mboxInfo + "progressIndicator is ::" + progressIndicator);

        var useTriggerFileEx = String(_proc.getCustomProperties().getProperty('use_trigger_file_extension'));
        _proc.logInfo(_mboxInfo + "use_trigger_file_extension is ::" + useTriggerFileEx);

        var replaceExtnWithTrigger = (String(_proc.getCustomProperties().getProperty('replace_extension_with_trigger')) == "true");
        _proc.logInfo(_mboxInfo + "replace_extension_with_trigger is ::" + replaceExtnWithTrigger);

        var fileNameWithProgressIndicatorStatus = function() {
            if (progressIndicator) {
                return (_actualFileName + progressIndicator);
            } else {
                return (_actualFileName);
            }
        };

        _proc.logInfo(_mboxInfo + "put file - name - " + fileNameWithProgressIndicatorStatus());

        file = new java.io.File(localParentDir + "/" + _actualFileName);
        var isFileModified = com.liaison.mailbox.service.util.MailBoxUtil.validateLastModifiedTolerance(file.toPath());
        if (isFileModified) {
            _proc.logInfo(_mboxInfo + "ingnoring the file since it is modified with in the given time limit ::" + _actualFileName);
            return;
        }

        inputStream = new java.io.FileInputStream(file);

        //setting the default
        replyCode = 0;

        if (_actualFileName != 'QLiaison_Mock.test') {
            replyCode = sftpRequest.putFile(fileNameWithProgressIndicatorStatus(), inputStream);
            _proc.logInfo(_mboxInfo + "replyCode : " + replyCode);
        }

        // File Uploading done successfully so rename the file
        //TODO handle if the upload is failed

        if (!isEmpty(useTriggerFileEx)) {
            replyCode = 0;
            var triggerFileName = _actualFileName;
            //Remove the extension if it is set ot true
            if (replaceExtnWithTrigger) {
                triggerFileName = triggerFileName.substring(0, triggerFileName.indexOf('.'));
            }
            replyCode = sftpRequest.putFile(triggerFileName + useTriggerFileEx, inputStream);
        }

        if (replyCode === 0) {
            if (file['delete']()) {
                _proc.logInfo(_mboxInfo + "deleted the file " + _actualFileName);
            }
        }

        if (_actualFileName == 'QLiaison_Mock.test') {
            msg = "File " + file.getName() + " (MOCK) uploaded successfully. " + _mboxInfo + _MSGContext + remoteExtendedFolder;
        } else {
            msg = "File " + file.getName() + " uploaded successfully. " + _mboxInfo + _MSGContext + remoteExtendedFolder;
        }

        _proc.logToLens(msg, file, Packages.com.liaison.mailbox.enums.ExecutionState.COMPLETED);

    } catch (e) {

        _proc.logError(_mboxInfo + "exception in upload " + e);
        msg = "File " + file.getName() + " is failed to upload to the remote location: " + _MSGContext + " " + e;
        _proc.logToLens(msg, file, Packages.com.liaison.mailbox.enums.ExecutionState.FAILED);

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
var cleanup = function(_proc) {
    _proc.logDebug('clean up');
    _proc.cleanup();
};

var isEmpty = function(str) {
    return (!str || 0 === str.length || "null" === str);
};
