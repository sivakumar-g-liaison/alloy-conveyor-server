'use strict'


myApp.controller('AddMailBoxCntrlr', ['$scope', '$http',
    function ($scope, $http) {

		/**
		 * Add MailBox Request model
		 */
        var request = {
            addMailBoxRequest: {
                mailBox: {
                    name: "",
                    description: " ",
                    status: "",
                    serviceInstId: "",
                    shardKey: "",
                    properties: []
                }
            }
        }

        $scope.hideBasicInfo = false;
        $scope.showjson = false;

        $scope.statusenum = [
            {
                name: 'ACTIVE'
            },
            {
                name: 'INACTIVE'
            }
        ];
        $scope.status = $scope.statusenum[0]; // default value

        $scope.datas = [];

        $scope.addProperty = function () {
            $scope.datas.push($scope.data);
        };

        $scope.removeProperty = function (data, event) {

            var d = data;
            var datas = $scope.datas;

            $scope.datas.splice(datas.indexOf(d), 1);
            // event.preventDefault();
            // event.stopPropagation();
        };


        $scope.show = function () {

            if ($scope.showjson == true) {
                $scope.showjson = false;
            } else {
                generateJson();
                $scope.showjson = true;
            }
        };

        function generateJson() {

            request.addMailBoxRequest.mailBox.name = $scope.mailBoxName;
            request.addMailBoxRequest.mailBox.description = $scope.mailBoxDesc;
            request.addMailBoxRequest.mailBox.status = $scope.status.name;
            request.addMailBoxRequest.mailBox.serviceInstId = $scope.mailBoxInstId;
            request.addMailBoxRequest.mailBox.shardKey = $scope.mailBoxShardKey;
            request.addMailBoxRequest.mailBox.properties = $scope.datas;
            $scope.requestjson = angular.toJson(request);
        }


        $scope.insert = function () {

            generateJson();
            $scope.restService.post('http://localhost:9090/g2mailboxservice/rest/mailbox', $scope.requestjson,
                function (data, status) {

                    alert(angular.toJson(data.addMailBoxResponse));
                    alert(status);
                }
            );

        };

}]);

/**
 * Controller for Configure mailbox setup search screen.
 */
myApp.controller('GetMailBoxCntrlr', ['$scope', '$http', 'rootUrl',
    function ($scope, $http, $rootUrl) {

	$scope.title="MailBox Profiles";//title
	
	//$scope.rootUrl = 'http://localhost:9090/g2mailboxservice/rest/mailbox';
	
	//Search Details
	$scope.mailBoxName = null;
	$scope.profileName = null;
	
	// Profiles loads initially
	$scope.profileNames = [];
	
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
    $scope.setPagingData = function(data, page, pageSize){
        var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
        $scope.mailboxes = pagedData;
        $scope.pagingOptions.totalServerItems = data.length;
        if (!$scope.$$phase) {
            $scope.$apply();
        }
    };
	
	// Customized column in the grid.
    $scope.editableInPopup = '<button class="btn" ng-click="edit(key)"><i class="icon-pencil"></i></button>' 
		+'      <button class="btn" ng-click="openDelete()"><i class="icon-trash"></i></button>';

    // Setting the grid details
    $scope.gridOptions = {
		 columnDefs: [
                {
                    field: 'name',
                    displayName: 'Name'
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
					field:'profiles',
					displayName:'LinkedProfiles'
				},
				{// Customized column
					displayName:'Action',
					cellTemplate:$scope.editableInPopup
				}
				
            ],
        data: 'mailboxes',
        enablePaging: true,
		showFooter: true,
		canSelectRows: true,
        multiSelect: false,
        jqueryUITheme: false,
        displaySelectionCheckbox: false,
        pagingOptions: $scope.pagingOptions,
        filterOptions: $scope.filterOptions,
		afterSelectionChange: function (rowItem, event) {
                if (rowItem.selected == true) {
                    // clone key object
                    $scope.key = JSON.parse(JSON.stringify(rowItem.entity));
                }
        } 
    };
    
	// Enable the delete modal dialog
    $scope.openDelete = function () {
	    $scope.deleteKey = true;
	};
	
	// calls the rest deactivate service
	$scope.doDeactivate = function (key) {
	
		$scope.restService.delete($rootUrl+$scope.key.guid)
			.success(function (data, status) {
	            alert(data.deactivateMailBoxResponse.response.message);
	            $scope.search();
	        })
	        .error(function () {
	        	alert(data.deactivateMailBoxResponse.response.message);
	        });
	    $scope.closeDelete();
	};
	
	$scope.closeDelete = function () {
	    $scope.deleteKey = false;
	};
	
	$scope.edit = function edit(row){
        alert("Here I need to know which button was selected " + $scope.key.name)
    }

	// Loading the profile details
	$scope.loadProfiles = function() {
	
		$scope.restService.get($rootUrl + '/profile').success(function (data) {
					$scope.profileNames = data.getProfileResponse.profiles;
		});
	}
	$scope.loadProfiles();
	
	// Search logic
	$scope.search = function() {
	
		if (checkEmpty($scope.mailBoxName) && checkEmpty($scope.profileName)) {

			$scope.restService.get($rootUrl).success(function (data) {
		            $scope.setPagingData(data.searchMailBoxResponse.mailBox,
	            		$scope.pagingOptions.currentPage,
	            		$scope.pagingOptions.pageSize);
	        });
		} else if (checkEmpty($scope.profileName) && !checkEmpty($scope.mailBoxName)) {
	
			$scope.restService.get($rootUrl + '?name='+$scope.mailBoxName)
				.success(function (data) {
			        $scope.setPagingData(data.searchMailBoxResponse.mailBox,
		        		$scope.pagingOptions.currentPage,
		        		$scope.pagingOptions.pageSize);
		    });
		} else if (!checkEmpty($scope.profileName) && checkEmpty($scope.mailBoxName)) {
		
			$scope.restService.get($rootUrl + '?profile='+$scope.profileName.name)
				.success(function (data) {
			        $scope.setPagingData(data.searchMailBoxResponse.mailBox,
		        		$scope.pagingOptions.currentPage,
		        		$scope.pagingOptions.pageSize);
		    });
		} else {
	
			$scope.restService.get($rootUrl + '?name='+$scope.mailBoxName+'&profile='+$scope.profileName.name)
				.success(function (data) {
					$scope.setPagingData(data.searchMailBoxResponse.mailBox,
	            		$scope.pagingOptions.currentPage,
	            		$scope.pagingOptions.pageSize);
	        });
		}
	}
	
	// Whenever changes occur in the mbx Name it calls search method
	$scope.$watch('mailBoxName', function () {
        $scope.search();
    });
	
	// Util function
	function checkEmpty(str) {
		if (null === str || str.length == 0) {
			return true;
		}
	}
	
	// Clearing the text boxes and grid
	$scope.clear = function() {
		$scope.profileName = null;
	}
		
}]);