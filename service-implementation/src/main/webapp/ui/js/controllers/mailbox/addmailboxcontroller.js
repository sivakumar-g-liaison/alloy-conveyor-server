var rest = myApp.controller('AddMailBoxCntrlr', ['$rootScope', '$scope', '$filter', '$location', '$log', '$modal', '$blockUI', '$http',
    function ($rootScope, $scope, $filter, $location, $log, $modal, $blockUI, $http) {

        //Remove if not needed
        $scope.isMailBoxEdit = false;

		$scope.addProcessorBtnValue = 'Add Processors';
		
		$scope.isProcessorsAvailable = false;
		$scope.isMailBoxSaved = false;
		
		 $scope.enumTenancyKey = [];
	     $scope.tenancyKey = {guid:'', name:''};
	     $scope.tenancyKeys = [];
		
        $scope.showMailboxGuid = false;
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
            properties: []
        };
        $scope.enumstats = [{"name":"Active","id":"ACTIVE"},
            {"name":"Inactive","id":"INACTIVE"}];
        
        // Default values of payloadsize and no of files threshold
        $scope.payloadSizeThreshold = 131072;
        $scope.numberOfFilesThreshold = 10

        var block = $rootScope.block;
        var fromAddProcsr = false;

        $scope.status = $scope.enumstats[0];
        
        $scope.mailboxPguidDisplayContent = '';

        //Data from server - YOU HAVE TO JUST ADD 'add new -->' manually to the list from server.
        $scope.allStaticPropertiesThatAreNotAssignedValuesYet = [{"name":"add new -->","id":"add new -->"},
            {"name":"Email Notification Ids","id":"emailnotificationids"}, {"name":"Time to Pickup File Posted to Mailbox","id":"timetopickupfilepostedtomailbox"}, {"name":"Time to Pickup File Posted By Mailbox","id":"timetopickupfilepostedbymailbox"}];

        $scope.allStaticProperties = [{"name":"Email Notification Ids","id":"emailnotificationids"}, {"name":"Time to Pickup File Posted to Mailbox","id":"timetopickupfilepostedtomailbox"}, {"name":"Time to Pickup File Posted By Mailbox","id":"timetopickupfilepostedbymailbox"}];
		//Data from server
        $scope.mailBoxProperties = [{
            name: '',
            value: '',
            allowAdd: true
        }];
        
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
                                 console.log("tenancyKeys"+$scope.tenancyKeys);  
                                
                             } else {
                                 showSaveMessage(data.getTenancyKeysResponse.response.message, 'error');
                                
                             }

                         } else {
                             showSaveMessage("Error while retrieving tenancykeys", 'error');
                             
                         }
                        
        	 });               
        };
        $scope.getTenancyKeys();
        
        // Loads the details initially if edit
        $scope.load = function () {

            if ($location.search().mailBoxId !== '' && typeof $location.search().mailBoxId !== 'undefined') { // Edit Mode On

                $scope.isMailBoxEdit = true;
                $scope.mailBoxId = $location.search().mailBoxId;
                block.blockUI();
                $scope.restService.get($scope.base_url + "/" + $scope.mailBoxId+ '?addServiceInstanceIdConstraint=' + true + '&sid=' + $rootScope.serviceInstanceId, //Get mail box Data
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200 || status === 400) {                        	
                        	if (data.getMailBoxResponse.response.status === 'success') {
                                $scope.mailBox.guid = $scope.mailBoxId;
                                $scope.mailBox.name = data.getMailBoxResponse.mailBox.name;
                                $scope.mailBox.description = data.getMailBoxResponse.mailBox.description;
                                $scope.showMailboxGuid = true;
                                $scope.mailboxPguidDisplayContent = ($rootScope.javaProperties.mailboxPguidDisplayPrefix != null && $rootScope.javaProperties.mailboxPguidDisplayPrefix != '')?
                                                                     $rootScope.javaProperties.mailboxPguidDisplayPrefix + $scope.mailBoxId: $scope.mailBoxId;
        						if(data.getMailBoxResponse.mailBox.processors.length > 0) {
        							
        							$scope.addProcessorBtnValue = 'List Processors';
        							$scope.isProcessorsAvailable = true;
        						}	else {
        							$scope.addProcessorBtnValue = 'Add Processors';
        							$scope.isProcessorsAvailable = false;
        						}
                                (data.getMailBoxResponse.mailBox.status === 'ACTIVE' ||
                                    data.getMailBoxResponse.mailBox.status === 'INCOMPLETE') ? $scope.status = $scope.enumstats[0] : $scope.status = $scope.enumstats[1];
                                $scope.mailBox.shardKey = data.getMailBoxResponse.mailBox.shardKey;
                                $scope.tenancyKey.name = data.getMailBoxResponse.mailBox.tenancyKeyDisplayName;
                                $scope.mailBox.tenancyKey = data.getMailBoxResponse.mailBox.tenancyKey;
                                $scope.mailBoxProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                                for (var i = 0; i < data.getMailBoxResponse.mailBox.properties.length; i++) {
                                					                          
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
            if ($scope.isMailBoxEdit) {

                $scope.editReq.reviseMailBoxRequest.mailBox = $scope.mailBox;
                $scope.editReq.reviseMailBoxRequest.mailBox.guid = $scope.mailBoxId;
                $scope.editReq.reviseMailBoxRequest.mailBox.status = $scope.status.id;

                $log.info($filter('json')(editReq));

                $scope.restService.put($scope.base_url + "/" + $scope.mailBoxId + "?sid=" +$rootScope.serviceInstanceId, $filter('json')(editReq),
                    function (data, status) {
                        
                	    block.unblockUI();
                        if (status === 200 || status === 400) {
                             $scope.isMailBoxSaved = true;
                            if (fromAddProcsr) {
                                $location.$$search = {};
                                $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name);
                            } else if (data.reviseMailBoxResponse.response.status === 'success') {
                                showSaveMessage(data.reviseMailBoxResponse.response.message, 'success');
                            } else {
                                showSaveMessage(data.reviseMailBoxResponse.response.message, 'error');
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
                $log.info($filter('json')(addRequest));

                $scope.restService.post($scope.base_url + '?sid=' + $rootScope.serviceInstanceId, $filter('json')(addRequest),
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200 || status === 400) {

                        	if(data.addMailBoxResponse.mailBox !== null) {
                            	$scope.mailBoxId = data.addMailBoxResponse.mailBox.guid;
                                $scope.showMailboxGuid = true;
                                $scope.mailboxPguidDisplayContent = ($rootScope.javaProperties.mailboxPguidDisplayPrefix != null && $rootScope.javaProperties.mailboxPguidDisplayPrefix != '')?
                                                     $rootScope.javaProperties.mailboxPguidDisplayPrefix + $scope.mailBoxId: $scope.mailBoxId;
                        	}

                        	if (data.addMailBoxResponse.response.status === 'success') {
                        		$scope.isMailBoxSaved = true;
								if (fromAddProcsr) {
									$location.$$search = {};
									$location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name);
								} else {
									showSaveMessage(data.addMailBoxResponse.response.message, 'success');
									$scope.isMailBoxEdit = true;
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
            $location.path('/mailbox/getMailBox');
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
            	$location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name);
            }
        }
        
        // method to close mailbox saving confirmation dialog
        $scope.closeMailboxConfirmationModal = function() {
       	 $('#saveMailboxConfirmationModal').modal('hide');
            $location.$$search = {};
            $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name);
           
       }

        $scope.addProcessor = function () {
            fromAddProcsr = true;
            $scope.saveForm();
        };

        $scope.saveMailbox = function () {
            fromAddProcsr = false;
            $scope.saveForm();
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
                width: 550,
                displayName: "Property Name",
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} add-new="showAddNew" added-property="addedProperty" />'
            }, {
                field: "value",
                width: 500,
                displayName: "Property Value",
                enableCellEdit: false,
                enableCellSelection: true,
                enableFocusedCellEdit: true,
                cellTemplate: '<div ng-switch on="getId(allStaticProperties, row)">\n\
                                   <div class="alignDiv" ng-switch-when="">\n\
                                         <div ng-switch on="valueSelectedinSelectionBox.value.id">\n\
                                            <div ng-switch-when="emailnotificationids">\n\
           										 <textarea class="form-control" ng-model="COL_FIELD" ng-init="COL_FIELD=null" ng-input="COL_FIELD" name="emailnotificationids" style="width:94%;height: 45px" placeholder="required"/>\n\
                                            </div>\n\
                                            <div ng-switch-default>\n\
                                                <textarea class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD=null" style="width:94%;height:45px" placeholder="required"/>\n\
                                            </div>\n\
                                          </div>\n\
                                    </div>\n\
                                   <div ng-switch-when="emailnotificationids">\n\
           								 <textarea class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" name="emailnotificationids" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.multipleEmailPattern + '" />\n\
          								  <div ng-show="formAddMbx.emailnotificationids.$dirty && formAddMbx.emailnotificationids.$invalid">\n\
            								 <span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.emailnotificationids.$error.pattern}" ng-show=formAddMbx.emailnotificationids.$error.pattern><strong>Invalid Email address</strong></span>\n\
           								 </div>\n\
           						   </div>\n\
                                   <div ng-switch-when="timetopickupfilepostedtomailbox">\n\
     								 <textarea class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" name="timetopickupfilepostedtomailbox" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
    								 <div ng-show="formAddMbx.timetopickupfilepostedtomailbox.$dirty && formAddMbx.timetopickupfilepostedtomailbox.$invalid">\n\
     								 	<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.timetopickupfilepostedtomailbox.$error.pattern}" ng-show=formAddMbx.timetopickupfilepostedtomailbox.$error.pattern><strong>Enter Valid Number</strong></span>\n\
     								 </div>\n\
     							   </div>\n\
                                   <div ng-switch-when="timetopickupfilepostedbymailbox">\n\
     								 <textarea class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" name="timetopickupfilepostedbymailbox" required ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
     								 	<div ng-show="formAddMbx.timetopickupfilepostedbymailbox.$dirty && formAddMbx.timetopickupfilepostedbymailbox.$invalid">\n\
     								 		<span class="customHide" ng-class="{\'help-block-custom\':formAddMbx.timetopickupfilepostedbymailbox.$error.pattern}" ng-show=formAddMbx.timetopickupfilepostedbymailbox.$error.pattern><strong>Enter Valid Number</strong></span>\n\
     								 	</div></div>\n\
     							  <div ng-switch-default>\n\
                                        <textarea class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" required style="width:94%;height:45px" ng-maxLength=512 placeholder="required"/>\n\
                                    </div>\n\
                                  </div>'

            }, {
                field: "allowAdd",
                width: 88,
                displayName: "Action",
                sortable: false,
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true"><button ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties,addedProperty)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
					'<div ng-switch-when="false"><button ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
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

            $log.info(valueSelectedinSelectionBox.value.id);
            $log.info(row.getProperty('value'));

            var attrName = '';
                      
            if (valueSelectedinSelectionBox.value.id !== 'add new -->') {

                attrName = valueSelectedinSelectionBox.value.name;
            } else if (addedProperty.value !== '') {
                attrName = addedProperty.value;
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
			if(valueSelectedinSelectionBox.value.id === 'timetopickupfilepostedtomailbox' || valueSelectedinSelectionBox.value.id === 'timetopickupfilepostedbymailbox') {
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
            $scope.tenancyKey = angular.copy(tenancyKey);
            $scope.mailBox.tenancyKey = tenancyKey.guid;
        };
        
}]);