myApp.controller('executingprocessorsCntrlr', ['$rootScope', '$scope', '$location',  '$filter',
    function ($rootScope, $scope, $location, $filter) {
	
    $scope.totalServerItems = 0;
    $scope.processorStatusUrl = '/processoradmin/processor/status';
    
    $scope.runningProcessorIds = {
    		updateProcessorsExecutionStateRequest: {
    			guids: []
            }
    };
    
    $scope.editor ;
    $scope.loadValueData = function (_editor) {	  
        $scope.editor = _editor;
    } 
    
    //Paging set up
    $scope.pagingOptions = {
        pageSizes: [25, 100, 1000],
        pageSize: 25,
        currentPage: 1
    };
    
    $scope.filterOptions = {
        filterText: [],
        useExternalFilter: true
    };

    $scope.sortInfo = {
        fields: ['modifiedDate'],
        directions: ['asc']
    };

    $scope.getExecutingProcessors = function () {
        $scope.sortInformation = {
            fields: $scope.sortInfo.fields,
            directions: $scope.sortInfo.directions
        };
        $scope.restService.get($scope.base_url + $scope.processorStatusUrl,
            function (data, status) {
                if (status === 200 || status === 400) {
                    if (data.processorExecutionStateResponse.response.status == 'success') {
                        $scope.getPagedDataAsync(data);
                    } else {
                        showSaveMessage(data.processorExecutionStateResponse.response.message, 'warning');
                    }
                } else {
                    showSaveMessage(data.processorExecutionStateResponse.response.message, 'error');
                }
            }, {page:$scope.pagingOptions.currentPage, pagesize:$scope.pagingOptions.pageSize, filterText:$scope.filterOptions, sortInfo:$scope.sortInformation}
        );
    }

	$scope.getPagedDataAsync = function (largeLoad) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad.processorExecutionStateResponse);
            }, 100);
    };
	
	// Set the paging data to grid from server object
    $scope.setPagingData = function (data) {

        $scope.runningProcessorIds.updateProcessorsExecutionStateRequest.guids = [];
        for (i=0; i<data.processors.length; i++) {
            $scope.runningProcessorIds.updateProcessorsExecutionStateRequest.guids.push(data.processors[i].processorId);
        }
        var ProcessorIdsCopy = angular.copy($scope.runningProcessorIds);
        if (angular.isObject(ProcessorIdsCopy)) {
            $scope.updateProcessorsStatusRequestJson = $filter('json')(ProcessorIdsCopy);
        }
        $scope.editor.getSession().setValue($scope.updateProcessorsStatusRequestJson);
        $scope.processors = data.processors;
        $scope.totalServerItems = data.totalItems;
        if ( $scope.processors.length === 0) {
            showSaveMessage("No Results Found", 'warning');
        }
        if (!$scope.$$phase) {
            $scope.$apply();
        }
        $rootScope.gridLoaded = true;

    };
    
    $scope.updateStatusForProcessor = function (entity) {
		
    	$rootScope.gridLoaded = false;
        $scope.restService.put($scope.base_url + $scope.processorStatusUrl + "?processorId=" + entity.processorId, "", 
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
    
    var filterWatch;
    var filterBarPlugin = {
        init: function(scope, grid) {
            filterBarPlugin.scope = scope;
            filterBarPlugin.grid = grid;
            filterWatch = scope.$watch('columns[0].filterText + columns[4].filterText', function(newVal, oldVal) {
                var colsEmpty = true;
                var searchQuery = [];
                angular.forEach(filterBarPlugin.scope.columns, function(col) {
                    if (col.visible && col.filterText) {
                        var filterObj = {};
                        filterObj.field = col.field;
                        filterObj.text = col.filterText;
                        colsEmpty = false;
                        if (col.field == 'createdDate') {
                            var date = col.filterText;
                            if (date.indexOf(",") != -1) {
                                var dates = date.split('-');
                                var startDate = new Date(dates[0]);
                                var endDate = new Date(dates[1]);
                                var dateFrom = startDate.getMonth() + 1 + '/' + startDate.getDate() + '/' + startDate.getFullYear();
                                var dateTo = endDate.getMonth() + 1 + '/' + endDate.getDate() + '/' + endDate.getFullYear();
                                filterObj.text = dateFrom + ' - ' + dateTo + ' - ' + $scope.dateFormat;
                            } else {
                                filterObj.text = date + ' - ' + $scope.dateFormat;
                            }
                        }
                        searchQuery.push(filterObj);
                    }
                });
                $scope.filterOptions.filterText = searchQuery;
                $scope.getExecutingProcessors();
            }, true);
        },
        scope: undefined,
        grid: undefined
    };
    
    // Setting the grid details	
    $scope.gridOptions = {
    		columnDefs: [{
                field: 'processorId',
                displayName: 'Processor Id',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            },
			{
                field: 'executionStatus',
                displayName: 'Status'
            },
            {
            	field: 'threadName',
            	displayName: 'Thread Name'
            },
/*            {
            	field: 'modifiedBy',
            	displayName: 'Modified BY'
            },*/
            {
            	field: 'modifiedDate',
            	displayName: 'Triggered Date',
            	cellFilter: "date:'dd-MMM-yy HH:mm:ss'" 
            },
           /* {
            	field: 'lastExecutionState',
            	displayName: 'Last Execution State'
            },
            {
            	field: 'lastExecutionDate',
            	displayName: 'Last Execution Date',
            	cellFilter: "date:'dd-MMM-yy HH:mm:ss'" 
            },*/
            {
                field: 'nodeInUse',
                displayName: 'Node In Use',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            },
            { 
                displayName: 'Action',
                width: '10%',
                sortable: false,
                cellTemplate: '<div style="margin-left: 30px"><button class="btn btn-default btn-xs" ng-click="confirmDialog(row.entity)" tooltip = "Stop Processor" tooltip-placement="left" tooltip-append-to-body="true">\n\<i class="glyphicon glyphicon-stop glyphicon-white"></i></button>&nbsp;&nbsp;<button class="btn btn-default btn-xs" title="" ng-click="showExecutingProcessorDialog(row.entity)" tooltip = "Show Details" tooltip-placement="right" tooltip-append-to-body="true"><i class="glyphicon glyphicon-info-sign glyphicon-white"></i></button></div>'
           }
        ],
        data: 'processors',
        // rowTemplate: customRowTemplate,
        enablePaging: true,
        headerRowHeight: 70,
        showFooter: true,
        canSelectRows: true,
        multiSelect: false,
        jqueryUITheme: false,
        displaySelectionCheckbox: false,
		useExternalSorting: true,
        pagingOptions: $scope.pagingOptions,
        filterOptions: $scope.filterOptions,
        plugins: [filterBarPlugin, new ngGridFlexibleHeightPlugin()],
        sortInfo: $scope.sortInfo,
        useExternalSorting: true,
		enableColumnResize : true,
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
    
    $scope.$watch('sortInfo.directions + sortInfo.fields', function(newVal, oldVal) {
        if (newVal !== oldVal) {
            $scope.getExecutingProcessors();
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

    $scope.hideConfirmDialog = function (entity) {
        $('#executingProcessorsConfirmationModal').modal('hide');
        $scope.updateStatusForProcessor(entity);
    }

    $scope.executionEntity = '';
    $scope.confirmDialog = function (entity) {
        $scope.executionEntity = entity;
        $('#executingProcessorsConfirmationModal').modal('show');
    }
    
    $scope.showExecutingProcessorDialog = function(entity) {
        $scope.getProcessorDetails(entity);
        $('#executingProcessorInfoModal').modal('show');
    }

    $scope.cancelExecutingProcessorInfo = function(entity) {
        $('#executingProcessorInfoModal').modal('hide');
    }

    $scope.updateRunningProcessorStatus = function(isUpdate) {
        $scope.isUpdateOnly = isUpdate;
    }

    $scope.updateStuckProcessorsConfirmation = function() {

        $rootScope.gridLoaded = false;
        $scope.restService.put($scope.base_url + $scope.processorStatusUrl + "?updateOnly=" + $scope.isUpdateOnly, $scope.updateProcessorsStatusRequestJson, 
            function (data, status) {                        
                if (status === 200) {
                    if (data.updateProcessorsExecutionStateResponse.response.status === 'success') {
                        $scope.getExecutingProcessors();
                        showSaveMessage(data.updateProcessorsExecutionStateResponse.response.message, 'success');
                    }
                } else {
                    showSaveMessage(data.updateProcessorsExecutionStateResponse.response.message, 'error');
                }
                $rootScope.gridLoaded = true;
            }
        );
    }

    $scope.getProcessorDetails = function(entity) {
    	var emptyMailBoxId = " ";
        $scope.restService.get($scope.base_url + "/" + emptyMailBoxId +"/processor/" + entity.processorId,
            function(data, status) {
                if (status === 200 || status === 400) {
                    if (data.getProcessorResponse.response.status == 'success') {
                        $scope.currentEntity = data.getProcessorResponse.processor;
                    } else {
                        showSaveMessage(data.getProcessorResponse.response.message, 'warning');
                    }
                } else {
                    showSaveMessage(data.getProcessorResponse.response.message, 'error');
                }
            });
    }
}
]);