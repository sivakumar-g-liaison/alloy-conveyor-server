var rest = myApp.controller(
    'ProcessorCntrlr', ['$rootScope', '$scope', '$timeout',
        '$filter', '$location', '$log', '$blockUI',
        function ($rootScope, $scope, $timeout, $filter,
            $location, $log, $blockUI) {
			
			//for loading js from git
			$scope.constructedGitUrl = "http://" + $rootScope.javaProperties.gitlabHost +"/"+ $rootScope.javaProperties.gitlabProjectName + "/" + $rootScope.javaProperties.gitlabBranchName;
			$scope.isGitUrlSelected = '1';

	    	//for pipeLineId
			if($rootScope.pipelineId === null || $rootScope.pipelineId === '') {
				$rootScope.pipelineId = $location.search().pipeLineId;
			}
			$scope.pipeId = $location.search().pipeLineId; 
		
			$scope.disablePipeLineId = false;
    	
             //ssh key implementation
            $scope.disableSSHKeys = true;
            $scope.disableCertificates = true;
    	    $scope.portRequired = true;
            $scope.isPortDisabled = false;
			
			//GMB-155
			$scope.sftpDefaultPort = '22';
			$scope.ftpDefaultPort = '21';
			$scope.ftpsDefaultPort = '989';
            // To be Populated
            $scope.mailBoxId;
            var block = $rootScope.block;
			
            // Function to modify the static properties to have additional properties of "binary"
            // and "passive" for FTP & FTPS protocols.
            $scope.modifyStaticPropertiesBasedOnProtocol = function () {
                if ($scope.processor.protocol === "FTP" || $scope.processor.protocol === "FTPS") {
                    /*issue: Binary and passive is added twice for FTP protocol.Hence condition is checked before adding the values. */
				var indexOfBinary = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'binary');
				var indexOfPassive = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'passive');
                if (indexOfBinary === -1 && indexOfPassive === -1){
                    $scope.allStaticPropertiesThatAreNotAssignedValuesYet.push({
                        "name": "Binary",
                        "id": "binary"
                    }, {
                        "name": "Passive",
                        "id": "passive"
                    });
                }
				var indexOfBinaryForAllStaticProperties = getIndexOfId($scope.allStaticProperties, 'binary');
				var indexOfPassiveForAllStaticProperties = getIndexOfId($scope.allStaticProperties, 'passive');	
				if (indexOfBinaryForAllStaticProperties === -1 && indexOfPassiveForAllStaticProperties === -1){
                    $scope.allStaticProperties.push({
                        "name": "Binary",
                        "id": "binary"
                    }, {
                        "name": "Passive",
                        "id": "passive"
                    });
                 }   
                } else {
                    // Remove binary and passive properties from the array allStaticPropertiesThatAreNotAssignedValuesYet
                    var indexOfBinary = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'binary');
                    if (indexOfBinary !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfBinary, 1);
                    var indexOfPassive = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'passive');
                    if (indexOfPassive !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfPassive, 1);
                    // Remove binary and passive properties from the array allStaticProperties
                    indexOfBinary = getIndexOfId($scope.allStaticProperties, 'binary');
                    if (indexOfBinary !== -1) $scope.allStaticProperties.splice(indexOfBinary, 1);
                    indexOfPassive = getIndexOfId($scope.allStaticProperties, 'passive');
                    if (indexOfPassive !== -1) $scope.allStaticProperties.splice(indexOfPassive, 1);
                }
            };
            // Function to modify the static properties to have additional properties specific for Directory Sweeper.
            $scope.modifyStaticPropertiesBasedOnProcessorType = function () {
                if ($scope.procsrType.id === "SWEEPER") {
                    $scope.allStaticPropertiesThatAreNotAssignedValuesYet.push({
                        "name": "File Rename Format",
                        "id": "filerenameformat"
                    }, {
                        "name": "Swept File Location",
                        "id": "sweepedfilelocation"
                    }, {
                        "name": "Payload Size Threshold",
                        "id": "payloadsizethreshold"
                    }, {
                        "name": "Number of File Threshold",
                        "id": "numoffilesthreshold"
                    });
                } else {
                    // Remove the static properties specific to processor Type SWEEPER from array allStaticPropertiesThatAreNotAssignedValuesYet
                    var indexOfFileRenameFormat = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'filerenameformat');
                    if (indexOfFileRenameFormat !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfFileRenameFormat, 1);
                    var indexOfPayloadThreshold = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'payloadsizethreshold');
                    if (indexOfPayloadThreshold !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfPayloadThreshold, 1);
                    var indexOfSweepedFileLocation = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'sweepedfilelocation');
                    if (indexOfSweepedFileLocation !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfSweepedFileLocation, 1);
                    var indexOfNumberOfFilesThreshold = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'numoffilesthreshold');
                    if (indexOfNumberOfFilesThreshold !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfNumberOfFilesThreshold, 1);
                }
            };
            $scope.loadOrigin = function () {
				
				//GMB-196
				$scope.sorting = 'name';
                $scope.isFileSelected = false;
        		$scope.isEdit = false;
                $scope.isProcessorTypeSweeper = false;
                $scope.mailboxName = $location.search().mbxname;
                
                $scope.isPrivateKeySelected = false;
                $scope.isPublicKeySelected = false;
                
                //Model for Add MB
                addRequest = $scope.addRequest = {
                    addProcessorToMailBoxRequest: {
                        processor: {}
                    }
                };
                editRequest = $scope.editRequest = {
                    reviseProcessorRequest: {
                        processor: {}
                    }
                };
                $scope.glyphIconColorForProcessorProperties = {
                    color: "glyphicon-white"
                };
                $scope.processor = {
                    guid: "",
                    name: "",
                    type: "",
                    javaScriptURI: "",
                    description: "",
                    status: "",
                    protocol: "",
                    linkedMailboxId: "",
                    linkedProfiles: [],
                    folders: [],
                    credentials: [],
                    dynamicProperties: [],
                    remoteProcessorProperties: {}
                };
                $scope.modal = {
                    "roleList": '',
                    "uri": ''
                };
				
                $scope.certificateModal = {
                    "certificates": '',
                    "certificateURI": '',
                    "isGlobalTrustore": '1',
                    "trustStoreGroupId": ''
                };
                
              // ssh key implementation
                $scope.sshkeyModal = {
                    "sshKeys": '',
                    "sshPrivateKeyURI": '',
                    "sshPublicKeyURI": '',
                    "sshKeyPairPguid": '',
                    "sshKeyPairPassphrase":'',
                    "sshKeyPairConfirmPassphrase":''
                };
                $scope.sshKeys = [];
                $scope.processor.remoteProcessorProperties = {
                    otherRequestHeader: []
                };
                $scope.enumstats = [{
                    "name": "Active",
                    "id": "ACTIVE"
                }, {
                    "name": "Inactive",
                    "id": "INACTIVE"
                }];
                $scope.status = $scope.enumstats[0];
                // Enum for procsr type
                $scope.enumprocsrtype = [{
                    "name": "Remote Downloader",
                    "id": "REMOTEDOWNLOADER"
                }, {
                    "name": "Remote Uploader",
                    "id": "REMOTEUPLOADER"
                }, {
                    "name": "Directory Sweeper",
                    "id": "SWEEPER"
                }];
                $scope.procsrType = $scope.enumprocsrtype[0];
                // Enum for protocol type
                $scope.enumprotocoltype = [
                    'FTP',
                    'FTPS',
                    'HTTP',
                    'HTTPS',
                    'SFTP'
                ];
                $scope.enumHttpVerb = [
                   'DELETE',
			       'GET',
				   'POST',
                   'PUT'          
                ];
              $scope.enumContentType = [                                                                                  
                 'application/atom+xml',
				 'application/EDIFACT',
				 'application/edi-consent',
				 'application/EDI-X12',
				 'application/json',
				 'application/octet-stream',
				 'application/PKCS7-mime',
				 'application/PKCS7-signature',
				 'application/svg+xml',
				 'application/xhtml+xml',
				 'application/xml',
                 'application/x-www-form-urlencoded',
				 'message/disposition-notification',
				 'multipart/form-data',  
				 'multipart/report',
				 'multipart/signed',                                 
                 'text/html',
                 'text/plain',
                 'text/xml'
               ];                
                $scope.verb = $scope.enumHttpVerb[0];
                $scope.processor.protocol = $scope.enumprotocoltype[0];
                $scope.content = $scope.enumContentType[0];
                // applying boolean value for chunked encoding
                $scope.booleanValues = [
                    false,
                    true
                ];
                // Procsr Dynamic Props
                $scope.sweeperMandatoryProperties = [{
                    name: 'PipeLine Id',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
                $scope.ftpMandatoryProperties = [{
                    name: 'URL',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'Port',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
                $scope.httpMandatoryProperties = [{
                    name: 'URL',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                },{
                    name: 'Port',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'HTTP Version',
                    value: '1.1',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'HTTP Verb',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'Content Type',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
                $scope.processorProperties = $scope.ftpMandatoryProperties;
                // Procsr Folder Props
                $scope.processorFolderProperties = [{
                    folderURI: '',
                    folderType: '',
                    folderDesc: '',
                    isMandatory: false,
                    allowAdd: 'true'
                }];
                // Procsr Credential Props
                $scope.processorCredProperties = [{
                    credentialURI: '',
                    credentialType: '',
                    userId: '',
                    password: '',
                    idpType: '',
                    idpURI: '',
                    allowAdd: 'true',
                    passwordDirtyState: ''
                }];
                // Default values of payloadsize and no of files threshold
                $scope.payloadSizeThreshold = 131072;
                $scope.numberOfFilesThreshold = 10;
                $scope.allStaticPropertiesThatAreNotAssignedValuesYet = [{
                    "name": "add new -->",
                    "id": "add new -->"
                }, {
                    "name": "Socket Timeout",
                    "id": "socketTimeout"
                }, {
                    "name": "Connection Timeout",
                    "id": "connectionTimeout"
                }, {
                    "name": "Retry Attempts",
                    "id": "retryAttempts"
                }, {
                    "name": "Chunked Encoding",
                    "id": "chunkedEncoding"
                }, {
                    "name": "Encoding Format",
                    "id": "encodingFormat"
                }, {
                    "name": "OtherRequest Header",
                    "id": "otherRequestHeader"
                }, {
                    "name": "Processed File Location",
                    "id": "processedfilelocation"
                }, {
                    "name": "Error File Location",
                    "id": "errorfilelocation"
                }];
                $scope.allStaticProperties = [{
                    "name": "Socket Timeout",
                    "id": "socketTimeout"
                }, {
                    "name": "Connection Timeout",
                    "id": "connectionTimeout"
                }, {
                    "name": "Retry Attempts",
                    "id": "retryAttempts"
                }, {
                    "name": "Chunked Encoding",
                    "id": "chunkedEncoding"
                }, {
                    "name": "Encoding Format",
                    "id": "encodingFormat"
                }, {
                    "name": "OtherRequest Header",
                    "id": "otherRequestHeader"
                }];
                $scope.dynamicPropertiesDisplayedAsStaticProperties = [{
                    "name": "Processed File Location",
                    "id": "processedfilelocation"
                }, {
                    "name": "File Rename Format",
                    "id": "filerenameformat"
                }, {
                    "name": "Swept File Location",
                    "id": "sweepedfilelocation"
                }, {
                    "name": "Payload Size Threshold",
                    "id": "payloadsizethreshold"
                }, {
                    "name": "Number of File Threshold",
                    "id": "numoffilesthreshold"
                 }, {
                	"name": "Error File Location",
                    "id": "errorfilelocation"
                }];
                 $scope.allStaticAndDynamicProperties = [{
                    "name": "Socket Timeout",
                    "id": "socketTimeout"
                }, {
                    "name": "Connection Timeout",
                    "id": "connectionTimeout"
                }, {
                    "name": "Retry Attempts",
                    "id": "retryAttempts"
                }, {
                    "name": "Chunked Encoding",
                    "id": "chunkedEncoding"
                }, {
                    "name": "Encoding Format",
                    "id": "encodingFormat"
                }, {
                    "name": "Port",
                    "id": "port"
                }, {
                    "name": "OtherRequest Header",
                    "id": "otherRequestHeader"
                }, {
                    "name": "Processed File Location",
                    "id": "processedfilelocation"
                }, {
                    "name": "File Rename Format",
                    "id": "filerenameformat"
                }, {
                    "name": "Swept File Location",
                    "id": "sweepedfilelocation"
                }, {
                    "name": "Payload Size Threshold",
                    "id": "payloadsizethreshold"
                }, {
                    "name": "Number of File Threshold",
                    "id": "numoffilesthreshold"
                 }, {
                 "name": "Error File Location",
                    "id": "errorfilelocation"
                }, {
                    "name": "Binary",
                    "id": "binary"
                }, {
                    "name": "Passive",
                    "id": "passive"
                }];

                // function to modify the static properties if the protocol is FTP or FTPS
                $scope.modifyStaticPropertiesBasedOnProtocol();
                $scope.allMandatoryFtpProperties = [{
                    "name": "URL",
                    "id": "url"
                }, {
                    "name": "Port",
                    "id": "port"
                }];
                $scope.allMandatorySweeperProperties = [{
                    "name": "PipeLine Id",
                    "id": "pipeLineID"
                }];
                $scope.allMandatoryHttpProperties = [{
                    "name": "HTTP Version",
                    "id": "httpVersion"
                }, {
                    "name": "HTTP Verb",
                    "id": "httpVerb"
                }, {
                    "name": "URL",
                    "id": "url"
                }, {
                    "name": "Content Type",
                    "id": "contentType"
                } , {
                    "name": "Port",
                    "id": "port"
                }];
				
				$scope.allStaticPropertiesForDownloaderProcessorFolder = [{
                    "name": "Remote Payload Location",
                    "id": "PAYLOAD_LOCATION"
                },{
                    "name": "Local Target Location",
                    "id": "RESPONSE_LOCATION"
                }];
				
				$scope.allStaticPropertiesForUploaderProcessorFolder = [{
                    "name": "Local Payload Location",
                    "id": "PAYLOAD_LOCATION"
                },{
                    "name": "Remote Target Location",
                    "id": "RESPONSE_LOCATION"
                }];
				
				$scope.allStaticPropertiesForSweeperProcessorFolder = [{
                    "name": "Payload Location",
                    "id": "PAYLOAD_LOCATION"
                }];
				
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = [{
                    "name": "Remote Payload Location",
                    "id": "PAYLOAD_LOCATION"
                },{
                    "name": "Local Target Location",
                    "id": "RESPONSE_LOCATION"
                }];
                $scope.allStaticPropertiesForProcessorFolder = [{
					"name": "Remote Payload Location",
					"id": "PAYLOAD_LOCATION"
				},{
					"name": "Remote Target Location",
					"id": "RESPONSE_LOCATION"
				},{
					"name": "Local Payload Location",
					"id": "PAYLOAD_LOCATION"
				},{
					"name": "Local Target Location",
					"id": "RESPONSE_LOCATION"
				},{
					"name": "Payload Location",
					"id": "PAYLOAD_LOCATION"
				}];
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential = [
	                {
	                    "name": "Login Credential",
	                    "id": "LOGIN_CREDENTIAL"
	                }];
                $scope.allStaticPropertiesForProcessorCredential = [
	                {
	                    "name": "Login Credential",
	                    "id": "LOGIN_CREDENTIAL"
	                }];
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp = [{
                    name: "FTPS",
                    id: "FTPS"
                }, {
                    name: "SFTP",
                    id: "SFTP"
                }];
                $scope.allStaticPropertiesForProcessorCredentialIdp = [{
                    name: "FTPS",
                    id: "FTPS"
                }, {
                    name: "SFTP",
                    id: "SFTP"
                }];
                $scope.disableAddNewTextBox = 'true';
                $scope.valueSelectedinSelectionBox = {
                    value: ''
                };
                $scope.showAddNew = {
                    value: 'false'
                };
                $scope.addedProperty = {
                    value: ''
                };
                /*Folder*/
                $scope.valueSelectedinSelectionBoxForProcessorFolder = {
                    value: ''
                };
                /*Credential*/
                $scope.valueSelectedinSelectionBoxForProcessorCredential = {
                    value: ''
                };
                /*Credential Idp*/
                $scope.valueSelectedinSelectionBoxForProcessorCredentialIdp = {
                    value: ''
                };
                // Profile Related Stuff.
                $scope.allProfiles = [];
                $scope.selectedProfiles = [];
            };
            $scope.loadOrigin();
            
            /*This function is used to identify the protocol and enable dynamic property accordingly in gridOptionsForProcessor grid */
			$scope.getProtocolProperty = function() {
			
				if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
					return "httpMandatoryProperty";
				}
				if ($scope.processor.protocol === 'FTP' || $scope.processor.protocol === 'FTPS') {
					return "ftpMandatoryProperty";
				}
				return "sftpmandatoryProperty";
				
			};
            
            $scope.gridOptionsForProcessor = {
                data: 'processorProperties',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 80,
				enableColumnResize : true,
				plugins: [new ngGridFlexibleHeightPlugin()],
                columnDefs: [{
                    field: "name",
                    width: "50%",
                    displayName: "Name*",
                    enableCellEdit: false,
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" sort-name="sorting" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} add-new="showAddNew" added-property="addedProperty" icon-color="glyphIconColorForProcessorProperties" />'
                }, {
                    field: "value",
                    width: "43%",
                    displayName: "Value*",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="getId(allStaticAndDynamicProperties, row)">\n\
                    	<div class="alignDiv" ng-switch-when="">\n\
                            <div ng-switch on="valueSelectedinSelectionBox.value.id">\n\
                                <div ng-switch-when="">\n\
                                    <textarea  class="form-control"  ng-model="COL_FIELD" style="width:90%;height:45px" placeholder="required" />\n\
                                    <a ng-click="isModal(row)" data-toggle="modal" data-backdrop="static" data-keyboard="false" data-target="#valueModal"  class="right">\n\
                                    <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                </div>\n\
                                <div ng-switch-when="chunkedEncoding">\n\
                                    <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD=false" ng-options="property for property in booleanValues"></select>\n\
                                </div>\n\
                                 <div ng-switch-when="binary">\n\
                                    <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD=false" ng-options="property for property in booleanValues"></select>\n\
                                </div>\n\
                                 <div ng-switch-when="passive">\n\
                                    <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD=false" ng-options="property for property in booleanValues"></select>\n\
                                </div>\n\
                                <div ng-switch-when="payloadsizethreshold">\n\
                                     <textarea   class="form-control" ng-model="COL_FIELD" ng-init="COL_FIELD=payloadSizeThreshold" style="width:90%;height:45px" placeholder="required"/>\n\
                                </div>\n\
                                <div ng-switch-when="numoffilesthreshold">\n\
                                      <textarea  class="form-control" ng-model="COL_FIELD" ng-init="COL_FIELD=numberOfFilesThreshold"  style="width:90%;height:45px" placeholder="required"/>\n\
                                </div>\n\
                                <div ng-switch-default>\n\
                                    <textarea  class="form-control" ng-model="COL_FIELD" ng-init="COL_FIELD=null" style="width:90%;height: 45px" placeholder="required" />\n\
                                    <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                                    <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                </div>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-default>\n\
                            <textarea class="form-control" ng-model="COL_FIELD" ng-maxLength=2048 required style="width:90%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i ng-disabled="true" class="glyphicon glyphicon-new-window"></i></a>\n\
                        </div>\n\
						 <div ng-switch-when="pipeLineID">\n\
                            <textarea ng-disabled="disablePipeLineId" class="form-control" ng-model="COL_FIELD" ng-maxLength=2048 required style="width:90%;height: 45px" placeholder="required" />\n\
                        </div>\n\
                        <div ng-switch-when="otherRequestHeader">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" ng-maxLength=512 required style="width:90%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                        </div>\n\
                        <div ng-switch-when="encodingFormat">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" ng-maxLength=512 required style="width:90%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                        </div>\n\
                        <div ng-switch-when="chunkedEncoding">\n\
                            <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-options="property for property in booleanValues"></select>\n\
                        </div>\n\
                        <div ng-switch-when="binary">\n\
                              <div class="alignDiv" ng-switch on = "getProtocolProperty()">\n\
                              	<div ng-switch-when="ftpMandatoryProperty">\n\
                                      <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-options="property for property in booleanValues"></select>\n\
                                </div>\n\
                          		<div ng-switch-default>\n\
                                      <textarea   class="form-control" ng-model="COL_FIELD" ng-maxLength=2048 required style="width:90%;height: 45px" placeholder="required" />\n\
                                      <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                                      <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                </div>\n\
                              </div>\n\
                       </div>\n\
                        <div ng-switch-when="passive">\n\
                              <div class="alignDiv" ng-switch on = "getProtocolProperty()">\n\
                                      <div ng-switch-when="ftpMandatoryProperty">\n\
                                      	<select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-options="property for property in booleanValues"></select>\n\
                                      </div>\n\
                                		<div ng-switch-default>\n\
                                            <textarea   class="form-control" ng-model="COL_FIELD" ng-maxLength=2048 required style="width:90%;height: 45px" placeholder="required" />\n\
                                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                      </div>\n\
                              </div>\n\
                        </div>\n\
                        <div ng-switch-when="httpVerb">\n\
                              <div class="alignDiv" ng-switch on = "getProtocolProperty()">\n\
                                      <div ng-switch-when="httpMandatoryProperty">\n\
                                      	<select ng-model="verb" ng-change="onVerbChange(verb)" ng-options="property for property in enumHttpVerb"></select>\n\
                                      </div>\n\
                          			  <div ng-switch-default>\n\
                                          <textarea   class="form-control" ng-model="COL_FIELD" ng-maxLength=2048 required style="width:90%;height: 45px" placeholder="required" />\n\
                                          <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                                          <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                    </div>\n\
                            </div>\n\
                      </div>\n\
                      <div ng-switch-when="contentType">\n\
                              <div class="alignDiv" ng-switch on = "getProtocolProperty()">\n\
                                      <div ng-switch-when="httpMandatoryProperty">\n\
                                      	<select ng-model="content" ng-change="onContentTypeChange(content)" ng-options="property for property in enumContentType"></select>\n\
                                      </div>\n\
                          			  <div ng-switch-default>\n\
                                          <textarea   class="form-control" ng-model="COL_FIELD" ng-maxLength=2048 required style="width:90%;height: 45px" placeholder="required" />\n\
                                          <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                                          <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                    </div>\n\
                            </div>\n\
                      </div>\n\
                      <div ng-switch-when="httpVersion">\n\
                                  <div class="alignDiv" ng-switch on = "getProtocolProperty()">\n\
                                      <div ng-switch-when="httpMandatoryProperty">\n\
				                            <textarea   class="form-control" ng-model="COL_FIELD" name="httpVersion" ng-pattern="' + $scope.httpVersionPattern + '" required style="width:90%;height: 45px" placeholder="required" />\n\
				                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
				                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
				                            <div ng-show="formAddPrcsr.httpVersion.$dirty && formAddPrcsr.httpVersion.$invalid">\n\
				                                <span class="help-block-custom" ng-show=formAddPrcsr.httpVersion.$error.pattern>Version should be 1.1</span>\n\
				                            </div>\n\
			                            </div>\n\
                          			    <div ng-switch-default>\n\
	                                          <textarea   class="form-control" ng-model="COL_FIELD" ng-maxLength=2048 required style="width:90%;height: 45px" placeholder="required" />\n\
	                                          <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
	                                          <i class="glyphicon glyphicon-new-window"></i></a>\n\
	                                    </div>\n\
	                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="url">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" name="propUrl" ng-pattern="' + $scope.inputPatternForURL + '" required style="width:90%;height: 45px" placeholder="required" ng-change = "OnChangeUrl(row)"/>\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.propUrl.$dirty && formAddPrcsr.propUrl.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.propUrl.$error.pattern>Enter valid URL</span>\n\
                            </div></div>\n\
                        <div ng-switch-when="socketTimeout">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" name="socketTimeout" required style="width:90%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberTimeOutPattern + '" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.socketTimeout.$dirty && formAddPrcsr.socketTimeout.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.socketTimeout.$error.pattern>Must be a numeric value (1-60000).</span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="connectionTimeout">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" name="connectionTimeout" required style="width:90%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberTimeOutPattern + '" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.connectionTimeout.$dirty && formAddPrcsr.connectionTimeout.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.connectionTimeout.$error.pattern>Must be a numeric value (1-60000).</span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="retryAttempts">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" name="retryAttempts" required style="width:90%;height: 45px" placeholder="required" ng-pattern="' + $scope.retryAttemptsPattern + '" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.retryAttempts.$dirty && formAddPrcsr.retryAttempts.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.retryAttempts.$error.pattern>Must be a numeric value (0-4).</span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="port">\n\
                        <div class="alignDiv" ng-switch on="portRequired">\n\
                        <div ng-switch-when="true"><textarea class="form-control" ng-model="COL_FIELD" ng-disabled="isPortDisabled" name="port" required style="width:90%;height: 45px" placeholder="required" ng-pattern="' + $scope.inputPatternForPort + '" />\n\
                            </div><div ng-switch-default>\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" ng-disabled="isPortDisabled" name="port" style="width:90%;height: 45px" ng-pattern="' + $scope.inputPatternForPort + '" />\n\
                            </div></div>\n\
							<div ng-switch on="isPortDisabled">\n\
							<div ng-switch-when="false">\n\
                            <a ng-click="isModal(row)" data-toggle="modal" data-target="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
							</div></div>\n\
							<div ng-show="formAddPrcsr.port.$dirty && formAddPrcsr.port.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.port.$error.pattern>Invalid Data.</span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="payloadsizethreshold">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" name ="payloadSizeThreshold" style="width:90%;height:45px" ng-maxLength=512 placeholder="required" required value=payloadSizeThreshold ng-pattern="' + $scope.numberPattern + '"/>\n\
                                <div ng-show="formAddPrcsr.payloadSizeThreshold.$dirty && formAddPrcsr.payloadSizeThreshold.$invalid">\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.payloadSizeThreshold.$error.pattern>Enter valid number</span>\n\
                                </div>\n\
                        </div>\n\
                        <div ng-switch-when="numoffilesthreshold">\n\
                            <textarea   class="form-control" ng-model="COL_FIELD" name="numoffilesthreshold" style="width:90%;height:45px" ng-maxLength=512 placeholder="required" required value=numberOfFilesThreshold ng-pattern="' + $scope.numberPattern + '"/>\n\
                                <div ng-show="formAddPrcsr.numoffilesthreshold.$dirty && formAddPrcsr.numoffilesthreshold.$invalid">\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.numoffilesthreshold.$error.pattern>Enter valid number</span>\n\
            					</div>\n\
                        </div>\n\
                    </div>'
                }, {
                    field: "allowAdd",
                    width: "7%",
                    enableCellEdit: false,
                    displayName: "Action",
                    sortable: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div class="alignButton" ng-switch-when="true">\n\
                                            <button   ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties,addedProperty)"><i class="glyphicon glyphicon-plus-sign" ng-class="glyphIconColorForProcessorProperties.color"></i></button></div>' +
                        '<div ng-switch-when="false">\n\
                                           <div ng-switch on="row.getProperty(\'isMandatory\')">' +
                        '<div ng-switch-when="true">-NA-</div>' +
                        '<div ng-switch-when="false"><button ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties,valueSelectedinSelectionBox)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                        '</div>\n\
                                    </div>\n\
                             </div>'
                }]
            };
           $scope.isPortDisabled = false;
            $scope.OnChangeUrl = function(row) {
               
			   var url = row.entity.value;
			   if(typeof url !== 'undefined') {
					var ip = url.split('/')[2].split(':')[0];
					var port = url.split('/')[2].split(':')[1]; 
                     for(i = 0; i < $scope.processorProperties.length; i++) {
                        if ($scope.processorProperties[i].name === 'Port') {
							if ($scope.inputPatternForPort.test(port)) {
								$scope.isPortDisabled = true;
							} else {
                                $scope.isPortDisabled = false;    
							}
							if (typeof port !== 'undefined' && port !== '') {
								$scope.processorProperties[i].value = port;
							} else {
								$scope.defaultPortValue();
								$scope.isPortDisabled = false;
							}
                            if(port === '') $scope.isPortDisabled = false;
                       }
                    }
				} else {
                    for(i = 0; i < $scope.processorProperties.length; i++) {
                        if ($scope.processorProperties[i].name === 'Port') {
                            $scope.processorProperties[i].value = '';
							$scope.defaultPortValue();
                            $scope.isPortDisabled = false;
                       }
                    }
                }
                               
           }     
            
            $scope.getFolderId = function (objArray, row) {
                return getId(objArray, row.getProperty('folderType'));
            };
            $scope.getCredentialId = function (objArray, row) {
                return getId(objArray, row.getProperty('credentialType'));
            };
            $scope.getId = function (objArray, row) {
                if (row.getProperty('name') === '') {
                    return '';
                }
                var ftpVal = getId($scope.allMandatoryFtpProperties, row.getProperty('name'));
                if (ftpVal.length > 0) {
                    return ftpVal;
                }
                var httpVal = getId($scope.allMandatoryHttpProperties, row.getProperty('name'));
                if (httpVal.length > 0) {
                    return httpVal;
                }
                var sweeperVal = getId($scope.allMandatorySweeperProperties, row.getProperty('name'));
                if (sweeperVal.length > 0) {
                    return sweeperVal;
                }
                var val = getId(objArray, row.getProperty('name'));
                if (val.length > 0) {
                    return val;
                }
                return "Dor%^7#@";
            };
            $scope.getIdValue = function (name) {
                var ftpVal = getId($scope.allMandatoryFtpProperties, name);
                if (ftpVal.length > 0) {
                    return ftpVal;
                }
                var httpVal = getId($scope.allMandatoryHttpProperties, name);
                if (httpVal.length > 0) {
                    return httpVal;
                }
                var sweeperVal = getId($scope.allMandatorySweeperProperties, name);
                if (sweeperVal.length > 0) {
                    return sweeperVal;
                }
                return getId($scope.allStaticProperties, name);
            };
            $scope.getNameValue = function (id) {
                var ftpVal = getName($scope.allMandatoryFtpProperties, id);
                if (ftpVal.length > 0) {
                    return ftpVal;
                }
                var httpVal = getName($scope.allMandatoryHttpProperties, id);
                if (httpVal.length > 0) {
                    return httpVal;
                }
                var sweeperVal = getName($scope.allMandatorySweeperProperties, id);
                if (sweeperVal.length > 0) {
                    return sweeperVal;
                }
                return getName($scope.allStaticProperties, id);
            };
            $scope.gridOptionsForProcessorFolder = {
                data: 'processorFolderProperties',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 80,
				enableColumnResize : true,
				plugins: [new ngGridFlexibleHeightPlugin()],
                columnDefs: [{
                    field: "folderURI",
                    width: "33%",
                    displayName: "URI*",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">' +
                        '<div class="alignDiv" ng-switch-when="false">' +
                        '<div ng-switch on="getFolderId(allStaticPropertiesForProcessorFolder, row)">\n\
                        <div ng-switch-when="PAYLOAD_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD"  style="width:95%;height:45px" required  placeholder="required" name="folderuripayload" ng-pattern="' + $scope.inputPatternForFolderURI + '" ng-maxLength=250 />\n\
                                <div ng-show="formAddPrcsr.folderuripayload.$dirty && formAddPrcsr.folderuripayload.$invalid">\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.folderuripayload.$error.pattern>Invalid Folder URI.</span>\n\
                                    <span class="custom-info-block" ng-show=formAddPrcsr.folderuripayload.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
                           </div></div>\n\
                        <div ng-switch-when="RESPONSE_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD"  style="width:95%;height:45px" required  placeholder="required" name="folderuriresponse" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                            <div ng-show="formAddPrcsr.folderuriresponse.$dirty && formAddPrcsr.folderuriresponse.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.folderuriresponse.$error.pattern>Invalid Folder URI.</span>\n\
                                <span class="custom-info-block" ng-show=formAddPrcsr.folderuriresponse.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span></div></div>\n\
                        </div></div>' +
                        '<div ng-switch-when="true">' +
                        '<textarea   class="form-control" name="folderuridefault" ng-model="COL_FIELD" style="width:95%;height:45px" placeholder="required" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                    <div ng-show="formAddPrcsr.folderuridefault.$dirty && formAddPrcsr.folderuridefault.$invalid">\n\
                             <span class="help-block-custom" ng-show=formAddPrcsr.folderuridefault.$error.pattern>Invalid Folder URI.</span>\n\
                             <span class="custom-info-block" ng-show=formAddPrcsr.folderuridefault.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
                        </div></div></div>'
                }, {
                    field: "folderType",
                    width: "20%",
                    displayName: "Type*",
                    enableCellEdit: false,
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder" selected-value="valueSelectedinSelectionBoxForProcessorFolder" prop-name={{row.getProperty(col.field)}}/>'
                }, {
                    field: "folderDesc",
                    width: "40%",
                    displayName: "Description",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">' +
                        '<div class="alignDiv" ng-switch-when="false">' +
                        '<div ng-switch on="getFolderId(allStaticPropertiesForProcessorFolder, row)">\n\
                        <div ng-switch-when="PAYLOAD_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD"  style="width:95%;height:45px" name="descriptionpayload" ng-pattern="' + $scope.userInputDescriptionPattern + '" ng-maxLength=250 />\n\
                                <div ng-show="formAddPrcsr.descriptionpayload.$dirty && formAddPrcsr.descriptionpayload.$invalid">\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.descriptionpayload.$error.pattern>Invalid Description.</span>\n\
                                    <span class="custom-info-block" ng-show=formAddPrcsr.descriptionpayload.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
                           </div></div>\n\
                        <div ng-switch-when="RESPONSE_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD"  style="width:95%;height:45px" name="descriptionresponse" ng-pattern="' + $scope.userInputDescriptionPattern + '" ng-maxLength=250 />\n\
                            <div ng-show="formAddPrcsr.descriptionresponse.$dirty && formAddPrcsr.descriptionresponse.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.descriptionresponse.$error.pattern>Invalid Description.</span>\n\
                                <span class="custom-info-block" ng-show=formAddPrcsr.descriptionresponse.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span></div></div>\n\
                        </div></div>' +
                        '<div ng-switch-when="true">' +
                        '<textarea   class="form-control" name="descriptiondefault" ng-model="COL_FIELD" style="width:95%;height:45px" ng-pattern="' + $scope.userInputDescriptionPattern + '" ng-maxLength=250/>\n\
                    <div ng-show="formAddPrcsr.descriptiondefault.$dirty && formAddPrcsr.descriptiondefault.$invalid">\n\
                        <span class="help-block-custom" ng-show=formAddPrcsr.descriptiondefault.$error.pattern>Invalid Description.</span>\n\
                        <span class="custom-info-block" ng-show=formAddPrcsr.descriptiondefault.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
                        </div></div></div>'
                }, {
                    field: "allowAdd",
                    width: "7%",
                    displayName: "Action",
                    enableCellEdit: false,
                    sortable: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div class="alignButton" ng-switch-when="true"><button ng-click="addFolderRow(row,valueSelectedinSelectionBoxForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                        '<div ng-switch-when="false"><div ng-switch on="row.getProperty(\'isMandatory\')"><div ng-switch-when="false"><button ng-click="removeFolderRow(row,allStaticPropertiesForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div><div ng-switch-when="true">-NA-</div></div></div>' +
                        '</div>'
                }]
            };
            // Credentials for Grid Options
            $scope.gridOptionsForProcessorCredential = {
                data: 'processorCredProperties',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 100,
				enableColumnResize : true,
				plugins: [new ngGridFlexibleHeightPlugin()],
                columnDefs: [{
                    field: "credentialURI",
                    width: "20%",
                    displayName: "Name",
                    enableCellEdit: false,
                    cellTemplate: '<div>{{row.getProperty(\'credentialURI\')}}</div>'
                }, {
                    field: "credentialType",
                    width: "20%",
                    displayName: "Type*",
                    enableCellEdit: false,
                    cellTemplate: '<!--<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential" selected-value="valueSelectedinSelectionBoxForProcessorCredential" prop-name={{row.getProperty(col.field)}}/>-->'+
                    '<div ng-switch on = row.getProperty(\'credentialType\')>'+
                    '<div ng-switch-when="Login Credential"><div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential" selected-value="valueSelectedinSelectionBoxForProcessorCredential" prop-name={{row.getProperty(col.field)}}/></div>'+
                    '<div ng-switch-when="SSH_KEYPAIR"><div ng-switch on = row.getProperty(\'idpType\')>\n\
                    <div ng-switch-when="PRIVATE">SSH Private Key</div><div ng-switch-when="PUBLIC">SSH Public Key</div></div></div>'+
                    '<div ng-switch-when="TRUSTSTORE_CERT">TrustStore Certificate</div>'+
                    '<div ng-switch-default><div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential" selected-value="valueSelectedinSelectionBoxForProcessorCredential" prop-name={{row.getProperty(col.field)}}/></div>'+
                    '</div>'
               }, {
                    field: "userId",
                    width: "20%",
                    displayName: "UserId",
                    enableCellEdit: false,
                   cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">'+
                    '<div ng-switch-when="true"><input type="text" ng-model="COL_FIELD" class="textboxingrid"></div>'+
                    '<div ng-switch-when="false"><div ng-switch on="row.getProperty(\'credentialType\')">'+
                    '<div ng-switch-when="Login Credential"><input type="text" ng-model="COL_FIELD" class="textboxingrid"></div>'+
                    '<div ng-swith-default></div>'+
                    '</div></div>'
                }, {
                    field: "password",
                    width: "20%",
                    displayName: "Password",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">'+
                    '<div ng-switch-when="true"><div class="passwordDirective" password={{row.getProperty(col.field)}} row-entity="row.entity" col-filed="col.field"/></div>'+
                    '<div ng-switch-when="false"><div ng-switch on="row.getProperty(\'credentialType\')">'+
                    '<div ng-switch-when="Login Credential"><div class="passwordDirective" password={{row.getProperty(col.field)}} row-entity="row.entity" col-filed="col.field"/></div>'+
                    '<div ng-swith-default></div></div>'+
                    '</div></div>'
                }, {
                    field: "idpURI",
                    width: "0%",
                    displayName: "IdpURI",
                    enableCellEdit: false
                }, {
                    field: "idpType",
                    width: "0%",
                    displayName: "IdpType",
                    enableCellEdit: false
                }, {
                    field: "allowAdd",
                    width: "20%",
                    displayName: "Action",
                    enableCellEdit: false,
                    sortable: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addCredentialRow(row,valueSelectedinSelectionBoxForProcessorCredential,valueSelectedinSelectionBoxForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                        '<div ng-switch-when="false"><button ng-click="removeCredentialRow(row,allStaticPropertiesForProcessorCredential,allStaticPropertiesForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                        '</div>'
                }]
            };
            $scope.onVerbChange = function (httpVerb) {
                $scope.verb = httpVerb;
            };
			$scope.onContentTypeChange = function (contentType) {
                $scope.content = contentType;
            };			
            $scope.initialLoad = function () {
                $scope.readAllProcessors();
                $scope.readAllProfiles();
            };
            //$scope.readOnlyProcessors = false;
            // Grid Setups
            $scope.filterOptions = {
                filterText: "",
                useExternalFilter: true
            };
            //Paging set up
            $scope.totalServerItems = 0;
            $scope.pagingOptions = {
                pageSizes: [5, 10, 50],
                pageSize: 5,
                currentPage: 1
            };
            $scope.readAllProcessors = function () {
                $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId + '?addServiceInstanceIdConstraint=' + true, //Get mail box Data
                    function (data) {
                        $scope.getPagedDataAsync(data,
                            $scope.pagingOptions.pageSize,
                            $scope.pagingOptions.currentPage);
                    }
                );
            };
            $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
                setTimeout(function () {
                    $scope.setPagingData(largeLoad.getMailBoxResponse.mailBox.processors, page, pageSize);
                }, 100);
            };
            // Set the paging data to grid from server object
            $scope.setPagingData = function (data, page, pageSize) {
                if (data === null || data.length <= 0) {
                    $scope.message = 'No results found.';
                    $scope.openMessage();
                }
                var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
                $scope.processorList = pagedData;
                $scope.totalServerItems = data.length;
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            };
            $scope.$watch('pagingOptions', function (newVal, oldVal) {
                if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                    $scope.readAllProcessors();
                }
                if (newVal !== oldVal && newVal.pageSize !== oldVal.pageSize) {
                    $scope.readAllProcessors();
                    newVal.currentPage = 1;
                }
            }, true);
            $scope.$watch('filterOptions', function (newVal, oldVal) {
                if (newVal !== oldVal) {
                    $scope.readAllProcessors();
                }
            }, true);
            $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="editProcessor(row.getProperty(\'guid\'),true)"><i class="glyphicon glyphicon-pencil"></i></button>';
            $scope.manageStatus = '<div ng-switch on="row.getProperty(\'status\')"><div ng-switch-when="ACTIVE">Active</div><div ng-switch-when="INACTIVE">Inactive</div></div>';
            $scope.manageType = '<div ng-switch on="row.getProperty(\'type\')"><div ng-switch-when="REMOTEDOWNLOADER">Remote Downloader</div><div ng-switch-when="REMOTEUPLOADER">Remote Uploader</div><div ng-switch-when="SWEEPER">Directory Sweeper</div></div>';
            $scope.gridOptionsForProcessorList = {
                columnDefs: [{
                    field: 'name',
                    displayName: 'Name',
                    width: "400px"
                }, {
                    field: 'type',
                    displayName: 'Type',
                    width: "400px",
                    cellTemplate: $scope.manageType
                }, {
                    field: 'status',
                    displayName: 'Status',
                    width: "250px",
                    cellTemplate: $scope.manageStatus
                }, {
                    displayName: 'Action',
                    sortable: false,
                    width: "77px",
                    cellTemplate: $scope.editableInPopup
                }],
                data: 'processorList',
                //rowTemplate: customRowTemplate,
                enablePaging: true,
                showFooter: true,
                canSelectRows: true,
                multiSelect: false,
                jqueryUITheme: false,
                displaySelectionCheckbox: false,
                pagingOptions: $scope.pagingOptions,
                filterOptions: $scope.filterOptions,
				enableColumnResize : true,
				plugins: [new ngGridFlexibleHeightPlugin()],
                totalServerItems: 'totalServerItems',
            };
            $scope.setRemotePropData = function (reqHeaderArray, value) {
                if (value === 'otherRequestHeader') {
                    var colonArray = [];
                    for (var i = 0; i < reqHeaderArray.length; i++) {
                        colonArray.push(reqHeaderArray[i].name + ':' + reqHeaderArray[i].value);
                    }
                    return colonArray.toString();
                } else if (value === 'httpVerb') {

                    $scope.verb = reqHeaderArray;
                } else if (value === 'contentType') {
					$scope.content = reqHeaderArray;
				} 
            };
            $scope.getPortFromURL = function(url) {
                if(typeof url !== 'undefined'  && url !== null && url !== '') {
					var ip = url.split('/')[2].split(':')[0];
					var port = url.split('/')[2].split(':')[1]; 
                    if (typeof port !== 'undefined') {
                        return port;
                    } else {
                        return '';
                    }
                        
				} else {
                    return '';
                }
            };
			
			$scope.editProcAfterReadSecret = function(data, profData, processorId, blockuiFlag) {
				
				$scope.isEdit = true;
                var procsrId = processorId;
                
				if (blockuiFlag === true) {
					block.unblockUI();
				}
				$scope.allProfiles = profData.getProfileResponse.profiles;
				$scope.clearProps();
				$scope.loadBrowseData();
				$scope.processor.guid = data.getProcessorResponse.processor.guid;
				$scope.processor.name = data.getProcessorResponse.processor.name;
											
				//check if it is the gitlab url
				if (data.getProcessorResponse.processor.javaScriptURI != null && data.getProcessorResponse.processor.javaScriptURI != "") {
					if(data.getProcessorResponse.processor.javaScriptURI.indexOf("gitlab:") != -1) {
						$scope.modal.uri = data.getProcessorResponse.processor.javaScriptURI.split("gitlab:").pop();
						$scope.isGitUrlSelected = '1';
					} else {
						$scope.modal.uri = data.getProcessorResponse.processor.javaScriptURI;
						$scope.isGitUrlSelected = '0';
					}
				}
				$scope.processor.description = data.getProcessorResponse.processor.description;
				(data.getProcessorResponse.processor.status === 'ACTIVE') ? $scope.status = $scope.enumstats[0] : $scope.status = $scope.enumstats[1];
				$scope.setTypeDuringProcessorEdit(data.getProcessorResponse.processor.type);
				$scope.processor.protocol = data.getProcessorResponse.processor.protocol;
				$scope.selectedProfiles = data.getProcessorResponse.processor.profiles;
				//Schedules
				for (var i = 0; i < $scope.selectedProfiles.length; i++) {
					// To remove $$hashKey
					//var profs = angular.fromJson(angular.toJson($scope.allProfiles));
					for (var j = 0; j < $scope.allProfiles.length; j++) {
						if ($scope.selectedProfiles[i].id === $scope.allProfiles[j].id) {
							$scope.allProfiles.splice(j, 1);
							break;
						}
					}
				}
				 if($scope.processor.protocol === "FTP" || $scope.processor.protocol === "SFTP" || $scope.processor.protocol === "FTPS") {
					$scope.portRequired = true;
				} else {
					$scope.portRequired = false;
				}
				
				//GMB 221
				if($scope.processor.protocol === "FTPS" || $scope.processor.protocol === "HTTPS") {
					$scope.disableCertificates = false;
					
				} else {
					$scope.disableCertificates = true;
				}
				$scope.disableSSHKeys = ($scope.processor.protocol === "SFTP")?false:true;

				// Pushing out dynamis props
				$scope.processorProperties = []; //Removing now so that the add new option always shows below the available properties
				$scope.httpMandatoryProperties = [];
				$scope.ftpMandatoryProperties = [];
				$scope.sweeperMandatoryProperties = [];
				$scope.modifyStaticPropertiesBasedOnProtocol();
				$scope.modifyStaticPropertiesBasedOnProcessorType();
				$scope.setFolderData();
				$scope.isPortDisabled = false;
				var json_data = data.getProcessorResponse.processor.remoteProcessorProperties;
				var otherReqIndex = -1;
				var i = 0;
				for (var prop in json_data) {
					var allowPort = false;
					if(prop === 'port' && json_data[prop] == 0) allowPort = true;
				
					if ((json_data[prop] !== 0 || allowPort) && json_data[prop] !== false && json_data[prop] !== null && json_data[prop] !== '') {
						i++;
						if (prop === 'otherRequestHeader' && json_data[prop].length === 0) {
							otherReqIndex = i;
						}
						var propertyValue = null;
						if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
							 if (prop === 'otherRequestHeader' || prop === 'httpVerb' || prop === 'contentType') {
								propertyValue = $scope.setRemotePropData(json_data[prop], prop);
							 } else if (prop === 'port') {
								propertyValue = (json_data[prop] != 0)?json_data[prop]:$scope.getPortFromURL(json_data['url']);
							 } else {
								propertyValue = json_data[prop];
							 }
							$scope.httpMandatoryProperties.push({
								name: $scope.getNameValue(prop),
								value: propertyValue,
								allowAdd: false,
								isMandatory: (getIndexOfId($scope.allMandatoryHttpProperties, prop) === -1) ? false : true
							});
							
							if(prop === 'port' && json_data[prop] != 0 && $scope.getPortFromURL(json_data['url']).length > 0) $scope.isPortDisabled = true;
							
						} else if ($scope.processor.protocol === 'SWEEPER') {

							 if (prop === 'otherRequestHeader') {
								propertyValue = $scope.setRemotePropData(json_data[prop], prop);
								
								if(json_data[prop].length > 0) {
									$scope.sweeperMandatoryProperties.push({
										name: $scope.getNameValue(prop),
										value: propertyValue,
										allowAdd: false,
										isMandatory: (getIndexOfId($scope.allMandatorySweeperProperties, prop) === -1) ? false : true
									});
								}
						   
							 } else if (prop === 'port') {
								propertyValue = (json_data[prop] != 0)?json_data[prop]:$scope.getPortFromURL(json_data['url']);
							 } else if (prop === 'pipeLineID') {
								propertyValue = $scope.pipeId;
								$scope.sweeperMandatoryProperties.push({
								name: $scope.getNameValue(prop),
								value: propertyValue,
								allowAdd: false,
								isMandatory: (getIndexOfId($scope.allMandatorySweeperProperties, prop) === -1) ? false : true
								});
							 } else {
								propertyValue = json_data[prop];
								$scope.sweeperMandatoryProperties.push({
								name: $scope.getNameValue(prop),
								value: propertyValue,
								allowAdd: false,
								isMandatory: (getIndexOfId($scope.allMandatorySweeperProperties, prop) === -1) ? false : true
							});
							 }
																	
						} else {
							 if (prop === 'otherRequestHeader') {
								propertyValue = $scope.setRemotePropData(json_data[prop], prop);
							 } else if (prop === 'port') {
								propertyValue = (json_data[prop] != 0)?json_data[prop]:$scope.getPortFromURL(json_data['url']);
							 } else {
								propertyValue = json_data[prop];
							 }
							$scope.ftpMandatoryProperties.push({
								name: $scope.getNameValue(prop),
								value: propertyValue,
								allowAdd: false,
								isMandatory: (getIndexOfId($scope.allMandatoryFtpProperties, prop) === -1) ? false : true
							});
							
							if(prop === 'port' && json_data[prop] != 0 && $scope.getPortFromURL(json_data['url']).length > 0) $scope.isPortDisabled = true;
						}
						var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, prop);
						if (indexOfElement !== -1) {
							$scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
						}
					}
				}
										
				
				// Condition which executes only if OtherRequest Headers comes with an empty value
				// So that there is no need to show it in the UI
				if (otherReqIndex !== -1) {
					if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
						$scope.httpMandatoryProperties.splice(otherReqIndex - 1, 1);
					} else if ($scope.processor.protocol === 'SWEEPER') {
						$scope.sweeperMandatoryProperties.splice(otherReqIndex - 1, 1);
					} else {
						$scope.ftpMandatoryProperties.splice(otherReqIndex - 1, 1);
					}
					$scope.allStaticPropertiesThatAreNotAssignedValuesYet.push({
						"name": "OtherRequest Header",
						"id": "otherRequestHeader"
					});
				}
				for (var i = 0; i < data.getProcessorResponse.processor.dynamicProperties.length; i++) {
					// To get id as property value for the dynamic properties which are displayed as static properties
					var dynamicPropertyIndex = getIndexOfId($scope.dynamicPropertiesDisplayedAsStaticProperties, data.getProcessorResponse.processor.dynamicProperties[i].name);
					var dynamicPropertyName = (dynamicPropertyIndex === -1) ? data.getProcessorResponse.processor.dynamicProperties[i].name : getName($scope.dynamicPropertiesDisplayedAsStaticProperties, data.getProcessorResponse.processor.dynamicProperties[i].name);
					if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
						/*if (data.getProcessorResponse.processor.dynamicProperties[i].name == 'Port') {
							$scope.httpMandatoryProperties.push({
								name: dynamicPropertyName,
								value: data.getProcessorResponse.processor.dynamicProperties[i].value,
								allowAdd: false,
								isMandatory: true
							});
							if (data.getProcessorResponse.processor.dynamicProperties[i].value !== '') $scope.isPortDisabled = true;
						} else {*/
							$scope.httpMandatoryProperties.push({
								name: dynamicPropertyName,
								value: data.getProcessorResponse.processor.dynamicProperties[i].value,
								allowAdd: false,
								isMandatory: false
							});
						//}
						
					} else if ($scope.processor.protocol === 'SWEEPER') {
						$scope.sweeperMandatoryProperties.push({
							name: dynamicPropertyName,
							value: data.getProcessorResponse.processor.dynamicProperties[i].value,
							allowAdd: false,
							isMandatory: false
						});
					} else {
						$scope.ftpMandatoryProperties.push({
							name: dynamicPropertyName,
							value: data.getProcessorResponse.processor.dynamicProperties[i].value,
							allowAdd: false,
							isMandatory: false
						});
						
						if (data.getProcessorResponse.processor.dynamicProperties[i].name == 'Port' && data.getProcessorResponse.processor.dynamicProperties[i].value !== '')
							$scope.isPortDisabled = true;
						
					}
					// To remove already value assigned properties from array allStaticPropertiesThatAreNotAssignedValuesYet
					var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, data.getProcessorResponse.processor.dynamicProperties[i].name);
					if (indexOfElement !== -1) {
						$scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
					}
				}
				if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
					$scope.httpMandatoryProperties.push({ //Adding now so that the add new option always shows below the available properties
						name: '',
						value: '',
						allowAdd: true,
						isMandatory: false
					});
					$scope.processorProperties = $scope.httpMandatoryProperties;
				} else if ($scope.processor.protocol === 'SWEEPER') {
					$scope.sweeperMandatoryProperties.push({ //Adding now so that the add new option always shows below the available properties
						name: '',
						value: '',
						allowAdd: true,
						isMandatory: false
					});
					$scope.processorProperties = $scope.sweeperMandatoryProperties;
					$scope.disablePipeLineId = true;

				} else {
					$scope.ftpMandatoryProperties.push({ //Adding now so that the add new option always shows below the available properties
						name: '',
						value: '',
						allowAdd: true,
						isMandatory: false
					});
					$scope.processorProperties = $scope.ftpMandatoryProperties;
				}
				$scope.processorFolderProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
				for (var i = 0; i < data.getProcessorResponse.processor.folders.length; i++) {
					$scope.processorFolderProperties.push({
						folderURI: data.getProcessorResponse.processor.folders[i].folderURI,
						folderType: $scope.getFolderTypeDuringProcessorEdit(data.getProcessorResponse.processor.folders[i].folderType),
						folderDesc: data.getProcessorResponse.processor.folders[i].folderDesc,
						isMandatory: ($scope.processor.protocol === 'SWEEPER' && data.getProcessorResponse.processor.folders[i].folderType === 'PAYLOAD_LOCATION') ? true : false,
						allowAdd: false
					});
					var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,
						data.getProcessorResponse.processor.folders[i].folderType);
					if (indexOfElement !== -1) {
						$scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder.splice(indexOfElement, 1);
					}
					
				};
				
				$scope.processorFolderProperties.push({
					folderURI: '',
					folderType: '',
					folderDesc: '',
					allowAdd: (data.getProcessorResponse.processor.type === 'SWEEPER') ? 'false' : 'true'
				});
				
				$scope.processorCredProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
				for (var i = 0; i < data.getProcessorResponse.processor.credentials.length; i++) {
					 var credentialType = (data.getProcessorResponse.processor.credentials[i].credentialType == 'LOGIN_CREDENTIAL')?getName($scope.allStaticPropertiesForProcessorCredential, data.getProcessorResponse.processor.credentials[i].credentialType): data.getProcessorResponse.processor.credentials[i].credentialType;
					$scope.processorCredProperties.push({
						credentialURI: data.getProcessorResponse.processor.credentials[i].credentialURI,
						//credentialType: getName($scope.allStaticPropertiesForProcessorCredential, data.getProcessorResponse.processor.credentials[i].credentialType),
						credentialType:credentialType,
						userId: data.getProcessorResponse.processor.credentials[i].userId,
						password: data.getProcessorResponse.processor.credentials[i].password,
						idpType: data.getProcessorResponse.processor.credentials[i].idpType,
						idpURI: data.getProcessorResponse.processor.credentials[i].idpURI,
						allowAdd: false
					});
					var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,
						data.getProcessorResponse.processor.credentials[i].credentialType);
					if (indexOfElement !== -1) {
						$scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential.splice(indexOfElement, 1);
					}
					var indexOfElementIdp = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,
						data.getProcessorResponse.processor.credentials[i].idpType);
					if (indexOfElementIdp !== -1) {
						$scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp.splice(indexOfElementIdp, 1);
					}
				};
				$scope.processorCredProperties.push({
					credentialURI: '',
					credentialType: '',
					userId: '',
					password: '',
					idpType: '',
					idpURI: '',
					allowAdd: 'true'
				});
				  // To Properly set the sshkey modal and certificate modal values
				$scope.processCredentialDetails();
			}
			
			function readSecretFromKM(url, a, data, profData, procsrId, blockuiFlag) {
				$scope.restService.get(url,
					function (secretData, status) {
						if(status === 200) {
							var decPwd = $.base64.decode($.base64.decode(secretData));
							data.getProcessorResponse.processor.credentials[a].password = decPwd;
							$scope.editProcAfterReadSecret(data, profData, procsrId, blockuiFlag);
						} else if(status === 404) {
							block.unblockUI();
							showSaveMessage('Read secret failed', 'error');
							return;
						} 
					}
				);
			}
			
            $scope.editProcessor = function (processorId, blockuiFlag) {
                
				if (blockuiFlag === true) {
                    block.blockUI();
                }
				
				$scope.formAddPrcsr.$setPristine();
                $scope.loadOrigin();
                //To notify passwordDirective to clear the password and error message
                $scope.doSend();
                $scope.isEdit = true;
                var procsrId = processorId;
                $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + procsrId, //Get mail box Data
                    function (data) {
                        $log.info($filter('json')(data));
                        //Fix: Reading profile in procsr callback
                        $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                            function (profData) {
                                
								$log.info($filter('json')(profData));
								
								if($scope.processor.protocol == 'HTTPS' || $scope.processor.protocol == 'HTTP' || data.getProcessorResponse.processor.credentials.length === 0) {
									$scope.editProcAfterReadSecret(data, profData, procsrId, blockuiFlag);
								} else {
									var editProcessor = false;
									for(var i = 0; i < data.getProcessorResponse.processor.credentials.length; i++) {
										$scope.credType = data.getProcessorResponse.processor.credentials[i].credentialType;
										
										if($scope.credType === 'LOGIN_CREDENTIAL') {
											readSecretFromKM($scope.url_secret_service + data.getProcessorResponse.processor.credentials[i].password, i, data, profData, processorId, blockuiFlag);
											editProcessor = true;
											break;
										}
									}
									if(editProcessor === false) {
										$scope.editProcAfterReadSecret(data, profData, procsrId, blockuiFlag);
									}
								}
                            }
                        );
                    }
                );
            };
            $scope.readAllProfiles = function () {
                $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                    function (data) {
                        $scope.allProfiles = data.getProfileResponse.profiles;
                        $scope.loadBrowseData();
                        $scope.loadCertificateData();
                    }
                );
            };
            $scope.loadBrowseData = function () {
                $scope.restService.get($scope.base_url + '/listFile', //Get mail box Data
                    function (data) {
                        $scope.roleList = data.ArrayList;
                        $log.info($scope.roleList);
                        $scope.modal.roleList = $scope.roleList;
                    }
                );
            };
            $scope.loadCertificateData = function () {
                $scope.restService.get($scope.base_url + '/listCertificates', //Get mail box Data
                    function (data) {
                        $scope.certificates = data.ArrayList;
                        $log.info($scope.certificates);
                        $scope.certificateModal.certificates = $scope.certificates;
                    }
                );
            };
            
           
            $scope.initialLoad();
           
            $scope.chooseProfile = function () {
                for (var i = 0; i < $scope.profilesSelectedinAllProfile.length; i++) {
                    $scope.selectedProfiles.push($scope.profilesSelectedinAllProfile[i]);
                    var indexOfSelectedElement = $scope.allProfiles.indexOf($scope.profilesSelectedinAllProfile[i]);
                    $scope.allProfiles.splice(indexOfSelectedElement, 1);
                }
                $scope.profilesSelectedinAllProfile = [];
            };
            $scope.removeProfile = function () {
                for (var i = 0; i < $scope.profilesSelectedinProcessorProfile.length; i++) {
                    $scope.allProfiles.push($scope.profilesSelectedinProcessorProfile[i]);
                    var indexOfSelectedElement = $scope.selectedProfiles.indexOf($scope.profilesSelectedinProcessorProfile[i]);
                    $scope.selectedProfiles.splice(indexOfSelectedElement, 1);
                }
                $scope.profilesSelectedinProcessorProfile = [];
            };
            $scope.setTypeDuringProcessorEdit = function (protoId) {
                console.log(protoId);
                console.log(getIndexOfId($scope.enumprocsrtype, protoId));
                $scope.procsrType = $scope.enumprocsrtype[getIndexOfId($scope.enumprocsrtype, protoId)];
            };
			
			$scope.getFolderTypeDuringProcessorEdit = function (folderID) {
				console.log(folderID);
                if($scope.procsrType.id === 'REMOTEDOWNLOADER') {
					return getName($scope.allStaticPropertiesForDownloaderProcessorFolder, folderID);
				} else if ($scope.procsrType.id === 'REMOTEUPLOADER') {
					return getName($scope.allStaticPropertiesForUploaderProcessorFolder, folderID);
				} else {
					return getName($scope.allStaticPropertiesForSweeperProcessorFolder, folderID);
				}
			};
            // For Procsr Dynamic Props
            $scope.addRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData, addedProperty) {
                
                if (valueSelectedinSelectionBox.value === null) {
                    showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                    return;
                }
                var attrName = '';
				if (valueSelectedinSelectionBox.value.id !== 'add new -->') {
                    attrName = valueSelectedinSelectionBox.value.name;
                } else if (addedProperty.value !== '') {
                    attrName = addedProperty.value;
                }				
				var rowVal;
                if (row.getProperty('value') === null) {
                    rowVal = '';
               } else if (typeof(row.getProperty('value')) === 'undefined') {			   
				    rowVal = null;
			   } else {
                    // row.getProperty('value') is converted to string since the Propertyvalue may contain boolean value for the property
                    // "chunkedEncoding" if the user has entered the value of "false", then checking the !row.getProperty('value')
                    // will always be true and the below alert will be displayed.
                   rowVal = row.getProperty('value').toString();
                }				
                 // console.log(row.getProperty('value').toString());
                 if (!attrName || !rowVal) {
                    showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                    return;
                }
                if (valueSelectedinSelectionBox.value.id === 'socketTimeout' || valueSelectedinSelectionBox.value.id === 'connectionTimeout' || valueSelectedinSelectionBox.value.id === 'retryAttempts' || valueSelectedinSelectionBox.value.id === 'port' || valueSelectedinSelectionBox.value.id === 'payloadsizethreshold' || valueSelectedinSelectionBox.value.id === 'numoffilesthreshold') {
						if (!($scope.numberPattern.test(row.getProperty('value')))) {
							showAlert('Value should be a number.', 'error');
							return;
						}
						
						if(valueSelectedinSelectionBox.value.id === 'retryAttempts') {
							if(row.getProperty('value') < 0 || row.getProperty('value') > 4) {
								showAlert('Value should be between 0 to 4.', 'error');
								return;
							}
						}
						
						if(valueSelectedinSelectionBox.value.id === 'socketTimeout' || valueSelectedinSelectionBox.value.id === 'connectionTimeout') {
							if(row.getProperty('value') < 1 || row.getProperty('value') > 60000) {
								showAlert('Value should be between 1 to 60000.', 'error');
								return;
							}
						}
				   } 
                 if (attrName.length > 128) {
					showAlert('Property Name cannot be longer than 128 characters.', 'information');
					return;
                } 				
				if (rowVal.length > 2048) {
				   showAlert('Property  Value cannot be longer than 2048 characters.', 'information');
                    return;			
			    }		
                if (checkNameDuplicate(gridData, attrName)) {
                    showAlert('Name already added.', 'error');
                    return;
                }
                var indexOfSelectedElement = getIndex(allPropsWithNovalue, attrName);
                // Displays an alert if the dynamic property entered by user is already in static properties provided
                if ((valueSelectedinSelectionBox.value.id === 'add new -->') && (indexOfSelectedElement !== -1)) {
                    showAlert('The property is already available in dropdown provided.Please use the appropriate property from dropdown menu', 'error');
                    return;
                }
                /*$scope.informer.inform("error message", "error");
             $scope.informer.inform("info message", "info");
             $scope.allInfos = $scope.informer.allInfos;
             $scope.remove = $scope.informer.remove;*/
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    name: attrName,
                    value: row.getProperty('value'),
                    allowAdd: false,
                    isMandatory: false
                });
                if (indexOfSelectedElement !== -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }
                //}
                gridData.push({
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                });
                valueSelectedinSelectionBox.value = '';
                addedProperty.value = '';
				
            };
            // For Procsr Dynamic Props
            $scope.removeRow = function (row, allProps, allPropsWithNovalue, gridData, valueSelectedinSelectionBox) {
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                var removedProperty = row.getProperty('name');
                var indexOfSelectedElement = getIndex(allProps, removedProperty);
                if (indexOfSelectedElement > -1) {
                    allPropsWithNovalue.push(allProps[indexOfSelectedElement]);
                }
            };
            // For Procsr Folder Props
            $scope.addFolderRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData) {
                //$log.info(valueSelectedinSelectionBox.value.id);
                // $log.info(row.getProperty('folderURI'));
                if (valueSelectedinSelectionBox.value === null) {
                    showAlert('It is mandatory to set the folder URI and Type.', 'error');
                    return;
                }
                if (!valueSelectedinSelectionBox.value.id || !row.getProperty('folderURI')) {
                    showAlert('It is mandatory to set the folder URI and Type.', 'error');
                    return;
                }
                /*if (valueSelectedinSelectionBox.name === '' || row.getProperty('folderURI') === '' || typeof row.getProperty('folderURI') === 'undefined') {
             showAlert('It is mandatory to set the folder URI and Type.');
             return;
             }*/
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    folderURI: row.getProperty('folderURI'),
                    folderType: valueSelectedinSelectionBox.value.name,
                    folderDesc: row.getProperty('folderDesc'),
                    isMandatory: false,
                    allowAdd: false
                });
                var indexOfSelectedElement = getIndex(allPropsWithNovalue, valueSelectedinSelectionBox.value.name);
                if (indexOfSelectedElement !== -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }
                //}
                gridData.push({
                    folderURI: '',
                    folderType: '',
                    folderDesc: '',
                    allowAdd: 'true'
                });
                valueSelectedinSelectionBox.value = '';
            };
            // For Procsr Folder Props
            $scope.removeFolderRow = function (row, allProps, allPropsWithNovalue, gridData) {
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                var removedProperty = row.getProperty('folderType');
                var indexOfSelectedElement = getIndex(allProps, removedProperty);
                if (indexOfSelectedElement > -1) {
                    allPropsWithNovalue.push(allProps[indexOfSelectedElement]);
                }
            };
            // For Procsr Credentials Props
            $scope.addCredentialRow = function (row, valueSelectedinSelectionBox, valueSelectedinSelectionBoxIdp, allPropsWithNovalue, allPropsWithNovalueIdp, gridData) {
                if (valueSelectedinSelectionBox.value === null) {
                    showAlert('It is mandatory to set the folder URI and Type.', 'error');
                    return;
                }
                var selectedIdp = '';
                if (valueSelectedinSelectionBoxIdp.value !== null && valueSelectedinSelectionBoxIdp.value !== '') {
                	selectedIdp = valueSelectedinSelectionBoxIdp.value.name;
                }
                if (!valueSelectedinSelectionBox.value.id) {
                    showAlert('It is mandatory to set credential type', 'error');
                    return;
                }
                if (row.getProperty('passwordDirtyState') === "nomatch") {
                    showAlert('The password and confirm password do not match', 'error');
                    return;
                }
               /*This condition is used to prevent the data from getting pushed to gridData array when maximum length of password is exceeded*/
                if(row.getProperty('passwordDirtyState') === "maxlengthError"){
					showAlert('The password cannot be longer than 63 characters', 'error');
					return;
				}
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    credentialURI: row.getProperty('credentialURI'),
                    credentialType: valueSelectedinSelectionBox.value.name,
                    userId: row.getProperty('userId'),
                    password: row.getProperty('password'),
                    idpType: selectedIdp,
                    idpURI: row.getProperty('idpURI'),
                    allowAdd: false
                });
                var indexOfSelectedElement = getIndex(allPropsWithNovalue, valueSelectedinSelectionBox.value.name);
                if (indexOfSelectedElement !== -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }
                var indexOfSelectedElementIdp = getIndex(allPropsWithNovalueIdp, selectedIdp);
                if (indexOfSelectedElementIdp !== -1) {
                    allPropsWithNovalueIdp.splice(indexOfSelectedElementIdp, 1);
                }
                //}
                gridData.push({
                    credentialURI: '',
                    credentialType: '',
                    userId: '',
                    password: '',
                    idpType: '',
                    idpURI: '',
                    allowAdd: 'true'
                });
                valueSelectedinSelectionBox.value = '';
                valueSelectedinSelectionBoxIdp.value = '';
            };
            // For Procsr Credentials Props
            $scope.removeCredentialRow = function (row, allProps, allPropsIdp, allPropsWithNovalue, allPropsWithNovalueIdp, gridData) {

            	//To notify passwordDirective to clear the password and error message
                $scope.doSend();
            	var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                var removedProperty = row.getProperty('credentialType');
                var indexOfSelectedElement = getIndex(allProps, removedProperty);
                if (indexOfSelectedElement > -1) {
                    allPropsWithNovalue.push(allProps[indexOfSelectedElement]);
                }
                if (row.getProperty("credentialType") == "SSH_KEYPAIR") {
                    $scope.deleteSSHKeyCredential();
                }
                if (row.getProperty("credentialType") == "TRUSTSTORE_CERT") {
                    $scope.deleteCertificateCredential();
                }
            };
            $scope.doCancel = function () {			
				$scope.closeModalView(); 
				$location.$$search = {};
				$location.path('/mailbox/getMailBox');
            };
	        $scope.closeModalView = function () {
                 $('#cancelAction').modal('hide')
            };
            $scope.backToMailbox = function () {			
				$scope.backToMailboxeModalView(); 
				var redirectToId = $location.search().mailBoxId;
				$location.$$search = {};
				$location.path('/mailbox/addMailBox').search('mailBoxId', redirectToId);
            };
			$scope.backToMailboxeModalView = function () {
                 $('#backToMailboxAction').modal('hide')
            };
            $scope.processOtherHeaderValue = function (value) {
                var commaSplit = val.split(",");
                var colonSplit;
            };
            $scope.getIndex = function (name) {
                var ftpVal = getIndex($scope.allMandatoryFtpProperties, name);
                if (ftpVal !== -1) {
                    return ftpVal;
                }
                var httpVal = getIndex($scope.allMandatoryHttpProperties, name);
                if (httpVal !== -1) {
                    return httpVal;
                }
                var sweeperVal = getIndex($scope.allMandatorySweeperProperties, name);
                if (sweeperVal !== -1) {
                    return sweeperVal;
                }
                return getId($scope.allStaticProperties, name);
            };
            $scope.save = function () {
            	//To notify passwordDirective to clear the password and error message
                $scope.doSend();
                $scope.saveProcessor();
                $scope.formAddPrcsr.$setPristine();
               
            };
            $scope.saveProcessor = function () {
			
				//regarding loading js from git
				if($scope.isGitUrlSelected === '1' && $scope.modal.uri !== "" && $scope.modal.uri !== null) {
					$scope.modal.uri = "gitlab:" + $scope.modal.uri;
				} 
					
            	var lenDynamicProps = $scope.processorProperties.length;
                var commaSplit = [];
                var mandatoryArray = [];
                for (var i = 0; i < lenDynamicProps - 1; i++) {
                    var index = $scope.getIndex($scope.processorProperties[i].name);
                    var name = (index === -1) ? $scope.processorProperties[i].name : $scope.getIdValue($scope.processorProperties[i].name);
                    var value = $scope.processorProperties[i].value;
                    if (name === 'url') {
                        mandatoryArray.push({
                            name: name,
                            value: value
                        });
                    }
                    if ((name === 'port') && ($scope.processor.type !== 'SWEEPER')) {
                        mandatoryArray.push({
                            name: name,
                            value: value
                        });
                    }
                    if (name === 'pipeLineID') {
                        mandatoryArray.push({
                            name: name,
                            value: $scope.pipeId
                        });
                    }
                    if (name === 'httpVerb' && ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS')) {
                        mandatoryArray.push({
                            name: name,
                            value: $scope.verb
                        });
                    }
                    if ((name === 'httpVersion') && ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS')) {
                        mandatoryArray.push({
                            name: name,
                            value: value
                        });
                    }
					if (name === 'contentType' && ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS')) {
                        mandatoryArray.push({
                            name: name,
                            value: $scope.content
                        });
                    }   
                    var index = getIndex($scope.allStaticProperties, $scope.processorProperties[i].name);
                    var indexMandatory;
                    if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                        indexMandatory = getIndex($scope.allMandatoryHttpProperties, $scope.processorProperties[i].name);
                    } else if ($scope.processor.protocol === 'SWEEPER') {
                        indexMandatory = getIndex($scope.allMandatorySweeperProperties, $scope.processorProperties[i].name);
                    } else indexMandatory = getIndex($scope.allMandatoryFtpProperties, $scope.processorProperties[i].name);
                    if (index === -1 && indexMandatory === -1) {
                        // To set proper id for dynamic properties which are displayed as static properties
                        var dynamicPropertyIndex = getIndex($scope.dynamicPropertiesDisplayedAsStaticProperties, $scope.processorProperties[i].name);
                        var dynamicPropertyName = (dynamicPropertyIndex === -1) ? $scope.processorProperties[i].name : getId($scope.dynamicPropertiesDisplayedAsStaticProperties, $scope.processorProperties[i].name);
                        $scope.processor.dynamicProperties.push({
                            name: dynamicPropertyName,
                            value: $scope.processorProperties[i].value
                        });
                    } else {
                        if (name === 'otherRequestHeader') {
                            commaSplit = $scope.processorProperties[i].value.split(",");
                        } else {
                            $scope.processor.remoteProcessorProperties[name] = $scope.processorProperties[i].value;
                        }
                    }
                }
                for (var i = 0; i < mandatoryArray.length; i++) {
                    if (mandatoryArray[i].value === '' || typeof mandatoryArray[i].value === 'undefined') {
                        if(!($scope.processor.protocol == 'HTTPS' || $scope.processor.protocol == 'HTTP') && (mandatoryArray[i].name == 'port')) {
                            showAlert('Enter MandatoryProperties', 'error');
                            return;
                        }    
                    } else $scope.processor.remoteProcessorProperties[mandatoryArray[i].name] = mandatoryArray[i].value;
                }
                for (var i = 0; i < commaSplit.length; i++) {
                    var colonSplit = commaSplit[i].split(":");
                    $scope.processor.remoteProcessorProperties.otherRequestHeader.push({
                        name: colonSplit[0],
                        value: colonSplit[1]
                    });
                }
                console.log(commaSplit);
                var lenFolderProps = $scope.processorFolderProperties.length;
				
				//Removed empty folder row for sweeper
				if ($scope.processor.protocol === 'SWEEPER') lenFolderProps = 2;
				
                for (var i = 0; i < lenFolderProps - 1; i++) {
                    $scope.processor.folders.push({
                        folderURI: $scope.processorFolderProperties[i].folderURI,
                        folderType: getId($scope.allStaticPropertiesForProcessorFolder, $scope.processorFolderProperties[i].folderType),
                        folderDesc: $scope.processorFolderProperties[i].folderDesc
                    });
                }
                var lenCredentialProps = $scope.processorCredProperties.length;
                for (var i = 0; i < lenCredentialProps - 1; i++) {
                    var credentialType = ($scope.processorCredProperties[i].credentialType == 'Login Credential')?'LOGIN_CREDENTIAL':$scope.processorCredProperties[i].credentialType;
                    $scope.processor.credentials.push({
                        credentialURI: $scope.processorCredProperties[i].credentialURI,
                        credentialType: credentialType,
                        //credentialType: getId($scope.allCredentialTypes, $scope.processorCredProperties[i].credentialType),
                        userId: $scope.processorCredProperties[i].userId,
                        password: $scope.processorCredProperties[i].password,
                       // idpType: getId($scope.allStaticPropertiesForProcessorCredentialIdp, $scope.processorCredProperties[i].idpType),
                        idpType: $scope.processorCredProperties[i].idpType,
                        idpURI: $scope.processorCredProperties[i].idpURI
                    });
                }
                $scope.processor.linkedMailboxId = $location.search().mailBoxId;
                //$scope.processor.linkedProfiles = $scope.selectedProfiles;
                var profileLen = $scope.selectedProfiles.length;
                for (var i = 0; i < profileLen; i++) {
                    $scope.processor.linkedProfiles[i] = $scope.selectedProfiles[i].name;
                }
                $scope.processor.javaScriptURI = $scope.modal.uri;
                block.blockUI();
                if ($scope.isEdit) {
                    editRequest.reviseProcessorRequest.processor = $scope.processor;
					$scope.appendPortToUrl();
                    editRequest.reviseProcessorRequest.processor.status = $scope.status.id;
                    editRequest.reviseProcessorRequest.processor.type = $scope.procsrType.id;
					
					if($scope.processor.protocol == 'HTTPS' || $scope.processor.protocol == 'HTTP') {
						$scope.processorReviseAfterKM();
					} else {
						var reviseProcessor = false;
						for(var i = 0; i < $scope.editRequest.reviseProcessorRequest.processor.credentials.length; i++) {
						
							$scope.credType = editRequest.reviseProcessorRequest.processor.credentials[i].credentialType;
							$scope.procName = editRequest.reviseProcessorRequest.processor.name;
							$scope.credUsrName = editRequest.reviseProcessorRequest.processor.credentials[i].userId;
							$scope.secret = editRequest.reviseProcessorRequest.processor.credentials[i].password;
							
							$scope.secretName = '';
							if($scope.credType === 'LOGIN_CREDENTIAL') {
								$scope.secretName = $scope.mailboxName + $scope.procName + $scope.credUsrName;
								base64EncodedSecret = $scope.base64EncodedSecret = $.base64.encode($scope.secret);
								$scope.secretUrl = $scope.url_secret_service + encodeURIComponent($scope.secretName);
								reviseSecret($scope.secretUrl, base64EncodedSecret, i);
								reviseProcessor = true;
								break;
							}					
						}
					
						if(reviseProcessor === false) {
							$scope.processorReviseAfterKM();
						}
					}
                } else {
				
                    addRequest.addProcessorToMailBoxRequest.processor = $scope.processor;
					$scope.appendPortToUrl();
                    addRequest.addProcessorToMailBoxRequest.processor.status = $scope.status.id;
                    addRequest.addProcessorToMailBoxRequest.processor.type = $scope.procsrType.id;
					
					if($scope.processor.protocol == 'HTTPS' || $scope.processor.protocol == 'HTTP' || $scope.addRequest.addProcessorToMailBoxRequest.processor.credentials.length === 0) {
						$scope.processorSaveAfterKM();
					} else {
						var saveProcessor = false;
						for(var i = 0; i < $scope.addRequest.addProcessorToMailBoxRequest.processor.credentials.length; i++) {
						
							$scope.credType = addRequest.addProcessorToMailBoxRequest.processor.credentials[i].credentialType;
							$scope.procName = addRequest.addProcessorToMailBoxRequest.processor.name;
							$scope.credUsrName = addRequest.addProcessorToMailBoxRequest.processor.credentials[i].userId;
							$scope.secret = addRequest.addProcessorToMailBoxRequest.processor.credentials[i].password;
							
							$scope.secretName = '';
							if($scope.credType === 'LOGIN_CREDENTIAL') {
							
								$scope.secretName = $scope.mailboxName + $scope.procName + $scope.credUsrName;
								base64EncodedSecret = $scope.base64EncodedSecret = $.base64.encode($scope.secret);
								$scope.secretUrl = $scope.url_secret_service + encodeURIComponent($scope.secretName);
								
								storeSecret($scope.secretUrl, base64EncodedSecret, i);
								
								saveProcessor = true;
								break;
							}
						}
					
						if(saveProcessor === false) {
							$scope.processorSaveAfterKM();
						}
					}
                }
            };
			
			
            function reviseSecret(secretUrl, base64EncodedSecret, a) {
				$scope.restService.put(secretUrl, base64EncodedSecret,
					function (data, status) {
						console.log('review secret status and data = ' + data + ', '+ status);
						if (status === 200) {
							editRequest.reviseProcessorRequest.processor.credentials[a].password = data;
							$scope.processorReviseAfterKM();
							
						} else if (status === 404) {
							$scope.restService.post($scope.secretUrl, base64EncodedSecret,
								function (crdata, status2) {
									if (status2 === 201) {
										editRequest.reviseProcessorRequest.processor.credentials[a].password = crdata;
										$scope.processorReviseAfterKM();
									} else {
										block.unblockUI();
										showSaveMessage("Error while revising secret", 'error');
										return;
									}
								}, {'Content-Type': 'application/octet-stream'}
							);
						} else {
							block.unblockUI();
							showSaveMessage("Error while revising secret", 'error');
							return;
						}
					}, {'Content-Type': 'application/octet-stream'}
				);
			}
			
			function storeSecret(secretUrl, base64EncodedSecret, a) {
				$scope.restService.post(secretUrl, base64EncodedSecret,
					function (secdata, status) {
						console.log('status and data = ' + secdata + ', '+ status);
						if (status === 201) {
							addRequest.addProcessorToMailBoxRequest.processor.credentials[a].password = secdata;
							$scope.processorSaveAfterKM();
						} else {
							block.unblockUI();
							showSaveMessage("Error while adding secret", 'error');
							return;
						}
					}, {'Content-Type': 'application/octet-stream'}
				);
			}
			
			$scope.processorReviseAfterKM = function() {
				$log.info($filter('json')(editRequest));
				$scope.restService.put($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + $scope.processor.guid, $filter('json')(editRequest),
					function (data, status) {
						if (status === 200) {
							$scope.editProcessor($scope.processor.guid, false);
							if (data.reviseProcessorResponse.response.status === 'success') {
								if($scope.isFileSelected)  $scope.isFileSelected = false;
								$scope.isPrivateKeySelected = false;
								$scope.isPublicKeySelected = false;
								showSaveMessage(data.reviseProcessorResponse.response.message, 'success');
							} else {
								showSaveMessage(data.reviseProcessorResponse.response.message, 'error');
							}
							//$scope.readOnlyProcessors = true;
							$scope.readAllProcessors();
							//$scope.readAllProfiles();
						} else {
							showSaveMessage("Error while saving processor", 'error');
						}
						block.unblockUI();
						$scope.clearProps();
					}
				);
			}
			
			$scope.processorSaveAfterKM = function() {
				$log.info($filter('json')(addRequest));
				$scope.restService.post($scope.base_url + '/' + $location.search().mailBoxId + '/processor', $filter('json')(addRequest),
					function (data, status) {
						if (status === 200) {
							//$scope.readOnlyProcessors = true;
							$scope.readAllProcessors();
							//$scope.readAllProfiles();
							$scope.isEdit = true;
							$scope.processor.guid = data.addProcessorToMailBoxResponse.processor.guId;
							$scope.editProcessor($scope.processor.guid, false);
							if (data.addProcessorToMailBoxResponse.response.status === 'success') {
								if($scope.isFileSelected)  $scope.isFileSelected = false;
								$scope.isPrivateKeySelected = false;
								$scope.isPublicKeySelected = false;
								showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'success');
							} else {
								showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'error');
							}
						} else {
							showSaveMessage("Error while saving processor", 'error');
						}
						$scope.clearProps();
						block.unblockUI();
					}
				);
			};
			
            $scope.clearProps = function () {
                $scope.processor.dynamicProperties = [];
                $scope.processor.folders = [];
                $scope.processor.credentials = [];
                $scope.processor.remoteProcessorProperties.otherRequestHeader = [];
            };
            /*This function is used to notify passwordDirective to clear the password and error message*/
            $scope.doSend = function(){
				$scope.$broadcast('clearPassword');
			}
            $scope.addNew = function () {

                    $scope.formAddPrcsr.$setPristine();
                    $scope.loadOrigin();
                    $scope.readAllProfiles();
                    $scope.closeDelete();
                    //To notify passwordDirective to clear the password and error message
                    $scope.doSend();
                    $scope.isPortDisabled = false;
					$scope.defaultPortValue();
                    $scope.disableSSHKeys = true;
                    $scope.disableCertificates = true;
                    formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';

            };
            
            // Close the modal
            $scope.closeDelete = function () {
                $('#myModal').modal('hide')
            };
        
            $scope.resetProcessorType = function (model) {
                $scope.resetStaticAndMandatoryProps();
                if (model.id === 'SWEEPER') {
                    $scope.isProcessorTypeSweeper = true;
                    $scope.processor.protocol = "SWEEPER";
                    $scope.setFolderData();
                    $scope.processorProperties = $scope.sweeperMandatoryProperties;
					for(i = 0; i < $scope.processorProperties.length; i++) {
                        if ($scope.processorProperties[i].name === 'PipeLine Id') {
							$scope.processorProperties[i].value = $scope.pipeId;
							$scope.disablePipeLineId = true;
                       }
                    }
                } else {
                	if($scope.isProcessorTypeSweeper) {
						$scope.processor.protocol = $scope.enumprotocoltype[0];
					}
					$scope.isProcessorTypeSweeper = false;
					if($scope.processor.protocol !== 'HTTP' && $scope.processor.protocol !== 'HTTPS') {
						$scope.processorProperties = $scope.ftpMandatoryProperties;
					}
                    $scope.setFolderData();
                    var indexOfPort = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'port');
                    if (indexOfPort !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfPort, 1);
					$scope.defaultPortValue();
                }
                // function to modify the static properties if the protocol is FTP or FTPS
                $scope.modifyStaticPropertiesBasedOnProtocol();
                // function to modify the static properties if the type is SWEEPER
                $scope.modifyStaticPropertiesBasedOnProcessorType();
                  // function to clear the credential details if protocol is changed
                $scope.resetProcessorCredentialDetails();
                $scope.resetCredentialModal();
            };
            $scope.resetProtocol = function (model) {
                console.log(model);
				$scope.isPortDisabled = false;
                $scope.resetStaticAndMandatoryProps();
                if ($scope.processor.protocol === "FTP" || $scope.processor.protocol === "FTPS" || $scope.processor.protocol === "SFTP") {
                    if ($scope.processor.type === "SWEEPER") $scope.processor.type = $scope.enumprocsrtype[0];
                    var indexOfPort = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'port');
                    if (indexOfPort !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfPort, 1);
                    $scope.processorProperties = $scope.ftpMandatoryProperties;
                    $scope.portRequired = true;
                    $scope.setFolderData();
					$scope.defaultPortValue();
                } else if ($scope.processor.protocol === "HTTP" || $scope.processor.protocol === "HTTPS") {
                    if ($scope.processor.type === "SWEEPER") $scope.processor.type = $scope.enumprocsrtype[0];
                    var indexOfPort = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'port');
                    if (indexOfPort !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfPort, 1);
                    $scope.processorProperties = $scope.httpMandatoryProperties;
                    $scope.portRequired = false;
                    $scope.setFolderData();
                } else if ($scope.processor.protocol === "SWEEPER") {
                    $scope.processorProperties = $scope.sweeperMandatoryProperties;
                    $scope.setFolderData();
                } else {
                    $scope.setFolderData();
                    $scope.processor.type = $scope.enumprocsrtype[2];
                }
                // function to modify the static properties if the protocol is FTP or FTPS
                $scope.modifyStaticPropertiesBasedOnProtocol();
                // function to modify the static properties if the type is SWEEPER
                $scope.modifyStaticPropertiesBasedOnProcessorType();
                
                // function to clear the credential details if protocol is changed
                $scope.resetProcessorCredentialDetails();
                $scope.resetCredentialModal();
                //GMB-201
                if($scope.processor.protocol === "FTPS" || $scope.processor.protocol === "HTTPS") {
					$scope.disableCertificates = false;
     			} else {
					$scope.disableCertificates = true;
 				}
                $scope.disableSSHKeys = ($scope.processor.protocol === "SFTP")?false:true;
            };
            $scope.setFolderData = function () {
                if ($scope.procsrType.id === "SWEEPER") {
                    $scope.processorFolderProperties = [{
                        folderURI: '',
                        folderType: 'Payload Location',
                        folderDesc: '',
                        isMandatory: true,
                        allowAdd: false
                    }];
                } else if ($scope.procsrType.id === "REMOTEDOWNLOADER"){
                    $scope.processorFolderProperties = [{
                        folderURI: '',
                        folderType: '',
                        folderDesc: '',
                        isMandatory: false,
                        allowAdd: 'true'
                    }];
                    $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = [{
						"name": "Remote Payload Location",
						"id": "PAYLOAD_LOCATION"
					},{
						"name": "Local Target Location",
						"id": "RESPONSE_LOCATION"
					}];
                } else {
					$scope.processorFolderProperties = [{
							folderURI: '',
							folderType: '',
							folderDesc: '',
							isMandatory: false,
							allowAdd: 'true'
						}];
					$scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = [{
						"name": "Local Payload Location",
						"id": "PAYLOAD_LOCATION"
					},{
						"name": "Remote Target Location",
						"id": "RESPONSE_LOCATION"
					}];
				}
            };
            $scope.resetStaticAndMandatoryProps = function () {
                $scope.allStaticPropertiesThatAreNotAssignedValuesYet = [{
                    "name": "add new -->",
                    "id": "add new -->"
                }, {
                    "name": "Socket Timeout",
                    "id": "socketTimeout"
                }, {
                    "name": "Connection Timeout",
                    "id": "connectionTimeout"
                }, {
                    "name": "Retry Attempts",
                    "id": "retryAttempts"
                }, {
                    "name": "Chunked Encoding",
                    "id": "chunkedEncoding"
                }, {
                    "name": "Encoding Format",
                    "id": "encodingFormat"
                }, {
                    "name": "OtherRequest Header",
                    "id": "otherRequestHeader"
                }, {
                    "name": "Processed File Location",
                    "id": "processedfilelocation"
                }, {
                    "name": "Error File Location",
                    "id": "errorfilelocation"
                }];
                // function to modify the static properties if the protocol is FTP or FTPS
                // $scope.modifyStaticPropertiesBasedOnProtocol();
                $scope.ftpMandatoryProperties = [{
                    name: 'URL',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'Port',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
                $scope.httpMandatoryProperties = [{
                    name: 'URL',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'Port',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'HTTP Version',
                    value: '1.1',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'HTTP Verb',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'Content Type',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
                $scope.sweeperMandatoryProperties = [{
                    name: 'PipeLine Id',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
            };
            
            // Editor Section Begins
            var editor;
            var rowObj;
            
            $scope.onCloseEditor = function (url) {
            
				if (url !== '' && $scope.inputPatternForURL.test(url)) {

					var port = url.split('/')[2].split(':')[1];
					for (i = 0; i < $scope.processorProperties.length; i++) {
						if ($scope.processorProperties[i].name === 'Port') {
							if ($scope.inputPatternForPort.test(port)) {
								$scope.isPortDisabled = true;
							} else {
								$scope.isPortDisabled = false;
							}
							if (typeof port !== 'undefined' && port !== '') {
								$scope.processorProperties[i].value = port;
							} else {
								$scope.defaultPortValue();
								$scope.isPortDisabled = false;
							}
							if(port === '') $scope.isPortDisabled = false;
						}
					}
				} else {
					for(i = 0; i < $scope.processorProperties.length; i++) {
						if ($scope.processorProperties[i].name === 'Port') {
							$scope.processorProperties[i].value = '';
							$scope.defaultPortValue();
							$scope.isPortDisabled = false;
						}
					}
				}
			};
            
            $scope.loadValueData = function (_editor) {
                editor = _editor;
                _editor.getSession().setUseWorker(false);
            };
			
			var enableAndFocusEditor = function() {
				if (editor) {
					editor.focus();
					var session = editor.getSession();
					//Get the number of lines
					var count = session.getLength();
					//Go to end of the last line
					editor.gotoLine(session.getLine(count-1).length);
                }
            };
			
            $scope.isModal = function (row) {
                rowObj = row;
				$timeout(enableAndFocusEditor,500);
				
				if (rowObj.entity.name === 'Retry Attempts' && !$scope.retryAttemptsPattern.test(rowObj.entity.value)) {
                    rowObj.entity.value = '';
                } else if ((rowObj.entity.name === 'Socket Timeout' || rowObj.entity.name === 'Connection Timeout')
                			 && !$scope.numberTimeOutPattern.test(rowObj.entity.value)) {
                    rowObj.entity.value = '';
				} else if (rowObj.entity.name === 'URL' && !$scope.inputPatternForURL.test(rowObj.entity.value)) {
					rowObj.entity.value = '';
				} else if (rowObj.entity.name === 'HTTP Version' && !$scope.httpVersionPattern.test(rowObj.entity.value)) {
					rowObj.entity.value = '';
				} else if (rowObj.entity.name === 'Port' && !$scope.inputPatternForPort.test(rowObj.entity.value)) {
                    rowObj.entity.value = '';
				}
				
				if (typeof rowObj.entity.value === 'undefined' || rowObj.entity.value === '') {
					editor.setValue('',0) 
				} else {
                	editor.setValue(row.getProperty('value').toString());
				}
            };
			
            $scope.close = function () {
            
                if (rowObj.entity.name === 'URL') {
                    $scope.onCloseEditor(editor.getValue());
                }
                
                rowObj.entity.value = editor.getValue();
				
            };
			    
            // Editor Section Ends
			    
            $scope.changeGlyphIconColor = function (icon, currentValue) {
                if (currentValue !== '') {
                    icon.color = "glyphicon-red";
                } else {
                    icon.color = "glyphicon-white";
                };
            };
            // File Upload Section Begins
            $scope.setFiles = function (element) {
                console.log(element.value);
                $scope.$apply(function ($scope) {
                    // Turn the FileList object into an Array
                    $scope.files = [];
                    for (var i = 0; i < element.files.length; i++) {
                        $scope.files.push(element.files[i]);
                    }
                    //console.log('files:', $scope.files);
                    $scope.certificateModal.certificateURI = $scope.files[0].name;
                    $scope.isFileSelected = true;
                    $scope.progressVisible = false;
                });
            };
            $scope.uploadFile = function () {
                console.log('Entering upload event');
                 block.blockUI();
                var fd = new FormData();
                $scope.pkObj['serviceInstanceId'] = Date.now().toString();
				$scope.pkObj.dataTransferObject['name'] = $scope.processor.name.concat('_',$scope.procsrType.name,'_',$scope.certificateModal.certificateURI);
				var currentDate = new Date();
				var afterOneYear = new Date();
				afterOneYear.setYear(currentDate.getFullYear() + 1);
				$("#yearFromNow").append(afterOneYear.toString());
				$scope.pkObj.dataTransferObject['createdDate'] = currentDate.toISOString();
				$scope.pkObj.dataTransferObject['validityDateFrom'] = currentDate.toISOString();
				$scope.pkObj.dataTransferObject['validityDateTo'] = afterOneYear.toISOString();
                fd.append("json", angular.toJson($scope.pkObj));
                for (var i in $scope.files) {
                    fd.append($scope.files[i].name, $scope.files[i]);
                }
                var xhr = new XMLHttpRequest();
                xhr.addEventListener("load", uploadComplete, false);
                xhr.addEventListener("error", uploadFailed, false);
                xhr.addEventListener("abort", uploadCanceled, false);
                xhr.open("POST", $scope.url_upload_key);
                xhr.send(fd);
            };

            function uploadComplete(evt) {
                /* This event is raised when the server send back a response */
                if (evt.target.status === 201) {
                    var resp = angular.fromJson(evt.target.responseText);
                    var arr = resp['dataTransferObject']['keyGroupMemberships'];
                    pkGuid = arr[0]['keyBase']['pguid'];
                    // Public Key guid 
                    //var pkGuid = 'testdata';
                    pkGuid = pkGuid.toString();
                    if ($scope.processor.isSelfSigned === "0" || $scope.processor.isSelfSigned == 0) {
                        console.log('creating self signed trust store');
                        $scope.uploadToSelfSignedTrustStore(pkGuid);
                    } else {
                        console.log('uploading to global trust store');
                        $scope.linkTrustStoreWithCertificate(pkGuid, $rootScope.javaProperties.globalTrustStoreId, $rootScope.javaProperties.globalTrustStoreGroupId);
                    }
                } else {
                    block.unblockUI();
                    /*var msg = ($scope.isEdit === true) ? 'certificate uploading failed because there is an error while uploading the certificate' : 'Processor creation failed because there is an error while uploading the certificate';*/
                    showSaveMessage('Certificate Uploading Failed', 'error');
                    return;
                }
            }
            $scope.uploadToSelfSignedTrustStore = function (pkGuid) {
                $scope.restService.get($scope.base_url + '/uploadSelfSigned', //Get mail box Data
                    function (data, status) {
                        if (status === 200 && data.getTrustStoreResponse.response.status === 'success') {
                            $scope.linkTrustStoreWithCertificate(pkGuid, data.getTrustStoreResponse.trustStore.trustStoreId,
                                data.getTrustStoreResponse.trustStore.trustStoreGroupId);
                        } else {
                            block.unblockUI();
                            showSaveMessage(data.getTrustStoreResponse.response.message, 'error');
                            return;
                        }
                    }
                );
            };
            $scope.linkTrustStoreWithCertificate = function (pkGuid, trustStoreId, trustStoreGroupId) {
                // To put public key is association json to TrustStore
                $scope.linkKeyTs['dataTransferObject']['trustStoreMemberships'][0]['publicKey']['pguid'] = pkGuid;
                $scope.restService.put($scope.url_link_key_store + trustStoreId, angular.toJson($scope.linkKeyTs),
                    function (data, status) {
                        if (status === 200) {
                            $scope.certificateModal.trustStoreGroupId = trustStoreGroupId;
                            $scope.addCertificateDetails();
                        } else {
                            block.unblockUI();
                           /* var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the certificate' : 'Processor creation failed because there is an error while uploading the certificate';*/
                            showSaveMessage('Certificate Uploading Failed', 'error');
                            return;
                        }
                    }
                );
            };

            function uploadFailed(evt) {
                block.unblockUI();
                /*var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the certificate' : 'Processor creation failed because there is an error while uploading the certificate';*/
                showSaveMessage('Certificate Uploading Failed', 'error');
                return;
            }

            function uploadCanceled(evt) {
                block.unblockUI();
                /*var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the certificate' : 'Processor creation failed because there is an error while uploading the certificate';*/
                showSaveMessage('Certificate Uploading Failed', 'error');
                return;
            }
            // File Upload Section Ends
            
            //GMB-201
			if($scope.processor.protocol === "FTPS" || $scope.processor.protocol === "HTTPS") {
				$scope.disableCertificates = false;
			} else {
                $scope.disableCertificates = true;
			}
            $scope.disableSSHKeys = ($scope.processor.protocol === "SFTP")?false:true;

			$scope.defaultPortValue = function() {
				for (i = 0; i < $scope.processorProperties.length; i++) {
					
					if ($scope.processorProperties[i].name === 'Port') {
						
						if ($scope.processor.protocol === "FTP") {
							$scope.processorProperties[i].value = $scope.ftpDefaultPort;
						} else if ($scope.processor.protocol === "SFTP") {
							$scope.processorProperties[i].value = $scope.sftpDefaultPort;
						} else if ($scope.processor.protocol === "FTPS") {
							$scope.processorProperties[i].value = $scope.ftpsDefaultPort;
						}
					}
				}
			}
			
            if ($scope.processor.protocol === "FTP" || $scope.processor.protocol === "SFTP" || $scope.processor.protocol === "FTPS") {
				$scope.portRequired = true;
				$scope.defaultPortValue();
				
            } else {
                $scope.portRequired = false;
            }
            
 			$scope.resetFiles = function() {
				document.getElementById('mbx-procsr-certificatebrowse').value = null;
			}

			$scope.appendPortToUrl = function() {
				var baseUrl = $scope.processor.remoteProcessorProperties.url;
				if (typeof baseUrl !== 'undefined' && baseUrl !== '') {
					var basePort = baseUrl.split('/')[2].split(':')[1]
					if (basePort === '' || basePort === null || typeof basePort === 'undefined') {
						var defaultPort = $scope.processor.remoteProcessorProperties.port;
						if (defaultPort !== '') {
							var url_parts = baseUrl.split('/');
							var domain_name_parts = url_parts[2].split(':');
							var domainWithPort = domain_name_parts[0].concat(':', defaultPort);
							var newBaseUrl = baseUrl.replace(domain_name_parts[0], domainWithPort);
							$scope.processor.remoteProcessorProperties.url = newBaseUrl;
						}

                    }
				}
			}	
			$scope.onScriptTypeSelected = function() {
				$scope.modal.uri = '';
			}
                       // SSHkeys Uploading section begins
             $scope.setSSHPrivateKey = function (element) {
                console.log(element.value);
                $scope.$apply(function ($scope) {
                    // Turn the FileList object into an Array
                    for (var i = 0; i < element.files.length; i++) {
                        $scope.sshKeys.push(element.files[i]);
                    }
                    console.log('sshKeys:', $scope.sshKeys);
                    $scope.sshkeyModal.sshPrivateKeyURI = element.files[0].name;
                    $scope.isPrivateKeySelected = true;
                    $scope.progressVisible = false;
                });
             };
             
              $scope.setSSHPublicKey = function (element) {
                console.log(element.value);
                $scope.$apply(function ($scope) {
                    // Turn the FileList object into an Array
                    for (var i = 0; i < element.files.length; i++) {
                        $scope.sshKeys.push(element.files[i]);
                    }
                    console.log('sshKeys:', $scope.sshKeys);
                    $scope.sshkeyModal.sshPublicKeyURI = element.files[0].name;
                    $scope.isPublicKeySelected = true;
                    $scope.progressVisible = false;
                });
             };
            
              $scope.uploadSSHKey = function () {
                console.log('Entering upload event of ssh keys');
                if ($scope.sshkeyModal.sshKeyPairPassphrase !== $scope.sshkeyModal.sshKeyPairConfirmPassphrase) {
    				showSaveMessage("Passwords does not match.", 'error');
    				$(sshkeypassphrase).focus();
    				return;
    			}
                block.blockUI();
                formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';
                var fd = new FormData();
                $scope.sshKeyObj['serviceInstanceId'] = Date.now().toString();
				$scope.sshKeyObj.dataTransferObject['name'] = $scope.processor.name.concat('_',$scope.procsrType.name,'_',$scope.sshkeyModal.sshkeyURI);
				var currentDate = new Date();
				var afterOneYear = new Date();
				afterOneYear.setYear(currentDate.getFullYear() + 1);
				$("#yearFromNow").append(afterOneYear.toString());
				$scope.sshKeyObj.dataTransferObject['createdDate'] = currentDate.toISOString();
				$scope.sshKeyObj.dataTransferObject['validityDateFrom'] = currentDate.toISOString();
				$scope.sshKeyObj.dataTransferObject['validityDateTo'] = afterOneYear.toISOString();
                $scope.sshKeyObj.dataTransferObject['custodianPassphrase'] = $scope.sshkeyModal.sshKeyPairPassphrase;
                fd.append("json", angular.toJson($scope.sshKeyObj));
                console.log(angular.toJson($scope.sshKeyObj));
                for (var i in $scope.sshKeys) {
                    fd.append($scope.sshKeys[i].name, $scope.sshKeys[i]);
                }
                var xhr = new XMLHttpRequest();
                xhr.addEventListener("load", sshkeyUploadComplete, false);
                xhr.addEventListener("error", sshkeyUploadFailed, false);
                xhr.addEventListener("abort", sshkeyUploadCanceled, false);
                xhr.open("POST", $scope.url_ssh_upload_key);
                xhr.send(fd);
            };
              function sshkeyUploadComplete(evt) {
                /* This event is raised when the server send back a response */
                if (evt.target.status === 201) {
                    console.log('ssh key uploaded successfully');
                    var resp = angular.fromJson(evt.target.responseText);
                    var pkGuid = resp['dataTransferObject']['pguid'];
                    //var pkGuid = 'F45EE0F10A006FF106655CE31D400F66';
                    // Keygroup guid 
                    pkGuid = pkGuid.toString();
                    $scope.sshkeyModal.sshKeyPairPguid = pkGuid;
                    $scope.addSSHKeyDetails();

               } else {
                    block.unblockUI();
                    /*var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the sshkey' : 'Processor creation failed because there is an error while uploading the sshkey';*/
                    showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                    return;
                }
            }
            
             function sshkeyUploadFailed(evt) {
                block.unblockUI();
                /*var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the sshkey' : 'Processor creation failed because there is an error while uploading the sshkey';*/
                showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                return;
            }

            function sshkeyUploadCanceled(evt) {
                block.unblockUI();
               /* var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the sshkey' : 'Processor creation failed because there is an error while uploading the sshkey';*/
                showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                return;
            }
           
            $scope.resetSSHKeys = function (element) {
                element.value = null;
            }
            // SSH Key Upload Section Ends
            
            $scope.processCredentialDetails = function() {
                  for (var i = 0; i <  $scope.processorCredProperties.length; i++) {
                    var credObj =  $scope.processorCredProperties[i];
                    if(credObj.credentialType == 'SSH_KEYPAIR') {
                        $scope.sshkeyModal.sshKeyPairPguid = credObj.idpURI;
                        $scope.sshkeyModal.sshKeyPairPassphrase = credObj.password;
                        $scope.sshkeyModal.sshKeyPairConfirmPassphrase = credObj.password;
                        if(credObj.idpType == 'PRIVATE') {
                             $scope.sshkeyModal.sshPrivateKeyURI = credObj.credentialURI; 
                        } else {
                             $scope.sshkeyModal.sshPublicKeyURI = credObj.credentialURI;
                        }
                                                    
                        
                    }
                    if (credObj.credentialType == 'TRUSTSTORE_CERT') {
                        $scope.certificateModal.certificateURI = credObj.credentialURI;
                        $scope.certificateModal.trustStoreGroupId = credObj.idpURI;
                        $scope.certificateModal.isGlobalTrustore = (credObj.idpType == 'GLOBAL')?"1":"0";
                    }
                }
            }
            $scope.addCertificateDetails = function() {
                $scope.removeCertificateDetails();
                var idpType = ($scope.certificateModal.isGlobalTrustore == '1' || $scope.certificateModal.isGlobalTrustore == 1)?'GLOBAL':'SELFSIGNED';
                $scope.processorCredProperties.splice(0,0,{
                                credentialURI: $scope.certificateModal.certificateURI,
                                credentialType: 'TRUSTSTORE_CERT',
                                userId: '',
                                password:'',
                                idpURI: $scope.certificateModal.trustStoreGroupId,
                                idpType: idpType,
                                allowAdd: false
                            });
                 $scope.processorCredProperties= $scope.processorCredProperties.slice();
                 block.unblockUI();
                
            }
            $scope.removeCertificateDetails = function() {
                for(var i = ($scope.processorCredProperties.length - 1); i >= 0; i--) {
                     var cred = $scope.processorCredProperties[i];
                    if((cred.credentialType == 'TRUSTSTORE_CERT')) {
                        $scope.processorCredProperties.splice(i, 1);
                    }
                }
               
            }

            $scope.addSSHKeyDetails = function() {
                 $scope.removeSSHKeyDetails();
                 $scope.processorCredProperties.splice(0,0,{
                    credentialURI: $scope.sshkeyModal.sshPrivateKeyURI,
                    credentialType: 'SSH_KEYPAIR',
                    userId: '',
                    password: $scope.sshkeyModal.sshKeyPairPassphrase,
                    idpURI: $scope.sshkeyModal.sshKeyPairPguid,
                    idpType: 'PRIVATE',
                    allowAdd: false
                }, {
                    credentialURI: $scope.sshkeyModal.sshPublicKeyURI,
                    credentialType: 'SSH_KEYPAIR',
                    userId: '',
                    password: $scope.sshkeyModal.sshKeyPairPassphrase,
                    idpURI: $scope.sshkeyModal.sshKeyPairPguid,
                    idpType: 'PUBLIC',
                    allowAdd: false
                });
                $scope.processorCredProperties = $scope.processorCredProperties.slice();
                $scope.$apply();
                block.unblockUI();
            }
            $scope.removeSSHKeyDetails = function () {
                for(var i = ($scope.processorCredProperties.length - 1); i >= 0; i--) {
                    var cred = $scope.processorCredProperties[i];
                    if((cred.credentialType == 'SSH_KEYPAIR')) {
                        $scope.processorCredProperties.splice(i, 1);
                    }
                }
              }
            
             $scope.deleteCertificateCredential = function() {			
  			   $scope.certificateModal.certificateURI = "";
               $scope.certificateModal.certificates = '';
               $scope.certificateModal.isGlobalTrustore = "1";
               $scope.certificateModal.trustStoreGroupId = '';
               $scope.resetFiles();
               $scope.removeCertificateDetails();
  			}
             $scope.deleteSSHKeyCredential = function() {			
  			   $scope.sshkeyModal.sshKeyPairPguid = "";
               $scope.sshkeyModal.sshPrivateKeyURI = "";
               $scope.sshkeyModal.sshPublicKeyURI = "";
               $scope.sshkeyModal.sshKeyPairPassphrase = "";
               $scope.sshkeyModal.sshKeyPairConfirmPassphrase = "";
               $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
               $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
               $scope.removeSSHKeyDetails();
  			}
            
            $scope.resetProcessorCredentialDetails = function() {
                  $scope.processorCredProperties = [{
                    credentialURI: '',
                    credentialType: '',
                    userId: '',
                    password: '',
                    idpType: '',
                    idpURI: '',
                    allowAdd: 'true',
                    passwordDirtyState: ''
                }];    
            }
            $scope.resetCredentialModal = function() {
                    $scope.certificateModal = {
                    "certificates": '',
                    "certificateURI": '',
                    "isGlobalTrustore": '1',
                    "trustStoreGroupId": ''
                };
                
                $scope.sshkeyModal = {
                    "sshKeys": '',
                    "sshPrivateKeyURI": '',
                    "sshPublicKeyURI": '',
                    "sshKeyPairPguid": '',
                    "sshKeyPairPassphrase":'',
                    "sshKeyPairConfirmPassphrase":''
                };
                formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';
                $scope.isPrivateKeySelected = false;
                $scope.isPublicKeySelected = false;
            }
            $scope.confirmPasswordColor = function () {		
  			  if (typeof($scope.sshkeyModal.sshKeyPairConfirmPassphrase) === 'undefined' || $scope.sshkeyModal.sshKeyPairConfirmPassphrase === '') {
  				formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';	 	
  			  } else if ($scope.sshkeyModal.sshKeyPairPassphrase === $scope.sshkeyModal.sshKeyPairConfirmPassphrase) {
  				formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '#78FA89';	
  			  }	else {
  				formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '#FA787E';
  			  }
           }
        }
    ]);