'use strict'

/**
 * Controller for Configure mailbox setup search screen.
 */
myApp.controller('SearchAccountCntrlr', ['$scope', '$location', '$blockUI',
    function ($scope, $location, $blockUI) {

        var block = $blockUI.createBlockUI();

        $scope.loadOrigin = function() {
            
            $scope.enumAccountType = [
                'machine',
                'person'
            ];
            
            $scope.enumIdpProvider = [
                'Provider'
            ];
            
            $scope.accountType = {
                name: ""
            }
            
            $scope.provider = {
                name: ""
            }
            
            $scope.loginId = "";
        }
        $scope.loadOrigin();
        
        //Search Details
        $scope.mailBoxName = null;
        $scope.profile = null;

        // Profiles loads initially
        $scope.accounts = [];

        // Loading the profile details
        $scope.search = function () {
            
            var url  = $scope.base_url + "/account?name="+$scope.accountType.name+"&provider="+$scope.provider.name+"&domain="+$scope.loginId+"";
            console.log("Url is "+url);
            $scope.restService.get(url).success(function (data) {
                //$scope.accounts = data.searchUserAccountResponse.profile;
                $scope.getPagedDataAsync(data, $scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
                $scope.showprogressbar=false;
            })
        };
        $scope.search(); //loads the profile

        // Whenever changes occur in the mbx Name it calls search method
        /*$scope.$watch('mailBoxName', function () {

            if ($scope.mailBoxName !== null && $scope.mailBoxName.length > 3) {
                $scope.search();
            } else if ($scope.profile !== null && $scope.mailBoxName.length === 0) {
                $scope.search();
            } else {
                $scope.mailboxes = [];
            }
        });*/

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
				$scope.message = 'No results found.';
				$scope.openMessage();
            }

            var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
            $scope.accounts = pagedData;
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
		
		// Enable the delete modal dialog
        $scope.openMessage = function () {
            $scope.customKey = true;
        };
		
		// Close the modal
        $scope.closeMessage = function () {
            $scope.customKey = false;
        };

        // calls the rest deactivate service
        $scope.deactivateUserAccount = function () {

            block.blockUI();
            $scope.restService.delete($scope.base_url + "/account/" +  $scope.key.guid)
                .success(function (data, status) {
                    //alert(data.reviseUserProfileResponse.response.message);
                    $scope.search();
                    block.unblockUI();
                })
                .error(function (data, status) {
                    alert(data);
                    block.unblockUI();
                });
            $scope.closeDelete();
        };

        // Close the modal
        $scope.closeDelete = function () {
            $scope.deleteKey = false;
        };

        // Dummy Impl for edit
        $scope.edit = function (row) {
            $location.path('/account/addAccount').search('accountId', row.entity.guid);
        };

        $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
            setTimeout(function () {
                $scope.setPagingData(largeLoad.searchUserAccountResponse.userAccount, page, pageSize);
            }, 100);
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
                    field: 'accountType',
                     width:'15%',
                    displayName: 'Type'
                },
                {
                    field: 'providerName',
                    width:'35%',
                    displayName: 'Provider'
                },
                {
                    field: 'loginId',
                    width:'30%',
                    displayName: 'Login domain'
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
            data: 'accounts',
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