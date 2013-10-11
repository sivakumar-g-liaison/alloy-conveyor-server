'use strict'

/**
 * Controller for Configure mailbox setup search screen.
 */
myApp.controller('SearchMailBoxCntrlr', ['$scope', 'rootUrl', '$location',
    function ($scope, $rootUrl, $location) {

        $scope.title = "MailBox Profiles"; //title

        //Search Details
        $scope.mailBoxName = null;
        $scope.profile = null;

        // Profiles loads initially
        $scope.profiles = [];

        // Loading the profile details
        $scope.loadProfiles = function () {

            $scope.restService.get($rootUrl + '/profile').success(function (data) {
                $scope.profiles = data.getProfileResponse.profiles;
            }).error(function (data) {
                alert("Failed to load profiles.");
            });
        };
        $scope.loadProfiles(); //loads the profile

        // Whenever changes occur in the mbx Name it calls search method
        $scope.$watch('mailBoxName', function () {
            if ($scope.mailBoxName !== null && $scope.mailBoxName.length > 3) {
                $scope.search($scope.filterOptions.filterText);
            }
        });

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

        // Set the paging data to grid from server object
        $scope.setPagingData = function (data, page, pageSize) {
		
			if (data === null || data.length <= 0) {
				alert('No data matches the given conditions.');
			}

            var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
            $scope.mailboxes = pagedData;
            $scope.pagingOptions.totalServerItems = data.length;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        };

        // Enable the delete modal dialog
        $scope.openDelete = function () {
            $scope.deleteKey = true;
        };

        // calls the rest deactivate service
        $scope.deactivateMailBox = function (key) {

            alert($rootUrl + '/' + $scope.key.guid);
            $scope.restService.delete($rootUrl + '/' + $scope.key.guid)
                .success(function (data, status) {
                    alert(data.deactivateMailBoxResponse.response.message);
                    $scope.search();
                })
                .error(function (data, status) {
                    alert(data);
                });
            $scope.closeDelete();
        };

        // Close the modal
        $scope.closeDelete = function () {
            $scope.deleteKey = false;
        };

        // Dummy Impl for edit
        $scope.edit = function () {

        	$scope.sharedService.setProperty($scope.key.guid);
            alert("Here I need to know which button was selected " + $scope.key.name);
			$location.path('/mailbox/addMailBox');
        };

        $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad.searchMailBoxResponse.mailBox, page, pageSize);
            }, 100);
        };

        // Search logic for mailbox
        $scope.search = function () {

            var profName = "";
            if (null !== $scope.profile) {
                profName = $scope.profile.name;
            }

            var mbxName = "";
            if (null !== $scope.mailBoxName) {
                mbxName = $scope.mailBoxName;
            }

            $scope.restService.get($rootUrl + '?name=' + mbxName + '&profile=' + profName)
                .success(function (data) {
                    $scope.getPagedDataAsync(data, $scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
                });
        };

        $scope.$watch('pagingOptions', function (newVal, oldVal) {
            if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                $scope.search();
            }
        }, true);

        $scope.$watch('filterOptions', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.search();
            }
        }, true);

        // Customized column in the grid.
        $scope.editableInPopup = '<i class="icon-pencil" ng-click="edit(key)"></i>  <i class="icon-trash" ng-click="openDelete()"> </i>';

        // Setting the grid details
        $scope.gridOptions = {
            columnDefs: [
                {
                    field: 'name',
                    displayName: 'Name',
                    cellTemplate: '<div class="customCell" status="{{row.getProperty(\'status\')}}" name="{{row.getProperty(col.field)}}"></div>'
                },
                {
                    field: 'description',
                    displayName: 'Desc'
                },
                {
                    field: 'status',
                    displayName: 'Status'
                },
                {
                    field: 'profiles',
                    displayName: 'LinkedProfiles'
                },
                { // Customized column
                    displayName: 'Action',
                    cellTemplate: $scope.editableInPopup
                }

            ],
            data: 'mailboxes',
            //rowTemplate: customRowTemplate,
            enablePaging: true,
            showFooter: true,
            canSelectRows: true,
            multiSelect: false,
            jqueryUITheme: false,
            displaySelectionCheckbox: false,
            pagingOptions: $scope.pagingOptions,
            filterOptions: $scope.filterOptions,
            afterSelectionChange: function (rowItem, event) {
                if (rowItem.selected === true) {
                    // clone key object
                    $scope.key = JSON.parse(JSON.stringify(rowItem.entity));
                }
            }
        };

        // used to move add screen
		$scope.goto = function (hash) { 
			$scope.testService.setProperty('test');
			$location.path(hash);
		}

}]);