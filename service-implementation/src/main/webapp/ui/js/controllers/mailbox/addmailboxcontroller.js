var rest = myApp.controller('AddMailBoxCntrlr', ['$rootScope', '$scope', '$filter', '$location', '$log', '$modal', '$blockUI', '$http',
    function ($rootScope, $scope, $filter, $location, $log, $modal, $blockUI, $http) {
	
		$scope.ttlDropdownValues = [
			{"name":"Seconds","id":"seconds"},
			{"name":"Minutes","id":"minutes"},
			{"name":"Hours","id":"hours"},
			{"name":"Days","id":"days"},
			{"name":"Months","id":"months"},
			{"name":"Years","id":"years"}
		];
		
		$scope.ttlUnit = $scope.ttlDropdownValues[0];
		$scope.enumNotification = [
		{"name":"ENABLED","id":"true"},
		{"name":"DISABLED","id":"false"},
		];
		$scope.notificationRequired = $scope.enumNotification[0];
		$scope.notificationRequiredValue = $scope.notificationRequired.id;    

        //Remove if not needed
        $scope.isMailBoxEdit = false;
		$scope.isEnable = false;

		$scope.addProcessorBtnValue = 'Add Processors';
		
		$scope.isProcessorsAvailable = false;
		$scope.isMailBoxSaved = false;
		
		 $scope.enumTenancyKey = [];
	     $scope.tenancyKey = {guid:'', name:''};
	     $scope.tenancyKeys = [];
		 
		 $scope.isDisableFilterVal = false;
		
        //Model for Add MB
        addRequest = $scope.addRequest = {
            addMailBoxRequest: {
                mailBox: {}
            }
        };
        //Model For Revise MB
        editReq = $scope.editReq = {
            reviseMailBoxRequest: {
                mailBox: {}
            }
        };

        $scope.mailBox = {
            guid: "",
            name: "",
            description: "",
            status: "",
            shardKey: "",
            tenancyKey: "",
            clusterType: "",
            properties: []
        };
        $scope.initEnumStats = [{"name":"Active","id":"ACTIVE"},
            {"name":"Inactive","id":"INACTIVE"}];
        
        $scope.editEnumStats = [{"name":"Active","id":"ACTIVE"},
            {"name":"Inactive","id":"INACTIVE"}];
        
        $scope.enumstats = $scope.initEnumStats;
        
        // Default values of payloadsize and no of files threshold
        $scope.payloadSizeThreshold = 131072;
        $scope.numberOfFilesThreshold = 10

        var block = $rootScope.block;
        var fromAddProcsr = false;
		var processorSearchFlag = false;

        $scope.status = $scope.enumstats[0];
        $scope.mailBox.clusterType = $rootScope.javaProperties.clusterTypes[0];
        

        //Data from server - YOU HAVE TO JUST ADD 'add new -->' manually to the list from server.
        $scope.allStaticPropertiesThatAreNotAssignedValuesYet = [{"name":"add new -->","id":"add new -->"},
            {"name":"Email Notification Ids","id":"emailnotificationids"}, {"name":"Time to Pickup File Posted to Mailbox","id":"timetopickupfilepostedtomailbox"}, {"name":"Time to Pickup File Posted By Mailbox","id":"timetopickupfilepostedbymailbox"}, 
			{"name":"TTL", "id":"ttl"}, {"name":"Email Notification for SLA violation", "id":"emailnotificationforslaviolation"}, {"name":"Max No of Notification for SLA violation", "id":"maxnumberofnotificationforslaviolation"}, {"name":"Lens Failure Notification Count", "id":"maxnumberoflensnotificationforuploaderfailure"}];

        $scope.allStaticProperties = [{"name":"Email Notification Ids","id":"emailnotificationids"}, {"name":"Time to Pickup File Posted to Mailbox","id":"timetopickupfilepostedtomailbox"}, {"name":"Time to Pickup File Posted By Mailbox","id":"timetopickupfilepostedbymailbox"}, 
		   {"name":"TTL", "id":"ttl"}, {"name":"Email Notification for SLA violation", "id":"emailnotificationforslaviolation"}, {"name":"Max No of Notification for SLA violation", "id":"maxnumberofnotificationforslaviolation"}, {"name":"Lens Failure Notification Count", "id":"maxnumberoflensnotificationforuploaderfailure"}];
		//Data from server
        $scope.mailBoxProperties = [{
            name: '',
            value: '',
            allowAdd: true
        }];
		
		$scope.isMailboxNameChanged = false;
        
       // Type ahead method to retrieve all domains in tenancy keys
       $scope.getTenancyKeys = function () {
            var retrievedTenancyKeys = [];  
           	$scope.restService.get($scope.base_url + '/tenancyKeys' ,
                     function (data, status) {
           				 block.unblockUI();
                         if (status === 200 || status === 400) {

                        	 if (data.getTenancyKeysResponse.response.status === 'success') {
                                  angular.forEach(data.getTenancyKeysResponse.tenancyKeys, function (item) {
                                	  retrievedTenancyKeys.push(item);
                                  });
                                 $scope.tenancyKeys = retrievedTenancyKeys;
                                 //console.log("tenancyKeys"+$scope.tenancyKeys);  
                                
                             } else {
                                 showSaveMessage(data.getTenancyKeysResponse.response.message, 'error');
                                
                             }

                         } else {
                             showSaveMessage("Error while retrieving tenancykeys", 'error');
                             
                         }
                        
        	 });               
        };
        $scope.getTenancyKeys();
		
		function selectTTLUnit(selectedTTLValue) {
			for(var i = 0; i < $scope.ttlDropdownValues.length; i++) {
				if($scope.ttlDropdownValues[i].name === selectedTTLValue) {
					$scope.ttlUnit = $scope.ttlDropdownValues[i];
					break;
				}
			}
		}
		$scope.onNotificationChange = function (notificationType) {
            $scope.notificationRequired = notificationType;
			$scope.notificationRequiredValue = notificationType.id;
        };	
		function setNotificationValue(selectedNotificationValue) {
		   if (selectedNotificationValue === "true") {
		   $scope.notificationRequired = $scope.enumNotification[0];
		   $scope.notificationRequiredValue = $scope.enumNotification[0].id;
		   } else {
		   $scope.notificationRequired = $scope.enumNotification[1];
		   $scope.notificationRequiredValue = $scope.enumNotification[1].id;
		   }
		}
        
        // Loads the details initially if edit
        $scope.load = function () {

            if ($location.search().mailBoxId !== '' && typeof $location.search().mailBoxId !== 'undefined') { // Edit Mode On

                $scope.enumstats = $scope.editEnumStats;
                $scope.isMailBoxEdit = true;
                $scope.isEnable = true;	
                $scope.mailBoxId = $location.search().mailBoxId;
				$scope.isDisableFilters = $location.search().disableFilters;
                block.blockUI();
                var sIdConstraint = $rootScope.serviceInstanceId == "" ? false : true;
                $scope.restService.get($scope.base_url + "/" + $scope.mailBoxId+ '?addServiceInstanceIdConstraint=' + sIdConstraint + '&sid=' + $rootScope.serviceInstanceId, //Get mail box Data
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200 || status === 400) {                        	
                        	if (data.getMailBoxResponse.response.status === 'success') {
                                $scope.mailBox.guid = $scope.mailBoxId;
                                $scope.mailBox.name = data.getMailBoxResponse.mailBox.name;
                                $scope.mailBox.description = data.getMailBoxResponse.mailBox.description;
                                $scope.mailBox.clusterType = data.getMailBoxResponse.mailBox.clusterType;
								$scope.mailBox.modifiedBy = data.getMailBoxResponse.mailBox.modifiedBy;
								$scope.mailBox.modifiedDate = data.getMailBoxResponse.mailBox.modifiedDate;
        						if(data.getMailBoxResponse.mailBox.processors.length > 0) {
        							
        							$scope.addProcessorBtnValue = 'List Processors';
        							$scope.isProcessorsAvailable = true;
									$scope.isDisableFilterVal = $scope.isDisableFilters;
        						}	else {
        							$scope.addProcessorBtnValue = 'Add Processors';
        							$scope.isProcessorsAvailable = false;
									$scope.isDisableFilterVal = $scope.isDisableFilters;
        						}

                                if (data.getMailBoxResponse.mailBox.status === 'ACTIVE' || data.getMailBoxResponse.mailBox.status === 'INCOMPLETE') {
                                    $scope.status = $scope.enumstats[0];
                                } else if (data.getMailBoxResponse.mailBox.status === 'INACTIVE') {
                                    $scope.status = $scope.enumstats[1];
                                } else {
                                    $scope.status = $scope.enumstats[2];
                                }
                                $scope.mailBox.shardKey = data.getMailBoxResponse.mailBox.shardKey;
                                $scope.tenancyKey.name = data.getMailBoxResponse.mailBox.tenancyKeyDisplayName;
                                $scope.mailBox.tenancyKey = data.getMailBoxResponse.mailBox.tenancyKey;
                                $scope.mailBoxProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                                for (var i = 0; i < data.getMailBoxResponse.mailBox.properties.length; i++) {
								
									if(data.getMailBoxResponse.mailBox.properties[i].name === "ttlunit") {
										selectTTLUnit(data.getMailBoxResponse.mailBox.properties[i].value);
										continue;
									}
									
									if(data.getMailBoxResponse.mailBox.properties[i].name === "emailnotificationforslaviolation") {
										setNotificationValue(data.getMailBoxResponse.mailBox.properties[i].value);										
									}			
                                					                          
                                    var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet,
                                        data.getMailBoxResponse.mailBox.properties[i].name);

                                        if (indexOfElement !== -1) {
                                        $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
                                    }
                                    $scope.mailBoxProperties.push({
                                        name: (indexOfElement == -1)?data.getMailBoxResponse.mailBox.properties[i].name:
                                            getName($scope.allStaticProperties, data.getMailBoxResponse.mailBox.properties[i].name),
                                        value: data.getMailBoxResponse.mailBox.properties[i].value,
                                        allowAdd: false
                                    });
                                }
                                $scope.mailBoxProperties.push({ //Adding now so that the add new option always shows below the available properties
                                    name: '',
                                    value: '',
                                    allowAdd: true
                                });
                            } else {
                        		showSaveMessage(data.getMailBoxResponse.response.message, 'error');
                        	}
                        	
                        } else {
                        	showSaveMessage("Error While loading Mailbox", 'error');
                        }
                	}    
                );
            }
        };

        $scope.load();

        $scope.saveForm = function () {
        	
        	block.blockUI();
            // $scope.mailBox.properties = $scope.mailBoxProperties; - DO NOT DO THIS THIS WILL IMPACT CURRENT UI VIEW
            var len = $scope.mailBoxProperties.length;
			
            for (var i = 0; i < len - 1; i++) {
                var index =  getIndex($scope.allStaticProperties, $scope.mailBoxProperties[i].name);
                
                $scope.mailBox.properties.push({
                    name: (index === -1)?$scope.mailBoxProperties[i].name:getId($scope.allStaticProperties, $scope.mailBoxProperties[i].name),
                    value: $scope.mailBoxProperties[i].value

                });
            }
			
            //ttl and ttlunit have different properties.
			if (getIndex($scope.mailBox.properties, "ttl") === -1) {			
			   for (var i = 0; i < $scope.mailBox.properties.length; i++) {
			     if ($scope.mailBox.properties[i].name === "ttlunit") {
				     $scope.mailBox.properties.splice(i, 1);
					 break;
				 }			   
			   }		    
			} else {
				  $scope.mailBox.properties.push({
					name: "ttlunit",
					value: $scope.ttlUnit.name
				  });
			}
			//adding the value for emailnotificationforslaviolation;
			if (getIndex($scope.mailBox.properties, "emailnotificationforslaviolation") != -1) {			
			   for (var i = 0; i < $scope.mailBox.properties.length; i++) {
			     if ($scope.mailBox.properties[i].name === "emailnotificationforslaviolation") {
				     $scope.mailBox.properties[i].value = $scope.notificationRequiredValue;
					 break;
				 }			   
			   }		    
			};			
			
            if ($scope.isMailBoxEdit) {

                $scope.editReq.reviseMailBoxRequest.mailBox = $scope.mailBox;
                $scope.editReq.reviseMailBoxRequest.mailBox.guid = $scope.mailBoxId;
                $scope.editReq.reviseMailBoxRequest.mailBox.status = $scope.status.id;

                var sidConstraint = true;
                if ($rootScope.serviceInstanceId == "") {
                	sidConstraint = false;
                }
                
                $scope.restService.put($scope.base_url + "/" + $scope.mailBoxId + "?sid=" + $rootScope.serviceInstanceId + '&addServiceInstanceIdConstraint=' +sidConstraint, $filter('json')(editReq),
                    function (data, status) {                        
                	    block.unblockUI();
                        if (status === 200 || status === 400) {
                             $scope.isMailBoxSaved = true;
                            if (fromAddProcsr) {
                                $location.$$search = {};
                                $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name).search('isProcessorSearch', processorSearchFlag);

                            } else if (data.reviseMailBoxResponse.response.status === 'success') {
                                showSaveMessage(data.reviseMailBoxResponse.response.message, 'success');
                            } else {
                                showSaveMessage(data.reviseMailBoxResponse.response.message, 'error');
                                $scope.isMailBoxSaved = false;
                            }
                        } else {
                            showSaveMessage("Error while saving Mailbox", 'error');
                        }
                        $scope.mailBox.properties = [];
                    }
                );
            } else {
                $scope.addRequest.addMailBoxRequest.mailBox = $scope.mailBox;
                $scope.addRequest.addMailBoxRequest.mailBox.status =$scope.status.id;
                //$log.info($filter('json')(addRequest));

                $scope.restService.post($scope.base_url + '?sid=' + $rootScope.serviceInstanceId, $filter('json')(addRequest),
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200 || status === 400) {

                        	if(data.addMailBoxResponse.mailBox !== null) {
                            	$scope.mailBoxId = data.addMailBoxResponse.mailBox.guid;
                        	}

                        	if (data.addMailBoxResponse.response.status === 'success') {
                        		$scope.isMailBoxSaved = true;
								$scope.mailBox.guid = $scope.mailBoxId;
								if (fromAddProcsr) {
									$location.$$search = {};
									$location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name).search('isProcessorSearch', processorSearchFlag);
								} else {
									showSaveMessage(data.addMailBoxResponse.response.message, 'success');
									$scope.isMailBoxEdit = true;
									$scope.isMailBoxSaved = false;
									$location.path('/mailbox/addMailBox').search('mailBoxId', $scope.mailBoxId);
									$scope.load();
								}
                            } else {
                                showSaveMessage(data.addMailBoxResponse.response.message, 'error');
                            }

                        } else {
                            showSaveMessage("Error while saving Mailbox", 'error');
                        }
                        $scope.mailBox.properties = [];

                    }
                );
            }
        };

        $scope.doCancel = function () {        	
    	    $scope.closeModalView(); 
            $location.$$search = {};
			if($scope.isDisableFilterVal === true || $scope.isDisableFilterVal === "true"){
				$location.path('/mailbox/getMailBox').search('disableFilters', true);
			} else {
				$location.path('/mailbox/getMailBox');
			}            
        };
        
        $scope.closeModalView = function () {
            $('#cancelAction').modal('hide')
        };
        
        // Method to display dialog window for saving mailbox before going to processor screen
        $scope.navigateToProcessorScreen = function() {
           	if ($scope.formAddMbx.$dirty && !$scope.isMailBoxSaved) {
                $('#saveMailboxConfirmationModal').modal('show');
            } else {
                $location.$$search = {};
            	$location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name).search('isProcessorSearch', processorSearchFlag).search('disableFilters', $scope.isDisableFilterVal);
            }
        }
        
        // method to close mailbox saving confirmation dialog
        $scope.closeMailboxConfirmationModal = function() {
       	 $('#saveMailboxConfirmationModal').modal('hide');
            $location.$$search = {};
            $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name).search('isProcessorSearch', processorSearchFlag);
           
       }

        $scope.addProcessor = function () {
            fromAddProcsr = true;
            $scope.saveForm();
        };

        $scope.saveMailbox = function () {
        	
        	if (!$scope.isMailBoxEdit && $rootScope.serviceInstanceId == "") {
    			showSaveMessage("Mailbox creation is not allowed, and it is allowed when it traverses from a task", 'error');
    			return;
    		}
            $scope.confirmMailboxSave();
        };

        $scope.confirmMailboxSave = function() {
            fromAddProcsr = false;
            $scope.saveForm();
            $scope.valueSelectedinSelectionBox.value = '';
            $scope.showAddNew.value = false ;
        };

        $scope.closeconfirmMailboxDelete = function() {
            $('#confirmMailboxDelete').modal('hide');
        };

        // Property grid

        $scope.valueSelectedinSelectionBox = {
            value: ''
        };

       /* $scope.valueSelectedinSelectionBox = {
            name: '', id: ''
        };*/

        $scope.showAddNew = {
            value: 'false'
        };

        $scope.addedProperty = {
            value: ''
        };

        $scope.tableValues = 'Not showing anything yet';

        $scope.gridOptionsForMailbox = {
            data: 'mailBoxProperties',
            displaySelectionCheckbox: false,
            canSelectRows: false,
            enablePaging: false,
            enableRowSelection: false,
			enableColumnResize : true,
			plugins: [new ngGridFlexibleHeightPlugin()],
            showFooter: false,
            rowHeight: 80,
            columnDefs: [{
                field: "name",
                width: 568,
                displayName: "Property Name",
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} add-new="showAddNew" added-property="addedProperty" />'
            }, {
                field: "value",
                width: 495,
                displayName: "Property Value",
                enableCellEdit: false,
                enableCellSelection: true,
                enableFocusedCellEdit: true,
                cellTemplate: '<div ng-switch on="getId(allStaticProperties, row)">\n\
                                   <div class="alignDiv" ng-switch-when="">\n\
                                         <div ng-switch on="valueSelectedinSelectionBox.value.id">\n\
                                            <div ng-switch-when="emailnotificationids">\n\
           										 <textarea class="form-control" ng-model="COL_FIELD" id="emailIds" ng-init="COL_FIELD=null" ng-input="COL_FIELD" name="emailnotificationids" style="width:94%;height: 45px" placeholder="required"/>\n\
                                            </div>\n\
											<div ng-switch-when="emailnotificationforslaviolation">\n\
												<div class="input-group-btn">\n\
														<select ng-model="notificationRequired" id="notificationRequired" ng-change="onNotificationChange(notificationRequired)" ng-options="property.name for property in enumNotification"></select>\n\
													</div>\n\
											</div>\n\
											<div ng-switch-when="ttl">\n\
												<div class="input-group col-md-5 ttl_alignment">\n\
													<input class="form-control" id="ttlField" name="ttl" ng-model="COL_FIELD" ng-input="COL_FIELD"/>\n\
													<div class="input-group-btn">\n\
														<select ng-model="ttlUnit" id="ttlUnit" class="btn btn-default" ng-options="timeunit.name for timeunit in ttlDropdownValues" ng-change="onTTLUnitChanged(ttlUnit)"/>\n\
													</div>\n\
												</div>\n\
											</div>\n\
                                            <div ng-switch-default>\n\
                                                <textarea class="form-control" ng-model="COL_FIELD" id="requiredFieldForProp" ng-input="COL_FIELD" ng-init="COL_FIELD=null" style="width:94%;height:45px" placeholder="required"/>\n\
                                            </div>\n\
                                          </div>\n\
                                    </div>\n\
								   <div ng-switch-when="emailnotificationids">\n\
           								 <textarea class="form-control" ng-model="COL_FIELD" id="emailNotifyIds" ng-input="COL_FIELD" name="emailnotificationids" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.multipleEmailPattern + '" />\n\
          								  <div ng-show="formAddMbx.emailnotificationids.$dirty && formAddMbx.emailnotificationids.$invalid">\n\
          								     <span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.emailnotificationids.$error.required}" ng-show=formAddMbx.emailnotificationids.$error.required><strong>Email is mandatory</strong></span>\n\
            								 <span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.emailnotificationids.$error.pattern}" ng-show=formAddMbx.emailnotificationids.$error.pattern><strong>Invalid Email address</strong></span>\n\
           								 </div>\n\
           						   </div>\n\
                                   <div ng-switch-when="timetopickupfilepostedtomailbox">\n\
     								 <textarea class="form-control" ng-model="COL_FIELD" id="timeToPickupMBX" ng-input="COL_FIELD" name="timetopickupfilepostedtomailbox" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
    								 <div ng-show="formAddMbx.timetopickupfilepostedtomailbox.$dirty && formAddMbx.timetopickupfilepostedtomailbox.$invalid">\n\
    								    <span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.timetopickupfilepostedtomailbox.$error.required}" ng-show=formAddMbx.timetopickupfilepostedtomailbox.$error.required><strong>Time to Pickup File Posted to Mailbox is mandatory</strong></span>\n\
     								 	<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.timetopickupfilepostedtomailbox.$error.pattern}" ng-show=formAddMbx.timetopickupfilepostedtomailbox.$error.pattern><strong>Enter Valid Number</strong></span>\n\
     								 </div>\n\
     							   </div>\n\
                                   <div ng-switch-when="timetopickupfilepostedbymailbox">\n\
     								 <textarea class="form-control" ng-model="COL_FIELD" id="timeToPickupByMBX" ng-input="COL_FIELD" name="timetopickupfilepostedbymailbox" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
     								 	<div ng-show="formAddMbx.timetopickupfilepostedbymailbox.$dirty && formAddMbx.timetopickupfilepostedbymailbox.$invalid">\n\
     								 	    <span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.timetopickupfilepostedbymailbox.$error.required}" ng-show=formAddMbx.timetopickupfilepostedbymailbox.$error.required><strong>Time to Pickup File Posted By Mailbox is mandatory</strong></span>\n\
     								 		<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.timetopickupfilepostedbymailbox.$error.pattern}" ng-show=formAddMbx.timetopickupfilepostedbymailbox.$error.pattern><strong>Enter Valid Number</strong></span>\n\
     								 	</div>\n\
									</div>\n\
     								<div ng-switch-when="maxnumberofnotificationforslaviolation">\n\
									 <textarea class="form-control" ng-model="COL_FIELD" id="maxNotifySLA" ng-input="COL_FIELD" name="maxnumberofnotificationforslaviolation" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
										<div ng-show="formAddMbx.maxnumberofnotificationforslaviolation.$dirty && formAddMbx.maxnumberofnotificationforslaviolation.$invalid">\n\
											<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.maxnumberofnotificationforslaviolation.$error.required}" ng-show=formAddMbx.maxnumberofnotificationforslaviolation.$error.required><strong>Email Notification for SLA violation is mandatory</strong></span>\n\
											<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.maxnumberofnotificationforslaviolation.$error.pattern}" ng-show=formAddMbx.maxnumberofnotificationforslaviolation.$error.pattern><strong>Enter Valid Number</strong></span>\n\
										</div>\n\
									</div>\n\
     								<div ng-switch-when="maxnumberoflensnotificationforuploaderfailure">\n\
									 <textarea class="form-control" ng-model="COL_FIELD" id="lensFailNotifyCount" ng-input="COL_FIELD" name="maxnumberoflensnotificationforuploaderfailure" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
										<div ng-show="formAddMbx.maxnumberoflensnotificationforuploaderfailure.$dirty && formAddMbx.maxnumberoflensnotificationforuploaderfailure.$invalid">\n\
											<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.maxnumberoflensnotificationforuploaderfailure.$error.required}" ng-show=formAddMbx.maxnumberoflensnotificationforuploaderfailure.$error.required><strong>Lens Failure Notification Count is mandatory</strong></span>\n\
											<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.maxnumberoflensnotificationforuploaderfailure.$error.pattern}" ng-show=formAddMbx.maxnumberoflensnotificationforuploaderfailure.$error.pattern><strong>Enter Valid Number</strong></span>\n\
										</div>\n\
									</div>\n\
										<div ng-switch-when="emailnotificationforslaviolation">\n\
											<div class="input-group-btn">\n\
													<select ng-model="notificationRequired" id="notificationRequired" ng-change="onNotificationChange(notificationRequired)" ng-options="property.name for property in enumNotification"></select>\n\
												</div>\n\
										</div>\n\
										<div ng-switch-when="ttl">\n\
										<div class="input-group col-md-5 ttl_alignment">\n\
											<input class="form-control" id="ttlField" name="ttl" ng-model="COL_FIELD" ng-input="COL_FIELD" required ng-pattern="' + $scope.numberPattern + '" />\n\
											<div class="input-group-btn">\n\
												<select ng-model="ttlUnit" id="ttlUnit" class="btn btn-default" ng-options="timeunit.name for timeunit in ttlDropdownValues" ng-change="onTTLUnitChanged(ttlUnit)"/>\n\
											</div>\n\
										</div>\n\
										<div class = clearfix></div>\n\
										<div class="col-md-5" ng-show="formAddMbx.ttl.$dirty && formAddMbx.ttl.$invalid">\n\
										     <span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.ttl.$error.required}" ng-show=formAddMbx.ttl.$error.required><strong>TTL is mandatory</strong></span>\n\
							 		         <span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.ttl.$error.pattern}" ng-show=formAddMbx.ttl.$error.pattern><strong>\Enter Valid Number</strong></span>\n\
							 	        </div>\n\
									</div>\n\
     							  <div ng-switch-default>\n\
                                        <textarea class="form-control" ng-model="COL_FIELD" id="requiredFieldForValue" ng-input="COL_FIELD" required style="width:94%;height:45px" ng-maxLength=512 placeholder="required"/>\n\
                                    </div>\n\
                                  </div>'

            }, {
                field: "allowAdd",
                width: 75,
                displayName: "Action",
                sortable: false,
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true"><button id="addBtn" ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties,addedProperty)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
					'<div ng-switch-when="false"><button id="delBtn" ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                    '</div>'

            }]
        };

        // adds a resource to the 'data' object
        $scope.addRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData, addedProperty) {

            if (valueSelectedinSelectionBox.value === null) {
                showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                return;
            }

            // validation

            //$log.info(valueSelectedinSelectionBox.value.id);
            //$log.info(row.getProperty('value'));

            var attrName = '';
                      
            if (valueSelectedinSelectionBox.value.id !== 'add new -->') {

                attrName = valueSelectedinSelectionBox.value.name;
            } else if (addedProperty.value !== '') {
                attrName = addedProperty.value;
            }
			if (attrName === 'Email Notification for SLA violation') {
			  row.entity.value = $scope.notificationRequiredValue;
			}			
            if (!attrName || !row.getProperty('value')) {
                showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                return;
            }
            if (attrName.length > 128) {
				showAlert('Property  Name cannot be longer than 128 characters.', 'information');
                return;
            } 
			
			if (row.getProperty('value').length > 512) {
			   showAlert('Property  Value cannot be longer than 512 characters.', 'information');
                return;			
			}   
			
			// To allow only numeric value
			if(valueSelectedinSelectionBox.value.id === 'timetopickupfilepostedtomailbox' || valueSelectedinSelectionBox.value.id === 'timetopickupfilepostedbymailbox' || valueSelectedinSelectionBox.value.id ==='ttl' || valueSelectedinSelectionBox.value.id === 'maxnumberoflensnotificationforuploaderfailure' || valueSelectedinSelectionBox.value.id === 'maxnumberofnotificationforslaviolation') {
				if (!($scope.numberPattern.test(row.getProperty('value')))) {
					showAlert('Value should be a number.', 'error');
					return;
				}
			}
          
             // To allow only proper mail Id values for property emailnotificationids
            if (valueSelectedinSelectionBox.value.id == 'emailnotificationids') {
                   if (!($scope.multipleEmailPattern.test(row.getProperty('value')))) {
                        showAlert('Invalid Email address.', 'error');
                        return;
                    }
            }
            if (checkNameDuplicate(gridData, attrName)) {

                showAlert('Name already added.', 'error');
                return;
            }

            var indexOfSelectedElement = getIndex(allPropsWithNovalue, attrName);

            // Displays an alert if the dynamic property entered by user is already in static properties provided
            if ((valueSelectedinSelectionBox.value.id === 'add new -->') && (indexOfSelectedElement != -1) ) {
                showAlert('The property is already available in dropdown provided.Please use the appropriate property from dropdown menu','error');
                return;
            }

            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);

            gridData.push({
                name: attrName,
                value: row.getProperty('value'),
                allowAdd: false
            });

            if (indexOfSelectedElement != -1) {
                allPropsWithNovalue.splice(indexOfSelectedElement, 1);
            }

            //}
            gridData.push({
                name: '',
                value: '',
                allowAdd: true
            });

            valueSelectedinSelectionBox.value = '';
            addedProperty.value = '';

        };

        $scope.removeRow = function (row, allProps, allStaticPropertiesThatAreNotAssignedValuesYet, gridData) {
            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            var removedProperty = row.getProperty('name');
            var indexOfSelectedElement = getIndex(allProps, removedProperty);
            if (indexOfSelectedElement != -1) {
                allStaticPropertiesThatAreNotAssignedValuesYet.push(allProps[indexOfSelectedElement]);
            }
        };
        
          $scope.getId = function(objArray, row) {

            if (row.getProperty('name') === '') {
                return '';
            }
            var val = getId(objArray, row.getProperty('name'));
            if (val.length > 0) {
                return val;
            }

            return "Dor%^7#@"
        };
        
        $scope.onTenancyKeySelected = function(tenancyKey) {
            $scope.mailBox.tenancyKey = tenancyKey.guid;
			$scope.isEnable = true;			
        };
        
		
		$scope.onTTLUnitChanged = function(ttl) {
			selectTTLUnit(ttl.name);
		};
		
		$scope.toggleWarningEnabler = function(value) {
			$scope.isMailboxNameChanged = value;
		}
}]);
