'use strict'

/**
 * Controller for Configure mailbox setup search screen.
 */
myApp.controller('SearchMailBoxCntrlr', ['$rootScope', '$scope', '$location',  '$filter',
    function ($rootScope, $scope, $location, $filter) {

		$rootScope.pipelineId = $location.search().pipeLineId;
        $scope.title = "MailBox Profiles"; // title

        // Search Details
        $scope.mailBoxName = null;
        $scope.profile = null;

        // To enable "No records found" div
        $scope.info = false;

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

            if (typeof($scope.mailBoxName) !== 'undefined' && $scope.mailBoxName !== null && $scope.mailBoxName.length >= $scope.searchMinCharacterCount) {

                $scope.search();
                if ($scope.pagingOptions.currentPage !== 1) {
                    $scope.pagingOptions.currentPage = 1;
                }

            } else if ($scope.profile !== null && $scope.mailBoxName.length === 0) {

                $scope.search();
                if ($scope.pagingOptions.currentPage !== 1) {
                    $scope.pagingOptions.currentPage = 1;
                }
            } else {
                $scope.reset();
            }
        });

        /**
		 * Remove all the data in grid and disable the info message.
		 */
        $scope.reset = function () {

            $scope.mailboxes = [];
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

        // Grid Setups
        $scope.filterOptions = {
            filterText: "",
            useExternalFilter: true
        };

        // Paging set up
        $scope.totalServerItems = 0;

        $scope.pagingOptions = {
            pageSizes: [25, 100, 1000],
            pageSize: 25,
            currentPage: 1
        };

        // Set the paging data to grid from server object
        $scope.setPagingData = function (data, page, pageSize) {

            if (data === null || data.length <= 0) {
                $scope.message = 'No results found.';
                $scope.info = true;
            } else if ($scope.profile === null && ($scope.mailBoxName === null || $scope.mailBoxName.length === 0)) {
                data = [];
                $scope.info = true;
            } else {
                $scope.info = false;
            }

            var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
            $scope.mailboxes = pagedData;
            $scope.totalServerItems = data.length;
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
            $location.path('/mailbox/addMailBox').search('mailBoxId', row.entity.guid).search('pipeLineId', $rootScope.pipelineId);
        };

        $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad.searchMailBoxResponse.mailBox, page, pageSize);
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
            if (null !== $scope.mailBoxName && $scope.mailBoxName.length >= $scope.searchMinCharacterCount) {
                mbxName = $scope.mailBoxName;
            }

            $scope.hitCounter = $scope.hitCounter + 1;
            $scope.restService.get($scope.base_url + "/" + '?name=' + mbxName + '&profile=' + profName + '&hitCounter=' + $scope.hitCounter/*, $filter('json')($scope.serviceInstanceIdsForSearch)*/,
                function (data, status) {
                    if (status == 200) {
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
                             $scope.getPagedDataAsync(data, $scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
                         }
                    }
                   
                    $scope.showprogressbar = false;
                });
        };

        // Customized search function for select component.
        $scope.selectSearch = function () {

            console.info("MailboxName : " + $scope.mailBoxName);
            console.info("ProfileName : " + $scope.profile);
            if ($scope.profile == null && ($scope.mailBoxName == null || $scope.mailBoxName == "")) {
                $scope.reset();
            } else {
                $scope.search();
            }



        };

        // Watch for paging options
        $scope.$watch('pagingOptions', function (newVal, oldVal) {

        	if(newVal.currentPage === null) {
				newVal.currentPage = 1;
			}
        	if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                $scope.search();
            }
        	if (newVal !== oldVal && newVal.pageSize !== oldVal.pageSize && (($scope.mailBoxName !== null && $scope.mailBoxName !== "")  || $scope.profile !== null)) {
                $scope.search();
                newVal.currentPage = 1;
            }
        }, true);

        $scope.$watch('filterOptions', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.search();
            }
        }, true);

        // Customized column in the grid.
        $scope.editableInPopup = '<div ng-switch on="row.getProperty(\'status\')">\n\
        <div ng-switch-when="INACTIVE" style="cursor: default;"><button class="btn btn-default btn-xs" ng-click="edit(row)">\n\
        <i class="glyphicon glyphicon glyphicon-pencil glyphicon-white"></i></button>\n\
        <button class="btn btn-default btn-xs" ng-disabled="true">\n\
        <i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>\n\
        <div ng-switch-default><button class="btn btn-default btn-xs" ng-click="edit(row)">\n\
        <i class="glyphicon glyphicon glyphicon-pencil glyphicon-white"></i></button>\n\
        <button class="btn btn-default btn-xs" ng-click="openDelete(row)" data-toggle="modal" data-target="#myModal">\n\
        <i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div></div>';

        $scope.manageStatus = '<div ng-switch on="row.getProperty(\'status\')"><div ng-switch-when="ACTIVE">Active</div><div ng-switch-when="INACTIVE">Inactive</div><div ng-switch-when="INCOMPLETE_CONFIGURATION">Incomplete_Configuration</div></div>';
        // Setting the grid details
        $scope.gridOptions = {
            columnDefs: [{
                    field: 'name',
                    width: '30%',
                    displayName: 'Name',
                    cellTemplate: '<div class="customCell" status="{{row.getProperty(\'status\')}}" name="{{row.getProperty(col.field)}}"></div>'
                }, {
                    field: 'description',
                    width: '37%',
                    displayName: 'Desc'
                }, {
                    field: 'status',
                    width: '23%',
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
            pagingOptions: $scope.pagingOptions,
            filterOptions: $scope.filterOptions,
            totalServerItems: 'totalServerItems',
        };

        // used to move add screen
        $scope.goto = function (hash) {
            $location.path(hash);
        };

    }
]);