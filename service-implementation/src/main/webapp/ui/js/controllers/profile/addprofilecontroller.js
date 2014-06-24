var rest = myApp.controller('ProfileCntrlr', ['$rootScope','$scope', '$filter', '$location', '$log',
    function ($rootScope,$scope, $filter, $location, $log) {
	
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
        
        reviseRequest = $scope.reviseRequest = {
                reviseProfileRequest: {
                    profile: {}
                }
        };
        
        // Search Profiles based on profile Name
        $scope.profileName = null;
               
         // To enable "No records found" div
        $scope.info = false;
        
        // Modify the value to change the search criteria
        $scope.searchMinCharacterCount = 5;

        // invokes add profile service
        $scope.insert = function () {

            $scope.addRequest.addProfileRequest.profile = $scope.profile;

            $scope.restService.post(url, $filter('json')(addRequest),
                function (data, status) {

                    if (status === 200) {
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
            filterText: "",
            useExternalFilter: true
        };

        //Paging set up
        $scope.totalServerItems = 0;

        $scope.pagingOptions = {
            pageSizes: [25, 100, 1000],
            pageSize: 25,
            currentPage: 1
        };

        $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad, page, pageSize);
            }, 100);
        };

        // Loading the profile details
        $scope.loadProfiles = function () {

        $scope.restService.get(url, 
        	function (data, status) {
        		if(status === 200) {
        			$scope.getPagedDataAsync(data.getProfileResponse.profiles, $scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
        		} else {
        			showSaveMessage("Failed to load profiles.", 'error');
        		}
        	}
        );


        };
        $scope.loadProfiles(); //loads the profile

        // Set the paging data to grid from server object
        $scope.setPagingData = function (data, page, pageSize) {
            
             //To enable No Results found div
             if (data === null || data.length <= 0) {
                $scope.message = 'No results found.';
                $scope.info = true;
            } else {
                $scope.info = false;
            }

            var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
            $scope.profiles = pagedData;
            $scope.totalServerItems = data.length;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        };

        $scope.$watch('pagingOptions', function (newVal, oldVal) {
            if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
               // $scope.loadProfiles();
                if((typeof($scope.profileName) !== 'undefined' && $scope.profileName !== null && $scope.profileName.length >= $scope.searchMinCharacterCount)) {
                     $scope.search();
                } else {
                    $scope.loadProfiles();
                }
               
            }

            if (newVal !== oldVal && newVal.pageSize !== oldVal.pageSize) {
               // $scope.loadProfiles();
                if((typeof($scope.profileName) !== 'undefined' && $scope.profileName !== null && $scope.profileName.length >= $scope.            searchMinCharacterCount)) {
                    $scope.search();
                } else {
                    $scope.loadProfiles();
                }
				newVal.currentPage = 1;
            }
        }, true);

        $scope.$watch('filterOptions', function (newVal, oldVal) {
            if (newVal !== oldVal) {
               // $scope.loadProfiles();
                if((typeof($scope.profileName) !== 'undefined' && $scope.profileName !== null && $scope.profileName.length >= $scope.searchMinCharacterCount)) {
                     $scope.search();
                } else {
                    $scope.loadProfiles();
                }
            }
        }, true);
        
        /**
         * Remove all the data in grid and disable the info message.
         */
        $scope.reset = function () {

            $scope.profiles = [];
            $scope.info = false;
            $scope.totalServerItems = 0;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
            // Set the default page to 1
            if ($scope.pagingOptions.currentPage !== 1) {
                $scope.pagingOptions.currentPage = 1;
            }
        };

         // Search logic for mailbox
        $scope.search = function () {

            $scope.showprogressbar = true;
            var profName = "";
            if (null !== $scope.profileName && $scope.profileName.length >= $scope.searchMinCharacterCount) {
                profName = $scope.profileName;
            }

           $scope.restService.get($scope.base_url + "/findprofile" + '?name=' + profName ,
                function (data, status) {
                    if (status == 200) {
                        if (data.getProfileResponse.response.status == 'failure') {
                        	//Commented out because of inconsistency
                            //showSaveMessage(data.searchMailBoxResponse.response.message, 'error');
                        }
                    } else {
                    	 showSaveMessage("retrieval of search results failed", 'error');
                    }
                    
                    $scope.getPagedDataAsync(data.getProfileResponse.profiles, $scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
                    
                    $scope.showprogressbar = false;
                });
        };
         // Whenever changes occur in the Profile Name it calls search method
        $scope.$watch('profileName', function () {

            if (typeof($scope.profileName) !== 'undefined' && $scope.profileName !== null && $scope.profileName.length >= $scope.searchMinCharacterCount) {

                $scope.search();
                if ($scope.pagingOptions.currentPage !== 1) {
                    $scope.pagingOptions.currentPage = 1;
                }

            } else if ($scope.profileName.length == 0) {
                $scope.reset();
                $scope.loadProfiles();
            } else if(typeof($scope.profileName) == 'undefined' || $scope.profileName == null || $scope.profileName.length < $scope.searchMinCharacterCount) {
                $scope.reset();
            }
        });
        // Setting the grid details
        $scope.gridOptions = {
            columnDefs: [{
                field: 'name',
                displayName: 'Profile Name',

            }],
            data: 'profiles',
            //rowTemplate: customRowTemplate,
            enablePaging: true,
            enableCellEditOnFocus: true,
            showFooter: true,
            canSelectRows: true,
            multiSelect: false,
            jqueryUITheme: false,
            displaySelectionCheckbox: false,
            pagingOptions: $scope.pagingOptions,
            filterOptions: $scope.filterOptions,
			plugins: [new ngGridFlexibleHeightPlugin()],
			totalServerItems:'totalServerItems',
			afterSelectionChange: function (row) {
				if(row.selected == true) {
					$scope.selectedProfileName = row.entity.name;
					$scope.selectedProfileId = row.entity.id;
				}
			}
        };
       
      //Event is triggered on blur after cell is edited
		$scope.$on('ngGridEventEndCellEdit', function(edittedCellData) {
			//Check if 
			if(edittedCellData.targetScope.row.entity.name != $scope.selectedProfileName && 
				edittedCellData.targetScope.row.entity.id == $scope.selectedProfileId) {
				
				$scope.reviseRequest.reviseProfileRequest.profile = edittedCellData.targetScope.row.entity;
				$scope.restService.put($rootScope.base_url + "/profile", $filter('json')(reviseRequest), "")
					 .success(function (data, status) {
						if (data.reviseProfileResponse.response.status == 'failure') {
							showSaveMessage(data.reviseProfileResponse.response.message, 'error');
							$scope.loadProfiles();
						}else {
							showSaveMessage("Profile is updated succesfully", 'success');
							$scope.loadProfiles();
						}
					 }).error(function (data, status) {
						showSaveMessage('Failed to update Profile.' + data, 'error');
						$scope.loadProfiles();
					});
			}
		});
		
       // To clear profile name in add profile div while clicking on cancel
        $scope.doCancel = function() {
            $scope.triggerForm.$setPristine();
            $scope.profile.name = "";
         
        } 
    }
]);