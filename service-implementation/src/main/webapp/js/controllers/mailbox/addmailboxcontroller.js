var rest = myApp.controller('AddMailBoxCntrlr', ['$scope', '$routeParams', '$http', '$filter', '$injector',
    function ($scope, $routeParams, $http, $filter, $injector) {

        req = $scope.request = {
            addMailBoxRequest: {
                mailBox: {
                    name: "",
                    description: "",
                    status: "",
                    properties: []
                }
            }
        };

        $scope.enumstats = [
            'ACTIVE',
            'INACTIVE'
        ];

        req.addMailBoxRequest.mailBox.status = $scope.enumstats[0];

        // Loads the details initially if edit
        $scope.load = function () {
            if ('test' !== $scope.sharedService.getProperty()) {

                $injector.get('RESTService').get($scope.base_url + 'mailbox/' + $scope.sharedService.getProperty(),
                    function (data) {
                        //alert(data.getMailBoxResponse.response.message);
                    }
                );

            }
        };
        $scope.load();


        $scope.saveForm = function (request) {

            //Iterating and Removing allowAdd property

            var removeProp = 'allowAdd';
            var len = request.addMailBoxRequest.mailBox.properties.length;

            for (var i = 0; i < len; i++) {

                delete request.addMailBoxRequest.mailBox.properties[i][removeProp];
                console.log(request.addMailBoxRequest.mailBox.properties[i]);
            }

            // For removing the final element which has addRow specific data

            request.addMailBoxRequest.mailBox.properties.splice(len - 1, 1);

            $injector.get('RESTService').post($scope.base_url + 'mailbox', $filter('json')(request),
                function (data, status) {
                    $(".alert").alert(data.addMailBoxResponse.response.message);
                    //alert(data.addMailBoxResponse.response.message);
                }
            );
        }

        $scope.doCancel = function () {

            //TODO
            // Need to customize dialog to "Yes" and "No" instead of "Ok" and "Cancel"

            var r = confirm("Are you sure about cancelling");

            if (r == true) {
                document.location.href = "#/mailbox/getMailBox";
            }
        }

        // Directive Section

        //PLEASE POPULATE THE FOLLOWING THREE PROPERTIES FROM SERVER.  

        //Data from server
        req.addMailBoxRequest.mailBox.properties = [{
            name: '',
            value: '',
            allowAdd: true
        }];
        //Data from server - YOU HAVE TO JUST ADD 'add new -->' manually to the list from server.
        $scope.allStaicPropertiesThatAreNotAssignedValuesYet = ['add new -->', 'assignuser', 'inputfolder', 'globalstatus', 'filerenameformat', 'retrycountglobal'];
        //Data from server
        $scope.allStaicProperties = ['assignuser', 'inputfolder', 'globalstatus', 'filerenameformat', 'retrycountglobal'];

        $scope.addedProperty = 'add new';
        $scope.disableAddNewTextBox = 'true';
        $scope.valueSelectedinSelectionBox;
        $scope.tableValues = 'Not showing anything yet';

        $scope.gridOptionsForMailbox = {
            data: 'request.addMailBoxRequest.mailBox.properties',
            displaySelectionCheckbox: false,
            canSelectRows: false,
            enablePaging: false,
            showFooter: false,
            rowHeight: 40,
            columnDefs: [{
                field: "name",
                width: 500,
                displayName: "Property Name",
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">' +
                    '<div ng-switch-when="false">{{row.getProperty(col.field)}}</div>' +
                    '<div ng-switch-when="true">\n\
                                                                 <select ng-change="shouldIShowAddTextBox(selectedproperty)" ng-model="selectedproperty" ng-options="property for property in allStaicPropertiesThatAreNotAssignedValuesYet">\n\
                                                                 <option value="">-- select--</option>\n\
                                                                 </select> <i>or</i>\n\
                                                                 <input type="text" ng-disabled=disableAddNewTextBox  ng-model="addedProperty" ng-change="setScopeValue(addedProperty)" required></input>\n\
                                                                 </div>'
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
                    '<div ng-switch-when="true"><button ng-click="addRow(row,selectedproperty)">add</button></div>' +
                    '<div ng-switch-when="false"><button ng-click="removeRow(row)">remove</button></div>' +
                    '</div>'

            }]
        };

        //GRID related functaions
        $scope.shouldIShowAddTextBox = function (selectedproperty) {
            $scope.valueSelectedinSelectionBox = selectedproperty;
            if (selectedproperty === "add new -->") {
                $scope.disableAddNewTextBox = false;
                $scope.addedProperty = '';
            } else {
                $scope.addedProperty = 'add new';
                $scope.disableAddNewTextBox = true;

            }
        };

        // adds a resource to the 'data' object
        $scope.addRow = function (row) {
            var index = req.addMailBoxRequest.mailBox.properties.indexOf(row.entity);
            req.addMailBoxRequest.mailBox.properties.splice(index, 1);
            if ($scope.addedProperty !== 'add new') {

                req.addMailBoxRequest.mailBox.properties.push({
                    name: $scope.addedProperty,
                    value: row.getProperty('value'),
                    allowAdd: false
                });
                $scope.addedProperty = 'add new';
                $scope.disableAddNewTextBox = true;
            } else {

                req.addMailBoxRequest.mailBox.properties.push({
                    name: $scope.valueSelectedinSelectionBox,
                    value: row.getProperty('value'),
                    allowAdd: false
                });
                var indexOfSelectedElement = $scope.allStaicPropertiesThatAreNotAssignedValuesYet.indexOf($scope.valueSelectedinSelectionBox);
                $scope.allStaicPropertiesThatAreNotAssignedValuesYet.splice(indexOfSelectedElement, 1);
            }
            req.addMailBoxRequest.mailBox.properties.push({
                name: '',
                value: '',
                allowAdd: true
            });

        };

        $scope.removeRow = function (row) {
            var index = req.addMailBoxRequest.mailBox.properties.indexOf(row.entity);
            req.addMailBoxRequest.mailBox.properties.splice(index, 1);
            var removedProperty = row.getProperty('name');
            var indexOfSelectedElement = $scope.allStaicProperties.indexOf(removedProperty);
            if (indexOfSelectedElement > -1) {
                $scope.allStaicPropertiesThatAreNotAssignedValuesYet.push(removedProperty);
            }
        };

        $scope.setScopeValue = function (value) {
            $scope.addedProperty = value;
        };

        /*$scope.displayAllTableValues = function(){               
                $scope.tableValues=req.addMailBoxRequest.mailBox.properties;
            };*/

    }
])