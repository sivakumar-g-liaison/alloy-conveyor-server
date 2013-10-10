'use strict'

/**
 * Controller for Configure mailbox setup search screen.
 */
myApp.controller('SearchMailBoxCntrlr', ['$scope', 'rootUrl',
    function ($scope, $rootUrl) {

        $scope.title = "MailBox Profiles"; //title

        //Search Details
        $scope.mailBoxName = null;
        $scope.profileName = null;

        // Profiles loads initially
        $scope.profiles = [];

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

            var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
            $scope.mailboxes = pagedData;
            $scope.pagingOptions.totalServerItems = data.length;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        };

        // Customized column in the grid.
        $scope.editableInPopup = '<i class="icon-pencil" ng-click="edit(key)"></i>  <i class="icon-trash" ng-click="openDelete()"> </i>';

        // Enable the delete modal dialog
        $scope.openDelete = function () {
            $scope.deleteKey = true;
        };

        // calls the rest deactivate service
        $scope.deactivateMailBox = function (key) {

			alert($rootUrl + '/' + $scope.key.guid);
            $scope.restService.delete($rootUrl + '/' +  $scope.key.guid)
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
            alert("Here I need to know which button was selected " + $scope.key.name);
        };

        // Loading the profile details
        $scope.loadProfiles = function () {

            $scope.restService.get($rootUrl + '/profile').success(function (data) {
                $scope.profiles = data.getProfileResponse.profiles;
            }).error(function (data) {
                    alert(data);
            });
        };
        $scope.loadProfiles();//loads the profile

        // Search logic
        $scope.search = function () {

            var profName = "";
            if (null !== $scope.profileName) {
                profName = $scope.profileName.name;
            }

            var mbxName = "";
            if (null !== $scope.mailBoxName) {
                mbxName = $scope.mailBoxName;
            }

            $scope.restService.get($rootUrl + '?name=' + mbxName + '&profile=' + profName)
                .success(function (data) {
                    $scope.setPagingData(data.searchMailBoxResponse.mailBox,
                        $scope.pagingOptions.currentPage,
                        $scope.pagingOptions.pageSize);
                });
        };

        // Whenever changes occur in the mbx Name it calls search method
        $scope.$watch('mailBoxName', function () {
           if ($scope.mailBoxName !== null && $scope.mailBoxName.length > 3) {
                $scope.search();
            }
        });

        // Util function
        function checkEmpty(str) {
            if (null === str || str.length === 0) {
                return true;
            }
        }

        // Clearing the text boxes and grid
        $scope.clear = function () {
            $scope.profileName = null;
        };

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

}]);

//TODO move to individual file
myApp.directive('customCell', function () {

    return {
        restrict: 'C',
        replace: true,
        transclude: true,
        scope: {
            status: '@status',
            name: '@name'
        },

        /*loading the required template based upon the model value*/
        template: '<div ng-switch on="status"><div ng-switch-when="INCOMPLETE"><i class="icon-warning-sign"></i> {{name}}</div><div ng-switch-default>{{name}}</div></div>'
    }

});
