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

        var block = $blockUI.createBlockUI();
        var fromAddProcsr = false;

        $scope.mailBox.status = $scope.enumstats[0];

        //Data from server - YOU HAVE TO JUST ADD 'add new -->' manually to the list from server.
        $scope.allStaticPropertiesThatAreNotAssignedValuesYet = ['add new -->', 'filerenameformat'];
        //Data from server
        $scope.allStaticProperties = ['filerenameformat'];

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
                $log.info($filter('json')(editReq));

                $scope.restService.put($scope.base_url + "/" + $scope.mailBoxId, $filter('json')(editReq),
                    function (data, status) {

                        if (status === 200) {

                            if (fromAddProcsr) {
                                $location.$$search = {};
                                $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId).search('mbxname', $scope.mailBox.name);
                            } else alert(data.reviseMailBoxResponse.response.message);
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
                            } else alert(data.addMailBoxResponse.response.message);

                            $scope.isMailBoxEdit = true;
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

        $scope.addedProperty = 'add new';
        $scope.disableAddNewTextBox = 'true';
        $scope.valueSelectedinSelectionBox = {
            name: ''
        };
        $scope.tableValues = 'Not showing anything yet';

        $scope.gridOptionsForMailbox = {
            data: 'mailBoxProperties',
            displaySelectionCheckbox: false,
            canSelectRows: false,
            enablePaging: false,
            showFooter: false,
            useExternalSorting: true,
            rowHeight: 40,
            columnDefs: [{
                field: "name",
                width: 500,
                displayName: "Property Name",
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} />'
            }, {
                field: "value",
                displayName: "Property Value",
                enableCellEdit: false,
                enableCellSelection: true,
                enableFocusedCellEdit: true,
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')"><div ng-switch-when="true"><input type="text" ng-model="COL_FIELD"  required="" class="textboxingrid" placeholder="required"></div><div ng-switch-when="false"><input type="text" ng-model="COL_FIELD"  required="" class="textboxingrid" placeholder="required" readonly></div></div>'

            }, {
                field: "allowAdd",
                displayName: "Action",
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true"><button ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                    '<div ng-switch-when="false"><button ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                    '</div>'

            }]
        };


        // adds a resource to the 'data' object
        $scope.addRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData) {

            // validation

            $log.info(valueSelectedinSelectionBox.name);
            $log.info(row.getProperty('value'));

            if (!valueSelectedinSelectionBox.name || valueSelectedinSelectionBox.name === 'add new -->' || !row.getProperty('value')) {
                showAlert('It is mandatory to set the name and value of the property being added.');
                return;
            }

            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            gridData.push({
                name: valueSelectedinSelectionBox.name,
                value: row.getProperty('value'),
                allowAdd: false
            });
            var indexOfSelectedElement = allPropsWithNovalue.indexOf(valueSelectedinSelectionBox.name);

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

        };

        $scope.removeRow = function (row, allProps, allPropsWithNovalue, gridData) {
            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            var removedProperty = row.getProperty('name');
            var indexOfSelectedElement = allProps.indexOf(removedProperty);
            if (indexOfSelectedElement > -1) {
                allPropsWithNovalue.push(removedProperty);
            }
        };



        /*$scope.displayAllTableValues = function(){               
                $scope.tableValues=$scope.mailBoxProperties;
            };*/

    }
]);