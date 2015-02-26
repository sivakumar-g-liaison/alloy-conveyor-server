var rest = myApp.controller(
    'ProcessorCntrlr', ['$rootScope', '$modal', '$scope', '$timeout',
        '$filter', '$location', '$log', '$blockUI',
        function ($rootScope, $modal, $scope, $timeout, $filter,
            $location, $log, $blockUI) {
		
			$scope.disablePipeLineId = false;
            $scope.disableHTTPListenerPipeLineId = false;
            $scope.isJavaScriptExecution = false;
            //check JavaScriptExecutor
            $scope.onChangeJavaScriptExecutor = function () {            	
            	if ($scope.isJavaScriptExecution) {            		
            		$scope.isJavaScriptExecution = false;
            	} else {            		
            		$scope.isJavaScriptExecution = true;
            	} 	            	
            } 
                       
             //ssh key implementation
            $scope.disableSSHKeys = true;
            $scope.disableCertificates = true;    	    
            $scope.isPortDisabled = false;		
			//check directory path in choose file
			$scope.isDirectoryPath = true;
			
            // To be Populated
            $scope.mailBoxId;
            var block = $rootScope.block;	
            		
			function getIndexOfValue(objArray, value) {
				var pos = -1;
				for (var i=0; i<objArray.length; i++) {
					pos ++;
					if(objArray[i].value === value) {
						return pos;
					}
				}
				return -1
		    };    
		           
            $scope.loadOrigin = function () {
				
				//GMB-196
				$scope.sorting = 'name';
                $scope.isFileSelected = false;
        		$scope.isEdit = false;
                $scope.isProcessorTypeSweeper = false;
                $scope.mailboxName = $location.search().mbxname;				
				//GIT URL
				$scope.script = '';
			    $scope.scriptIsEdit = false; 
				$scope.isJavaScriptExecution = false;
                // to disable protocol for http listener processor
                $scope.isProcessorTypeHTTPListener = false;
                // to disable protocol for file writer
                $scope.isProcessorTypeFileWriter = false;
                // to disable protocol for file writer
                $scope.isProcessorTypeDropbox = false;
                
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
                    processorProperties: {
                    	type: "",
                    	displayName: "",
						protocol: "",
                    	handOverExecutionToJavaScript: false,
                    	staticProperties: []
                    }
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
                $scope.sshKeys = {
                    "privatekey":'',
                    "publickey":''
                };
                $scope.status = $scope.initialProcessorData.supportedStatus.options[0];                
                $scope.procsrType = $scope.initialProcessorData.supportedProcessors.options[0];				
                $scope.selectedProcessorType =  $scope.procsrType.value;               
                $scope.processor.protocol = $scope.initialProcessorData.supportedProtocols.options[0];      
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
				
				$scope.allStaticPropertiesForFileWriterProcessorFolder = [{
                    "name": "File Write Location",
                    "id": "FILE_WRITE_LOCATION"
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
				},{
					"name": "File Write Location",
					"id": "FILE_WRITE_LOCATION"
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
            $scope.isPortDisabled = false;
            
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
                
                if($scope.processor.protocol.value === 'DROPBOXPROCESSOR') {
                	var dbxVal = getId($scope.allMandatoryDropboxProperties, row.getProperty('name'));
              		if (dbxVal.length > 0) {
                    	return dbxVal;
                	}
                } else {
                	var httpListenerVal = getId($scope.allMandatoryHttpListenerProperties, row.getProperty('name'));
                	if (httpListenerVal.length > 0) {
                    	return httpListenerVal;
                	}
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
            
            	if($scope.processor.protocol.value === 'DROPBOXPROCESSOR') {
            		var dbxVal = getId($scope.allMandatoryDropboxProperties, name);
               		 if (dbxVal.length > 0) {
             		   return dbxVal;
              		  }
            	} else {
            		var httpListenerVal = getId($scope.allMandatoryHttpListenerProperties, name);
               		 if (httpListenerVal.length > 0) {
                 	   return httpListenerVal;
               		 }
            	}
            	
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
			
				if($scope.processor.protocol.value === 'DROPBOXPROCESSOR') {
					var dbxVal = getName($scope.allMandatoryDropboxProperties, id);
					if (dbxVal.length > 0) {
						return dbxVal;
					}
				} else {
					var httpListenerVal = getName($scope.allMandatoryHttpListenerProperties, id);
					if (httpListenerVal.length > 0) {
						return httpListenerVal;
					}
				}
				
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
                        <div ng-switch-when="PAYLOAD_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" required  placeholder="required" name="folderuripayload" ng-pattern="' + $scope.inputPatternForFolderURI + '" ng-maxLength=250 />\n\
                                <div ng-show="formAddPrcsr.folderuripayload.$dirty && formAddPrcsr.folderuripayload.$invalid">\n\
                                    <span class="customHide" ng-class="{\'help-block-custom\':formAddPrcsr.folderuripayload.$error.pattern}" ng-show=formAddPrcsr.folderuripayload.$error.pattern>Invalid Folder URI.</span>\n\
                                    <span class="customHide" ng-class="{\'custom-info-block\':formAddPrcsr.folderuripayload.$error.maxlength}" ng-show=formAddPrcsr.folderuripayload.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
                           </div></div>\n\
                        <div ng-switch-when="RESPONSE_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" required  placeholder="required" name="folderuriresponse" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                            <div ng-show="formAddPrcsr.folderuriresponse.$dirty && formAddPrcsr.folderuriresponse.$invalid">\n\
                                <span class="customHide" ng-class="{\'help-block-custom\':formAddPrcsr.folderuriresponse.$error.pattern}" ng-show=formAddPrcsr.folderuriresponse.$error.pattern>Invalid Folder URI.</span>\n\
                                <span class="customHide" ng-class="{\'custom-info-block\':formAddPrcsr.folderuriresponse.$error.maxlength}" ng-show=formAddPrcsr.folderuriresponse.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span></div></div>\n\
                        <div ng-switch-when="FILE_WRITE_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" required  placeholder="required" name="folderurifilewrite" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                        <div ng-show="formAddPrcsr.folderurifilewrite.$dirty && formAddPrcsr.folderurifilewrite.$invalid">\n\
                            <span class="customHide" ng-class="{\'help-block-custom\':formAddPrcsr.folderurifilewrite.$error.pattern}" ng-show=formAddPrcsr.folderurifilewrite.$error.pattern>Invalid Folder URI.</span>\n\
                            <span class="customHide" ng-class="{\'custom-info-block\':formAddPrcsr.folderurifilewrite.$error.maxlength}" ng-show=formAddPrcsr.folderurifilewrite.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span></div></div>\n\
                    </div></div>' +
                        '<div ng-switch-when="true">' +
                        '<textarea   class="form-control" name="folderuridefault" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" placeholder="required" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                    <div ng-show="formAddPrcsr.folderuridefault.$dirty && formAddPrcsr.folderuridefault.$invalid">\n\
                             <span class="customHide" ng-class="{\'help-block-custom\':formAddPrcsr.folderuridefault.$error.pattern}" ng-show=formAddPrcsr.folderuridefault.$error.pattern>Invalid Folder URI.</span>\n\
                             <span class="customHide" ng-class="{\'custom-info-block\':formAddPrcsr.folderuridefault.$error.maxlength}" ng-show=formAddPrcsr.folderuridefault.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
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
                        <div ng-switch-when="PAYLOAD_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" name="descriptionpayload" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250 />\n\
                                <div ng-show="formAddPrcsr.descriptionpayload.$dirty && formAddPrcsr.descriptionpayload.$invalid">\n\
                                    <span class="customHide" ng-class="{\'help-block-custom\':formAddPrcsr.descriptionpayload.$error.pattern}" ng-show=formAddPrcsr.descriptionpayload.$error.pattern>Invalid Description.</span>\n\
                                    <span class="customHide" ng-class="{\'custom-info-block\':formAddPrcsr.descriptionpayload.$error.maxlength}" ng-show=formAddPrcsr.descriptionpayload.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
                           </div></div>\n\
                        <div ng-switch-when="RESPONSE_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" name="descriptionresponse" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250 />\n\
                            <div ng-show="formAddPrcsr.descriptionresponse.$dirty && formAddPrcsr.descriptionresponse.$invalid">\n\
                                <span class="customHide" ng-class = "{\'help-block-custom\':formAddPrcsr.descriptionresponse.$error.pattern}" ng-show=formAddPrcsr.descriptionresponse.$error.pattern>Invalid Description.</span>\n\
                                <span class="customHide" ng-class="{\'custom-info-block\':formAddPrcsr.descriptionresponse.$error.maxlength}" ng-show=formAddPrcsr.descriptionresponse.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span></div></div>\n\
                        <div ng-switch-when="FILE_WRITE_LOCATION"><textarea   class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" name="descriptionfilewrite" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250 />\n\
                        <div ng-show="formAddPrcsr.descriptionfilewrite.$dirty && formAddPrcsr.descriptionfilewrite.$invalid">\n\
                            <span class="customHide" ng-class = "{\'help-block-custom\':formAddPrcsr.descriptionfilewrite.$error.pattern}" ng-show=formAddPrcsr.descriptionfilewrite.$error.pattern>Invalid Description.</span>\n\
                            <span class="customHide" ng-class="{\'custom-info-block\':formAddPrcsr.descriptionfilewrite.$error.maxlength}" ng-show=formAddPrcsr.descriptionfilewrite.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span></div></div>\n\
                    </div></div>' +
                        '<div ng-switch-when="true">' +
                        '<textarea   class="form-control" name="descriptiondefault" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:95%;height:45px" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250/>\n\
                    <div ng-show="formAddPrcsr.descriptiondefault.$dirty && formAddPrcsr.descriptiondefault.$invalid">\n\
                        <span class="customHide" ng-class="{\'help-block-custom\':formAddPrcsr.descriptiondefault.$error.pattern}" ng-show=formAddPrcsr.descriptiondefault.$error.pattern>Invalid Description.</span>\n\
                        <span class="customHide" ng-class = "{\'custom-info-block\':formAddPrcsr.descriptiondefault.$error.maxlength}" ng-show=formAddPrcsr.descriptiondefault.$error.maxlength><span class="adjustPaddingRight"><img class="infoiconimg" ng-src="{{infoIconImgUrl}}"/></span>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</span>\n\
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
                    '<div ng-switch-when="true"><input type="text" ng-model="COL_FIELD" ng-input="COL_FIELD" class="textboxingrid"></div>'+
                    '<div ng-switch-when="false"><div ng-switch on="row.getProperty(\'credentialType\')">'+
                    '<div ng-switch-when="Login Credential"><input type="text" ng-model="COL_FIELD" ng-input="COL_FIELD" class="textboxingrid"></div>'+
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
                $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId + '?addServiceInstanceIdConstraint=' + true + '&sid=' + $rootScope.serviceInstanceId, //Get mail box Data
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
            $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="editProcessor(row.getProperty(\'guid\'),true)"><i class="glyphicon glyphicon-wrench"></i></button>';
            $scope.manageStatus = '<div ng-switch on="row.getProperty(\'status\')"><div ng-switch-when="ACTIVE">Active</div><div ng-switch-when="INACTIVE">Inactive</div></div>';
            $scope.manageType = '<div ng-switch on="row.getProperty(\'type\')"><div ng-switch-when="REMOTEDOWNLOADER">Remote Downloader</div><div ng-switch-when="REMOTEUPLOADER">Remote Uploader</div>\n\
            <div ng-switch-when="SWEEPER">Directory Sweeper</div><div ng-switch-when="HTTPASYNCPROCESSOR">HTTP Async Processor</div><div ng-switch-when="HTTPSYNCPROCESSOR">HTTP Sync Processor</div>\n\
            	 <div ng-switch-when="FILEWRITER">File Writer</div><div ng-switch-when="DROPBOXPROCESSOR">Dropbox Processor</div></div>';
            $scope.gridOptionsForProcessorList = {
                columnDefs: [{
                    field: 'name',
                    displayName: 'Name',
                    width: "40%"
                }, {
                    field: 'type',
                    displayName: 'Type',
                    width: "20%",
                    cellTemplate: $scope.manageType
                }, {
                    field: 'status',
                    displayName: 'Status',
                    width: "20%",
                    cellTemplate: $scope.manageStatus
                }, {
                    displayName: 'Action',
                    sortable: false,
                    width: "20%",
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
            $scope.staticProperties;			
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
						$scope.modal.uri = data.getProcessorResponse.processor.javaScriptURI;
						$scope.modal.uri = 'gitlab:/'+ $scope.modal.uri;
				}	
							
				$scope.isJavaScriptExecution = data.getProcessorResponse.processor.processorProperties.handOverExecutionToJavaScript;
				
							
				$scope.processor.description = data.getProcessorResponse.processor.description;				
				(data.getProcessorResponse.processor.status === 'ACTIVE') ? $scope.status = $scope.initialProcessorData.supportedStatus.options[0] : $scope.status = $scope.initialProcessorData.supportedStatus.options[1];
				$scope.setTypeDuringProcessorEdit(data.getProcessorResponse.processor.type);				
				$scope.setTypeDuringProtocolEdit(data.getProcessorResponse.processor.protocol);				
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
                if ($scope.processor.protocol.value === 'HTTPSYNCPROCESSOR' || $scope.processor.protocol.value === 'HTTPASYNCPROCESSOR') {
                    $scope.isProcessorTypeHTTPListener = true;
                } else {
                    $scope.isProcessorTypeHTTPListener = false;
                }
                
                if ($scope.processor.protocol.value === 'FILEWRITER') {
                    $scope.isProcessorTypeFileWriter = true;
                } else {
                    $scope.isProcessorTypeFileWriter = false;
                }
                
                if ($scope.processor.protocol.value === 'DROPBOXPROCESSOR') {
                    $scope.isProcessorTypeDropbox = true;
                } else {
                    $scope.isProcessorTypeDropbox = false;
                }
				//GMB 221
				if($scope.processor.protocol.value === "FTPS" || $scope.processor.protocol.value === "HTTPS") {
					$scope.disableCertificates = false;	
									
				} else {
					$scope.disableCertificates = true;
				}
				$scope.disableSSHKeys = ($scope.processor.protocol.value === "SFTP")?false:true; 
                 
				$scope.setFolderData();
				$scope.isPortDisabled = false;
				$scope.propertiesAddedToProcessor = [];
			    $scope.availableProperties = [];
                $scope.staticProperties = data.getProcessorResponse.processor.processorProperties.staticProperties;
                
                for (var i = 0; i < $scope.staticProperties.length; i++) {				     
					 var property = $scope.staticProperties[i];
					 if (property.mandatory === true || property.valueProvided === true) {
                        $scope.propertiesAddedToProcessor.push(property);
                    } else if (property.mandatory === false || property.valueProvided === false) {
                        $scope.availableProperties.push(property);
                    }
				}
                $scope.propertiesAddedToProcessor.push({
                	
                    "name":"",
                    "displayName" : "",
                    "value":"",
                    "type":"textarea",
                    "readOnly":"",
                    "mandatory":false,
                    "dynamic":false,
                    "valueProvided":false,
                    "validationRules": {}
                 });            

				
				//var json_data = data.getProcessorResponse.processor.processorProperties;
				//$scope.propertiesAddedToProcessorpush(data.getProcessorResponse.processor.processorProperties);						
				$scope.processorFolderProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
				for (var i = 0; i < data.getProcessorResponse.processor.folders.length; i++) {
					$scope.processorFolderProperties.push({
						folderURI: data.getProcessorResponse.processor.folders[i].folderURI,
						folderType: $scope.getFolderTypeDuringProcessorEdit(data.getProcessorResponse.processor.folders[i].folderType),
						folderDesc: data.getProcessorResponse.processor.folders[i].folderDesc,
						isMandatory: (($scope.processor.protocol.value === 'SWEEPER' || $scope.processor.protocol.value === 'FILEWRITER') && (data.getProcessorResponse.processor.folders[i].folderType === 'PAYLOAD_LOCATION' || data.getProcessorResponse.processor.folders[i].folderType === 'FILE_WRITE_LOCATION')) ? true : false,
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
					allowAdd: (data.getProcessorResponse.processor.type === 'SWEEPER' || data.getProcessorResponse.processor.type === 'HTTPASYNCPROCESSOR' || data.getProcessorResponse.processor.type === 'HTTPSYNCPROCESSOR' || data.getProcessorResponse.processor.type === 'FILEWRITER') ? 'false' : 'true'
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
							showSaveMessage('Key manager failed to retrieve the stored secret', 'error');
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
                        //$log.info($filter('json')(data));
						$scope.scriptIsEdit = false;
						if (data.getProcessorResponse.processor.javaScriptURI != null && 
						data.getProcessorResponse.processor.javaScriptURI != "") {
						$scope.scriptIsEdit = true;
						}
						
						$scope.isJavaScriptExecution = data.getProcessorResponse.processor.processorProperties.handOverExecutionToJavaScript;					
						
                        //Fix: Reading profile in procsr callback
                        $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                            function (profData) {
                                
								//$log.info($filter('json')(profData));
								
									var editProcessor = false;
									for(var i = 0; i < data.getProcessorResponse.processor.credentials.length; i++) {
										$scope.credType = data.getProcessorResponse.processor.credentials[i].credentialType;
										$scope.secret = data.getProcessorResponse.processor.credentials[i].password;
										
										// read secret should be called only if password is available in the login credential
										// for sftp processor with keys, password will not be available and hence 
										// read secret call to KMS is not applicable for this case
										if($scope.credType === 'LOGIN_CREDENTIAL' && ($scope.secret != null && $scope.secret != "" && typeof $scope.secret != 'undefined')) {
											readSecretFromKM($scope.url_secret_service + $scope.secret, i, data, profData, processorId, blockuiFlag);
											editProcessor = true;
											break;
										}
									}
									if(editProcessor === false) {
										$scope.editProcAfterReadSecret(data, profData, procsrId, blockuiFlag);

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
                    }
                );
            };
            $scope.loadBrowseData = function () {
                $scope.restService.get($scope.base_url + '/listFile', //Get mail box Data
                    function (data) {
                        $scope.roleList = data.ArrayList;
                        //$log.info($scope.roleList);
                        $scope.modal.roleList = $scope.roleList;
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
            $scope.setTypeDuringProcessorEdit = function (processorValue) {               
                $scope.procsrType = $scope.initialProcessorData.supportedProcessors.options[getIndexOfValue($scope.initialProcessorData.supportedProcessors.options, processorValue)];
            };
			$scope.setTypeDuringProtocolEdit = function (protocolValue) {               
                $scope.processor.protocol = $scope.initialProcessorData.supportedProtocols.options[getIndexOfValue($scope.initialProcessorData.supportedProtocols.options, protocolValue)];
				if (typeof($scope.processor.protocol) === "undefined") {
				$scope.processor.protocol = $scope.initialProcessorData.supportedProcessors.options[getIndexOfValue($scope.initialProcessorData.supportedProcessors.options, protocolValue)];
				}
            };			
			
			$scope.getFolderTypeDuringProcessorEdit = function (folderID) {
				//console.log(folderID);
                if($scope.procsrType.value === 'REMOTEDOWNLOADER') {
					return getName($scope.allStaticPropertiesForDownloaderProcessorFolder, folderID);
				} else if ($scope.procsrType.value === 'REMOTEUPLOADER') {
					return getName($scope.allStaticPropertiesForUploaderProcessorFolder, folderID);
				} else if ($scope.procsrType.value === 'FILEWRITER') {
					return getName($scope.allStaticPropertiesForFileWriterProcessorFolder, folderID);
				} else {
					return getName($scope.allStaticPropertiesForSweeperProcessorFolder, folderID);
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
            	
            	if($scope.processor.protocol.value === 'DROPBOXPROCESSOR') {
            		var dbxVal = getIndex($scope.allMandatoryDropboxProperties, name);
					if (dbxVal !== -1) {
                    	return dbxVal;
                	}
            	} else {
            		var httpListenerVal = getIndex($scope.allMandatoryHttpListenerProperties, name);
					if (httpListenerVal !== -1) {
                  	  return httpListenerVal;
               		 }	
            	}
            	
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
			
				$scope.processor.processorProperties.staticProperties = [];	
                for (var i = 0; i < $scope.propertiesAddedToProcessor.length; i++) {
                    var property = $scope.propertiesAddedToProcessor[i];
                     if ($scope.propertiesAddedToProcessor[i].name === "") {
				    	 continue;				    	 
				    }			
					$scope.processor.processorProperties.staticProperties.push(property);				
                }
				for (var i = 0; i < $scope.availableProperties.length; i++) {
                    var property = $scope.availableProperties[i];
                    if ($scope.availableProperties[i].name === "") {
				    	 continue;				    	 
				     }					
					$scope.processor.processorProperties.staticProperties.push(property);				
                }  				
				
                $scope.processor.processorProperties.handOverExecutionToJavaScript = $scope.isJavaScriptExecution;
				$scope.processor.processorProperties.type = $scope.procsrType.value;
				$scope.processor.processorProperties.protocol = $scope.processor.protocol.value;
				$scope.processor.processorProperties.displayName = $scope.procsrType.key;				
                //console.log(commaSplit);
                var lenFolderProps = $scope.processorFolderProperties.length;
				
				//Removed empty folder row for sweeper and File Writer
				if ($scope.processor.protocol.value === 'SWEEPER' || $scope.processor.protocol.value === 'FILEWRITER') lenFolderProps = 2;
				
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
                        userId: $scope.processorCredProperties[i].userId,
                        password: $scope.processorCredProperties[i].password,
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
                $scope.processor.javaScriptURI = $scope.trimScriptTemplateName();                
				$scope.scriptIsEdit = false;
				if ($scope.processor.javaScriptURI != null && 
						$scope.processor.javaScriptURI != "") {
						$scope.scriptIsEdit = true;
				}				
				
                block.blockUI();
                if ($scope.isEdit) {
                    editRequest.reviseProcessorRequest.processor = $scope.processor;
					$scope.appendPortToUrl();
                    editRequest.reviseProcessorRequest.processor.status = $scope.status.value;
                    editRequest.reviseProcessorRequest.processor.type = $scope.procsrType.value;
					editRequest.reviseProcessorRequest.processor.protocol = $scope.processor.protocol.value;
					
						var reviseProcessor = false;
						for(var i = 0; i < $scope.editRequest.reviseProcessorRequest.processor.credentials.length; i++) {
						
							$scope.credType = editRequest.reviseProcessorRequest.processor.credentials[i].credentialType;
							$scope.procName = editRequest.reviseProcessorRequest.processor.name;
							$scope.credUsrName = editRequest.reviseProcessorRequest.processor.credentials[i].userId;
							$scope.secret = editRequest.reviseProcessorRequest.processor.credentials[i].password;
							
							$scope.secretName = '';
							
							// revise secret should be called only if password is available in the login credential
							// for sftp processor with keys, password will not be available and hence 
							// revise secret call to KMS is not applicable for this case
							if($scope.credType === 'LOGIN_CREDENTIAL' && ($scope.secret != null && $scope.secret != "" && typeof $scope.secret != 'undefined')) {
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
                } else {
				
                    addRequest.addProcessorToMailBoxRequest.processor = $scope.processor;
					$scope.appendPortToUrl();
                    addRequest.addProcessorToMailBoxRequest.processor.status = $scope.status.value;
                    addRequest.addProcessorToMailBoxRequest.processor.type = $scope.procsrType.value;
					addRequest.addProcessorToMailBoxRequest.processor.protocol = $scope.processor.protocol.value;
					
						var saveProcessor = false;
						for(var i = 0; i < $scope.addRequest.addProcessorToMailBoxRequest.processor.credentials.length; i++) {
						
							$scope.credType = addRequest.addProcessorToMailBoxRequest.processor.credentials[i].credentialType;
							$scope.procName = addRequest.addProcessorToMailBoxRequest.processor.name;
							$scope.credUsrName = addRequest.addProcessorToMailBoxRequest.processor.credentials[i].userId;
							$scope.secret = addRequest.addProcessorToMailBoxRequest.processor.credentials[i].password;
							
							$scope.secretName = '';
							// store secret should be called only if password is available in the login credential
							// for sftp processor with keys, password will not be available and hence 
							// store secret call to KMS is not applicable for this case
							if($scope.credType === 'LOGIN_CREDENTIAL' && ($scope.secret != null && $scope.secret != "" && typeof $scope.secret != 'undefined')) {
							
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
            };
			
			
            function reviseSecret(secretUrl, base64EncodedSecret, a) {
				$scope.restService.put(secretUrl, base64EncodedSecret,
					function (data, status) {
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
										showSaveMessage("Key manager failed to revise stored secret", 'error');
										return;
									}
								}, {'Content-Type': 'application/octet-stream'}
							);
						} else {
							block.unblockUI();
							showSaveMessage("Key manager failed to revise stored secret", 'error');
							return;
						}
					}, {'Content-Type': 'application/octet-stream'}
				);
			}
			
			function storeSecret(secretUrl, base64EncodedSecret, a) {
				$scope.restService.post(secretUrl, base64EncodedSecret,
					function (secdata, status) {
						//console.log('status and data = ' + secdata + ', '+ status);
						if (status === 201) {
							addRequest.addProcessorToMailBoxRequest.processor.credentials[a].password = secdata;
							$scope.processorSaveAfterKM();
						} else {
							block.unblockUI();
							showSaveMessage("Key manager failed to add stored secret", 'error');
							return;
						}
					}, {'Content-Type': 'application/octet-stream'}
				);
			}
			
			$scope.processorReviseAfterKM = function() {
				//$log.info($filter('json')(editRequest));
				$scope.restService.put($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + $scope.processor.guid, $filter('json')(editRequest),
					function (data, status) {
						if (status === 200 || status === 400) {
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
						   $scope.setTypeDuringProtocolEdit($scope.processor.protocol);	
							showSaveMessage("Error while saving processor", 'error');
						}
						block.unblockUI();
						$scope.clearProps();
					}
				);
			}
			
			$scope.processorSaveAfterKM = function() {
				//$log.info($filter('json')(addRequest));
				$scope.restService.post($scope.base_url + '/' + $location.search().mailBoxId + '/processor' + '?sid=' + $rootScope.serviceInstanceId, $filter('json')(addRequest),
					function (data, status) {
						if (status === 200 || status === 400) {
							if (data.addProcessorToMailBoxResponse.response.status === 'success') {
							//$scope.readOnlyProcessors = true;
							$scope.readAllProcessors();
							//$scope.readAllProfiles();
							$scope.isEdit = true;
							$scope.processor.guid = data.addProcessorToMailBoxResponse.processor.guId;
							$scope.editProcessor($scope.processor.guid, false);
								if($scope.isFileSelected)  $scope.isFileSelected = false;
								$scope.isPrivateKeySelected = false;
								$scope.isPublicKeySelected = false;
								showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'success');
							} else {
								showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'error');
							}
						} else {
						    $scope.setTypeDuringProtocolEdit($scope.processor.protocol);	
							showSaveMessage("Error while saving processor", 'error');
						}
						$scope.clearProps();
						block.unblockUI();
					}
				);
			};
			
            $scope.clearProps = function () {
                $scope.processor.folders = [];
                $scope.processor.credentials = [];
                //$scope.processor.remoteProcessorProperties.otherRequestHeader = [];
            };
            /*This function is used to notify passwordDirective to clear the password and error message*/
            $scope.doSend = function(){
				$scope.$broadcast('clearPassword');
			}
            $scope.addNew = function () {

                    $scope.formAddPrcsr.$setPristine();
                    $scope.loadOrigin();
					$scope.resetProcessorType($scope.procsrType);
                    $scope.readAllProfiles();
                    $scope.closeDelete();
                    //To notify passwordDirective to clear the password and error message
                    $scope.doSend();
                    $scope.isPortDisabled = false;					
                    $scope.disableSSHKeys = true;
                    $scope.disableCertificates = true;
					//GIT URL
				    $scope.script = '';
			        $scope.scriptIsEdit = false; 	
                    $scope.isJavaScriptExecution = false;
                    $scope.formAddPrcsr.scriptName.$setValidity('allowed', true);
                    formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';
                     // To reset the values in the file browser window
                    $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                    $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
            };            
            // Close the modal
            $scope.closeDelete = function () {
                $('#myModal').modal('hide')
            };           
            $scope.setFolderData = function () {
                if ($scope.procsrType.value === "SWEEPER") {
                    $scope.processorFolderProperties = [{
                        folderURI: '',
                        folderType: 'Payload Location',
                        folderDesc: '',
                        isMandatory: true,
                        allowAdd: false
                    }];
                } else if ($scope.procsrType.value === "FILEWRITER") {
                    $scope.processorFolderProperties = [{
                        folderURI: '',
                        folderType: 'File Write Location',
                        folderDesc: '',
                        isMandatory: true,
                        allowAdd: false
                    }];
                } else if ($scope.procsrType.value === "REMOTEDOWNLOADER"){
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
                } else if ($scope.procsrType.value === "REMOTEUPLOADER") {
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
				} else {
					$scope.processorFolderProperties = [];
					$scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = [];
				}
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
               // console.log(element.value);
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
                //console.log('Entering upload event');
                 block.blockUI();
                var fd = new FormData();
                $scope.pkObj['serviceInstanceId'] = Date.now().toString();
				$scope.pkObj.dataTransferObject['name'] = $scope.processor.name.concat('_',$scope.procsrType.value,'_',$scope.certificateModal.certificateURI);
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
                    if ($scope.certificateModal.isGlobalTrustore === "0") {
                        //console.log('creating self signed trust store');
                        $scope.uploadToSelfSignedTrustStore(pkGuid);
                    } else {
                       // console.log('uploading to global trust store');
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
                $scope.restService.post($scope.base_url + '/processor/uploadkey','POST Self signed key to key manager',
                    function (data, status) {
                        if (status === 200 && data.getTrustStoreResponse.response.status === 'success') {
                            $scope.linkTrustStoreWithCertificate(pkGuid, data.getTrustStoreResponse.trustStore.trustStoreId,
                                data.getTrustStoreResponse.trustStore.trustStoreGroupId);
                        } else {
                            block.unblockUI();
                            showSaveMessage('Certificate Uploading Failed', 'error');
                            return;
                        }
                    }
                );
            };
            $scope.linkTrustStoreWithCertificate = function (pkGuid, trustStoreId, trustStoreGroupId) {
            	
            	$scope.linkKeyTs['serviceInstanceId'] = Date.now().toString();
                // To put public key is association json to TrustStore
                $scope.linkKeyTs['dataTransferObject']['trustStoreMemberships'][0]['publicKey']['pguid'] = pkGuid;
                $scope.restService.put($scope.url_link_key_store + trustStoreId, angular.toJson($scope.linkKeyTs),
                    function (data, status) {
                        if (status === 200) {
                            $scope.certificateModal.trustStoreGroupId = trustStoreGroupId;
                            $scope.addCertificateDetails();
                            $scope.resetCredentialModal();
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
			if($scope.processor.protocol.value === "FTPS" || $scope.processor.protocol.value === "HTTPS") {
				$scope.disableCertificates = false;
			} else {
                $scope.disableCertificates = true;
			}
            $scope.disableSSHKeys = ($scope.processor.protocol.value === "SFTP")?false:true;
            
 			$scope.resetFiles = function() {
				document.getElementById('mbx-procsr-certificatebrowse').value = null;
			}

			$scope.appendPortToUrl = function() {
			    
				var defaultPort = '';
				var baseUrl = '';
                for (var i = 0; i < $scope.processor.processorProperties.staticProperties.length; i++) {
                    var portProperty = $scope.processor.processorProperties.staticProperties[i];
					if (portProperty.name === "port") {
					   defaultPort = portProperty.value;				   
					   for (var j = 0; j < $scope.processor.processorProperties.staticProperties.length; j++) {
					       var urlProperty = $scope.processor.processorProperties.staticProperties[j];
						   if (urlProperty.name == "url") {
						      baseUrl = urlProperty.value;							  
							  if (typeof baseUrl !== 'undefined' && baseUrl !== '') {
								var basePort = baseUrl.split('/')[2].split(':')[1]
								if (basePort === '' || basePort === null || typeof basePort === 'undefined') {
									if (defaultPort !== '') {
										var url_parts = baseUrl.split('/');
										var domain_name_parts = url_parts[2].split(':');
										var domainWithPort = domain_name_parts[0].concat(':', defaultPort);
										var newBaseUrl = baseUrl.replace(domain_name_parts[0], domainWithPort);
										$scope.processor.processorProperties.staticProperties[j].value = newBaseUrl;
										if ($scope.processor.processorProperties.staticProperties[i].readOnly === false) {						    
										   $scope.processor.processorProperties.staticProperties[i].readOnly = true;
										}
									}
								}
							  }						  
						    }
					     }
						 
					  }
			     }				
			};
			
             // SSHkeys Uploading section begins
             $scope.setSSHPrivateKey = function (element) {
                //console.log(element.value);
                $scope.$apply(function ($scope) {
                    // Turn the FileList object into an Array
                    for (var i = 0; i < element.files.length; i++) {
                         $scope.sshKeys.privatekey = element.files[i];
                    }
                    //console.log('sshKeys:', $scope.sshKeys);
                    $scope.sshkeyModal.sshPrivateKeyURI = element.files[0].name;
                    $scope.isPrivateKeySelected = true;
                    $scope.progressVisible = false;
                });
             };
             
              $scope.setSSHPublicKey = function (element) {
               // console.log(element.value);
                $scope.$apply(function ($scope) {
                    // Turn the FileList object into an Array
                    for (var i = 0; i < element.files.length; i++) {
                       $scope.sshKeys.publickey = element.files[i];
                    }
                   // console.log('sshKeys:', $scope.sshKeys);
                    $scope.sshkeyModal.sshPublicKeyURI = element.files[0].name;
                    $scope.isPublicKeySelected = true;
                    $scope.progressVisible = false;
                });
             };
            
              $scope.uploadSSHKey = function () {
                //console.log('Entering upload event of ssh keys');
                if ($scope.sshkeyModal.sshKeyPairPassphrase !== $scope.sshkeyModal.sshKeyPairConfirmPassphrase) {
    				showSaveMessage("Passwords does not match.", 'error');
    				$(sshkeypassphrase).focus();
    				return;
    			}
                block.blockUI();
                formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';
                var fd = new FormData();
                $scope.sshKeyObj['serviceInstanceId'] = Date.now().toString();
				$scope.sshKeyObj.dataTransferObject['name'] = $scope.processor.name.concat('_',$scope.procsrType.value,'_',$scope.sshkeyModal.sshkeyURI);
				var currentDate = new Date();
				var afterOneYear = new Date();
				afterOneYear.setYear(currentDate.getFullYear() + 1);
				$("#yearFromNow").append(afterOneYear.toString());
				$scope.sshKeyObj.dataTransferObject['createdDate'] = currentDate.toISOString();
				$scope.sshKeyObj.dataTransferObject['validityDateFrom'] = currentDate.toISOString();
				$scope.sshKeyObj.dataTransferObject['validityDateTo'] = afterOneYear.toISOString();
                $scope.sshKeyObj.dataTransferObject['custodianPassphrase'] = $scope.sshkeyModal.sshKeyPairPassphrase;
                fd.append("json", angular.toJson($scope.sshKeyObj));
                //console.log(angular.toJson($scope.sshKeyObj));
                fd.append($scope.sshKeys.privatekey.name, $scope.sshKeys.privatekey);
                fd.append($scope.sshKeys.publickey.name, $scope.sshKeys.publickey);
                var xhr = new XMLHttpRequest();
                xhr.addEventListener("load", sshkeyUploadComplete, false);
                xhr.addEventListener("error", sshkeyUploadFailed, false);
                xhr.addEventListener("abort", sshkeyUploadCanceled, false);
                xhr.open("POST", $scope.url_ssh_upload_key);
                xhr.send(fd);
            };
              function sshkeyUploadComplete(evt) {
                // To reset the values in the file browser window
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
                /* This event is raised when the server send back a response */
                if (evt.target.status === 201) {
                   // console.log('ssh key uploaded successfully');
                    var resp = angular.fromJson(evt.target.responseText);
                    var pkGuid = resp['dataTransferObject']['pguid'];
                    //var pkGuid = 'F45EE0F10A006FF106655CE31D400F66';
                    // Keygroup guid 
                    pkGuid = pkGuid.toString();
                    $scope.sshkeyModal.sshKeyPairPguid = pkGuid;
                    $scope.addSSHKeyDetails();
                    $scope.resetCredentialModal();

               } else {
                    block.unblockUI();
                    /*var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the sshkey' : 'Processor creation failed because there is an error while uploading the sshkey';*/
                    showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                    return;
                }
            }
            
             function sshkeyUploadFailed(evt) {
                // To reset the values in the file browser window
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
                block.unblockUI();
                /*var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the sshkey' : 'Processor creation failed because there is an error while uploading the sshkey';*/
                showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                return;
            }

            function sshkeyUploadCanceled(evt) {
                // To reset the values in the file browser window
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
                block.unblockUI();
                /*var msg = ($scope.isEdit === true) ? 'Processor revision failed because there is an error while uploading the sshkey' : 'Processor creation failed because there is an error while uploading the sshkey';*/
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
                        $scope.sshkeyModal.sshKeyPairPassphrase = '';
                        $scope.sshkeyModal.sshKeyPairConfirmPassphrase = '';
                        if(credObj.idpType == 'PRIVATE') {
                             $scope.sshkeyModal.sshPrivateKeyURI = ''; 
                        } else {
                             $scope.sshkeyModal.sshPublicKeyURI = '';
                        }                                                                        
                    }
                    if (credObj.credentialType == 'TRUSTSTORE_CERT') {
                        $scope.certificateModal.certificateURI = '';
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
                    password: '',
                    idpURI: $scope.sshkeyModal.sshKeyPairPguid,
                    idpType: 'PRIVATE',
                    allowAdd: false
                }, {
                    credentialURI: $scope.sshkeyModal.sshPublicKeyURI,
                    credentialType: 'SSH_KEYPAIR',
                    userId: '',
                    password: '',
                    idpURI: $scope.sshkeyModal.sshKeyPairPguid,
                    idpType: 'PUBLIC',
                    allowAdd: false
                });
                $scope.processorCredProperties = $scope.processorCredProperties.slice();
                if (!$scope.$$phase) {
                    $scope.$apply();
                };
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
		   
		   //whenever selection change in choose a file
			$scope.$watch('currentNode.roleName', function () {
				if(typeof ($scope.currentNode) !== 'undefined') {
					var path = $scope.currentNode.roleName;
					if(path.split('/').pop().split('.').length > 1) {
						$scope.isDirectoryPath = false;
					} else {
						$scope.isDirectoryPath = true;
					}
				}
			});
			
			$scope.script = '';
			$scope.scriptIsEdit = false;
			$scope.scriptUrlIsValid = false;
			$scope.disable = true;	
			$scope.scriptTemplateIsExist = false;		
			$scope.editor = '';
			$scope.loader = false;
			//create new script.			
			$scope.editScripTemplatetName = '';
			$scope.trimScriptTemplateName = function () {
			  if ($scope.modal.uri != '' && $scope.modal.uri != null && $scope.modal.uri.indexOf('gitlab:/') == 0) {
				  return $scope.modal.uri.split("gitlab:/").pop();
			  }
			  return $scope.modal.uri;
			}			
			$scope.onScriptTypeSelected = function () {				  
				  $scope.editScripTemplatetName = $scope.modal.uri;
				  if ($scope.modal.uri) {	
                      block.blockUI();			  
					  $scope.restService.get($scope.base_url + "/git/content/" + $scope.trimScriptTemplateName(),
					  function (data, status) { 
                         block.unblockUI();
                        if (status === 200 || status === 400) {
							if (data.scriptserviceResponse.response.status === 'success') {
								 $scope.scriptIsEdit = true;					
								 $scope.script = data.scriptserviceResponse.script;
								 $scope.populateScriptOnModel();
							} else {
							   $scope.script = '';
							   if (!$scope.scriptIsEdit) {
							    $scope.scriptIsEdit = false;
							   }							   
							   $scope.populateScriptOnModel();
							}	
						  } else {
							 showSaveMessage("Error while retrieve File from GitLab", 'error');
						  }				   
				    }
                );				   
				 }
				 $scope.disable = false;	 
			 };			  
			 $scope.populateScriptOnModel = function () {			  
			  var modalInstance = $modal.open({
				            templateUrl: 'partials/processor/createScriptFileModal.html',
				            controller: ScriptCreateFileController,
				            scope: $scope,
							resolve: {}
				        });
			};
			
			$scope.resetProcessorType = function(proceesorType) {
				  $scope.selectedProcessorType = proceesorType.value;
				  $scope.initialSetUp();			  
				if ($scope.selectedProcessorType === 'SWEEPER') {
					$scope.isProcessorTypeSweeper = true;
					$scope.isProcessorTypeHTTPListener = false;
					$scope.isProcessorTypeFileWriter = false;
					$scope.isProcessorTypeDropbox = false;
					$scope.setFolderData();
					$scope.processor.protocol = $scope.initialProcessorData.supportedProcessors.options[getIndexOfValue($scope.initialProcessorData.supportedProcessors.options, $scope.selectedProcessorType)];
					$rootScope.restService.get('data/processor/properties/sweeper.json', function (data) {                    
					  $scope.separateProperties(data.processorDefinition.staticProperties);
					
					});	
				} else if ($scope.selectedProcessorType === 'HTTPSYNCPROCESSOR' || $scope.selectedProcessorType === 'HTTPASYNCPROCESSOR') {
					$scope.isProcessorTypeSweeper = false;
					$scope.isProcessorTypeHTTPListener = true;
					$scope.isProcessorTypeFileWriter = false;
					$scope.isProcessorTypeDropbox = false;
					$scope.setFolderData();
					$scope.processor.protocol = $scope.initialProcessorData.supportedProcessors.options[getIndexOfValue($scope.initialProcessorData.supportedProcessors.options, $scope.selectedProcessorType)];
					$rootScope.restService.get('data/processor/properties/httpsyncAndAsync.json', function (data) {						
					  $scope.separateProperties(data.processorDefinition.staticProperties);
					});	
				} else if ($scope.selectedProcessorType === 'FILEWRITER') {
					$scope.isProcessorTypeSweeper = false;
					$scope.isProcessorTypeHTTPListener = false;
					$scope.isProcessorTypeDropbox = false;
					$scope.isProcessorTypeFileWriter = true;
					$scope.setFolderData();
					$scope.processor.protocol = $scope.initialProcessorData.supportedProcessors.options[getIndexOfValue($scope.initialProcessorData.supportedProcessors.options, $scope.selectedProcessorType)];					
				} else if ($scope.selectedProcessorType === 'DROPBOXPROCESSOR') {
					$scope.isProcessorTypeSweeper = false;
					$scope.isProcessorTypeHTTPListener = false;
					$scope.isProcessorTypeFileWriter = false;
					$scope.isProcessorTypeDropbox = true;
					$scope.setFolderData();
					$scope.processor.protocol = $scope.initialProcessorData.supportedProcessors.options[getIndexOfValue($scope.initialProcessorData.supportedProcessors.options, $scope.selectedProcessorType)];
					$rootScope.restService.get('data/processor/properties/dropboxProcessor.json', function (data) {					
					  $scope.separateProperties(data.processorDefinition.staticProperties);
					});	
				} else  {
					$scope.resetProtocol($scope.processor.protocol);
				} 
                // function to clear the credential details if protocol is changed
                $scope.resetProcessorCredentialDetails();
                $scope.resetCredentialModal();                         		
			};
			$scope.resetProtocol = function(potocolType) {
			
				 $scope.isProcessorTypeSweeper = false;
				 $scope.isProcessorTypeHTTPListener = false;
				 $scope.isProcessorTypeFileWriter = false;
				 $scope.isProcessorTypeDropbox = false;
				 var protocalName = potocolType.value;
				 $scope.initialSetUp();
				 $scope.setFolderData();
				 $scope.processor.protocol = $scope.initialProcessorData.supportedProtocols.options[getIndexOfValue($scope.initialProcessorData.supportedProtocols.options, protocalName)];
				 if (!$scope.processor.protocol) {
					 $scope.processor.protocol = $scope.initialProcessorData.supportedProtocols.options[0];
					 protocalName = $scope.processor.protocol.value;
				 }	
				if ($scope.selectedProcessorType == 'REMOTEDOWNLOADER') {                     				 
					 if (protocalName == 'SFTP') {
					    $rootScope.restService.get('data/processor/properties/sftpdownloader.json', function (data) {					
						  $scope.separateProperties(data.processorDefinition.staticProperties);
				       });	
					 } else if (protocalName == 'FTP') {
					    $rootScope.restService.get('data/processor/properties/ftpdownloader.json', function (data) {						
							  $scope.separateProperties(data.processorDefinition.staticProperties);
						});
					 } else if (protocalName == 'FTPS') {
					    $rootScope.restService.get('data/processor/properties/ftpsdownloader.json', function (data) {							
							  $scope.separateProperties(data.processorDefinition.staticProperties);
						});
					 } else if (protocalName == 'HTTP' || protocalName == 'HTTPS') {
					    $rootScope.restService.get('data/processor/properties/httpdownloader.json', function (data) {				        
						  $scope.separateProperties(data.processorDefinition.staticProperties);
						});
					 }			  			
				} else if ($scope.selectedProcessorType == 'REMOTEUPLOADER') {					
                    if (protocalName == 'SFTP') {
					    $rootScope.restService.get('data/processor/properties/sftpuploader.json', function (data) {				        
						  $scope.separateProperties(data.processorDefinition.staticProperties);
				       });	
					 } else if (protocalName == 'FTP') {
					    $rootScope.restService.get('data/processor/properties/ftpuploader.json', function (data) {				        
						  $scope.separateProperties(data.processorDefinition.staticProperties);
						});
					 } else if (protocalName == 'FTPS') {
					    $rootScope.restService.get('data/processor/properties/ftpsuploader.json', function (data) {					       
						  $scope.separateProperties(data.processorDefinition.staticProperties);
						});
					 } else if (protocalName == 'HTTP' || protocalName == 'HTTPS') {
					    $rootScope.restService.get('data/processor/properties/httpuploader.json', function (data) {					       
						  $scope.separateProperties(data.processorDefinition.staticProperties);
						});
					 }				 
				}
                //GMB-201
                if(protocalName === "FTPS" || protocalName === "HTTPS") {
					$scope.disableCertificates = false;
     			} else {
					$scope.disableCertificates = true;
 				}
                $scope.disableSSHKeys = (protocalName === "SFTP")?false:true; 				
			};			
			
            $scope.separateProperties = function(jsonProperties) {
            	
                for (var i = 0; i < jsonProperties.length; i++) {
                    var property = jsonProperties[i];
                   if (property.hasOwnProperty('mandatory') && (property.mandatory === true)) {
                        $scope.propertiesAddedToProcessor.push(property);
                    } else if (property.hasOwnProperty('mandatory') && (property.mandatory === false)){
                        $scope.availableProperties.push(property);
                    }
                }
                
                // push empty object to enable additional
             $scope.propertiesAddedToProcessor.push({
            	
                    "name":"",
                    "displayName" : "",
                    "value":"",
                    "type":"textarea",
                    "readOnly":"",
                    "mandatory":false,
                    "dynamic":false,
                    "valueProvided":false,
                    "validationRules": {}
                 });              
            };
            
            $scope.propertiesAddedToProcessor = [];
			$scope.availableProperties = []; 
			$scope.propertiesEditToProcessor = [];
            $scope.initialSetUp = function() {            	          
                $scope.propertiesAddedToProcessor = [];
				$scope.propertiesEditToProcessor = [];
                $scope.availableProperties = [];         
                $scope.selectedProperty = {value:''};
                $scope.showAddNewComponent = {value:false};
            }
            $scope.cleanup = function() {
        	   
               $scope.selectedProperty.value = '';
               $scope.showAddNewComponent.value = false;
               
            }		   
            $scope.resetProcessorType($scope.procsrType);            

            $scope.gridOptionsTesting = {
                data: 'propertiesAddedToProcessor',
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
                    width: "40%",
                    displayName: "Name*",
                    enableCellEdit: false,
                    cellTemplate: '<dynamic-property-name-field-directive sort-name="sorting"  all-props=availableProperties selected-value=selectedProperty show-add-new-component="showAddNewComponent" current-row-object= propertiesAddedToProcessor[row.rowIndex] initial-state-object={{row.entity}}/>'
                   
                }, {
                     field: "value",
                     width: "40%",
                     displayName: "Value*",
                     enableCellEdit: false,
                     cellTemplate: '<dynamic-property-value-field-directive current-row-object = propertiesAddedToProcessor[row.rowIndex] test-attr=currentType/>'
                 }, {
                     field: "mandatory",
                     width: "20%",
                     displayName: "Action*",
                     enableCellEdit: false,
                     cellTemplate: '<dynamic-action-field-directive available-properties = availableProperties added-properties = propertiesAddedToProcessor current-row-object = propertiesAddedToProcessor[row.rowIndex] initial-state-object={{row.entity}}/>',
                 }
                ]
            }; 
            // listen to property modified notification and do clean up
            $rootScope.$on("propertyModificationActionEvent", function() {
               $scope.cleanup(); 
            });

        }
    ]);
var ScriptCreateFileController = function($rootScope, $scope, $filter, $http, $blockUI)  {
     
    // Editor Section Begins
      
      var block = $rootScope.block;
      $scope.createdBy = $rootScope.loginInput; 
	  $scope.loadValueData = function (_editor) {	  
        $scope.$parent.editor = _editor;
	    $scope.$parent.editor.getSession().setValue($scope.$parent.script);
     };
	 
     var defaultScriptFile = '';
     $scope.loadDefaultScript = function() {
	   if (defaultScriptFile === '' || defaultScriptFile !== $scope.$parent.editor.getSession().getValue()) {
	    $scope.loader = true;
     	 $scope.restService.get($scope.base_url + "/git/content/" + $scope.javaProperties.defaultScriptTemplateName,
          function (data, status) {
			   $scope.loader = false;
              if (status === 200 || status === 400) { 			
 				if (data.scriptserviceResponse.response.status === 'success') {
 					defaultScriptFile = data.scriptserviceResponse.script;
 				   $scope.$parent.editor.getSession().setValue(defaultScriptFile);
 				} else {
 					showSaveMessage(data.scriptserviceResponse.response.message, 'error');
 				} 
 			} else {
 			 showSaveMessage("Error while retrieve File from GitLab", 'error');
 			}          	
           }
 		);	
       }		
     };	 	 
     $scope.save = function() {	    	 
    	 if ($scope.$parent.scriptIsEdit) {
		     $scope.checkScript();	     		 
    	 } else {
		    $scope.saveScript();	
		 }    	 
     }
     $scope.revertScriptURL = function () {
	    $scope.$parent.modal.uri = $scope.$parent.editScripTemplatetName;	 
	 }
    //checkProfileJson     
	 $scope.checkScript = function() {
	    if ( $scope.$parent.editScripTemplatetName !== $scope.$parent.modal.uri && !$scope.scriptTemplateIsExist) {			 
			 $scope.saveScript();
	    } else if ($scope.$parent.script !== $scope.$parent.editor.getSession().getValue()){
		    $scope.editScript();		    
		} else {		  
		   showSaveMessage('No changes made to save.');
		}
	 }
     
   //CREATE NEW SCRIPT FILE IN GIT.     
	$scope.saveScript = function() {
		
		 block.blockUI();
		 $scope.createFileRequest = {
	  			scriptserviceRequest: {
	  				script : {
	              	data: "",
	              	scriptFileUri: "",
	              	createdBy: ""
	  				}
	           }
	      }; 
		  
		 $scope.$parent.editor = ace.edit("aceEditor");
		 $scope.scriptContents = $scope.$parent.editor.getSession().getValue();
         	    	     	
    	 $scope.createFileRequest.scriptserviceRequest.script.data = $scope.scriptContents;
         $scope.createFileRequest.scriptserviceRequest.script.scriptFileUri = $scope.$parent.trimScriptTemplateName();
         $scope.createFileRequest.scriptserviceRequest.script.createdBy = $scope.createdBy;
      
         $scope.restService.post($scope.base_url + "/git/content", $filter("json")($scope.createFileRequest), function() {} )
         .success(function (data) {
        	block.unblockUI(); 
        		$scope.$parent.scriptIsEdit = true;
        		$scope.$parent.script = $scope.scriptContents;
			    showSaveMessage(data.scriptserviceResponse.response.message, 'success');
         })
         .error(function(data) { 
        	block.unblockUI();
        	if (angular.isObject(data)) {
        		if (!$scope.$parent.scriptIsEdit) {
   				 $scope.$parent.scriptIsEdit = false;				
   				} else {
   				 $scope.$parent.modal.uri = $scope.$parent.editScripTemplatetName;				
   				}				
   				showSaveMessage(data.scriptserviceResponse.response.message, 'error');
        	} else {
        		showSaveMessage("Error while File save to GitLab", 'error');
        	}     	
        	
         });
      $scope.$dismiss();		 
	}; 
	
	//UPDATE EXIT SCRIPT FILE IN GIT. 
	 $scope.editScript = function() {
			
	    	block.blockUI();
	    	$scope.editFileRequest = {
	    			scriptserviceRequest: {
	    				script : {
		              	data: "",
		              	scriptFileUri: "",
		              	createdBy: ""
		  				}
		           }
	         };    	
	    	
	    	 $scope.$parent.editor = ace.edit("aceEditor");
			 $scope.scriptContents = $scope.$parent.editor.getSession().getValue();	    	
		    	     	
	    	 $scope.editFileRequest.scriptserviceRequest.script.data = $scope.scriptContents;
	         $scope.editFileRequest.scriptserviceRequest.script.scriptFileUri = $scope.$parent.trimScriptTemplateName();
	         $scope.editFileRequest.scriptserviceRequest.script.createdBy = $scope.createdBy;

	         $scope.restService.put($scope.base_url + "/git/content", $filter("json")($scope.editFileRequest), function() {} )
	            .success(function (data) {     
	            	block.unblockUI(); 
	            		$scope.$parent.scriptIsEdit = true;
	            		$scope.$parent.script = $scope.scriptContents;
					    showSaveMessage(data.scriptserviceResponse.response.message, 'success');					
	            })
	            .error(function(data) {
	            	block.unblockUI();
	            	if (angular.isObject(data)) {
	            	    showSaveMessage(data.scriptserviceResponse.response.message, 'error');
	            	} else {
	            		showSaveMessage("Error while File update to GitLab", 'error');	            		
	            	}	            	
	            });
				$scope.$dismiss();
	     };	
		 
     $scope.cancel = function () {
        if ($scope.$parent.scriptIsEdit) {
		  $scope.revertScriptURL();
		}	 
        $scope.$dismiss();
     }; 
};
