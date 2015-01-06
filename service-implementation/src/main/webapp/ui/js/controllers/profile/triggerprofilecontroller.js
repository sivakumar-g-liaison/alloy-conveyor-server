'use strict'

/**
 * Controller for trigger profile.
 */
myApp.controller('TriggerProfileCntrlr', ['$rootScope', '$scope', '$location', '$blockUI',
    function ($rootScope, $scope, $location, $blockUI) {
	
        $scope.title = "Trigger Profile"; //title

        //Search Details
        $scope.mailBoxName = null;
        $scope.profile = null;
        $scope.mailBoxSharedKey = null;

        var block = $rootScope.block;
        
        // Profiles loads initially
        $scope.profiles = [];

        // Loading the profile details
        $scope.loadProfiles = function () {
        	
        	block.blockUI();
        	
            $scope.restService.get($scope.base_url + "/profile", 
            	function (data, status) {
            		if(status === 200) {
            			$scope.profiles = data.getProfileResponse.profiles;
            		} else {
                        showSaveMessage("Failed to load Profiles", 'error');
            		}
            		block.unblockUI();
            	}
            );
        };
        $scope.loadProfiles(); //loads the profile

        // Search logic for mailbox
        $scope.trigger = function () {

            //$scope.showprogressbar=true;

            var profName = "";
            if (null !== $scope.profile) {
                profName = $scope.profile.name;
            }

            var mbxName = "";
            if (null !== $scope.mailBoxName) {
                mbxName = $scope.mailBoxName;
            }
            
            var shardKey = "";
            if (null !== $scope.mailBoxSharedKey) {
            	shardKey = $scope.mailBoxSharedKey;
            }
            
            $scope.restService.post($scope.base_url + "/trigger/profile"
            		+ '?name=' + profName
            		+ '&excludeMailbox=' + mbxName
            		+ '&shardKey=' + shardKey, "{}")
                .success(function (data) {
                     var messageType = (data.triggerProfileResponse.response.status == 'success')?'success':'error';
                	 showSaveMessage(data.triggerProfileResponse.response.message, messageType);
                }).error(function (data) {
                	if (angular.isObject(data)) {
                		showSaveMessage(data.triggerProfileResponse.response.message, 'error');
                	} else {
                		showSaveMessage("Error triggering the profile", 'error');
                	}
                	 
                });
        };
		
}]);
