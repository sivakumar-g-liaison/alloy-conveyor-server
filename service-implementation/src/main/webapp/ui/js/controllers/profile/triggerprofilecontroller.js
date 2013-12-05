'use strict'

/**
 * Controller for trigger profile.
 */
myApp.controller('TriggerProfileCntrlr', ['$scope', '$location',
    function ($scope, $location) {

        $scope.title = "Trigger Profile"; //title

        //Search Details
        $scope.mailBoxName = null;
        $scope.profile = null;
        $scope.mailBoxSharedKey = null;

        // Profiles loads initially
        $scope.profiles = [];

        // Loading the profile details
        $scope.loadProfiles = function () {
            $scope.restService.get($scope.base_url + "/profile").success(function (data) {
                $scope.profiles = data.getProfileResponse.profiles;
            }).error(function (data) {
                showSaveMessage("Failed to load Profiles", 'error');
            });
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
            
            $scope.restService.post($scope.base_url + "/triggerProfile"
            		+ '?name=' + profName
            		+ '&excludeMailbox=' + mbxName
            		+ '&shardKey=' + shardKey, "{}")
                .success(function (data) {
                     var messageType = (data.triggerProfileResponse.response.status == 'success')?'success':'error';
                	 showSaveMessage(data.triggerProfileResponse.response.message, messageType);
                }).error(function (data){
                	 showSaveMessage(data.triggerProfileResponse.response.message, 'error');
                });
        };
		
}]);
