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
			
			// Profiles loads initially
        $scope.profiles = [];
     
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
            if($scope.sortInfo.fields && $scope.sortInfo.directions) {
            	sortField = String($scope.sortInfo.fields);
            	sortDirection = String($scope.sortInfo.directions);
            }
			
			$rootScope.gridLoaded = false;
                $scope.restService.get($scope.base_url + '/processorsearch',
                    function (data) {
                        $scope.getPagedDataAsync(data,
                            $scope.pagingOptions.pageSize,
                            $scope.pagingOptions.currentPage);
						$rootScope.gridLoaded = true;
						 $scope.showprogressbar = false;
                    },{page:$scope.pagingOptions.currentPage, pagesize:$scope.pagingOptions.pageSize, sortField:sortField, sortDirection:sortDirection}				
                );				
            };
			$scope.readAllProcessors();
			
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

            $scope.restService.delete($scope.base_url + '/' + $scope.key.linkedMailboxId + '/processor/' + $scope.key.guid , function (data, status) {								  
                $scope.readAllProcessors();
            });
            $scope.closeDelete();
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
                    $scope.message = 'No results found.';
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
        $scope.editableInPopup = '<div ng-switch on="row.getProperty(\'status\')">\n\
        <div ng-switch-when="INACTIVE" style="cursor: default;"><button class="btn btn-default btn-xs" ng-click="edit(row)">\n\
        <i class="glyphicon glyphicon glyphicon-wrench glyphicon-white"></i></button>\n\
        <button class="btn btn-default btn-xs" ng-disabled="true">\n\
        <i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>\n\
        <div ng-switch-default><button class="btn btn-default btn-xs" ng-click="edit(row)">\n\
        <i class="glyphicon glyphicon glyphicon-wrench glyphicon-white"></i></button>\n\
        <button class="btn btn-default btn-xs" ng-click="openDelete(row)" data-toggle="modal" data-target="#myModal">\n\
        <i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div></div>';

            $scope.gridOptionsForGetProcessor = {
                columnDefs: [{
                    field: 'mailboxName',
                    displayName: 'Mailbox Name',
                    width: "25%"
                },{
                    field: 'name',
                    displayName: 'Processor Name',
                    width: "25%"
                }, {
                    field: 'type',
                    displayName: 'Type',
                    width: "15%"                    
                }, {
                    field: 'protocol',
                    displayName: 'Protocol',
                    width: "15%"                    
                }, {
                    field: 'status',
                    displayName: 'Status',
                    width: "10%"                    
                }, {
                    displayName: 'Action',
                    sortable: false,
                    width: "10%",
					cellTemplate: $scope.editableInPopup
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
			if (newVal !== oldVal) {
				$scope.readAllProcessors();
			}

		}, true);
	}
]);