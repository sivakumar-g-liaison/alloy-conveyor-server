var rest = myApp.controller('UpdateProcessorDcCntrlr', ['$rootScope', '$scope', '$blockUI', '$filter',
    function($rootScope, $scope, $blockUI, $filter) {

        $scope.title = "Update Processor DC"; //title
        $scope.processorDCs = [];
        $scope.processorType = null;
        $scope.currentDc = null;
        $scope.disableDcUpdateBtn = true;
        var block = $rootScope.block;

        updateReq = $scope.updatedcreqest = {
            reviseProcessorDcRequest: {
                processorGuid: "",
                processorDC: "",
                processorType:""
            }
        };

        // Loading the processor DC details
        $scope.getProcessorToUpdateDC = function() {

            block.blockUI();

            if ($scope.processorGuid == null || $scope.processorGuid == "") {
                showSaveMessage("Processor guid cannot be empty.", 'error');
                $scope.cleanUp();
                block.unblockUI();
                return;
            }

            $scope.restService.get($scope.base_url + '/ /processor/' + $scope.processorGuid,
                function(data, status) {
                    if (status === 200) {
                        $scope.showDc = true;
                        $scope.disableDcUpdateBtn = true;
                        $scope.processorName = data.getProcessorResponse.processor.name;
                        $scope.processorType = data.getProcessorResponse.processor.type;
                        $scope.processorDCs = $rootScope.javaProperties.dataCenters;
                        $scope.selectedProcessorDC = data.getProcessorResponse.processor.processorDC;
                        $scope.currentDc = data.getProcessorResponse.processor.processorDC;
                    } else {
                        $scope.cleanUp();
                        showSaveMessage("Failed to load Processor details", 'error');
                    }
                    block.unblockUI();
                });

        };


        // Search logic for mailbox
        $scope.updateProcessorDC = function() {

            $scope.showprogressbar = true;

            updateReq.reviseProcessorDcRequest.processorGuid = $scope.processorGuid;
            updateReq.reviseProcessorDcRequest.processorDC = $scope.selectedProcessorDC;
            updateReq.reviseProcessorDcRequest.processorType = $scope.processorType;

            $scope.restService.put($scope.base_url + "/processordc", $filter('json')(updateReq), function() {})
                .success(function(data) {
                    var messageType = (data.reviseProcessorResponse.response.status == 'success') ? 'success' : 'error';
                    showSaveMessage(data.reviseProcessorResponse.response.message, messageType);
                }).error(function(data) {
                    if (angular.isObject(data)) {
                        showSaveMessage(data.reviseProcessorResponse.response.message, 'error');
                    } else {
                        showSaveMessage("Error while updating processor DC", 'error');
                    }
                });
        };
        
        $scope.disableDcUpdate = function() {
            if ($scope.currentDc == $scope.selectedProcessorDC) {
        	    $scope.disableDcUpdateBtn = true;
                return;
            }
            $scope.disableDcUpdateBtn = false;
        }

        $scope.cleanUp = function() {
            $scope.processorName = null;
            $scope.processorDCs = [];
            $scope.selectedProcessorDC = null;
            $scope.disableDcUpdateBtn = true;
            $scope.selectedProcessorDC = null;
            $scope.currentDc = null;
        };

    }
]);
