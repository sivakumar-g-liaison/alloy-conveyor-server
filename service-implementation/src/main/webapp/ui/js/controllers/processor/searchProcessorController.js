var rest = myApp.controller(
    'SearchProcessorCntrlr', ['$rootScope', '$modal', '$scope', '$timeout',
        '$filter', '$location', '$log', '$blockUI',
        function ($rootScope, $modal, $scope, $timeout, $filter,
            $location, $log, $blockUI) {		
            
            //Paging set up
            $scope.totalServerItems = 0;
			
            $scope.pagingOptions = {
                pageSizes: [25, 100, 1000],
                pageSize: 25,
                currentPage: 1
            };
			
			$scope.sortInfo = {
				fields: ['name'],
				directions: ['asc']
			};

		$scope.mailBoxName = null;
		$scope.procName = null;
		$scope.prcsrGuid = null;
		$scope.mailboxId = null;
		$scope.scriptName =null;

		// Profiles loads initially
		$scope.profiles = [];
        
        $scope.isProcessorTypeSweeper = false;
		$scope.isProcessorTypeHTTPListener = false;
		$scope.isProcessorTypeDropbox = false;
		$scope.isProcessorTypeFileWriter = false;
     
        // Loading the profile details
        $scope.loadProfiles = function () {
            $scope.restService.get($scope.base_url + "/profile", function (data, status) {
                if (status == 200) {
                    $scope.profiles = data.getProfileResponse.profiles;
                } else {
                    showSaveMessage("failed to load Profiles", 'error');
                }

            })
        };
        $scope.loadProfiles(); // initial load for the profiles
			
            $scope.readAllProcessors = function () {
			
			var sortField = "";
        	var sortDirection = "";
        	var prcsrTypeVal = "";
			var prcsrProtocol = "";

            if($scope.sortInfo.fields && $scope.sortInfo.directions) {
            	sortField = String($scope.sortInfo.fields);
            	sortDirection = String($scope.sortInfo.directions);
            }
            if($scope.processorType) {
            	prcsrTypeVal = String($scope.processorType.value);
            }
			if($scope.protocolName) {
				prcsrProtocol = String($scope.protocolName.value);
			}

            $rootScope.gridLoaded = false;
            var minRespond = true;
            $scope.restService.get($scope.base_url + '/searchprocessor',
                function (data) {
                    $scope.getPagedDataAsync(data,
                        $scope.pagingOptions.pageSize,
                        $scope.pagingOptions.currentPage);
                    $rootScope.gridLoaded = true;
                    $scope.showprogressbar = false;
                }, {
                    page: $scope.pagingOptions.currentPage,
                    pagesize: $scope.pagingOptions.pageSize,
                    sortField: sortField,
                    sortDirection: sortDirection,
                    mbxName: $scope.mailBoxName,
                    pipelineId: $scope.PrcsrPipelineId,
                    folderPath: $scope.folderPath,
                    profileName: $scope.profileName,
                    protocol: prcsrProtocol,
                    prcsrType: prcsrTypeVal,
                    prcsrName: $scope.procName,
                    prcsrGuid: $scope.prcsrGuid,
                    mbxGuid: $scope.mailboxId,
                    scriptName: $scope.scriptName,
                    idpURI: $scope.idpURI,
                    minResponse: minRespond,
                    clusterType: $scope.clusterType
                }
            );
        };
        $scope.readAllProcessors();

        $scope.clearAllFilters = function() {
		$scope.mailBoxName = null;
		$scope.PrcsrPipelineId = null;
		$scope.folderPath = null;
		$scope.profileName = null;
		$scope.protocolName = null;
		$scope.processorType = null;
		$scope.procName = null;
		$scope.prcsrGuid = null;
		$scope.mailboxId = null;
		$scope.scriptName = null;
        $scope.idpURI = null;
	}

	// Get Mailbox names for Typeahead display		
	$scope.getMailboxNames = function(choice) {
        var restUrl = $scope.base_url + '/typeAhead/getEntityByNames';
        var type = "mailbox";
		var mailBoxName = choice;
		//check lists mailboxNames.
        if ((typeof mailBoxName !== 'undefined' && mailBoxName !== null && mailBoxName.length >= $rootScope.typeaheadMinLength)  || mailBoxName === null || mailBoxName === "" || (typeof mailBoxName !== 'undefined' && mailBoxName !== null && mailBoxName.length === 0)) {
            restUrl += '?name=' + mailBoxName + '&type=' + type;
        return $scope.restService.get(restUrl, function(data) {}).then(function(res){            
            var data = res.data.searchProcessorResponse;
            return data.mailbox;
        });   
		}
    }
	
	// Get Processor names for Typeahead display		
	$scope.getProcessorNames = function(choice) {
        var restUrl = $scope.base_url + '/typeAhead/getEntityByNames';
        var type = "processor";
		var procName = choice;
		//check lists processorNames.
        if ((typeof procName !== 'undefined' && procName !== null && procName.length >= $rootScope.typeaheadMinLength)  || procName === null || procName === "" || (typeof procName !== 'undefined' && procName !== null && procName.length === 0)) {
            restUrl += '?name=' + procName + '&type=' + type;
        return $scope.restService.get(restUrl, function(data) {}).then(function(res){            
            var data = res.data.searchProcessorResponse;
            return data.processor;
        });   
		}
    }
	
	// Get Profile names for Typeahead display		
	$scope.getProfileNames = function(choice) {
        var restUrl = $scope.base_url + '/typeAhead/getEntityByNames';
        var type = "profile";
        var profileName = choice;
		//check lists profile names.
		if ((typeof profileName !== 'undefined' && profileName !== null && profileName.length >= $rootScope.typeaheadMinLength)  || profileName === null || profileName === "" || (typeof profileName !== 'undefined' && profileName !== null && profileName.length === 0)) {
            restUrl += '?name=' + profileName + '&type=' + type;
        return $scope.restService.get(restUrl, function(data) {}).then(function(res){            
            var data = res.data.searchProcessorResponse;
            return data.profiles;
        });        
		}
    }
			
			// Enable the delete modal dialog
        $scope.openDelete = function (row) {
            $scope.key = row.entity;
            // $scope.deleteKey = true;
        };

        // Enable the delete modal dialog
        $scope.openMessage = function () {
            $scope.customKey = true;
        };

        // Close the modal
        $scope.closeMessage = function () {
            $scope.customKey = false;
        };

        // calls the rest deactivate service
        $scope.deactivateProcessor = function () {

            $scope.block.blockUI();
            $scope.restService.delete($scope.base_url + '/' + $scope.key.linkedMailboxId + '/processor/' + $scope.key.guid,
                function (data, status) {
                    if (status === 200) {

                        $scope.block.unblockUI();
                        $scope.closeDelete();
                        showSaveMessage(data.deActivateProcessorResponse.response.message, 'success');
                        $scope.readAllProcessors();
                    } else {

                        $scope.block.unblockUI();
                        $scope.closeDelete();
                        if (data.deActivateProcessorResponse) {
                            showSaveMessage(data.deActivateProcessorResponse.response.message, 'error');
                        } else {
                            showSaveMessage("Failed to delete processor", 'error');
                        }
                    }
            });
        };

        // Close the modal
        $scope.closeDelete = function () {
        	$('#myModal').modal('hide')
        };
			// Navigate to mailbox screen for edit operation
			$scope.edit = function (row) {
				var processorSearchFlag = true;
				$location.$$search = {};
				$location.path('/mailbox/processor').search('mailBoxId', row.entity.linkedMailboxId).search('mbxname', row.entity.mailboxName).search('isProcessorSearch', processorSearchFlag).search('processorId', row.entity.guid);
			};
            $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
                setTimeout(function () {
					$scope.totalServerItems = largeLoad.getProcessorResponse.totalItems;
                    $scope.setPagingData(largeLoad.getProcessorResponse.processors, page, pageSize);
                }, 100);
            };
            // Set the paging data to grid from server object
            $scope.setPagingData = function (data, page, pageSize) {
                if (data === null || data.length <= 0) {
                     showSaveMessage("No results found", 'warning');
                }				
                var pagedData = data;
                $scope.processorList = pagedData;                				
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
			// Customized column in the grid.
        $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="edit(row)" tooltip = "Edit" tooltip-placement="left" tooltip-append-to-body="true">\n\
        <i class="glyphicon glyphicon glyphicon-wrench glyphicon-white"></i></button>\n\
        <button class="btn btn-default btn-xs" ng-click="openDelete(row)" data-toggle="modal" data-target="#myModal" tooltip = "Delete" tooltip-placement="right" tooltip-append-to-body="true">\n\
        <i class="glyphicon glyphicon-trash glyphicon-white"></i></button>';
		
		$scope.cellToolTip = {
			overflow: 'visible'
		};

            $scope.gridOptionsForGetProcessor = {
                columnDefs: [{
                    field: 'mailboxName',
                    displayName: 'Mailbox Name',
                    width: "18%"
                },{
                    field: 'name',
                    displayName: 'Processor Name',
                    width: "18%"
                }, {
                    field: 'type',
                    displayName: 'Type',
                    width: "16%",
                    cellTemplate: 'partials/processor/processor_section_templates/processor_type_field_template.html'                    
                }, {
                    field: 'protocol',
                    displayName: 'Protocol',
                    width: "8%",
                    cellTemplate: 'partials/processor/processor_section_templates/processor_protocol_field_template.html'                    
                }, {
                    field: 'status',
                    displayName: 'Processor Status',
                    width: "10%"                    
                }, {
                    field: 'mailboxStatus',
                    displayName: 'Mailbox Status',
                    width: "10%"                    
                }, {
                    field: 'processorDC',
                    displayName: 'Processor DC',
                    width: "10%"                    
                }, {
                    displayName: 'Action',
                    sortable: false,
                    width: "8%",
					cellTemplate: $scope.editableInPopup,
					cellClass: $scope.cellToolTip
                }],
				data: 'processorList',
                enablePaging: true,
                showFooter: true,
                canSelectRows: true,
                multiSelect: false,
                jqueryUITheme: false,
                displaySelectionCheckbox: false,
				sortInfo: $scope.sortInfo,
				useExternalSorting: true,
                pagingOptions: $scope.pagingOptions,                
				enableColumnResize : true,
				plugins: [new ngGridFlexibleHeightPlugin()],
				totalServerItems: 'totalServerItems'
            };
			
		// Sort listener for search account grid
		$scope.$watch('sortInfo.directions + sortInfo.fields', function (newVal, oldVal) {
			var sortingField = String($scope.sortInfo.fields);
			if (newVal !== oldVal) {
				if(sortingField !== 'type') {
					$scope.readAllProcessors();
				}
			}

		}, true);
		
	$scope.resetProcessorType = function(proceesorType) {
			    $scope.selectedProcessorType = proceesorType.value;			    
                switch ($scope.selectedProcessorType) {
                  case "SWEEPER":
					$scope.isProcessorTypeSweeper = true;
					$scope.isProcessorTypeHTTPListener = false;
					$scope.isProcessorTypeFileWriter = false;
					$scope.isProcessorTypeDropbox = false;
					$scope.protocolName = null;					
					break;
                  case "HTTPSYNCPROCESSOR": 
				  case "HTTPASYNCPROCESSOR": 				 
					$scope.isProcessorTypeSweeper = false;
					$scope.isProcessorTypeHTTPListener = true;
					$scope.isProcessorTypeFileWriter = false;
					$scope.isProcessorTypeDropbox = false;
					$scope.protocolName = null;					
					break;
				  case "FILEWRITER": 				
					$scope.isProcessorTypeSweeper = false;
					$scope.isProcessorTypeHTTPListener = false;
					$scope.isProcessorTypeDropbox = false;
					$scope.isProcessorTypeFileWriter = true;
					$scope.protocolName = null;
                    break;
                  case "DROPBOXPROCESSOR":			
					$scope.isProcessorTypeSweeper = false;
					$scope.isProcessorTypeHTTPListener = false;
					$scope.isProcessorTypeFileWriter = false;
					$scope.isProcessorTypeDropbox = true;
					$scope.protocolName = null;
                    break;
				  case "REMOTEDOWNLOADER":
				  case "REMOTEUPLOADER":				  
					$scope.isProcessorTypeSweeper = false;
					if($scope.selectedProcessorType === 'HTTP' || $scope.selectedProcessorType === 'HTTPS') {
						$scope.isProcessorTypeHTTPRemoteUploader = true;
					}
					$scope.isProcessorTypeDropbox = false;
					$scope.isProcessorTypeFileWriter = false;
                    break;                  
			    }
		}	
	}
]);