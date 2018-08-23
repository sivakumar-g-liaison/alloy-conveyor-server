/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
var rest = myApp.controller('InboundFilesCntrlr', ['$rootScope', '$scope', '$filter', '$location', '$log', '$modal', '$blockUI', '$http',
    function($rootScope, $scope, $filter, $location, $log, $modal, $blockUI, $http) {

        var url = $scope.base_url + "/inboundFiles";

        $scope.files = [];

        $scope.filterOptions = {
            filterText: [],
            useExternalFilter: true
        };

        $scope.sortInfo = {
            fields: ['fileName'],
            directions: ['asc']
        };

        $scope.pagingOptions = {
            pageSizes: [25, 100, 1000],
            pageSize: 25,
            currentPage: 1
        };

        $scope.fileStatus = [
            "ACTIVE",
            "INACTIVE"
        ];

                
        $scope.inboundFileName = [];
        
		
        /* Sort listener. */
        $scope.$watch('sortInfo.directions + sortInfo.fields', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.loadInboundFiles();
            }
        }, true);

        //Paging listener for grid page size
        $scope.$watch('pagingOptions.pageSize', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                //Get data when in first page
                if ($scope.pagingOptions.currentPage === 1) {
                    $scope.loadInboundFiles();
                }
                //If on other page than 1 go back
                else {
                    $scope.pagingOptions.currentPage = 1;
                }
            }
        }, true);

        //Paging listener for grid currentPage handling
        $scope.$watch('pagingOptions.currentPage', function(newVal, oldVal) {
            if ($scope.validatePageNumberValue(newVal, oldVal) && newVal !== oldVal) {
                $scope.loadInboundFiles();
            }
        }, true);

        // Helper method to check the entered page number is valid
        $scope.validatePageNumberValue = function(newVal, oldVal) {
            // Value cannot be empty, non number or zero
            var valid = true;
            if (newVal === '' || !/^\d+$/.test(newVal) || newVal * 1 == 0) {
                valid = false;
            }
            // Value cannot be bigger than calculated max page count
            else if ($scope.totalServerItems !== undefined && $scope.totalServerItems !== 0 && newVal * 1 > Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize)) {
                valid = false;
            }

            if (!valid) {
                $scope.pagingOptions.currentPage = oldVal;
                showSaveMessage('error', 'Invalid input value. Page ' + oldVal + ' is shown.');
            }
            return valid;
        }

        var filterWatch;
        var filterBarPlugin = {
            init: function(scope, grid) {
                filterBarPlugin.scope = scope;
                filterBarPlugin.grid = grid;
                filterWatch = scope.$watch('columns[0].filterText + columns[1].filterText + columns[2].filterText + columns[3].filterText + columns[4].filterText', function(newVal, oldVal) {
                    var colsEmpty = true;
                    var searchQuery = [];
                    angular.forEach(filterBarPlugin.scope.columns, function(col) {
                        if (col.visible && col.filterText) {
                            var filterObj = {};
                            filterObj.field = col.field;
                            filterObj.text = col.filterText;
                            colsEmpty = false;
                            searchQuery.push(filterObj);
                        }
                    });
                    $scope.filterOptions.filterText = searchQuery;
                    if (colsEmpty == false || ( newVal != oldVal)) {
                        $scope.loadInboundFiles();
                      }
                }, true);
            },
            scope: undefined,
            grid: undefined
        };

        $scope.gridOptions = {
            columnDefs: [{
                field: 'parentGlobalProcessId',
                displayName: 'Parent Global Process Id',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }, {
                field: 'fileName',
                displayName: 'File Name',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'

            }, {
                field: 'filePath',
                displayName: 'File Path',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }, {
                field: 'processorId',
                displayName: 'Processor Id',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }, {
                field: 'inboundFileStatus',
                displayName: 'Status',
                width: '10%',
                headerCellTemplate:'partials/filterStatusComboBoxHeaderTemplate.html'
            }, {
                field: 'processDc',
                displayName: 'Process DC',
                width: '10%'
            }, {
                displayName: 'Action',
                width: '12%',
                sortable: false,
                cellTemplate: 'partials/inbound-file-action-template.html'
            }],
            data: 'inboundFiles',
            //rowTemplate: customRowTemplate,
            enablePaging: true,
            headerRowHeight: 70, // give room for filter bar
            enableCellEditOnFocus: false,
            showFooter: true,
            canSelectRows: true,
            multiSelect: false,
            jqueryUITheme: false,
            enableColumnResize: true,
            displaySelectionCheckbox: false,
            pagingOptions: $scope.pagingOptions,
            filterOptions: $scope.filterOptions,
            plugins: [filterBarPlugin, new ngGridFlexibleHeightPlugin()],
            sortInfo: $scope.sortInfo,
            useExternalSorting: true,
            totalServerItems: 'totalServerItems',
        };

		$scope.gridOptions.columnDefs[3].visible = isShowProcessorIdColumn();
		function isShowProcessorIdColumn() {
			if ($scope.javaProperties.deployAsDropbox) {
				return false;
			} else {
				return true;
			}
		}
		
        // Loading the profile details
        $scope.loadInboundFiles = function() {
            setTimeout(function() {
                $scope.getPagedDataAsync(url, $scope.pagingOptions.pageSize,
                    $scope.pagingOptions.currentPage, $scope.filterOptions, $scope.sortInfo);
            }, 100);
        };

       

        $scope.getPagedDataAsync = function(url, pageSize, page, filterText, sortInfo) {

            $scope.restService.get(url,
                function(data) {
                    if (data.getInboundFilesResponse.inboundFiles.length != 0) {
                        $scope.inboundFiles = data.getInboundFilesResponse.inboundFiles;
                        $scope.totalServerItems = data.getInboundFilesResponse.totalItems;
                        
                        showSaveMessage("Inbound files retrieved successfully ", 'success');
                    } else {
                        $scope.inboundFiles = [];
                        $scope.totalServerItems = 0;
                        showSaveMessage("No Results Found ", 'warning');
                    }
                   
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                    $scope.gridLoaded = true;
                }, {pageSize: pageSize, page: page, filterText: filterText, sortInfo: sortInfo}
            );
        };

        // Loading the Inbound files
        $scope.loadInboundFiles();

        $scope.openInfoModal = function(entity) {
            $scope.selectedFile = angular.copy(entity);
            $('#fileInfoModal').modal('show');
        }

        $scope.cancelInfo = function() {
            $scope.form.fileInfoModalForm.$setPristine();
            $('#fileInfoModal').modal('hide');
            $scope.selectedFile = '';
        }
    
          
             
    }
]);