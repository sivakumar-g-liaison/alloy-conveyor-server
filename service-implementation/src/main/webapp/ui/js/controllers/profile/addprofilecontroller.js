var rest = myApp.controller('ProfileCntrlr', ['$rootScope','$scope', '$filter', '$location', '$log', '$timeout',
    function ($rootScope,$scope, $filter, $location, $log, $timeout) {
	
        var url = $scope.base_url + "/profile";

        addRequest = $scope.addRequest = {
            addProfileRequest: {
                profile: {}
            }
        };
	
        $scope.profile = {
            id: "",
            name: ""
        };
        
		$scope.reviseProfile = {
            id: "",
            name: ""
        };
		
        reviseRequest = $scope.reviseRequest = {
            reviseProfileRequest: {
                profile: {}
            }
        };
        
		//to get model form object
		$scope.form = {};
        // Search Profiles based on profile Name
        $scope.profileName = null;
               
        // Modify the value to change the search criteria
        $scope.searchMinCharacterCount = 5;

        // invokes add profile service
        $scope.insert = function () {

            $scope.addRequest.addProfileRequest.profile = $scope.profile;

            $scope.restService.post(url, $filter('json')(addRequest),
                function (data, status) {

            	  if (status === 200 || status === 400) {
                    	var messageType = (data.addProfileResponse.response.status == 'success')?'success':'error';
                        showSaveMessage(data.addProfileResponse.response.message, messageType);
                  
                        $scope.profile.name = "";
                        $scope.triggerForm.$setPristine();
                        $scope.loadProfiles();
						if ($scope.pagingOptions.currentPage !== 1) {
							$scope.pagingOptions.currentPage = 1;
						}
                    }

                }
            );
         };

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
		 * @param searchText
		 */
        $scope.getPagedDataAsync = function (url, pageSize, page, filterText, sortInfo) {
			delete filterText.useExternalFilter;
			$scope.restService.get(url, 
				function (data) {
					if(data) {
						$scope.profiles = data.getProfileResponse.profiles;
						$scope.totalServerItems =  data.getProfileResponse.totalItems;
					} else {
						$scope.profiles = [];
						$scope.totalServerItems = 0;
					}
					if ($scope.profiles.length === 0)
					{
					 showSaveMessage("No Results Found ", 'error');
					 }
					if (!$scope.$$phase){
					 $scope.$apply();
					 }
				}, {pageSize: pageSize, page: page, filterText: filterText, sortInfo: sortInfo}
			);
        };

        // Loading the profile details
        $scope.loadProfiles = function () {
			setTimeout(function () {
				$scope.getPagedDataAsync(url, $scope.pagingOptions.pageSize,
						$scope.pagingOptions.currentPage, $scope.filterOptions, $scope.sortInfo);
			}, 100);
        };
        
        $scope.loadProfiles(); //loads the profile initially
        
     // Sort listener for Scripts grid
		$scope.$watch('sortInfo.directions + sortInfo.fields', function (newVal, oldVal) {
			if (newVal !== oldVal) {
				 $scope.loadProfiles();
			}

		}, true);
		
        $scope.$watch('pagingOptions.currentPage', function (newVal, oldVal) {
            if (newVal !== oldVal  && $scope.validatePageNumberValue(newVal, oldVal)) {
            	$scope.loadProfiles();
            }
        }, true);
        
		$scope.$watch('pagingOptions.pageSize', function (newVal, oldVal) {
            if (newVal !== oldVal) {
               //Get data when in first page
               if ( $scope.pagingOptions.currentPage === 1) {
                    $scope.loadProfiles();
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
     // Filter plugin
        var filterBarPlugin = {
            init: function (scope, grid) {
                filterBarPlugin.scope = scope;
                filterBarPlugin.grid = grid;
                scope.$watch('columns[0].filterText + columns[1].filterText + columns[2].filterText + columns[3].filterText', function(newVal, oldVal) {
                    var searchQuery = [];
                    var colsEmpty= true;
                    angular.forEach(filterBarPlugin.scope.columns, function(col) {
                        if (col.visible && col.filterText) {
                            var filterObj = {};
                            filterObj.field = col.field;
                            filterObj.text = col.filterText.toUpperCase();
                            colsEmpty = false;
                            searchQuery.push(filterObj);
                        }
                    });
                    $scope.filterOptions.filterText = searchQuery;

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
    		  $scope.loadProfiles();
    	};
        // Setting the grid details
        $scope.gridOptions = {
            columnDefs: [{
                field: 'name',
                displayName: 'Profile Name',
				headerCellTemplate:'partials/filterInputTypeHeaderTemplate.html'

            },
			{ // Customized column
                    displayName: 'Action',
                    width: '10%',
                    sortable: false,
                    cellTemplate: '<button class="btn btn-default btn-xs" ng-click="openEditModal(row.entity)">\n\
									<i class="glyphicon glyphicon glyphicon-wrench glyphicon-white"></i></button>'
             }],
            data: 'profiles',
            //rowTemplate: customRowTemplate,
            enablePaging: true,
			headerRowHeight: 70, // give room for filter bar
            enableCellEditOnFocus: false,
            showFooter: true,
            canSelectRows: true,
            multiSelect: false,
            jqueryUITheme: false,
            displaySelectionCheckbox: false,
            pagingOptions: $scope.pagingOptions,
            filterOptions: $scope.filterOptions,
			plugins: [filterBarPlugin, new ngGridFlexibleHeightPlugin()],
			sortInfo: $scope.sortInfo,
			useExternalSorting: true,
			totalServerItems:'totalServerItems',
        };
		//open edit model for revising profile
		$scope.openEditModal = function(entity) {
			$scope.editEntity = entity;
			$scope.reviseProfile = angular.copy($scope.editEntity);
			$('#profileReviseModal').modal('show');
		};
		
		$scope.cancelRevise = function() {
			$('#profileReviseModal').modal('hide');
			$scope.form.reviseProfileForm.$setPristine();
			$scope.reviseProfile.name = '';
		};
		
		//method for revising profile
		$scope.editSave = function() {
			if($scope.editEntity && $scope.editEntity.name != $scope.reviseProfile.name && $scope.editEntity.id == $scope.reviseProfile.id) {

				$scope.reviseRequest.reviseProfileRequest.profile = $scope.reviseProfile;
				$scope.restService.put($rootScope.base_url + "/profile", $filter('json')(reviseRequest), function() {} )
					 .success(function (data, status) {
						showSaveMessage(data.reviseProfileResponse.response.message, 'success');
						$scope.loadProfiles();
						$scope.cancelRevise();
					 }).error(function (data) {
						 if (angular.isObject(data)) {
							 showSaveMessage(data.reviseProfileResponse.response.message, 'error');
							 $scope.loadProfiles();
							$scope.cancelRevise();
						 } else {
							 showSaveMessage("Failed to update Profile", 'error');
							 $scope.loadProfiles();
							 $scope.cancelRevise();
						 }						
					});
			}else {
				showSaveMessage('No changes made to save.');
			}
		};
		
       // To clear profile name in add profile div while clicking on cancel
        $scope.doCancel = function() {
            $scope.triggerForm.$setPristine();
            $scope.profile.name = "";
         
        } 
    }
]);