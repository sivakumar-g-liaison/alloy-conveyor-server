var rest = myApp.controller('ProcessorScriptCntrlr', ['$rootScope', '$modal', '$scope', '$timeout', '$filter', '$location', '$log', '$blockUI',
    function($rootScope, $modal, $scope, $timeout, $filter,
        $location, $log, $blockUI) {
	
        var url = $scope.base_url + "/script";

        // Search Script
        $scope.scriptName = null;
		

        // Grid Setups
        $scope.filterOptions = {
            filterText: [],
            useExternalFilter: true
        };
		var filterTimeout;
		
		$scope.sortInfo = {
			fields: ['name'],
			directions: ['asc']
		};
		
        //Paging set up
        $scope.totalServerItems = 0;

        $scope.pagingOptions = {
            pageSizes: [25, 100, 1000],
            pageSize: 25,
            currentPage: 1
        };

        /**
		 * Get data for paging
		 * 
		 * @param url
		 * @param pageSize
		 * @param page
		 * @param filterText
		 * @param sortInfo
		 */
        $scope.getPagedDataAsync = function (url, pageSize, page, filterText, sortInfo) {
			$scope.restService.get(url, 
				function (data) {
					if(data) {
						$scope.scripts = data.getScriptListResponse.scripts;
						$scope.totalServerItems = data.getScriptListResponse.totalItems;
					} else {
						$scope.scripts = [];
						$scope.totalServerItems = 0;
					}
					if ($scope.scripts.length === 0)
					{
					 showSaveMessage("No Results Found ", 'warning');
					}
					$scope.gridLoaded = true;
				
					if (!$scope.$$phase){
					 $scope.$apply();
					 }
				}, {pageSize: pageSize, page: page, scriptName: filterText, sortDirection: sortInfo}
			);
        };

        // Loading the Scripts details
        $scope.loadScripts = function () {
			setTimeout(function () {
				$scope.getPagedDataAsync(url, $scope.pagingOptions.pageSize,
						$scope.pagingOptions.currentPage, $scope.scriptName, $scope.sortInfo.directions);
			}, 100);
        };
        
        $scope.loadScripts(); //loads the Scripts initially
        
     // Sort listener for Scripts grid
		$scope.$watch('sortInfo.directions + sortInfo.fields', function (newVal, oldVal) {
			if (newVal !== oldVal) {
				 $scope.loadScripts();
			}

		}, true);
		
        $scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
            if (newVal !== oldVal  && $scope.validatePageNumberValue(newVal, oldVal)) {
            	$scope.loadScripts();
            }
        }, true);
        
		$scope.$watch('pagingOptions.pageSize', function (newVal, oldVal) {
            if (newVal !== oldVal) {
               //Get data when in first page
               if ( $scope.pagingOptions.currentPage === 1) {
                    $scope.loadScripts();
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

            if (!valid)
            {
                $scope.pagingOptions.currentPage = oldVal;
                showSaveMessage("Invalid input value. Page "+$scope.pagingOptions.currentPage+" is shown.", 'error');
            }
            return valid;
        }
     // Filter plugin
        var filterBarPlugin = {
            init: function (scope, grid) {
                filterBarPlugin.scope = scope;
                filterBarPlugin.grid = grid;
                scope.$watch('columns[0].filterText', function(newVal, oldVal) {
                    var seachScript = "";
                    var colsEmpty= true;
                    angular.forEach(filterBarPlugin.scope.columns, function(col) {
                        if (col.visible && col.filterText) {
                            seachScript = col.filterText;
                        }
                    });

					$scope.scriptName = seachScript;
                    if (colsEmpty == false) {
                        $timeout.cancel(filterTimeout);
                        filterTimeout = $timeout(function() {
                            $scope.applyFilter(newVal, oldVal);
                        }, 1000);
                    }
                    // No timeout needed when the filter texts are cleared
                    else {
                        $scope.applyFilter(newVal, oldVal);
                    }
                },true);
            },
            scope: undefined,
            grid: undefined
        };
		
        // Apply filtering
        $scope.applyFilter = function (newVal, oldVal) {
    		  $scope.pagingOptions.currentPage = 1;
    		  $scope.loadScripts();
    	};
        // Setting the grid details
         $scope.gridOptionsForProcessorScript = {
            columnDefs: [{
                field: 'name',
                displayName: 'Script Name',
                width: "100%",
                headerCellTemplate: 'partials/filterInputTypeHeaderTemplate.html'
            }],

            afterSelectionChange: function(rowItem) {
                if (rowItem.selected == true) {
					$scope.edit(rowItem);
                    $scope.showProcessorsList = true;
                    setTimeout(function() {
                        $scope.Processorscript = rowItem.entity.name;
                        $scope.classification = rowItem.entity.classification;
                        var itemCtlrScope = angular.element('#processorScriptDetController').scope();
                        itemCtlrScope.getProcessorDetails(rowItem.entity.name);
                    }, 200);
                }
            },
            data: 'scripts',
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
		
		    $scope.edit = function(row) {
            $scope.isScriptSelected = true;
            $scope.$parent.editor = ace.edit("aceEditor");
            $scope.scriptContent = '';
            if (row.entity.name) {
                $scope.scriptTemplateName = row.entity.name;
                $scope.restService.get($scope.base_url + "/git/content/" + $.base64.encode(row.entity.name),
                    function(data) {
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
		
    }
]);