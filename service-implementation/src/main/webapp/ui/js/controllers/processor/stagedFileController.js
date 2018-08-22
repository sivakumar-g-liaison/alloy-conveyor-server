/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
var rest = myApp.controller('StagedFilesCntrlr', ['$rootScope', '$scope', '$filter', '$location', '$log', '$modal', '$blockUI', '$http', 'daterangepickerappConfig',
    function($rootScope, $scope, $filter, $location, $log, $modal, $blockUI, $http, daterangepickerappConfig) {

        var url = $scope.base_url + "/stagedFiles";

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
            "INACTIVE",
            "STAGED",
            "FAILED",
            "DELETED"
        ];

        $scope.clusterTypes = [
            "SECURE",
            "LOWSECURE"
        ];

        $scope.reviseRequest = {
            reviseStagedFileRequest: {
                guids: []
            }
        };

        $scope.stagedFileName = [];

        if ($rootScope.languageFormatData.dateRangePattern != undefined) {
            daterangepickerappConfig.dateFormat = $rootScope.languageFormatData.dateRangePattern;
            $scope.dateFormat = daterangepickerappConfig.dateFormat;
        } else {
            $scope.restService.get('../language/userLocale')
                .success(function(data) {
                    daterangepickerappConfig.dateFormat = data.dateRangePattern;
                    $scope.dateFormat = daterangepickerappConfig.dateFormat;
                });
        }

        /* Sort listener. */
        $scope.$watch('sortInfo.directions + sortInfo.fields', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.loadStagedFiles();
            }
        }, true);

        //Paging listener for grid page size
        $scope.$watch('pagingOptions.pageSize', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                //Get data when in first page
                if ($scope.pagingOptions.currentPage === 1) {
                    $scope.loadStagedFiles();
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
                $scope.loadStagedFiles();
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
                filterWatch = scope.$watch('columns[0].filterText + columns[1].filterText + columns[2].filterText + columns[3].filterText + columns[4].filterText + columns[5].filterText + + columns[6].filterText', function(newVal, oldVal) {
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
                    $scope.loadStagedFiles();
                }, true);
            },
            scope: undefined,
            grid: undefined
        };

        $scope.gridOptions = {
            columnDefs: [{
                field: 'globalProcessId',
                displayName: 'Global Process Id',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }, {
                field: 'name',
                displayName: 'File Name',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'

            }, {
                field: 'mailboxGuid',
                displayName: 'Mailbox Id',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }, {
                field: 'processorId',
                displayName: 'Processor Id',
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }, {
                field: 'createdDate',
                displayName: 'Created On',
                headerCellTemplate: 'partials/filterHeaderDateRangeTemplate.html',
                cellTemplate: '<div class="ngCellText" id="createdOnId-{{row.rowIndex}}"><date-formatter value="{{row.entity.createdDate}}"></date-formatter></div>'
            }, {
                field: 'status',
                displayName: 'Status',
                width: '10%',
                headerCellTemplate:'partials/filterStatusComboBoxHeaderTemplate.html'
            }, {
                field: 'clusterType',
                displayName: 'Cluster Type',
                width: '10%',
                headerCellTemplate:'partials/filterClusterTypeComboBoxHeaderTemplate.html'
            }, {
                field: 'processDc',
                displayName: 'Process DC',
                width: '10%'
            }, {
                displayName: 'Action',
                width: '12%',
                sortable: false,
                cellTemplate: 'partials/staged-file-action-template.html'
            }],
            data: 'stagedFiles',
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
        $scope.loadStagedFiles = function() {
            setTimeout(function() {
                $scope.gridLoaded = false;
                $scope.getPagedDataAsync(url, $scope.pagingOptions.pageSize,
                    $scope.pagingOptions.currentPage, $scope.filterOptions, $scope.sortInfo);
            }, 100);
        };

        $scope.setStagedFileData = function() {
            setTimeout(function() {
                if ($scope.reviseRequest.reviseStagedFileRequest.guids.length > 0) {
                    for (var i = 0; i < $scope.files.length; i++) {
                        if ($scope.reviseRequest.reviseStagedFileRequest.guids.
                                indexOf($scope.files[i].id) != -1) {
                            $scope.files[i].value = true;
                        } else {
                            $scope.files[i].value = false;
                        }
                    } 
                } else {
                    for (var i = 0; i < $scope.files.length; i++) {
                        $scope.files[i].value = false;
                    }
                }

           	 $scope.stagedFiles = $scope.files;
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
           }, 100);
        }

        $scope.getPagedDataAsync = function(url, pageSize, page, filterText, sortInfo) {

            $scope.restService.get(url,
                function(data) {
                    if (data) {
                        $scope.files = data.getStagedFilesResponse.stagedFiles;
                        $scope.totalServerItems = data.getStagedFilesResponse.totalItems;
                        $scope.setStagedFileData();
                        showSaveMessage("Staged files retrieved successfully ", 'success');
                    } else {
                        $scope.stagedFiles = [];
                        $scope.totalServerItems = 0;
                    }

                    if ($scope.files.length === 0) {
                        showSaveMessage("No Results Found ", 'warning');
                    }
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                    $scope.gridLoaded = true;
                }, {pageSize: pageSize, page: page, filterText: filterText, sortInfo: sortInfo}
            );
        };

        // Loading the staged files
        $scope.loadStagedFiles();

        $scope.openInfoModal = function(entity) {
            $scope.selectedFile = angular.copy(entity);
            $('#fileInfoModal').modal('show');
        }

        $scope.cancelInfo = function() {
            $scope.form.fileInfoModalForm.$setPristine();
            $('#fileInfoModal').modal('hide');
            $scope.selectedFile = '';
        }

        $scope.openUpdateModal = function(entity) {
            $scope.selectedFileToUpdate = angular.copy(entity);
            if ($scope.selectedFileToUpdate.status != 'STAGED') {
                showSaveMessage("Staged file must be in STAGED status to update the file", 'warning');
            } else {
                $('#fileUpdateModal').modal('show');
            }
        }

        $scope.cancelUpdate = function() {
            $scope.form.fileUpdateModalForm.$setPristine();
            $scope.updateModelHide();
            $scope.selectedFileToUpdate = '';
        }

        $scope.deactivateStagedFile = function(pguid) {
            $scope.restService.put(url + '/' + pguid, '',
                function(data, status) {
                    if (status == 200) {
                        showSaveMessage("Staged file updated successfully", 'success');
                        $scope.loadStagedFiles();
                        $scope.updateModelHide();
                    } else {
                        showSaveMessage("Failed to update the Staged file ", 'error');
                        $scope.updateModelHide();
                    }
                }
            );
        }

        $scope.updateModelHide = function() {
            $('#fileUpdateModal').modal('hide');
        }

        $scope.selectStagedFile = function(entity) {

            var filesToUpdate = angular.copy(entity);
            var filePosition = $scope.reviseRequest.reviseStagedFileRequest.guids.indexOf(filesToUpdate.id);
            var fileNamePos = $scope.stagedFileName.indexOf(filesToUpdate.name);

            if (true == filesToUpdate.value) {
                if (filePosition == -1) {
                    $scope.reviseRequest.reviseStagedFileRequest.guids.push(filesToUpdate.id)
                    $scope.stagedFileName.push(filesToUpdate.name)
                }
            } else {
                if (filePosition != -1) {
                    $scope.reviseRequest.reviseStagedFileRequest.guids.splice(filePosition, 1);
                }

                if (fileNamePos != -1) {
                    $scope.stagedFileName.splice(fileNamePos, 1);
                }
            }
        };

        $scope.reviseStagedFileModal = function() {
            if ($scope.reviseRequest.reviseStagedFileRequest.guids.length == 0) {
                showSaveMessage('No files are selected to update', 'warning');
            } else {
                $('#updateSelectedFilesModal').modal('show');
            }
        };

        $scope.cancelSelectedFiles = function() {
            $('#updateSelectedFilesModal').modal('hide');
        }

        $scope.updateSelectedFiles = function() {

            $scope.restService.put("../mailbox/stagedFiles/revise", $filter('json')($scope.reviseRequest), function() {})
                .success(function(data, status) {
                    if (status == 200) {
                        showSaveMessage('Staged files updated successfully', 'success');
                        $scope.reviseRequest.reviseStagedFileRequest.guids = [];
                        $scope.stagedFileName = [];
                        $scope.loadStagedFiles();
                        $('#updateSelectedFilesModal').modal('hide');
                    } else {
                        showSaveMessage('error', data);
                        $('#updateSelectedFilesModal').modal('hide');
                    }
                })
                .error(function(data, status) {
                    showSaveMessage('error', data);
                })
        }
    }
]);