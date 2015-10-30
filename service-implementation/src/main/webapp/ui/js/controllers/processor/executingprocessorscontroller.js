myApp.controller('executingprocessorsCntrlr', ['$rootScope', '$scope', '$location',  '$filter',
    function ($rootScope, $scope, $location, $filter) {
	
    $scope.totalServerItems = 0;
	
    //Paging set up
    $scope.pagingOptions = {
        pageSizes: [25, 100, 1000],
        pageSize: 25,
        currentPage: 1
    };
    
	$scope.getExecutingProcessors = function () {
	
	$scope.restService.get($scope.base_url +'/processoradmin/processor/status',
                function (data, status) {
            	    if (status === 200 || status === 400) {
                        if (data.processorExecutionStateResponse.response.status == 'success') {
                        	 $scope.getPagedDataAsync(data);
                        } else {
                        	showSaveMessage(data.processorExecutionStateResponse.response.message, 'error');
                          }
                    } else {
                    	 showSaveMessage(data.processorExecutionStateResponse.response.message, 'error');
                      }
                   
                    $rootScope.gridLoaded = true;
				}, {page:$scope.pagingOptions.currentPage, pagesize:$scope.pagingOptions.pageSize}
            );
	}
    
	$scope.getPagedDataAsync = function (largeLoad) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad.processorExecutionStateResponse);
            }, 100);
    };
	
	// Set the paging data to grid from server object
     $scope.setPagingData = function (data) {

            $scope.processors = data.processors;
            $scope.totalServerItems = data.totalItems;
            if ( $scope.processors.length === 0) {
            	showSaveMessage("No Results Found", 'error');
			}
            if (!$scope.$$phase) {
                $scope.$apply();
            }

    };
    
    $scope.updateStatusForProcessor = function (entity) {
		
    	$rootScope.gridLoaded = false;
		$scope.restService.put($scope.base_url + '/processoradmin/processor/status' + "?processorId=" + entity.processorId, "", 
                    function (data, status) {                        
                        if (status === 200 || status === 400) {
                             if (data.updateProcessorExecutionStateResponse.response.status === 'success') {
                            	$scope.getExecutingProcessors();
                                showSaveMessage(data.updateProcessorExecutionStateResponse.response.message, 'success');
                            }
                        } else {
                                showSaveMessage(data.updateProcessorExecutionStateResponse.response.message, 'error');
                          }
                        $rootScope.gridLoaded = true;
                    } 
                   
        );
	}
    
    
    // Setting the grid details	
    $scope.gridOptions = {
    		columnDefs: [{
                field: 'processorId',
                width: '45%',
                displayName: 'Processor Id'
            },
			{
                field: 'executionStatus',
                width: '45%',
                displayName: 'Status'
            },
            { 
                displayName: 'Action',
                width: '10%',
                sortable: false,
                cellTemplate: '<div style="margin-left: 30px"><button class="btn btn-default btn-xs" title="Stop Processor" ng-click="updateStatusForProcessor(row.entity)">\n\
					   		   <i class="glyphicon glyphicon-stop glyphicon-white"></i></button></div>'
           }
        ],
        data: 'processors',
        // rowTemplate: customRowTemplate,
        enablePaging: true,
        showFooter: true,
        canSelectRows: true,
        multiSelect: false,
        jqueryUITheme: false,
        displaySelectionCheckbox: false,
		useExternalSorting: true,
        pagingOptions: $scope.pagingOptions,
		enableColumnResize : true,
		plugins: [new ngGridFlexibleHeightPlugin()],
        totalServerItems: 'totalServerItems',
    };
	
	$scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
            if (newVal !== oldVal  && $scope.validatePageNumberValue(newVal, oldVal)) {
            	$scope.getExecutingProcessors();
            }
        }, true);

    $scope.$watch('pagingOptions.pageSize', function (newVal, oldVal) {
            if (newVal !== oldVal) {
               //Get data when in first page
               if ( $scope.pagingOptions.currentPage === 1) {
                    $scope.getExecutingProcessors();
               } else {
                    //If on other page than 1 go back
                    $scope.pagingOptions.currentPage = 1;
               }               
            }
    }, true);
	
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

            if(!valid) {
            	
                $scope.pagingOptions.currentPage = oldVal;
                showSaveMessage("Invalid input value. Page "+$scope.pagingOptions.currentPage+" is shown.", 'error');
            }
            return valid;
    }
	
	$scope.getExecutingProcessors();
	
}
]);