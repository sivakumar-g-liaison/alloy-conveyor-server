'use strict'

/**
 * Controller for Configure mailbox setup search screen.
 */
myApp.controller('SearchMailBoxCntrlr', ['$rootScope', '$scope', '$location',  '$filter',
    function ($rootScope, $scope, $location, $filter) {

        $scope.title = "MailBox Profiles"; // title

        // Search Details
        $scope.mailBoxName = null;
        $scope.profile = null;

        // Counter to ensure the result is for the given request.
        $scope.hitCounter = 1;

        // Modify the value to change the search criteria
        $scope.searchMinCharacterCount = 5;

        // Profiles loads initially
        $scope.profiles = [];
     
        // Loading the profile details
        $scope.loadProfiles = function () {
            $scope.restService.get($scope.base_url + "/profile", function (data, status) {
                if (status == 200) {
                    $scope.profiles = data.getProfileResponse.profiles;
                } else {
                    showSaveMessage("failed to load Profiles", 'error');
                }

            })
        };
        $scope.loadProfiles(); // initial load for the profiles

        // Whenever changes occur in the mbx Name it calls search method
        $scope.$watch('mailBoxName', function () {

        	if ((typeof $scope.mailBoxName !== 'undefined' && $scope.mailBoxName !== null && $scope.mailBoxName.length >= $scope.searchMinCharacterCount)  || $scope.mailBoxName === null || $scope.mailBoxName === "" || (typeof $scope.mailBoxName !== 'undefined' && $scope.mailBoxName !== null && $scope.mailBoxName.length === 0)) {
            	            	
            	$scope.pagingOptions.currentPage = 1;
                $scope.search();

            } 
        });

        /**
		 * Remove all the data in grid and disable the info message.
		 */
       /* $scope.reset = function () {

            $scope.mailboxes = [];
            $scope.totalServerItems = 0;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
            // Set the default page to 1
            if ($scope.pagingOptions.currentPage !== 1) {
                $scope.pagingOptions.currentPage = 1;
            }
        }; */

        // Paging set up
        $scope.totalServerItems = 0;

        $scope.pagingOptions = {
            pageSizes: [25, 100, 1000],
            pageSize: 25,
            currentPage: 1
        };
        
        $scope.sortInfo = {
			fields: ['name'],
			directions: ['asc']
		};
        // Set the paging data to grid from server object
        $scope.setPagingData = function (data) {

           /* if ($scope.profile === null && ($scope.mailBoxName === null || $scope.mailBoxName.length === 0)) {
                data = [];
            }  */

            $scope.mailboxes = data.mailBox;
            $scope.totalServerItems = data.totalItems;
            if ( $scope.mailboxes.length === 0)
			{
            	showSaveMessage("No Results Found", 'error');
			}
            if (!$scope.$$phase) {
                $scope.$apply();
            }

        };

        // Enable the delete modal dialog
        $scope.openDelete = function (row) {
            $scope.key = row.entity;
            // $scope.deleteKey = true;
        };

        // Enable the delete modal dialog
        $scope.openMessage = function () {
            $scope.customKey = true;
        };

        // Close the modal
        $scope.closeMessage = function () {
            $scope.customKey = false;
        };

        // calls the rest deactivate service
        $scope.deactivateMailBox = function () {

            $scope.restService.delete($scope.base_url + "/" + $scope.key.guid, function (data, status) {
                // alert(data.deactivateMailBoxResponse.response.message); TODO
				// modal dialog
                $scope.search();
            });
            $scope.closeDelete();
        };

        // Close the modal
        $scope.closeDelete = function () {
        	$('#myModal').modal('hide')
        };

        // Navigate to mailbox screen for edit operation
        $scope.edit = function (row) {
            $location.path('/mailbox/addMailBox').search('mailBoxId', row.entity.guid);
        };

        $scope.getPagedDataAsync = function (largeLoad) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad.searchMailBoxResponse);
            }, 100);
        };

        // Search logic for mailbox
        $scope.search = function () {

            $scope.showprogressbar = true;
            var profName = "";
            if (null !== $scope.profile) {
                profName = $scope.profile.name;
            }

            var mbxName = "";
            if ($scope.mailBoxName && $scope.mailBoxName.length >= $scope.searchMinCharacterCount) {
                mbxName = $scope.mailBoxName;
            }
            
            var sortField = "";
        	var sortDirection = "";
            if($scope.sortInfo.fields && $scope.sortInfo.directions) {
            	sortField = String($scope.sortInfo.fields);
            	sortDirection = String($scope.sortInfo.directions);
            }
            
            $scope.hitCounter = $scope.hitCounter + 1;
            $rootScope.gridLoaded = false;
            $scope.restService.get($scope.base_url +'?siid=' + $rootScope.serviceInstanceId ,/*, $filter('json')($scope.serviceInstanceIdsForSearch)*/
                function (data, status) {
            	if (status === 200 || status === 400) {
                        if (data.searchMailBoxResponse.response.status == 'failure') {
                        	// Commented out because of inconsistency
                            // showSaveMessage(data.searchMailBoxResponse.response.message,
							// 'error');
                        }
                    } else {
                    	 showSaveMessage("retrieval of search results failed", 'error');
                    }
                    // if the data does not contain proper response hitCounter
					// property will not be available and throws an error,
                    // the progress bar will be displayed even after the display
					// of error message.
                    // To avoid the above error, the hitCounter will be
					// validated only if proper response is available
                    if (data.searchMailBoxResponse) { 
                    	 if (data.searchMailBoxResponse.hitCounter >= $scope.hitCounter) {
                             $scope.getPagedDataAsync(data);
                         }
                    }
                    $rootScope.gridLoaded = true;
                    $scope.showprogressbar = false;
                }, {name:mbxName, profile:profName, hitCounter:$scope.hitCounter, page:$scope.pagingOptions.currentPage, pagesize:$scope.pagingOptions.pageSize, sortField:sortField, sortDirection:sortDirection}
            );
        };

        // Customized search function for select component.
        $scope.selectSearch = function () {

            //console.info("MailboxName : " + $scope.mailBoxName);
            //console.info("ProfileName : " + $scope.profile);
            $scope.pagingOptions.currentPage = 1;
            $scope.search();

        };

        // Watch for paging options
        $scope.$watch('pagingOptions', function (newVal, oldVal) {

        	if(newVal.currentPage === null) {
				newVal.currentPage = 1;
			}
        	if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                $scope.search();
            }
        	if (newVal !== oldVal && newVal.pageSize !== oldVal.pageSize) {
                $scope.search();
                newVal.currentPage = 1;
            }
        }, true);

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

        $scope.manageStatus = '<div ng-switch on="row.getProperty(\'status\')"><div ng-switch-when="ACTIVE">Active</div><div ng-switch-when="INACTIVE">Inactive</div></div>';
        $scope.manageConfigStatus = '<div ng-switch on="row.getProperty(\'configStatus\')"><div ng-switch-when="COMPLETED">Completed</div><div ng-switch-when="INCOMPLETE_CONFIGURATION">Incomplete Configuration</div></div>';
       
        // Setting the grid details
        $scope.gridOptions = {
        		columnDefs: [{
                    field: 'guid',
                    width: '20%',
                    displayName: 'MailboxId'
                }, {
                    field: 'name',
                    width: '20%',
                    displayName: 'Name',
                    cellTemplate: '<div class="customCell" status="{{row.getProperty(\'status\')}}" name="{{row.getProperty(col.field)}}"></div>'
                }, {
                    field: 'description',
                    width: '20%',
                    displayName: 'Description'
                }, {
                	field: 'configStatus' ,
                	width: '20%' ,
                	displayName: 'Config Status' , 
                	cellTemplate: $scope.manageConfigStatus
                }, {
                    field: 'status',
                    width: '10%',
                    displayName: 'Status',
                    cellTemplate: $scope.manageStatus
                },
                /*
				 * { field: 'profiles', displayName: 'LinkedProfiles' },
				 */
                { // Customized column
                    displayName: 'Action',
                    width: '10%',
                    sortable: false,
                    cellTemplate: $scope.editableInPopup
                }
            ],
            data: 'mailboxes',
            // rowTemplate: customRowTemplate,
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
            totalServerItems: 'totalServerItems',
        };
        
     // Sort listener for search account grid
		$scope.$watch('sortInfo.directions + sortInfo.fields', function (newVal, oldVal) {
			if (newVal !== oldVal) {
				$scope.search();
			}

		}, true);
		
        // used to move add screen
        $scope.goto = function (hash) {
            $location.path(hash);
        };
		$scope.search();
    }
]);