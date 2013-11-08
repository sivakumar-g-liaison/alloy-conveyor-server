var rest = myApp.controller('ProfileCntrlr', ['$scope', '$filter', '$location', '$log',
    function ($scope, $filter, $location, $log) {

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

        // invokes add profile service
        $scope.insert = function () {

            $scope.addRequest.addProfileRequest.profile = $scope.profile;

            $scope.restService.post(url, $filter('json')(addRequest),
                function (data, status) {

                    if (status === 200) {
                        alert(data.addProfileResponse.response.message);
                        $scope.profile.name = "";
                        $scope.loadProfiles();
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

            $scope.restService.get(url).success(function (data) {
                $scope.getPagedDataAsync(data.getProfileResponse.profiles, $scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
            }).error(function (data) {
                alert("Failed to load profiles.");
            });


        };
        $scope.loadProfiles(); //loads the profile

        // Set the paging data to grid from server object
        $scope.setPagingData = function (data, page, pageSize) {

            var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
            $scope.profiles = pagedData;
            $scope.pagingOptions.totalServerItems = data.length;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        };

        $scope.$watch('pagingOptions', function (newVal, oldVal) {
            if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                $scope.loadProfiles();
            }

            if (newVal !== oldVal && newVal.pageSize !== oldVal.pageSize) {
                $scope.loadProfiles();
            }
        }, true);

        $scope.$watch('filterOptions', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.loadProfiles();
            }
        }, true);

        // Setting the grid details
        $scope.gridOptions = {
            columnDefs: [{
                field: 'name',
                displayName: 'ProfileName',

            }],
            data: 'profiles',
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
    }
]);