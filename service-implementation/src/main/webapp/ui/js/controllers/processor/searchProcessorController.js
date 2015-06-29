var rest = myApp.controller(
    'SearchProcessorCntrlr', ['$rootScope', '$modal', '$scope', '$timeout',
        '$filter', '$location', '$log', '$blockUI',
        function ($rootScope, $modal, $scope, $timeout, $filter,
            $location, $log, $blockUI) {		
            
            //Paging set up
            $scope.totalServerItems = 0;
			$scope.mailBoxId = "";
			$scope.mailBoxName = "";
            $scope.pagingOptions = {
                pageSizes: [5, 10, 50],
                pageSize: 5,
                currentPage: 1
            };
			
			$scope.sortInfo = {
				fields: ['name'],
				directions: ['asc']
			};
			
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
                    }				
                );				
            };
			$scope.readAllProcessors();
			// Navigate to mailbox screen for edit operation
			$scope.edit = function (row) {
				$location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name);
			};
            $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
                setTimeout(function () {
                    $scope.setPagingData(largeLoad.getProcessorResponse.processors, page, pageSize);
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
				$scope.mailBoxId = data.linkedMailboxId;
				$scope.mailBoxName = data.mailboxName;
				
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
                    field: 'name',
                    displayName: 'Name',
                    width: "40%"
                }, {
                    field: 'description',
                    displayName: 'Description',
                    width: "20%"                    
                }, {
                    field: 'status',
                    displayName: 'Status',
                    width: "20%"                    
                }, {
                    displayName: 'Action',
                    sortable: false,
                    width: "20%",
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