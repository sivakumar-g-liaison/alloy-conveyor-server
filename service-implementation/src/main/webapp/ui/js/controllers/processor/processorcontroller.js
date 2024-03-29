var rest = myApp.controller(
    'ProcessorCntrlr', ['$rootScope', '$modal', '$scope', '$timeout',
        '$filter', '$location', '$log', '$blockUI',
        function ($rootScope, $modal, $scope, $timeout, $filter,
            $location, $log, $blockUI) {
		
    		$scope.showCredentialInvalid = false;    
    		$scope.disableHTTPListenerPipeLineId = false;
            $scope.isJavaScriptExecution = false;
            $scope.isDirectSubmit = false;
            $scope.showDirectSubmit = false;
            //check JavaScriptExecutor
            $scope.onChangeJavaScriptExecutor = function () {            	
            	if ($scope.isJavaScriptExecution) {            		
            		$scope.isJavaScriptExecution = false;
            	} else {            		
            		$scope.isJavaScriptExecution = true;
            	} 	            	
            }
            //check Direct Submit
            $scope.onChangeDirectSubmit = function() {
                if ($scope.isDirectSubmit) {
                    $scope.isDirectSubmit = false;
                } else {
                    $scope.isDirectSubmit = true;
                }

                if ($scope.isEdit) {
                    $scope.editProcAfterDirectSubmit($scope.downloaderData,  $scope.profileData, $scope.processor.guid, true);
                } else {
                    $scope.resetProtocol($scope.processor.protocol);
                }
            }

            // variable to show or hide ssh keys section
            $scope.showSSHKeysSection = false;
            // variable to determine enable or disable add ssh keypair button
            $scope.disableSSHKeysAddition = true;
            // variable to show or hide truststore section
            $scope.showTruststoreSection = false;   
            //variable to determine enable or disable add Truststore button
            $scope.disableTrustoreAddition = true;
			//check directory path in choose file
			$scope.isDirectoryPath = true;
		
            // To be Populated
            $scope.mailBoxId;
            $scope.urlType = '';
			var isProcessorSearchFlag = false;
			var procsrId = '';
			var isSIdConstraint = true;
			var isDisableFilterValue = false;
            		
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
		    var secure = 'SECURE';
		    $scope.handleDisplayOfHTTPListenerURL = function (prcsrType, clusterType) {
				$scope.mailboxId = $location.search().mailBoxId;
				$scope.mailboxName = $location.search().mbxname;
				if( prcsrType === "HTTPSYNCPROCESSOR") {
					$scope.isProcessorTypeHTTPListener = true;
					if( $scope.isEdit && $scope.isProcessorTypeHTTPListener) {
						$scope.urlType = "HTTP Sync URL";
						if (secure === clusterType) {
						   $scope.urlPrefix = $rootScope.javaProperties.processorSecureSyncUrlDisplayPrefix;
						} else {
						   $scope.urlPrefix = $rootScope.javaProperties.processorLowSecureSyncUrlDisplayPrefix;
						}						
						$scope.displayHTTPListenerURL();
					}	
				}
				if( prcsrType === "HTTPASYNCPROCESSOR") {
					$scope.isProcessorTypeHTTPListener = true;
					if( $scope.isEdit && $scope.isProcessorTypeHTTPListener) {
						$scope.urlType = "HTTP Async URL";
						if (secure === clusterType) {
						   $scope.urlPrefix = $rootScope.javaProperties.processorSecureAsyncUrlDisplayPrefix;
						} else {
						   $scope.urlPrefix = $rootScope.javaProperties.processorLowSecureAsyncUrlDisplayPrefix;
						}
						$scope.displayHTTPListenerURL();
					}
				}
			}
		           
            $scope.loadOrigin = function () {
				
				//GMB-196
				$scope.sorting = 'name';
                $scope.isFileSelected = false;
        		$scope.isEdit = false;
                $scope.isProcessorTypeSweeper = false;
                $scope.isProcessorTypeConditionalSweeper = false;
                $scope.mailboxName = $location.search().mbxname;				
				//GIT URL
				$scope.script = '';
			    $scope.scriptIsEdit = false; 
				$scope.isJavaScriptExecution = false;
				$scope.isDirectSubmit = false;
				$scope.isCreateConfiguredLocation = true;
                // to disable protocol for http listener processor
                $scope.isProcessorTypeHTTPListener = false;
                // to disable protocol for file writer
                $scope.isProcessorTypeFileWriter = false;
                // to disable protocol for file writer
                $scope.isProcessorTypeDropbox = false;
                // to disable folder for http remote Uploader
                $scope.isProcessorTypeHTTPRemoteUploader = false;
                
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
                    clusterType: "",
                    createConfiguredLocation: true,
                    protocol: "",
                    linkedMailboxId: "",
                    linkedProfiles: [],              
                    processorPropertiesInTemplateJson: {
                    	type: "",
                    	displayName: "",
						protocol: "",
                    	handOverExecutionToJavaScript: false,
                    	staticProperties: [],
						folderProperties: [],
						credentialProperties:[]
                    }
                };
                
                $scope.directSubmit = {
                    "name":"directSubmit",
                    "displayName" : "Direct Submit",
                    "value":false,
                    "type":"select",
                    "readOnly":"",
                    "mandatory":false,
                    "dynamic":false,
                    "valueProvided":false,
                    "validationRules": null,
                	"options":[false,true]
            	};
                
                $scope.modal = {
                    "roleList": '',
                    "uri": ''
                };
				
                $scope.truststoreModal = {
                    "trustStoreGroupId": ''
                };
                
              // ssh key implementation
                $scope.sshkeyModal = {
                    "sshKeyPairGroupId": ''
                };
                $scope.processorData = $scope.initialProcessorData;
                $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
                $scope.status = $scope.processorData.supportedStatus.options[0];
                $scope.procsrType = $scope.processorData.supportedProcessors.options[0];
                $scope.processor.protocol = $scope.processorData.supportedProtocols.options[0];
                $scope.selectedProcessorType =  $scope.procsrType.value;
                $scope.processor.clusterType = $rootScope.javaProperties.clusterTypes[0];
                
                // Procsr Credential Props
               $scope.processorCredProperties = [];           
				
                $scope.valueSelectedinSelectionBox = {
                    value: ''
                };                               
                
                // Profile Related Stuff.
                $scope.allProfiles = [];
                $scope.selectedProfiles = [];
                
                $scope.oldLoginDetails = {
                		userId:"",
                		password:"",
                		passwordPguidInKMS:""
                }
            };
            
            $scope.supportedProtocols = angular.copy($scope.initialProcessorData.supportedProtocols.options);
            $scope.onClusterChange = function () {
     
                if ($scope.processor.clusterType == "LOWSECURE") {
     
                    $scope.lowSecureSupportedProtocols = [{"key":"FTP", "value":"FTP"}, {"key":"FTPS", "value":"FTPS"},{"key":"HTTP", "value":"HTTP"}, {"key":"HTTPS", "value":"HTTPS"}];
                    $scope.processorData.supportedProtocols.options = $scope.lowSecureSupportedProtocols;
                } else {
     
                    $scope.secureSupportedProtocols = $scope.supportedProtocols;
                    $scope.processorData.supportedProtocols.options = $scope.secureSupportedProtocols;
                }
                
                if ($scope.procsrType.value === 'REMOTEDOWNLOADER' || $scope.procsrType.value === 'REMOTEUPLOADER') {
                    $scope.processor.protocol = $scope.processorData.supportedProtocols.options[0];
                }
            }

            $scope.loadOrigin();
			
            $scope.initialLoad = function () {
			
			isProcessorSearchFlag = $location.search().isProcessorSearch;
			procsrId = $location.search().processorId;
			$rootScope.gridLoaded = false;
			isDisableFilterValue = $location.search().disableFilters;
				if(isProcessorSearchFlag === true || isProcessorSearchFlag === "true"){
					isSIdConstraint = false;
					$scope.readAllProcessors();
					$scope.readAllProfiles();
					$scope.editProcessor(procsrId,true);
				} else if (isDisableFilterValue === true || isDisableFilterValue === "true"){
					isSIdConstraint = false;
					$scope.readAllProcessors();
					$scope.readAllProfiles();
				} else {
					$scope.readAllProcessors();
					$scope.readAllProfiles();
				}
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
                $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId + '?addServiceInstanceIdConstraint=' + isSIdConstraint + '&sid=' + $rootScope.serviceInstanceId, //Get mail box Data
                    function (data) {
                        $scope.getPagedDataAsync(data,
                            $scope.pagingOptions.pageSize,
                            $scope.pagingOptions.currentPage);
					$rootScope.gridLoaded = true;		
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
		$scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
            if (newVal !== oldVal  && $scope.validatePageNumberValue(newVal, oldVal)) {
            	$scope.readAllProcessors();
            }
        }, true);

        $scope.$watch('pagingOptions.pageSize', function (newVal, oldVal) {
            if (newVal !== oldVal) {
               //Get data when in first page
               if ( $scope.pagingOptions.currentPage === 1) {
                    $scope.readAllProcessors();
               } else {
                    //If on other page than 1 go back
                    $scope.pagingOptions.currentPage = 1;
               }               
            }
        }, true);

		// Check that value in page number field is valid. Shows error if not valid value and set current page to 1
        $scope.validatePageNumberValue = function(newVal, oldVal) {
            // Value cannot be empty, non number or zero
            var valid = true;
            if(newVal === '' || !/^\d+$/.test(newVal) || newVal*1 == 0) {
                valid = false;
            }
            // Value cannot be bigger than calculated max page count
            else if($scope.totalServerItems !== undefined && $scope.totalServerItems !== 0 && newVal*1 > Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize)) {
                valid = false;
            }

            if(!valid)
            {
                $scope.pagingOptions.currentPage = oldVal;
                showSaveMessage("Invalid input value. Page "+$scope.pagingOptions.currentPage+" is shown.", 'error');
            }
            return valid;
        }			

            $scope.gridOptionsForProcessorList = {
                columnDefs: [{
                    field: 'name',
                    displayName: 'Name',
                    width: "40%"
                }, {
                    field: 'type',
                    displayName: 'Type',
                    width: "20%",
                    cellTemplate: 'partials/processor/processor_section_templates/processor_type_field_template.html'
                }, {
                    field: 'status',
                    displayName: 'Status',
                    width: "20%",
                    cellTemplate: 'partials/processor/processor_section_templates/processor_status_field_template.html'
                }, {
                    displayName: 'Action',
                    sortable: false,
                    width: "20%",
                    cellTemplate: 'partials/processor/processor_section_templates/processor_action_field_template.html'
                }],
                data: 'processorList',
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
            
            $scope.staticProperties;
            $scope.folderProperties;
            
			$scope.editProcAfterReadSecret = function(data, profData, processorId, blockuiFlag) {
				
				$scope.isEdit = true;
                var procsrId = processorId;                
				if (blockuiFlag === true) {
					$scope.block.unblockUI();
				}

                $scope.showDirectSubmit = false;				
				$scope.processorData = $scope.editProcessorData;
				$scope.allProfiles = profData.getProfileResponse.profiles;
				$scope.clearProps();
				$scope.processor.guid = data.getProcessorResponse.processor.guid;
				$scope.processor.name = data.getProcessorResponse.processor.name;
				$scope.processor.modifiedBy = data.getProcessorResponse.processor.modifiedBy;
				$scope.processor.modifiedDate = data.getProcessorResponse.processor.modifiedDate;
											
				//check if it is the gitlab url
				if (data.getProcessorResponse.processor.javaScriptURI != null && data.getProcessorResponse.processor.javaScriptURI != "") {
						$scope.modal.uri = data.getProcessorResponse.processor.javaScriptURI;
						$scope.modal.uri = 'gitlab:/'+ $scope.modal.uri;
				}	
							
				$scope.isJavaScriptExecution = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.handOverExecutionToJavaScript;
				
				$scope.processor.description = data.getProcessorResponse.processor.description;
				$scope.processor.clusterType = data.getProcessorResponse.processor.clusterType;
                if (data.getProcessorResponse.processor.status === 'ACTIVE') {
                    $scope.status = $scope.processorData.supportedStatus.options[0];
                } else if (data.getProcessorResponse.processor.status === 'INACTIVE') {
                    $scope.status = $scope.processorData.supportedStatus.options[1];
                } else {
                    $scope.status = $scope.processorData.supportedStatus.options[2];
                }

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
                if ($scope.processor.protocol.value === 'HTTPSYNCPROCESSOR' || $scope.processor.protocol.value === 'HTTPASYNCPROCESSOR' || $scope.processor.protocol.value === 'LITEHTTPSYNCPROCESSOR') {
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
                
                if ($scope.processor.protocol.value === 'SWEEPER') {
                    $scope.isProcessorTypeSweeper = true;
                } else {
                    $scope.isProcessorTypeSweeper = false;
                }
                
                if ($scope.processor.protocol.value === 'CONDITIONALSWEEPER') {
                    $scope.isProcessorTypeConditionalSweeper = true;
                } else {
                    $scope.isProcessorTypeConditionalSweeper = false;
                }
                
                if ($scope.processor.protocol.value === 'SWEEPER' || $scope.processor.protocol.value === 'CONDITIONALSWEEPER') {
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBoxSweeper.supportedJavaScriptCheckBox;
                } else {
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
                }

                if ($scope.procsrType.value === 'REMOTEDOWNLOADER') {
                    $scope.showDirectSubmit = true;
                }

				//GMB 221
				if($scope.processor.protocol.value === "FTPS" || $scope.processor.protocol.value === "HTTPS") {
					$scope.showTruststoreSection = true;	
									
				} else {
					$scope.showTruststoreSection = false;
				}
				if($scope.processor.protocol.value === "HTTP" || $scope.processor.protocol.value === "HTTPS") {
					$scope.isProcessorTypeHTTPRemoteUploader = true;	
				}
				$scope.showSSHKeysSection = ($scope.processor.protocol.value === "SFTP") ? true : false;
				
				$scope.propertiesAddedToProcessor = [];
			    $scope.availableProperties = [];
			    $scope.folderAddedToProcessor = [];
			    $scope.folderAvailableProperties = [];
                $scope.processorCredProperties = [];
                $scope.staticProperties = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.staticProperties;                
                $scope.folderProperties = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.folderProperties;

                for (var i = 0; i < $scope.staticProperties.length; i++) {
                    var property = $scope.staticProperties[i];
                    if (property.name === "directSubmit" && $scope.procsrType.value === "REMOTEDOWNLOADER") {
                        if (property.value === "true") {
                            $scope.isDirectSubmit = true;
                        } else {
                            $scope.isDirectSubmit = false;
                        }
                    }
                    if (property.mandatory === true || property.valueProvided === true) {
                        $scope.propertiesAddedToProcessor.push(property);
                    } else {
                        $scope.availableProperties.push(property);
                    }
                }

                for (var i=0; i < $scope.propertiesAddedToProcessor.length; i++) {
                    var property = $scope.propertiesAddedToProcessor[i];
                    if ($scope.procsrType.value === "REMOTEDOWNLOADER" && !$scope.isDirectSubmit) {
                        if (property.name === "pipeLineID" || property.name === "securedPayload" || property.name === "lensVisibility" || property.name === "directSubmit" ||  property.name === "useFileSystem") {
                            $scope.propertiesAddedToProcessor.splice(i, 1);
                            i--;
                        }
                    } else if (property.name === "directSubmit" && $scope.procsrType.value === "REMOTEDOWNLOADER") {
                        $scope.propertiesAddedToProcessor.splice(i, 1);
                        i--;
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
                for (var i = 0; i < $scope.folderProperties.length; i++) {
                    var property = $scope.folderProperties[i];
                    if (property.mandatory === true || property.valueProvided === true) {
                        $scope.folderAddedToProcessor.push(property);
                    } else {
                        $scope.folderAvailableProperties.push(property);
                    }
                }
            	
            	if ($scope.folderProperties.length > 1) {
                    $scope.folderAddedToProcessor.push({
                        "folderURI": "",
                        "folderDisplayType": "",
                        "folderType": "",
                        "folderDesc": "",
                        "mandatory": false,
                        "readOnly":false,
                        "valueProvided":false,
                        "validationRules":{}
                    });
                }
                $scope.processorCredProperties = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.credentialProperties.slice();
                if (!$scope.$$phase) {
                    $scope.$apply();
                };
            }

            $scope.editProcAfterDirectSubmit = function(data, profData, processorId, blockuiFlag) {

                $scope.processorData = $scope.editProcessorData;

                $scope.propertiesAddedToProcessor = [];
                $scope.availableProperties = [];
                $scope.staticProperties = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.staticProperties;                

                for (var i = 0; i < $scope.staticProperties.length; i++) {
                    var property = $scope.staticProperties[i];
                    if (property.mandatory === true || property.valueProvided === true) {
                        if (property.name === "pipeLineID") {
                            property.value = $rootScope.pipelineId;
                        }
                        $scope.propertiesAddedToProcessor.push(property);
                    } else {
                        $scope.availableProperties.push(property);
                    }
                }

                for (var i=0; i < $scope.propertiesAddedToProcessor.length; i++) {
                    var property = $scope.propertiesAddedToProcessor[i];
                    if ($scope.procsrType.value === "REMOTEDOWNLOADER" && !$scope.isDirectSubmit) {
                        if (property.name === "pipeLineID" || property.name === "securedPayload" || property.name === "lensVisibility" || property.name === "directSubmit" || property.name === "useFileSystem") {
                            $scope.propertiesAddedToProcessor.splice(i, 1);
                            i--;
                        }
                    } else if (property.name === "directSubmit" && $scope.procsrType.value === "REMOTEDOWNLOADER" ) {
                        $scope.propertiesAddedToProcessor.splice(i, 1);
                        i--;
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
            }
			
			function readSecretFromKM(url, a, data, profData, procsrId, blockuiFlag) {
				$scope.restService.get(url,
					function (secretData, status) {
						if(status === 200) {
							var decPwd = '';
							try {
								decPwd = $.base64.decode($.base64.decode(secretData));
							} catch (e) {
								showSaveMessage('The password from KMS isn\'t encoded. Please update the password.', 'error');
							}
							$scope.oldLoginDetails.password = decPwd;
							data.getProcessorResponse.processor.processorPropertiesInTemplateJson.credentialProperties[a].password = decPwd;
							$scope.editProcAfterReadSecret(data, profData, procsrId, blockuiFlag);
	
						} else if(status === 404) {
							$scope.setTypeDuringProtocolEdit(data.getProcessorResponse.processor.protocol);
							$scope.block.unblockUI();
							showSaveMessage('Key manager failed to retrieve the stored secret', 'error');
							return;
						}
					}
				);
			}
			
			$scope.clearOldLoginDetails = function() {				
                $scope.oldLoginDetails = {
                		userId:"",
                		password:"",
                		passwordPguidInKMS:""
                }
			}
			
            $scope.editProcessor = function (processorId, blockuiFlag) {
                
				if (blockuiFlag === true) {
					$scope.block.blockUI();
				}
				if(typeof $scope.formAddPrcsr != 'undefined'){
					$scope.formAddPrcsr.$setPristine();
				}
                $scope.loadOrigin();
                //To notify passwordDirective to clear the password and error message
                if(typeof $scope.formAddPrcsr != 'undefined'){
					$scope.doSend();
				}
                $scope.isEdit = true;
                var procsrId = processorId;
                $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + procsrId, //Get mail box Data
                    function (data, status) {

                        $scope.block.unblockUI();
                        if (status === 200) {
    						$scope.scriptIsEdit = false;
                            $scope.downloaderData = data;
    						if (data.getProcessorResponse.processor.javaScriptURI != null && 
    						data.getProcessorResponse.processor.javaScriptURI != "") {
    						$scope.scriptIsEdit = true;
    						}
    						//To display the posting HTTP Sync/Async URL for end user if they select HTTP Listener
    						$scope.handleDisplayOfHTTPListenerURL(data.getProcessorResponse.processor.type, data.getProcessorResponse.processor.clusterType);
    						$scope.isJavaScriptExecution = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.handOverExecutionToJavaScript;
    						
                            //Fix: Reading profile in procsr callback
                            $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                                function (profData) {
                                    
                                        $scope.profileData = profData;
    									var editProcessor = false;
    									for(var i = 0; i < data.getProcessorResponse.processor.processorPropertiesInTemplateJson.credentialProperties.length; i++) {
    										$scope.credType = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.credentialProperties[i].credentialType;
    										$scope.secret = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.credentialProperties[i].password;
    										
    										// read secret should be called only if password is available in the login credential
    										// for sftp processor with keys, password will not be available and hence 
    										// read secret call to KMS is not applicable for this case
    										if($scope.credType === 'LOGIN_CREDENTIAL' && ($scope.secret != null && $scope.secret != "" && typeof $scope.secret != 'undefined')) {
    											$scope.oldLoginDetails.userId = data.getProcessorResponse.processor.processorPropertiesInTemplateJson.credentialProperties[i].userId;
    											$scope.oldLoginDetails.passwordPguidInKMS = $scope.secret;											
    											readSecretFromKM($scope.url_secret_service + encodeURIComponent($scope.secret), i, data, profData, processorId, blockuiFlag);
    											editProcessor = true;
    											break;
    										}
    									}
    									if(editProcessor === false) {
    										$scope.editProcAfterReadSecret(data, profData, procsrId, blockuiFlag);

    									}											
                                }
                            );
                	    } else {
                	    	showSaveMessage(data.getProcessorResponse.response.message, 'error');
                	    	$scope.readAllProcessors();
                	    	$scope.addNew();
                	    }
                    }
                );
            };
            
            $scope.readAllProfiles = function () {
                $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                    function (data) {
                        $scope.allProfiles = data.getProfileResponse.profiles;
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
                $scope.procsrType = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options, processorValue)];
            };
			$scope.setTypeDuringProtocolEdit = function (protocolValue) {               
                $scope.processor.protocol = $scope.processorData.supportedProtocols.options[getIndexOfValue($scope.processorData.supportedProtocols.options, protocolValue)];
				if (typeof($scope.processor.protocol) === "undefined") {
				$scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options, protocolValue)];
				}
            };			
			
            $scope.doCancel = function () {			
				$scope.closeModalView(); 				
				if($location.search().isProcessorSearch){
					$location.$$search = {};
					$location.path('/mailbox/getProcessor');
				} else {
					$location.$$search = {};
					$location.path('/mailbox/getMailBox');
				}
            };
	        $scope.closeModalView = function () {
                 $('#cancelAction').modal('hide')
            };
            $scope.backToMailbox = function () {			
				$scope.backToMailboxeModalView(); 
				var redirectToId = $location.search().mailBoxId;
                if (typeof isDisableFilterValue == "undefined") {
                    if ($rootScope.serviceInstanceId == ""){
                        isDisableFilterValue = true;
                    } else {
                        isDisableFilterValue = false;
                    }
                }
				$location.$$search = {};
				$location.path('/mailbox/addMailBox').search('mailBoxId', redirectToId).search('disableFilters', isDisableFilterValue);
            };
			$scope.backToMailboxeModalView = function () {
                 $('#backToMailboxAction').modal('hide')
            };           
            
            $scope.save = function () {
                //To notify passwordDirective to clear the password and error message
            	if ($rootScope.serviceInstanceId == "" && !$scope.isEdit) {
            		showSaveMessage('Processor creation is not allowed, and it is allowed when it traverses from a task', 'error');
        			return;
        		}
                $scope.confirmProcessorSave();
            };

            $scope.closeConfirmProcessorDelete = function () {
                $("#confirmProcessorDelete").modal('hide');
            }

            $scope.confirmProcessorSave = function () {
                $scope.saveProcessor();
                $scope.formAddPrcsr.$setPristine();
                $scope.showAddNewComponent.value=false;
            }

            $scope.saveProcessor = function () {		    
			
				$scope.processor.processorPropertiesInTemplateJson.staticProperties = [];
                $scope.processor.processorPropertiesInTemplateJson.folderProperties = [];
                $scope.processor.processorPropertiesInTemplateJson.credentialProperties = [];
                
                //add static properties	
                for (var i = 0; i < $scope.propertiesAddedToProcessor.length; i++) {
                    var property = $scope.propertiesAddedToProcessor[i];
                     if ($scope.propertiesAddedToProcessor[i].name !== "" 
					     && (property.valueProvided === true || property.mandatory === true)) {
				    	 $scope.processor.processorPropertiesInTemplateJson.staticProperties.push(property);				    	 
				    }					
                }
                
            	if ($scope.procsrType.value === "REMOTEDOWNLOADER") {
                    $scope.directSubmit.value = $scope.isDirectSubmit;
                    $scope.processor.processorPropertiesInTemplateJson.staticProperties.push($scope.directSubmit);
                }
                
				//add folder properties
				for (var i = 0; i < $scope.folderAddedToProcessor.length; i++) {
				     var property = $scope.folderAddedToProcessor[i];
					 if ($scope.folderAddedToProcessor[i].folderURI !== "" && 
					     (property.valueProvided === true || property.mandatory === true)) {
				    	 $scope.processor.processorPropertiesInTemplateJson.folderProperties.push(property);				    	 
				    }                   					
				}			
				
                $scope.processor.processorPropertiesInTemplateJson.handOverExecutionToJavaScript = $scope.isJavaScriptExecution;
				$scope.processor.processorPropertiesInTemplateJson.type = $scope.procsrType.value;
				$scope.processor.processorPropertiesInTemplateJson.protocol = $scope.processor.protocol.value;
				$scope.processor.processorPropertiesInTemplateJson.displayName = $scope.procsrType.key;
                
				//add credential properties
                for (var i = 0; i < $scope.processorCredProperties.length; i++) {
                    var credObj = $scope.processorCredProperties[i];
					if (!isvalidCredentials($scope.processor, credObj)) {
						$scope.clearCredentialProps();
						return;
					}
                    delete credObj.passwordDirtyState;
                    if (credObj.credentialType === "LOGIN_CREDENTIAL" && credObj.userId !== "" && credObj.userId !== null 
                    		&& typeof credObj.userId !== "undefined" && credObj.valueProvided === true ) {
                        $scope.processor.processorPropertiesInTemplateJson.credentialProperties.push(credObj);
                    } else if (credObj.credentialType !== "LOGIN_CREDENTIAL"){
                        $scope.processor.processorPropertiesInTemplateJson.credentialProperties.push(credObj);
                    }
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
				
                $scope.block.blockUI();
                if ($scope.isEdit) {
                    editRequest.reviseProcessorRequest.processor = $scope.processor;
					$scope.appendPortToUrl();
                    editRequest.reviseProcessorRequest.processor.status = $scope.status.value;
                    editRequest.reviseProcessorRequest.processor.type = $scope.procsrType.value;
					editRequest.reviseProcessorRequest.processor.protocol = $scope.processor.protocol.value;
                    editRequest.reviseProcessorRequest.processor.createConfiguredLocation = $scope.isCreateConfiguredLocation;
					
						var reviseProcessor = false;
						for(var i = 0; i < $scope.editRequest.reviseProcessorRequest.processor.processorPropertiesInTemplateJson.credentialProperties.length; i++) {
						
							$scope.credType = editRequest.reviseProcessorRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].credentialType;
							$scope.procName = editRequest.reviseProcessorRequest.processor.name;
							$scope.credUsrName = editRequest.reviseProcessorRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].userId;
							$scope.secret = editRequest.reviseProcessorRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].password;					
							$scope.secretName = '';
							
							// revise secret should be called only if password is available in the login credential
							// for sftp processor with keys, password will not be available and hence 
							// revise secret call to KMS is not applicable for this case
							if($scope.credType === 'LOGIN_CREDENTIAL' && ($scope.secret != null && $scope.secret != "" && typeof $scope.secret != 'undefined')) {
								
								// if the existing userName and password remains same then no need to go for a KMS call
								if ($scope.oldLoginDetails.password === $scope.secret && $scope.oldLoginDetails.userId === $scope.credUsrName) {
									editRequest.reviseProcessorRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].password = $scope.oldLoginDetails.passwordPguidInKMS;
									break;
								} else {
									$scope.secretName = $scope.mailboxName + $scope.procName + $scope.credUsrName;
									base64EncodedSecret = $scope.base64EncodedSecret = $.base64.encode($scope.secret);
									$scope.secretUrl = $scope.url_secret_service + encodeURIComponent($scope.secretName);
									reviseSecret($scope.secretUrl, base64EncodedSecret, i);
									reviseProcessor = true;
									break;
								}

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
                    addRequest.addProcessorToMailBoxRequest.processor.createConfiguredLocation = $scope.isCreateConfiguredLocation;
                    //To display the posting HTTP Sync/Async URL for end user if they select HTTP Listener
                    $scope.handleDisplayOfHTTPListenerURL($scope.procsrType.value, $scope.processor.clusterType);
						var saveProcessor = false;
						for(var i = 0; i < $scope.addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties.length; i++) {
						
							$scope.credType = addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].credentialType;
							$scope.procName = addRequest.addProcessorToMailBoxRequest.processor.name;
							$scope.credUsrName = addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].userId;
							$scope.secret = addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].password;
							
							$scope.secretName = '';
							// store secret should be called only if password is available in the login credential
							// for sftp processor with keys, password will not be available and hence 
							// store secret call to KMS is not applicable for this case
							if($scope.credType === 'LOGIN_CREDENTIAL' && ($scope.secret != null && $scope.secret != "" && typeof $scope.secret != 'undefined')) {
								
								// if the existing userName and password remains same then no need to got for a KMS call
								if ($scope.oldLoginDetails.password === $scope.secret && $scope.oldLoginDetails.userId === $scope.credUsrName) {
									addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties[i].password = $scope.oldLoginDetails.passwordPguidInKMS;
									break;
								} else {
									$scope.secretName = $scope.mailboxName + $scope.procName + $scope.credUsrName;
									base64EncodedSecret = $scope.base64EncodedSecret = $.base64.encode($scope.secret);
									$scope.secretUrl = $scope.url_secret_service + encodeURIComponent($scope.secretName);
									
									storeSecret($scope.secretUrl, base64EncodedSecret, i);
									
									saveProcessor = true;
									break;
								}
								
							}
						}
					
						if(saveProcessor === false) {
							$scope.processorSaveAfterKM();
						}
                }
            };
			
			function isvalidCredentials(processor, credObj) {
				
				var credType = credObj.credentialType;
				if (credType === "LOGIN_CREDENTIAL" && credObj.valueProvided == true) {
					
					var ProcessorType = $scope.processor.processorPropertiesInTemplateJson.type;
					if (ProcessorType === "REMOTEUPLOADER" || ProcessorType === "REMOTEDOWNLOADER") {
						
						var protocol =  $scope.processor.processorPropertiesInTemplateJson.protocol;
						
						switch (protocol) {
							
							case "FTP":
							case "FTPS":
							case "FTPS":
								if (credObj.userId === "" || credObj.userId === null || typeof credObj.userId === "undefined") {
									$scope.block.unblockUI();
									showSaveMessage('Username cannot be empty', 'error');
									return false;
								} else if (credObj.password === "" || credObj.password === null || typeof credObj.password === "undefined") {
									$scope.block.unblockUI();
									showSaveMessage('Password cannot be empty', 'error');
									return false;
								} else {
									return true;
								}
							case "SFTP":
								if ((credObj.userId != null && credObj.userId != "" && typeof credObj.userId != "undefined") 
									&& (credObj.password === "" || credObj.password === null || typeof credObj.password === "undefined")) {
										
									if (!isSSHKeyPairAvailable()) {
										$scope.block.unblockUI();
										showSaveMessage('Either the Password or the SSH Keypair is Empty.', 'error');
										return false;
									}
								} else if (credObj.userId === "" || credObj.userId === null || typeof credObj.userId === "undefined") {
									$scope.block.unblockUI();
									showSaveMessage('Username cannot be empty', 'error');
									return false;
								} else if (credObj.password === "" || credObj.password === null || typeof credObj.password === "undefined") {
									$scope.block.unblockUI();
									showSaveMessage('Password cannot be empty', 'error');
									return false;
								} else {
									return true;
								}
							default:
								return true;
						}
					} else {
						return true;
					}
				
				} else {
					return true;
				}
			}
			
			function isSSHKeyPairAvailable() {
				
				for (var credProperty = 0; credProperty < $scope.processorCredProperties.length; credProperty++) {
					
					var credObj = $scope.processorCredProperties[credProperty];
					if (credObj.credentialType === "SSH_KEYPAIR" && (credObj.idpURI != null 
						&& credObj.idpURI != "" && typeof credObj.idpURI != "undefined")) {
							return true;
						}
				}
				return false;
			}
			
            function reviseSecret(secretUrl, base64EncodedSecret, a) {
				$scope.restService.put(secretUrl, base64EncodedSecret,
					function (data, status) {
					if (status === 200) {
							editRequest.reviseProcessorRequest.processor.processorPropertiesInTemplateJson.credentialProperties[a].password = data;
							$scope.processorReviseAfterKM();
							
						} else if (status === 404) {
							$scope.restService.post($scope.secretUrl, base64EncodedSecret,
								function (crdata, status2) {
									if (status2 === 201) {
										editRequest.reviseProcessorRequest.processor.processorPropertiesInTemplateJson.credentialProperties[a].password = crdata;
										$scope.processorReviseAfterKM();
									} else {
									    $scope.setTypeDuringProtocolEdit(editRequest.reviseProcessorRequest.processor.protocol);
										$scope.clearCredentialProps();	
										$scope.block.unblockUI();										
										showSaveMessage("Key manager failed to revise stored secret", 'error');
										return;
									}
								}, {'Content-Type': 'application/octet-stream'}
							);
						} else {
						    $scope.setTypeDuringProtocolEdit(editRequest.reviseProcessorRequest.processor.protocol);
							$scope.clearCredentialProps();	
							$scope.block.unblockUI();							
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
							addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties[a].password = secdata;
							$scope.processorSaveAfterKM();
						} else if(status === 409){
							handleConflictInKMSDuringStoredSecretAddition($scope.secretUrl, base64EncodedSecret, a);
						}else {
						    $scope.setTypeDuringProtocolEdit(addRequest.addProcessorToMailBoxRequest.processor.protocol);
							$scope.clearCredentialProps();	
							$scope.block.unblockUI();							
							showSaveMessage("Key manager failed to add stored secret", 'error');
							return;
						}
					}, {'Content-Type': 'application/octet-stream'}
				);
			}
			
			function handleConflictInKMSDuringStoredSecretAddition(secretUrl, base64EncodedSecret, a){
				$scope.restService.put(secretUrl, base64EncodedSecret,
					function (data, status) {
					if (status === 200) {
							addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties[a].password = data;
							$scope.processorSaveAfterKM();
							
						} else if (status === 404) {
							$scope.restService.post($scope.secretUrl, base64EncodedSecret,
								function (crdata, status2) {
									if (status2 === 201) {
										addRequest.addProcessorToMailBoxRequest.processor.processorPropertiesInTemplateJson.credentialProperties[a].password =crdata;
										$scope.processorSaveAfterKM();
									}
									else {
									    $scope.setTypeDuringProtocolEdit(editRequest.reviseProcessorRequest.processor.protocol);
										$scope.clearCredentialProps();	
										$scope.block.unblockUI();										
										showSaveMessage("Key manager failed to revise stored secret", 'error');
										return;
									}
								}, {'Content-Type': 'application/octet-stream'}
							);
						} else {
						    $scope.setTypeDuringProtocolEdit(editRequest.reviseProcessorRequest.processor.protocol);
							$scope.clearCredentialProps();	
							$scope.block.unblockUI();							
							showSaveMessage("Key manager failed to revise stored secret", 'error');
							return;
						}
					}, {'Content-Type': 'application/octet-stream'}
				);
			}
			
			$scope.processorReviseAfterKM = function() {
				$scope.restService.put($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + $scope.processor.guid, $filter('json')(editRequest),
					function (data, status) {
						if (status === 200 || status === 400) {
							if (data.reviseProcessorResponse.response.status === 'success') {

                                $scope.editProcessor($scope.processor.guid, false);
                                if($scope.isFileSelected)  $scope.isFileSelected = false;
                                $scope.isPrivateKeySelected = false;
                                $scope.isPublicKeySelected = false;
                                showSaveMessage(data.reviseProcessorResponse.response.message, 'success');
							} else {
								showSaveMessage(data.reviseProcessorResponse.response.message, 'error');	
								$scope.setTypeDuringProtocolEdit($scope.processor.protocol);
							    $scope.clearCredentialProps();														
								
							}
							//$scope.readOnlyProcessors = true;
							$scope.readAllProcessors();
							//$scope.readAllProfiles();
						} else {
							$scope.setTypeDuringProtocolEdit($scope.processor.protocol);
						    $scope.clearCredentialProps();								
							showSaveMessage("Error while saving processor", 'error');
						}
						$scope.block.unblockUI();
						$scope.clearProps();
					}
				);
			}
			
			$scope.processorSaveAfterKM = function() {
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
							showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'success');
							} else {
							    $scope.clearCredentialProps();								
								$scope.setTypeDuringProtocolEdit($scope.processor.protocol);
								showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'error');
							}
						} else {
						    $scope.clearCredentialProps();
						    $scope.setTypeDuringProtocolEdit($scope.processor.protocol);	
							showSaveMessage("Error while saving processor", 'error');
						}
						$scope.clearProps();
						$scope.block.unblockUI();
					}
				);
			};
			//clear Credential properties if processor creation failed.
			$scope.clearCredentialProps = function () {	
				
				for (var i = 0; i < $scope.processorCredProperties.length; i++) {
					if ($scope.processorCredProperties[i].credentialType === "LOGIN_CREDENTIAL" && $scope.processorCredProperties[i].valueProvided == true) {
						
						// if both old login details and current login details remains same, 
						// then there will not be any call to KMS so just retain the password details 
						// without clearing it
						if  ($scope.oldLoginDetails.password === $scope.secret && $scope.oldLoginDetails.userId === $scope.processorCredProperties[i].userId) {
							// the password is set again otherwise KMS guid will be displayed
							$scope.processorCredProperties[i].password = $scope.secret;
						} else { // if not there will be a call to KMS for secret creation/revision so clear the password
							$scope.processorCredProperties[i].password = ''; 
							$scope.$broadcast('mandatePassword');
						}

					}                    				   
                }		   
			}
            $scope.clearProps = function () {
                $scope.processor.processorPropertiesInTemplateJson.credentialProperties = [];
                //$scope.processor.remoteProcessorProperties.otherRequestHeader = [];
            };
            /*This function is used to notify passwordDirective to clear the password and error message*/
            $scope.doSend = function(){
				$scope.$broadcast('clearPassword');
			}
            $scope.addNew = function () {

	            	if ($rootScope.serviceInstanceId == "" && !$scope.isEdit) {
						showSaveMessage('Processor creation is not allowed, and it is allowed when it traverses from a task', 'error');
						return;
					}
                    $scope.formAddPrcsr.$setPristine();
                    $scope.loadOrigin();
					$scope.resetProcessorType($scope.procsrType);
                    $scope.readAllProfiles();
                    $scope.closeDelete();
                    //To notify passwordDirective to clear the password and error message
                    $scope.doSend();
                    $scope.showSSHKeysSection = false;
                    $scope.showTruststoreSection = false;
                    $scope.isEdit = false;
                    $scope.isProcessorTypeHTTPListener = false;
					//GIT URL
				    $scope.script = '';
			        $scope.scriptIsEdit = false; 	
                    $scope.isJavaScriptExecution = false;
                    $scope.isCreateConfiguredLocation = true;
                    $scope.formAddPrcsr.scriptName.$setValidity('allowed', true);
                    $scope.scriptUrlIsValid=false;
                    $scope.$broadcast("resetCredentialSection");
            };            
            // Close the modal
            $scope.closeDelete = function () {
                $('#myModal').modal('hide')
            };		    
            
            //GMB-201
			$scope. showTruststoreSection = ($scope.processor.protocol && ($scope.processor.protocol.value === "FTPS" || $scope.processor.protocol.value === "HTTPS")) ? true : false;
            $scope.showSSHKeysSection = ($scope.processor.protocol && $scope.processor.protocol.value === "SFTP") ? true : false;
            
			$scope.appendPortToUrl = function() {
			    
				var defaultPort = '';
				var baseUrl = '';
                for (var i = 0; i < $scope.processor.processorPropertiesInTemplateJson.staticProperties.length; i++) {
                    var portProperty = $scope.processor.processorPropertiesInTemplateJson.staticProperties[i];
					if (portProperty.name === "port") {
					   defaultPort = portProperty.value;				   
					   for (var j = 0; j < $scope.processor.processorPropertiesInTemplateJson.staticProperties.length; j++) {
					       var urlProperty = $scope.processor.processorPropertiesInTemplateJson.staticProperties[j];
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
										$scope.processor.processorPropertiesInTemplateJson.staticProperties[j].value = newBaseUrl;
										if ($scope.processor.processorPropertiesInTemplateJson.staticProperties[i].readOnly === false) {						    
										   $scope.processor.processorPropertiesInTemplateJson.staticProperties[i].readOnly = true;
										}
									}
								}
							  }						  
						    }
					     }
						 
					  }
			     }				
			};            
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
                      $scope.block.blockUI();			  
					  $scope.restService.get($scope.base_url + "/git/content/" + $.base64.encode($scope.trimScriptTemplateName()),
					  function (data, status) { 
                         $scope.block.unblockUI();
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

                $scope.showDirectSubmit = false;
			    $scope.selectedProcessorType = proceesorType.value;
			    $scope.initialSetUp();
			    $scope.clearOldLoginDetails();
			    //To notify passwordDirective to clear the password and error message
                $scope.doSend();
                switch ($scope.selectedProcessorType) {
                  case "SWEEPER":
                    $scope.isProcessorTypeSweeper = true;
                    $scope.isProcessorTypeConditionalSweeper = false;
                    $scope.isProcessorTypeHTTPListener = false;
                    $scope.isProcessorTypeFileWriter = false;
                    $scope.isProcessorTypeDropbox = false;
                    $scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options,$scope.selectedProcessorType)];
                    var setSweeperData = '';
                    if ($rootScope.isLegacy) {
                        setSweeperData = 'data/processor/properties/legacysweeper.json';
                    } else {
                        setSweeperData = 'data/processor/properties/sweeper.json';	
                    }
                    $rootScope.restService.get(setSweeperData, function (data) {                    
                        $scope.separateProperties(data.processorDefinition.staticProperties);
                        $scope.separateFolderProperties(data.processorDefinition.folderProperties);	
                        $scope.processorCredProperties = data.processorDefinition.credentialProperties;
                    });
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBoxSweeper.supportedJavaScriptCheckBox;
                    break;
                  case "CONDITIONALSWEEPER":
                    $scope.isProcessorTypeSweeper = false;
                    $scope.isProcessorTypeConditionalSweeper = true;
                    $scope.isProcessorTypeHTTPListener = false;
                    $scope.isProcessorTypeFileWriter = false;
                    $scope.isProcessorTypeDropbox = false;
                    $scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options,$scope.selectedProcessorType)];
                    var setConditionalSweeperData = '';
                    if ($rootScope.isLegacy) {
                        setConditionalSweeperData = 'data/processor/properties/legacyconditionalsweeper.json';
                    } else {
                        setConditionalSweeperData = 'data/processor/properties/conditionalsweeper.json';
                    }
                    $rootScope.restService.get(setConditionalSweeperData, function (data) {                    
                        $scope.separateProperties(data.processorDefinition.staticProperties);
                        $scope.separateFolderProperties(data.processorDefinition.folderProperties); 
                        $scope.processorCredProperties = data.processorDefinition.credentialProperties;
                    });
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBoxSweeper.supportedJavaScriptCheckBox;
                    break;
                  case "LITEHTTPSYNCPROCESSOR":
                    $scope.isProcessorTypeSweeper = false;
                    $scope.isProcessorTypeConditionalSweeper = false;
                    $scope.isProcessorTypeHTTPListener = true;
                    $scope.isProcessorTypeFileWriter = false;
                    $scope.isProcessorTypeDropbox = false;
                    $scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options, $scope.selectedProcessorType)];
                    $rootScope.restService.get('data/processor/properties/liteHttpSync.json', function (data) {
                        $scope.separateProperties(data.processorDefinition.staticProperties);
                        $scope.processorCredProperties = data.processorDefinition.credentialProperties;
                    });
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
                    break;
                  case "HTTPSYNCPROCESSOR":
                    $scope.isProcessorTypeSweeper = false;
                    $scope.isProcessorTypeConditionalSweeper = false;
                    $scope.isProcessorTypeHTTPListener = true;
                    $scope.isProcessorTypeFileWriter = false;
                    $scope.isProcessorTypeDropbox = false;
                    $scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options, $scope.selectedProcessorType)];
                    var setHttpSyncData = '';
                    if ($rootScope.isLegacy) {
                        setHttpSyncData = 'data/processor/properties/legacyhttpSync.json';
                    } else {
                    	setHttpSyncData = 'data/processor/properties/httpSync.json';
                    }
                    $rootScope.restService.get(setHttpSyncData, function (data) {
                        $scope.separateProperties(data.processorDefinition.staticProperties);
                        $scope.processorCredProperties = data.processorDefinition.credentialProperties;				  	      
                    });
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
                    break;
                  case "HTTPASYNCPROCESSOR": 				 
                    $scope.isProcessorTypeSweeper = false;
                    $scope.isProcessorTypeConditionalSweeper = false;
                    $scope.isProcessorTypeHTTPListener = true;
                    $scope.isProcessorTypeFileWriter = false;
                    $scope.isProcessorTypeDropbox = false;
                    $scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options, $scope.selectedProcessorType)];
                    var setHttpAsyncData = '';
                    if ($rootScope.isLegacy) {
                        setHttpAsyncData = 'data/processor/properties/legacyhttpAsync.json';
                    } else {
                        setHttpAsyncData = 'data/processor/properties/httpAsync.json';
                    }
                    $rootScope.restService.get(setHttpAsyncData, function (data) {						
                      $scope.separateProperties(data.processorDefinition.staticProperties);
                      $scope.processorCredProperties = data.processorDefinition.credentialProperties;
                    });
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
                    break;
                  case "FILEWRITER":
                    $scope.isProcessorTypeSweeper = false;
                    $scope.isProcessorTypeConditionalSweeper = false;
                    $scope.isProcessorTypeHTTPListener = false;
                    $scope.isProcessorTypeDropbox = false;
                    $scope.isProcessorTypeFileWriter = true;
                    $scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options, $scope.selectedProcessorType)];
                    $rootScope.restService.get('data/processor/properties/fileWriter.json', function (data) {
                      $scope.separateProperties(data.processorDefinition.staticProperties);				  
                      $scope.separateFolderProperties(data.processorDefinition.folderProperties);
                      $scope.processorCredProperties = data.processorDefinition.credentialProperties;
                    });
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
                    break;
                  case "DROPBOXPROCESSOR":			
                    $scope.isProcessorTypeSweeper = false;
                    $scope.isProcessorTypeConditionalSweeper = false;
                    $scope.isProcessorTypeHTTPListener = false;
                    $scope.isProcessorTypeFileWriter = false;
                    $scope.isProcessorTypeDropbox = true;
                    $scope.processor.protocol = $scope.processorData.supportedProcessors.options[getIndexOfValue($scope.processorData.supportedProcessors.options, $scope.selectedProcessorType)];
                    var setDropBoxProcessData = '';
                    if ($rootScope.isLegacy) {
                        setDropBoxProcessData = 'data/processor/properties/legacydropboxProcessor.json';
                    } else {
                        setDropBoxProcessData = 'data/processor/properties/dropboxProcessor.json';											
                    }
                    $rootScope.restService.get(setDropBoxProcessData, function (data) {
                      $scope.separateProperties(data.processorDefinition.staticProperties);		
                      $scope.processorCredProperties = data.processorDefinition.credentialProperties;
                    });
                    $scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
                    break;
				  default:
					$scope.resetProtocol($scope.processor.protocol);
					$scope.supportedJavaScriptCheckBox = $scope.javaScriptCheckBox.supportedJavaScriptCheckBox;
				    break;
			    }		
                $scope.$broadcast("resetCredentialSection");
				
			};
			$scope.resetProtocol = function(potocolType) {
			
				 $scope.isProcessorTypeSweeper = false;
				 $scope.isProcessorTypeConditionalSweeper = false;
				 $scope.isProcessorTypeHTTPListener = false;
				 $scope.isProcessorTypeFileWriter = false;
				 $scope.isProcessorTypeDropbox = false;
				 var protocalName = potocolType.value;
				 $scope.initialSetUp();
				 $scope.clearOldLoginDetails();
				 //To notify passwordDirective to clear the password and error message
	             $scope.doSend();
				 $scope.processor.protocol = $scope.processorData.supportedProtocols.options[getIndexOfValue($scope.processorData.supportedProtocols.options, protocalName)];
				 if (!$scope.processor.protocol) {
					 $scope.processor.protocol = $scope.processorData.supportedProtocols.options[0];
					 protocalName = $scope.processor.protocol.value;
				 }
              	 switch ($scope.selectedProcessorType) {
				   case "REMOTEDOWNLOADER":
                       $scope.showDirectSubmit = true;
				       switch (protocalName) {
							case "SFTP":
								if ($scope.isDirectSubmit) {
									$rootScope.restService.get('data/processor/properties/sftpdownloaderwithsweeper.json', function (data) {					
										$scope.separateProperties(data.processorDefinition.staticProperties);
										$scope.separateFolderProperties(data.processorDefinition.folderProperties);
										$scope.processorCredProperties = data.processorDefinition.credentialProperties;   
									});
								} else {
									$rootScope.restService.get('data/processor/properties/sftpdownloader.json', function (data) {					
										$scope.separateProperties(data.processorDefinition.staticProperties);
										$scope.separateFolderProperties(data.processorDefinition.folderProperties);
										$scope.processorCredProperties = data.processorDefinition.credentialProperties;   
									});
								}	
								break;
							case "FTP":
								if ($scope.isDirectSubmit) {
									$rootScope.restService.get('data/processor/properties/ftpdownloaderwithsweeper.json', function (data) {						
										$scope.separateProperties(data.processorDefinition.staticProperties);
										$scope.separateFolderProperties(data.processorDefinition.folderProperties);
										$scope.processorCredProperties = data.processorDefinition.credentialProperties;   
									});
								} else {
									$rootScope.restService.get('data/processor/properties/ftpdownloader.json', function (data) {						
										$scope.separateProperties(data.processorDefinition.staticProperties);
										$scope.separateFolderProperties(data.processorDefinition.folderProperties);
										$scope.processorCredProperties = data.processorDefinition.credentialProperties;   
									});
								}								
								break;
							case "FTPS":
								if ($scope.isDirectSubmit) {
									$rootScope.restService.get('data/processor/properties/ftpsdownloaderwithsweeper.json', function (data) {							
										  $scope.separateProperties(data.processorDefinition.staticProperties);
										  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
										  $scope.processorCredProperties = data.processorDefinition.credentialProperties;   
										});
								} else {
									$rootScope.restService.get('data/processor/properties/ftpsdownloader.json', function (data) {							
										  $scope.separateProperties(data.processorDefinition.staticProperties);
										  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
										  $scope.processorCredProperties = data.processorDefinition.credentialProperties;   
									});
								}
								break;
							case "HTTP":
							case "HTTPS":
								if ($scope.isDirectSubmit) {
									$rootScope.restService.get('data/processor/properties/httpdownloaderwithsweeper.json', function (data) {
										  $scope.separateProperties(data.processorDefinition.staticProperties);
										  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
										  $scope.processorCredProperties = data.processorDefinition.credentialProperties;   
										});
								} else {
									$rootScope.restService.get('data/processor/properties/httpdownloader.json', function (data) {
										  $scope.separateProperties(data.processorDefinition.staticProperties);
										  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
										  $scope.processorCredProperties = data.processorDefinition.credentialProperties;   
									});
								}
								 
								break;
							default:	
							   break;
						}
					  break;	
				  case "REMOTEUPLOADER":				
					 switch (protocalName) {						
						case "SFTP":
							$rootScope.restService.get('data/processor/properties/sftpuploader.json', function (data) {				        
							  $scope.separateProperties(data.processorDefinition.staticProperties);
							  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
							  $scope.processorCredProperties = data.processorDefinition.credentialProperties;   
						   });
						   break;
						case "FTP":					
							$rootScope.restService.get('data/processor/properties/ftpuploader.json', function (data) {				        
							  $scope.separateProperties(data.processorDefinition.staticProperties);
							  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
							  $scope.processorCredProperties = data.processorDefinition.credentialProperties;
							});
							break;
						case "FTPS":					
							$rootScope.restService.get('data/processor/properties/ftpsuploader.json', function (data) {					       
							  $scope.separateProperties(data.processorDefinition.staticProperties);
							  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
							  $scope.processorCredProperties = data.processorDefinition.credentialProperties;
							});
							break;
						case "HTTP":
						case "HTTPS":					
							$rootScope.restService.get('data/processor/properties/httpuploader.json', function (data) {
							  $scope.isProcessorTypeHTTPRemoteUploader = true;
							  $scope.separateProperties(data.processorDefinition.staticProperties);
							  $scope.separateFolderProperties(data.processorDefinition.folderProperties);
							  $scope.processorCredProperties = data.processorDefinition.credentialProperties;
							});
							break;
						default:
						   break;
				     }
                    break;
			      default:
				    break;				   
				}
                //GMB-201
                $scope.showTruststoreSection = (protocalName === "FTPS" || protocalName === "HTTPS") ? true : false;
                $scope.showSSHKeysSection = (protocalName === "SFTP") ? true : false; 
                $scope.$broadcast("resetCredentialSection");				
			};
			
            $scope.separateProperties = function(jsonProperties) {
                for (var i = 0; i < jsonProperties.length; i++) {
                    var property = jsonProperties[i];
                    if (property.hasOwnProperty('mandatory') && (property.mandatory === true)) {
                        $scope.propertiesAddedToProcessor.push(property);
                    } else if (property.hasOwnProperty('mandatory') && (property.mandatory === false)) {
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
            $scope.separateFolderProperties = function(jsonProperties) {
			  
			  for (var i = 0; i < jsonProperties.length; i++) {
                    var property = jsonProperties[i];
                   if (property.hasOwnProperty('mandatory') && (property.mandatory === true)) {
                        $scope.folderAddedToProcessor.push(property);
                    } else if (property.hasOwnProperty('mandatory') && (property.mandatory === false)){
                        $scope.folderAvailableProperties.push(property);
                    }
                }
				// push empty object to enable additional
				if (jsonProperties.length > 1) {
				   $scope.folderAddedToProcessor.push({
				   
						  "folderURI": "",
						  "folderDisplayType": "",
						  "folderType": "",
						  "folderDesc": "",
						  "mandatory": false,
						  "readOnly":false,
						  "valueProvided":false,
						  "validationRules":{}
					  
					 });      
				}
		   };
		   
            $scope.propertiesAddedToProcessor = [];
			$scope.availableProperties = []; 
			$scope.propertiesEditToProcessor = [];
            $scope.initialSetUp = function() {
                $scope.folderAvailableProperties = []; 
			    $scope.folderAddedToProcessor = [];			
                $scope.propertiesAddedToProcessor = [];
				$scope.propertiesEditToProcessor = [];
                $scope.availableProperties = [];         
                $scope.selectedProperty = {value:''};
                $scope.showAddNewComponent = {value:false};
				$scope.selectedFolderProperty = {value:''};                
            }
            $scope.cleanup = function() {        	   
               $scope.selectedProperty.value = '';
               $scope.showAddNewComponent.value = false;
			   $scope.selectedFolderProperty = '';                             
            }		   
            $scope.resetProcessorType($scope.procsrType);            
            
			$scope.processorFoldersSortInfo = {
				fields: ['folderURI'],
				directions: ['asc']
			};
			
			$scope.sortProcessorFolders = function () {
				 var reverse = ($scope.processorFoldersSortInfo.directions[0] === 'asc') ? false : true; 
				 var fieldToBeSorted = ($scope.processorFoldersSortInfo.fields[0] === 'folderType') ? 'folderDisplayType' : $scope.processorFoldersSortInfo.fields[0];
				 $scope.folderAddedToProcessor = $filter('orderBy')($scope.folderAddedToProcessor, fieldToBeSorted, reverse);
			};
			// Sort listener for Scripts grid
			$scope.$watch('processorFoldersSortInfo.directions + processorFoldersSortInfo.fields', function (newVal, oldVal) {
				if (newVal !== oldVal) {
					$scope.sortProcessorFolders();
				}

			}, true);
			
			//New folder section
			$scope.folderAvailableProperties = []; 
			$scope.folderAddedToProcessor = [];
			$scope.gridOptionsForProcessorFolder = {
                data: 'folderAddedToProcessor',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 80,
				enableColumnResize : true,
				sortInfo : $scope.processorFoldersSortInfo,
				useExternalSorting : true,
				plugins: [new ngGridFlexibleHeightPlugin()],
                columnDefs: [{
                    field: "folderURI",
                    width: "33%",
                    displayName: "URI*",
                    enableCellEdit: false,
                    cellTemplate: '<dynamic-folder-uri-value-field-directive current-row-object = folderAddedToProcessor[row.rowIndex]/>'                   
                }, {
                    field: "folderType",
                    width: "20%",
                    displayName: "Type*",
                    enableCellEdit: false,
                    cellTemplate: '<dynamic-folder-type-field-directive sort-name="sorting"  all-props=folderAvailableProperties selected-value=selectedFolderProperty current-row-object= folderAddedToProcessor[row.rowIndex] initial-state-object={{row.entity}}/>'
                }, {
                    field: "folderDesc",
                    width: "40%",
                    displayName: "Description",
                    enableCellEdit: false,
                    cellTemplate: '<dynamic-folder-desc-value-field-directive current-row-object = folderAddedToProcessor[row.rowIndex]/>'                   
                }, {
                    field: "allowAdd",
                    width: "7%",
                    displayName: "Action",
                    enableCellEdit: false,
                    sortable: false,
                    cellTemplate: '<dynamic-folder-action-field-directive folder-available-properties = folderAvailableProperties folder-added-properties = folderAddedToProcessor current-row-object = folderAddedToProcessor[row.rowIndex] initial-state-object={{row.entity}}/>',
                }]
            };
			
			$scope.processorPropertiesSortInfo = {
				fields: ['value'],
				directions: ['asc']
			};
			$scope.sortProcessorProperties = function () {
				 var reverse = ($scope.processorPropertiesSortInfo.directions[0] === 'asc') ? false : true; 
				 var fieldToBeSorted = ($scope.processorPropertiesSortInfo.fields[0] === 'name') ? 'displayName' : $scope.processorPropertiesSortInfo.fields[0];
				 $scope.propertiesAddedToProcessor = $filter('orderBy')($scope.propertiesAddedToProcessor, fieldToBeSorted, reverse);
			};
			// Sort listener for Scripts grid
			$scope.$watch('processorPropertiesSortInfo.directions + processorPropertiesSortInfo.fields', function (newVal, oldVal) {
				if (newVal !== oldVal) {
					$scope.sortProcessorProperties();
				}

			}, true);
            $scope.gridOptionsForProcessor = {
                data: 'propertiesAddedToProcessor',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 80,
				enableColumnResize : true,
				useExternalSorting : true,
				sortInfo : $scope.processorPropertiesSortInfo,
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
					 sortable: false,
                     cellTemplate: '<dynamic-action-field-directive available-properties = availableProperties added-properties = propertiesAddedToProcessor current-row-object = propertiesAddedToProcessor[row.rowIndex] initial-state-object={{row.entity}}/>',
                 }
                ]
            }; 
            // listen to property modified notification and do clean up
            $rootScope.$on("propertyModificationActionEvent", function() {
               $scope.cleanup(); 
            });
            
            $scope.displayHTTPListenerURL = function() {
            	
				$scope.urlSuffixForId = "mailboxId=" + $scope.mailboxId;
				$scope.urlSuffixForName = "mailboxName=" + encodeURIComponent($scope.mailboxName);
				$scope.processorUrlDisplayContentById = ($scope.urlPrefix != null && $scope.urlPrefix != '')?
						$scope.urlPrefix + $scope.urlSuffixForId: $scope.urlSuffixForId;
				$scope.processorUrlDisplayContentByName = ($scope.urlPrefix != null && $scope.urlPrefix != '')?
						$scope.urlPrefix + $scope.urlSuffixForName : $scope.urlSuffixForName;
            }
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
     	 $scope.restService.get($scope.base_url + "/git/content/" + $.base64.encode($scope.javaProperties.defaultScriptTemplateName),
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
		
		 $scope.block.blockUI();
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
        	$scope.block.unblockUI(); 
        		$scope.$parent.scriptIsEdit = true;
        		$scope.$parent.script = $scope.scriptContents;
			    showSaveMessage(data.scriptserviceResponse.response.message, 'success');
         })
         .error(function(data) { 
        	$scope.block.unblockUI();
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
			
	    	$scope.block.blockUI();
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
	            	$scope.block.unblockUI(); 
	            		$scope.$parent.scriptIsEdit = true;
	            		$scope.$parent.script = $scope.scriptContents;
					    showSaveMessage(data.scriptserviceResponse.response.message, 'success');					
	            })
	            .error(function(data) {
	            	$scope.block.unblockUI();
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

