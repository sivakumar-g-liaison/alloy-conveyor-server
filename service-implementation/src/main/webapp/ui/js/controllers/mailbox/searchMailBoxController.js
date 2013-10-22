'use strict'

/**
 * Controller for Configure mailbox setup search screen.
 */
myApp.controller('SearchMailBoxCntrlr', ['$scope', '$location',
    function ($scope, $location) {

        $scope.title = "MailBox Profiles"; //title

        //Search Details
        $scope.mailBoxName = null;
        $scope.profile = null;

        // Profiles loads initially
        $scope.profiles = [];

        // Loading the profile details
        $scope.loadProfiles = function () {
            $scope.restService.get($scope.base_url + "/profile").success(function (data) {
                $scope.profiles = data.getProfileResponse.profiles;
            }).error(function (data) {
                alert("Failed to load profiles.");
            });
        };
        $scope.loadProfiles(); //loads the profile

        // Whenever changes occur in the mbx Name it calls search method
        $scope.$watch('mailBoxName', function () {

            if ($scope.mailBoxName !== null && $scope.mailBoxName.length > 3) {
                $scope.search();
            } else if ($scope.profile !== null && $scope.mailBoxName.length === 0) {
                $scope.search();
            } else {
                $scope.mailboxes = [];
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
        $scope.openDelete = function (row) {
            $scope.key = row.entity;
            $scope.deleteKey = true;
        };

        // calls the rest deactivate service
        $scope.deactivateMailBox = function () {

            $scope.restService.delete($scope.base_url + "/" +  $scope.key.guid)
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
        $scope.edit = function (row) {
            $location.path('/mailbox/addMailBox').search('mailBoxId', row.entity.guid);
        };

        $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad.searchMailBoxResponse.mailBox, page, pageSize);
            }, 100);
        };

        // Search logic for mailbox
        $scope.search = function () {
            $scope.showprogressbar=true;
            var profName = "";
            if (null !== $scope.profile) {
                profName = $scope.profile.name;
            }

            var mbxName = "";
            if (null !== $scope.mailBoxName) {
                mbxName = $scope.mailBoxName;
            }

            $scope.restService.get($scope.base_url + "/" + '?name=' + mbxName + '&profile=' + profName)
                .success(function (data) {
                    $scope.getPagedDataAsync(data, $scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
                    $scope.showprogressbar=false;
                });
        };

        $scope.$watch('pagingOptions', function (newVal, oldVal) {
            if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                $scope.search();
            }
            if (newVal !== oldVal && newVal.pageSize !== oldVal.pageSize) {
                $scope.search();
            }
        }, true);

        $scope.$watch('filterOptions', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.search();
            }
        }, true);

        // Customized column in the grid.
        $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="edit(row)"><i class="glyphicon glyphicon glyphicon-pencil glyphicon-white"></i></button> <button class="btn btn-default btn-xs" ng-click="openDelete(row)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button>';

        // Setting the grid details
        $scope.gridOptions = {
            columnDefs: [
                {
                    field: 'name',
                     width:'30%',
                    displayName: 'Name',
                    cellTemplate: '<div class="customCell" status="{{row.getProperty(\'status\')}}" name="{{row.getProperty(col.field)}}"></div>'
                },
                {
                    field: 'description',
                    width:'50%',
                    displayName: 'Desc'
                },
                {
                    field: 'status',
                    width:'10%',
                    displayName: 'Status'
                },
                /*{
                    field: 'profiles',
                    displayName: 'LinkedProfiles'
                },*/
                { // Customized column
                    displayName: 'Action',
                     width:'10%',
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
            filterOptions: $scope.filterOptions
        };

        // used to move add screen
        $scope.goto = function (hash) {
            $location.path(hash);
        };

}]);