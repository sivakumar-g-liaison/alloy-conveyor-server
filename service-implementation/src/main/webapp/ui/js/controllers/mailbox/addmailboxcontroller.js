var rest = myApp.controller('AddMailBoxCntrlr', ['$scope', '$filter', '$location', '$log', '$blockUI',
    function ($scope, $filter, $location, $log, $blockUI) {

        //Remove if not needed
        $scope.isMailBoxEdit = false;



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
            properties: []
        };

        $scope.enumstats = [
            'ACTIVE',
            'INACTIVE'
        ];
        
        // Default values of payloadsize and no of files threshold
        $scope.payloadSizeThreshold = 131072;
        $scope.numberOfFilesThreshold = 10

        var block = $blockUI.createBlockUI();
        var fromAddProcsr = false;

        $scope.mailBox.status = $scope.enumstats[0];

        //Data from server - YOU HAVE TO JUST ADD 'add new -->' manually to the list from server.
        $scope.allStaticPropertiesThatAreNotAssignedValuesYet = ['add new -->', 'filerenameformat', 'emailnotificationids', 'sweepedfilelocation', 'payloadsizethreshold', 'numoffilesthreshold'];
        //Data from server
        $scope.allStaticProperties = ['filerenameformat', 'emailnotificationids', 'sweepedfilelocation', 'payloadsizethreshold', 'numoffilesthreshold'];

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
                $scope.restService.get($scope.base_url + "/" + $scope.mailBoxId, //Get mail box Data
                    function (data) {

                        block.unblockUI();
                        $scope.mailBox.guid = $scope.mailBoxId;
                        $scope.mailBox.name = data.getMailBoxResponse.mailBox.name;
                        $scope.mailBox.description = data.getMailBoxResponse.mailBox.description;
                        (data.getMailBoxResponse.mailBox.status === 'ACTIVE' ||
                            data.getMailBoxResponse.mailBox.status === 'INCOMPLETE') ? $scope.mailBox.status = $scope.enumstats[0] : $scope.mailBox.status = $scope.enumstats[1];
                        $scope.mailBox.shardKey = data.getMailBoxResponse.mailBox.shardKey;
                        $scope.mailBoxProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                        for (var i = 0; i < data.getMailBoxResponse.mailBox.properties.length; i++) {
                            $scope.mailBoxProperties.push({
                                name: data.getMailBoxResponse.mailBox.properties[i].name,
                                value: data.getMailBoxResponse.mailBox.properties[i].value,
                                allowAdd: false
                            });

                            var indexOfElement = $scope.allStaticPropertiesThatAreNotAssignedValuesYet.indexOf(data.getMailBoxResponse.mailBox.properties[i].name);
                            if (indexOfElement > 0) {
                                $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
                            }
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
                $scope.mailBox.properties.push({
                    name: $scope.mailBoxProperties[i].name,
                    value: $scope.mailBoxProperties[i].value,

                });
            }

            if ($scope.isMailBoxEdit) {

                $scope.editReq.reviseMailBoxRequest.mailBox = $scope.mailBox;
                $scope.editReq.reviseMailBoxRequest.mailBox.guid = $scope.mailBoxId;

                $log.info($filter('json')(editReq));

                $scope.restService.put($scope.base_url + "/" + $scope.mailBoxId, $filter('json')(editReq),
                    function (data, status) {

                        if (status === 200) {

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
                $log.info($filter('json')(addRequest));

                block.blockUI();
                $scope.restService.post($scope.base_url, $filter('json')(addRequest),
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200) {

                            $scope.mailBoxId = data.addMailBoxResponse.mailBox.guid;

                            if (fromAddProcsr) {
                                $location.$$search = {};
                                $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name);
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
            var resp = confirm("Are you  sure you want to cancel the Operation? All unsaved changes will be lost.");
            if (resp === true) {
                $location.$$search = {};
                $location.path('/mailbox/getMailBox');
            }
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
            name: ''
        };

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
                width: 500,
                displayName: "Property Name",
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} add-new="showAddNew" added-property="addedProperty" />'
            }, {
                field: "value",
                displayName: "Property Value",
                enableCellEdit: false,
                enableCellSelection: true,
                enableFocusedCellEdit: true,
                cellTemplate: '<div ng-switch on="row.getProperty(\'name\')">\n\
                                   <div ng-switch-when="">\n\
                                         <div ng-switch on="valueSelectedinSelectionBox.name">\n\
                                            <div ng-switch-when="payloadsizethreshold">\n\
                                                <textarea ng-model="COL_FIELD" ng-init="COL_FIELD=payloadSizeThreshold" style="width:94%;height:45px" ng-maxLength=512 placeholder="required" value=payloadSizeThreshold/>\n\
                                            </div>\n\
                                            <div ng-switch-when="numoffilesthreshold">\n\
                                                <textarea ng-model="COL_FIELD" ng-init="COL_FIELD=numberOfFilesThreshold"  style="width:94%;height:45px" ng-maxLength=512 placeholder="required" value=numberOfFilesThreshold/>\n\
                                            </div>\n\
                                            <div ng-switch-default>\n\
                                                <textarea ng-model="COL_FIELD" ng-init="COL_FIELD=null" style="width:94%;height:45px" ng-maxLength=512 placeholder="required"/>\n\
                                            </div>\n\
                                          </div>\n\
                                    </div>\n\
                                   <div ng-switch-default>\n\
                                        <textarea ng-model="COL_FIELD"  required style="width:94%;height:45px" ng-maxLength=512 placeholder="required"/>\n\
                                    </div>\n\
                                  </div>'

            }, {
                field: "allowAdd",
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

            // validation

            $log.info(valueSelectedinSelectionBox.name);
            $log.info(row.getProperty('value'));

            var attrName = '';

            if (valueSelectedinSelectionBox.name !== 'add new -->') {

                attrName = valueSelectedinSelectionBox.name;
            } else if (addedProperty.value !== '') {
                attrName = addedProperty.value;
            }

            if (!attrName || !row.getProperty('value')) {
                showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                return;
            }

            if (checkNameDuplicate(gridData, attrName)) {

                showAlert('Name already added.', 'error');
                return;
            }
      	  // Displays an alert if the dynamic property entered by user is already in static properties provided
            if ((valueSelectedinSelectionBox.name == 'add new -->') && ($scope.allStaticProperties.indexOf(attrName) > -1) ) {
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
            var indexOfSelectedElement = allPropsWithNovalue.indexOf(attrName);

            if (indexOfSelectedElement != -1) {
                allPropsWithNovalue.splice(indexOfSelectedElement, 1);
            }

            //}
            gridData.push({
                name: '',
                value: '',
                allowAdd: true
            });

            valueSelectedinSelectionBox.name = '';
            addedProperty.value = '';

        };

        $scope.removeRow = function (row, allProps, allStaticPropertiesThatAreNotAssignedValuesYet, gridData) {
            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            var removedProperty = row.getProperty('name');
            var indexOfSelectedElement = allProps.indexOf(removedProperty);
            if (indexOfSelectedElement > -1) {
                allStaticPropertiesThatAreNotAssignedValuesYet.push(removedProperty);
            }
        };

        /*$scope.displayAllTableValues = function(){               
         $scope.tableValues=$scope.mailBoxProperties;
         };*/

    }
]);