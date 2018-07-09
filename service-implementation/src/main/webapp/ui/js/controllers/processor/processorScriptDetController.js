/**
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
'use strict'

var processorScriptDetController = myApp.controller('processorScriptDetController', ['$scope', 'RESTService', '$rootScope', '$filter', '$location', '$timeout',
    function($scope, RESTService, $rootScope, $filter, $location, $timeout) {

        $scope.pagingOptions = {
            pageSizes: [25, 100, 1000],
            pageSize: 25,
            currentPage: 1
        };
        $scope.processors = [];
        $scope.sortInfo = {
            fields: ['createdOn'],
            directions: ['asc']
        };
        $scope.filterOptions = {
            filterText: [],
            useExternalFilter: true
        };
        var filteredData = "";
        var searchQuery = [];
        var filterObj = {};
		
        $scope.getProcessorDetails = function(name) {

            $scope.showItems = true;
            $scope.scriptName = name;
            var sortingParams = {
                fields: $scope.sortInfo.fields,
                directions: $scope.sortInfo.directions
            }
            var url = $scope.base_url + "/script/linkedProcessor?scriptName=" + name;

			

            RESTService.get(url,
                    function(data) {}, {
                        pageSize: $scope.pagingOptions.pageSize,
                        page: $scope.pagingOptions.currentPage,
						sortDirection: $scope.sortInfo.directions,
						sortField: $scope.sortInfo.fields,
						filterText:$scope.filterOptions
                    })
                .success(function(response) {

                    if (response.getScriptProcessorListResponse.totalItems == 0) { 
					    $scope.processors = response.getScriptProcessorListResponse.processors;
                        $scope.totalServerItems = response.getScriptProcessorListResponse.totalItems;
                        showSaveMessage("No processor details available.",'warning');
						$scope.getprocessorDetailsPagedDataAsync();
                       
                    } else {
                        $scope.processors = response.getScriptProcessorListResponse.processors;
                        $scope.totalServerItems = response.getScriptProcessorListResponse.totalItems;
                        $scope.getprocessorDetailsPagedDataAsync();
                    }
                })
                .error(function(response) {
                	showSaveMessage('Unable to find the processor details', 'error');
                    $scope.clearData(); //clear processor details on error
                });
        };

        $scope.getprocessorDetailsPagedDataAsync = function() {
            setTimeout(function() {
                $scope.processorDetailsInGrid = $scope.processors;
                $scope.processors.scriptName = $scope.scriptName;
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            }, 100);
        };

        /* Sort listener. */
        $scope.$watch('sortInfo.directions + sortInfo.fields', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.getProcessorDetails($scope.scriptName);
            }
        }, true);

        //Paging listener for grid page size
        $scope.$watch('pagingOptions.pageSize', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                //Get data when in first page
                if ($scope.pagingOptions.currentPage === 1) {
                    $scope.getProcessorDetails($scope.scriptName);
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
                $scope.getProcessorDetails($scope.scriptName);
            }
        }, true);

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
                showSaveMessage('Invalid input value. Page ' + oldVal + ' is shown.', 'error');
            }
            return valid;
        }

        $scope.clearData = function() {
            $scope.processData = [];
            $scope.processorDetailsInGrid = [];
            $scope.totalServerItems = 0;
        };
        $scope.edit = function(row) {
            var processorSearchFlag = true;
            $location.$$search = {};
            $location.path('/mailbox/processor').search('mailBoxId', row.entity.mailboxId).search('isProcessorSearch', processorSearchFlag).search('processorId', row.entity.guid);
        };
        var filterWatch;
        var filterBarPlugin = {
            init: function(scope, grid) {
                filterBarPlugin.scope = scope;
                filterBarPlugin.grid = grid;
                filterWatch = scope.$watch('columns[0].filterText + columns[1].filterText', function(newVal, oldVal) {
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
					if (newVal !== undefined) {
					$scope.getProcessorDetails($scope.scriptName);
					}
					
                }, true);
            },
            scope: undefined,
            grid: undefined
        };
        // Customized column in the grid.
        $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="edit(row)" tooltip = "Edit Processor" tooltip-placement="left" tooltip-append-to-body="true">\n\
        <i class="glyphicon glyphicon glyphicon-wrench glyphicon-white"></i></button>';

        $scope.gridOptionsscriptItems = {
            canSelectRows: true,
            multiSelect: false,
            headerRowHeight: 70,
            enableRowSelection: true,
            selectedItems: $scope.selectedItems, // selected Row will be assign in this element
            data: 'processors',
            enablePaging: true,
            showFooter: true,
            enableColumnResize: true,
            totalServerItems: 'totalServerItems',
            pagingOptions: $scope.pagingOptions,
            sortInfo: $scope.sortInfo,
            showGroupPanel: false,
            jqueryUIDraggable: false,
            plugins: [filterBarPlugin, new ngGridFlexibleHeightPlugin()],

            columnDefs: [{
                    field: 'guid',
                    displayName: 'Processor Guid',
                    headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
                },
                {
                    field: 'name',
                    displayName: 'Processor Name',
					cellTemplate: '<div class="ngCellText" id="ProcessorName-{{row.rowIndex}}" tooltip="{{row.entity.name}}" tooltip-append-to-body="true" tooltip-placement="top">{{row.entity.name}}</div>',
                    headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
                },
                {
                    field: 'scriptExecutionEnabled',
                    displayName: 'HandOver Execution To JavaScript',
                    sortable: false
                },
                {
                    displayName: 'Action',
                    width: '10%',
                    sortable: false,
                    cellTemplate: $scope.editableInPopup
                }
            ]
        }
    }
]);
