var rest = myApp.controller('AddMailBoxCntrlr', ['$scope', '$filter', '$location', '$log',
    function ($scope, $filter, $location, $log) {

        //Remove if not needed
        $scope.isMailBoxEdit = false;
        //Needed only for Edir
        $scope.procsBtnStatus = true;
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

        addRequest.addMailBoxRequest.mailBox.status = $scope.enumstats[0];

        //Data from server - YOU HAVE TO JUST ADD 'add new -->' manually to the list from server.
        $scope.allStaticPropertiesThatAreNotAssignedValuesYet = ['add new -->', 'assignuser', 'inputfolder', 'globalstatus', 'filerenameformat', 'retrycountglobal'];
        //Data from server
        $scope.allStaticProperties = ['assignuser', 'inputfolder', 'globalstatus', 'filerenameformat', 'retrycountglobal'];

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
                $scope.procsBtnStatus = false;
                $scope.mailBoxId = $location.search().mailBoxId;
                //$scope.sharedService.setProperty('test');
                $scope.restService.get($scope.base_url + "/"+ $scope.mailBoxId, //Get mail box Data
                    function (data) {
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
                            $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
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
                
                $scope.restService.put($scope.base_url + "/"+ $scope.mailBoxId, $filter('json')(editReq),
                    function (data, status) {

                        if (status === 200) {
                            alert(data.reviseMailBoxResponse.response.message);
                        }
                        $scope.mailBox.properties = [];
                    }
                );
            } else {
                $scope.addRequest.addMailBoxRequest.mailBox = $scope.mailBox;
                $log.info($filter('json')(addRequest));
                $scope.restService.post($scope.base_url, $filter('json')(addRequest),
                    function (data, status) {

                        if (status === 200) {
                            alert(data.addMailBoxResponse.response.message);
                            $scope.mailBoxId = data.addMailBoxResponse.mailBox.guid;
                            $scope.procsBtnStatus = false;
                        }
                        $scope.mailBox.properties = [];

                    }
                );
            }
        };

        $scope.doCancel = function () {
            var response = confirm("Are you  sure you want to cancel the Operation?");
            if (response === true) {
                $location.path('/mailbox/getMailBox');
            }
        };

        $scope.addProcessor = function () {

            $location.path('/mailbox/processor').search('mailBoxId', $scope.mailBoxId);
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
            rowHeight: 40,
            columnDefs: [{
                field: "name",
                width: 500,
                displayName: "Property Name",
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} />'
            }, {
                field: "value",
                displayName: "Property Value",
                enableCellEdit: true,
                enableCellSelection: true,
                enableFocusedCellEdit: true
            }, {
                field: "allowAdd",
                displayName: "Action",
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true"><button ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties)">add</button></div>' +
                    '<div ng-switch-when="false"><button ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,mailBoxProperties)">remove</button></div>' +
                    '</div>'

            }]
        };


        // adds a resource to the 'data' object
        $scope.addRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData) {

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