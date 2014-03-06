var rest = myApp.controller('AddMailBoxCntrlr', ['$rootScope', '$scope', '$filter', '$location', '$log', '$blockUI',
    function ($rootScope, $scope, $filter, $location, $log, $blockUI) {

        //Remove if not needed
        $scope.isMailBoxEdit = false;

		if($rootScope.pipelineId === null || $rootScope.pipelineId === '') {
			$rootScope.pipelineId = $location.search().pipeLineId;
		}
		
		//for pipeLineId
		$scope.pipeId = $location.search().pipeLineId; 

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
            properties: [],
            serviceInstanceId: $rootScope.serviceInstancePrimaryId
        };

        $scope.enumstats = [{"name":"Active","id":"ACTIVE"},
            {"name":"Inactive","id":"INACTIVE"}];
        
        // Default values of payloadsize and no of files threshold
        $scope.payloadSizeThreshold = 131072;
        $scope.numberOfFilesThreshold = 10

        var block = $blockUI.createBlockUI();
        var fromAddProcsr = false;

        $scope.status = $scope.enumstats[0];

        //Data from server - YOU HAVE TO JUST ADD 'add new -->' manually to the list from server.

        $scope.allStaticPropertiesThatAreNotAssignedValuesYet = [{"name":"add new -->","id":"add new -->"},
            {"name":"Email Notification Ids","id":"emailnotificationids"}];

        $scope.allStaticProperties = [{"name":"Email Notification Ids","id":"emailnotificationids"}];
        //Data from server
        $scope.mailBoxProperties = [{
            name: '',
            value: '',
            allowAdd: true
        }];

        // Loads the details initially if edit
        $scope.load = function () {

            if ($location.search().mailBoxId !== '' && typeof $location.search().mailBoxId !== 'undefined') { // Edit Mode On

                $scope.isMailBoxEdit = true;
                $scope.mailBoxId = $location.search().mailBoxId;
                //$scope.sharedService.setProperty('test');

                block.blockUI();
                $scope.restService.get($scope.base_url + "/" + $scope.mailBoxId+ '?serviceInstanceId=' + $rootScope.serviceInstancePrimaryId+ '&addServiceInstanceIdConstraint=' + false, //Get mail box Data
                    function (data) {

                        block.unblockUI();
                        $scope.mailBox.guid = $scope.mailBoxId;
                        $scope.mailBox.name = data.getMailBoxResponse.mailBox.name;
                        $scope.mailBox.description = data.getMailBoxResponse.mailBox.description;
                        (data.getMailBoxResponse.mailBox.status === 'ACTIVE' ||
                            data.getMailBoxResponse.mailBox.status === 'INCOMPLETE') ? $scope.status = $scope.enumstats[0] : $scope.status = $scope.enumstats[1];
                        $scope.mailBox.shardKey = data.getMailBoxResponse.mailBox.shardKey;
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
                        };
                        $scope.mailBoxProperties.push({ //Adding now so that the add new option always shows below the available properties
                            name: '',
                            value: '',
                            allowAdd: true
                        });
                    }
                );
            }
        };

        $scope.load();

        $scope.saveForm = function () {

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

                $scope.restService.put($scope.base_url + "/" + $scope.mailBoxId, $filter('json')(editReq),
                    function (data, status) {

                        if (status === 200) {

                            if (fromAddProcsr) {
                                $location.$$search = {};
                                $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name).search('pipeLineId', $scope.pipeId);
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

                block.blockUI();
                $scope.restService.post($scope.base_url, $filter('json')(addRequest),
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200) {

                            $scope.mailBoxId = data.addMailBoxResponse.mailBox.guid;

                            if (fromAddProcsr) {
                                $location.$$search = {};
                                $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name).search('pipeLineId', $scope.pipeId);
                            } else if (data.addMailBoxResponse.response.status === 'success') {
                                showSaveMessage(data.addMailBoxResponse.response.message, 'success');
                            } else {
                                showSaveMessage(data.addMailBoxResponse.response.message, 'error');
                            }

                            $scope.isMailBoxEdit = true;

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
            showFooter: false,
            rowHeight: 80,
            columnDefs: [{
                field: "name",
                width: 525,
                displayName: "Property Name",
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} add-new="showAddNew" added-property="addedProperty" />'
            }, {
                field: "value",
                width: 525,
                displayName: "Property Value",
                enableCellEdit: false,
                enableCellSelection: true,
                enableFocusedCellEdit: true,
                cellTemplate: '<div ng-switch on="getId(allStaticProperties, row)">\n\
                                   <div class="alignDiv" ng-switch-when="">\n\
                                         <div ng-switch on="valueSelectedinSelectionBox.value.id">\n\
                                            <div ng-switch-when="emailnotificationids">\n\
           										 <textarea class="form-control" ng-model="COL_FIELD" name="emailnotificationids" style="width:94%;height: 45px" placeholder="required"/>\n\
                                            </div>\n\
                                            <div ng-switch-default>\n\
                                                <textarea class="form-control" ng-model="COL_FIELD" ng-init="COL_FIELD=null" style="width:94%;height:45px" placeholder="required"/>\n\
                                            </div>\n\
                                          </div>\n\
                                    </div>\n\
                                   <div ng-switch-when="emailnotificationids">\n\
           								 <textarea class="form-control" ng-model="COL_FIELD" name="emailnotificationids" ng-maxLength=512 style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.multipleEmailPattern + '" />\n\
          								  <div ng-show="formAddMbx.emailnotificationids.$dirty && formAddMbx.emailnotificationids.$invalid">\n\
            								 <span class="help-block-custom" ng-show=formAddMbx.emailnotificationids.$error.pattern><strong>Invalid Email address</strong></span>\n\
           								 </div>\n\
           						   </div>\n\
                                   <div ng-switch-default>\n\
                                        <textarea class="form-control" ng-model="COL_FIELD"  required style="width:94%;height:45px" ng-maxLength=512 placeholder="required"/>\n\
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
            }   var val = getId(objArray, row.getProperty('name'));
            if (val.length > 0) {
                return val;
            }

            return "Dor%^7#@"
        };
        /*$scope.displayAllTableValues = function(){               
         $scope.tableValues=$scope.mailBoxProperties;
         };*/

    }
]);