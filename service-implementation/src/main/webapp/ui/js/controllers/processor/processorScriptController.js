var rest = myApp.controller('ProcessorScriptCntrlr', ['$rootScope', '$modal', '$scope', '$timeout', '$filter', '$location', '$log', '$blockUI',
    function($rootScope, $modal, $scope, $timeout, $filter,
        $location, $log, $blockUI) {

        //Paging set up
        $scope.totalServerItems = 0;

        $scope.pagingOptions = {
            pageSizes: [25, 100, 1000],
            pageSize: 25,
            currentPage: 1
        };
        // script loads initially
        $scope.script = [];

        $scope.sortInfo = {
            fields: ['name'],
            directions: ['asc']
        };
		$scope.filterOptions = {
            filterText: [],
            useExternalFilter: true
        };

        $scope.readAllScript = function() {

            //var sortDirection = "";	
            var totalItems = "";

            if ($scope.sortInfo.directions) {
                $scope.sortDirection = String($scope.sortInfo.directions);
            }

            $rootScope.gridLoaded = false;
            var minRespond = true;
            $scope.restService.get($scope.base_url + '/script',
                function(data) {
                    $scope.getPagedDataAsync(data,
                        $scope.pagingOptions.pageSize,
                        $scope.pagingOptions.currentPage);
                    $rootScope.gridLoaded = true;
                    $scope.showprogressbar = false;
                }, {
                    page: $scope.pagingOptions.currentPage,
                    pageSize: $scope.pagingOptions.pageSize,
                    sortDirection: $scope.sortDirection
                }
            );
        };
        $scope.readAllScript();

        $scope.clearAllFilters = function() {
            $scope.scriptName = null;
        }

        // Get script names for Typeahead display		
        $scope.getScriptNames = function(choice) {


            var restUrl = $scope.base_url + '/script';
            var type = "script";
            var scriptName = choice;
            //check lists scriptName.
            if ((typeof scriptName !== 'undefined' && scriptName !== null && (scriptName.length == 0 || scriptName.length >= 1)) || scriptName === null) {
                if (scriptName.length == 0) {
                    restUrl += '?page=' + $scope.pagingOptions.currentPage + '&pageSize=' + $scope.pagingOptions.pageSize + '&sortDirection=' + $scope.sortDirection;
                } else {
                    restUrl += '?page=' + $scope.pagingOptions.currentPage + '&pageSize=' + $scope.pagingOptions.pageSize + '&sortDirection=' + $scope.sortDirection + '&scriptName=' + scriptName;
                }

                return $scope.restService.get(restUrl, function(data) {}).then(function(res) {
                    var data = res.data.getScriptListResponse;
                    $scope.script = data.scripts;
                    //setTimeout(function () {
                    $scope.totalServerItems = data.length;
                    $scope.setPagingData(data.scripts, $scope.pagingOptions.currentPage, $scope.pagingOptions.pageSize);
                    return $scope.script;
                    //}, 100);
                });
            }
        }

       
        $scope.getPagedDataAsync = function(largeLoad, pageSize, page) {
            setTimeout(function() {
                $scope.totalServerItems = largeLoad.getScriptListResponse.totalItems;
                $scope.setPagingData(largeLoad.getScriptListResponse.scripts, page, pageSize);
            }, 100);
        };

        // Set the paging data to grid from server object
        $scope.setPagingData = function(data, page, pageSize) {
            if (data === null || data.length <= 0) {
                showSaveMessage("No results found", 'warning');
            }
            var pagedData = data;
            $scope.script = pagedData;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        };

        $scope.$watch('pagingOptions.currentPage', function(newVal, oldVal) {
            if (newVal !== oldVal && $scope.validatePageNumberValue(newVal, oldVal)) {
                $scope.readAllScript();
            }
        }, true);

        $scope.$watch('pagingOptions.pageSize', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                //Get data when in first page
                if ($scope.pagingOptions.currentPage === 1) {
                    $scope.readAllScript();
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
            if (newVal === '' || !/^\d+$/.test(newVal) || newVal * 1 == 0) {
                valid = false;
            }
            // Value cannot be bigger than calculated max page count
            else if ($scope.totalServerItems !== undefined && $scope.totalServerItems !== 0 && newVal * 1 > Math.ceil($scope.totalServerItems / $scope.pagingOptions.pageSize)) {
                valid = false;
            }

            if (!valid) {
                $scope.pagingOptions.currentPage = oldVal;
                showSaveMessage("Invalid input value. Page " + $scope.pagingOptions.currentPage + " is shown.", 'error');
            }
            return valid;
        }

        $scope.edit = function(row) {
            $scope.isScriptSelected = true;
            $scope.$parent.editor = ace.edit("aceEditor");
            $scope.scriptContent = '';
            if (row.entity.name) {
                $scope.scriptTemplateName = row.entity.name;
                $scope.restService.get($scope.base_url + "/git/content/" + $.base64.encode(row.entity.name),
                    function(data, status) {
                        if (status === 200 || status === 400) {
                            if (data.scriptserviceResponse.response.status === 'success') {
                                scriptContent = data.scriptserviceResponse.script;
                                $scope.$parent.editor.getSession().setValue(scriptContent);
								$scope.restService.get($scope.base_url + "/commithistory"+ "?url=gitlab:/" + row.entity.name)
                                           .success(function(data){
                	                $scope.gitlabLastModifiedBy = data.GitlabCommitResponse.gitlabCommit.authorName;
                                    $scope.gitlabLastModified = data.GitlabCommitResponse.gitlabCommit.createdAt;
                                     })
                                          .error(function(data){
                                    $scope.gitlabLastModifiedBy = "";
                                    $scope.gitlabLastModified = "";
                                    });
                            } else {
                                $scope.scriptContent = '';
                                if (!$scope.isScriptSelected) {
                                    $scope.cc = false;
                                }
                            }
                        } else {
                            showSaveMessage("Error while retrieve File from GitLab", 'error');
                        }
                    }
                );
            }
        };

        $scope.save = function() {
            if ($scope.isScriptSelected) {
                $scope.checkScript();
            } else {
                $scope.saveScript();
            }
        }

        $scope.checkScript = function() {
            $scope.$parent.editor = ace.edit("aceEditor");
            if ($scope.scriptContent !== $scope.$parent.editor.getSession().getValue()) {
                $scope.editScript();
            } else {
                showSaveMessage('No changes made to save.');
            }
        }

        $scope.editScript = function() {

            $scope.editFileRequest = {
                scriptserviceRequest: {
                    script: {
                        data: "",
                        scriptFileUri: "",
                        createdBy: ""
                    }
                }
            };


            $scope.scriptContents = $scope.$parent.editor.getSession().getValue();

            $scope.editFileRequest.scriptserviceRequest.script.data = $scope.scriptContents;
            $scope.editFileRequest.scriptserviceRequest.script.scriptFileUri = $scope.scriptTemplateName;
            $scope.editFileRequest.scriptserviceRequest.script.createdBy = $scope.createdBy;

            $scope.restService.put($scope.base_url + "/git/content", $filter("json")($scope.editFileRequest), function() {})
                .success(function(data) {
                    $scope.isScriptSelected = true;
                    showSaveMessage(data.scriptserviceResponse.response.message, 'success');
                })
                .error(function(data) {
                    if (angular.isObject(data)) {
                        showSaveMessage(data.scriptserviceResponse.response.message, 'error');
                    } else {
                        showSaveMessage("Error while File update to GitLab", 'error');
                    }
                });
        };
        // Customized column in the grid.
       // $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="edit(row)" tooltip = "Edit" tooltip-placement="left" tooltip-append-to-body="true">\n\
        //<i class="glyphicon glyphicon glyphicon-wrench glyphicon-white"></i></button>';
        var filterWatch;
        var filterBarPlugin = {
            init: function(scope, grid) {
                filterBarPlugin.scope = scope;
                filterBarPlugin.grid = grid;
                filterWatch = scope.$watch('columns[0].filterText', function(newVal, oldVal) {
					var seachScript = "";
                    angular.forEach(filterBarPlugin.scope.columns, function(col) {
                        if (col.visible && col.filterText) {
							seachScript = col.filterText;
							}
                    });
					if (newVal !== undefined) {
					$scope.getScriptNames(seachScript);
					}
                    
                }, true);
            },
            scope: undefined,
            grid: undefined
        };
        $scope.cellToolTip = {
            overflow: 'visible'
        };

        $scope.gridOptionsForProcessorScript = {
            columnDefs: [{
                field: 'name',
                displayName: 'Script Name',
                width: "100%",
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }/*, {  //Customized column
                displayName: 'Action',
                width: '10%',
                sortable: false,
                cellTemplate: $scope.editableInPopup
            }*/],

            afterSelectionChange: function(rowItem) {
                if (rowItem.selected == true) {
					$scope.edit(rowItem);
                    $scope.showItems = true;
                    setTimeout(function() {
                        $scope.Processorscript = rowItem.entity.name;
                        $scope.classification = rowItem.entity.classification;
                        var itemCtlrSCope = angular.element('#processorScriptDetController').scope();
                        itemCtlrSCope.itemName = [];
                        itemCtlrSCope.getProcessorDetails(rowItem.entity.name);
                    }, 200);
                }
            },
            data: 'script',
            enablePaging: true,
            showFooter: true,
            canSelectRows: true,
            headerRowHeight: 70,
            multiSelect: false,
            jqueryUITheme: false,
            displaySelectionCheckbox: false,
            sortInfo: $scope.sortInfo,
            useExternalSorting: true,
            pagingOptions: $scope.pagingOptions,
            enableColumnResize: true,
            plugins: [filterBarPlugin, new ngGridFlexibleHeightPlugin()],
            totalServerItems: 'totalServerItems'
        };

        // Sort listener for search account grid
        $scope.$watch('sortInfo.directions + sortInfo.fields', function(newVal, oldVal) {
            var sortingField = String($scope.sortInfo.fields);
            if (newVal !== oldVal) {
                if (sortingField !== 'type') {
                    $scope.readAllScript();
                }
            }
        }, true);
    }
]);