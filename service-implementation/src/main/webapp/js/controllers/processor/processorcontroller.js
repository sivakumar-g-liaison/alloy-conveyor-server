var rest = myApp.controller(
    'ProcessorCntrlr', ['$scope',
        '$filter', '$location', '$log',
        function ($scope, $filter,
            $location, $log) {

            // To be Populated
            $scope.mailBoxId;

            //Model for Add MB
            addRequest = $scope.addRequest = {
                addProcessorToMailBoxRequest: {
                    processor: {}
                }
            };

            $scope.processor = {
                guid: "",
                name: "",
                type: "",
                javaScriptURI: "",
                description: "",
                status: "",
                protocol: "",
                linkedMailboxId: "",
                linkedProfiles: [],
                folders: [],
                credentials: [],
                dynamicProperties: [],
                remoteProcessorProperties: {}
            };

            $scope.remoteProcessorProperties = {
                otherRequestHeader: []
            };

            //Dummy
            addRequest.addProcessorToMailBoxRequest.processor.linkedMailboxId = 'C6A30A26C0A800FE0B62C7C85060484E';

            $scope.enumstats = [
                'ACTIVE',
                'INACTIVE'
            ];

            addRequest.addProcessorToMailBoxRequest.processor.status = $scope.enumstats[0];

            // Enum for procsr type
            $scope.enumprocsrtype = [
                'REMOTEDOWNLOADER',
                'REMOTEUPLOADER'
            ];

            addRequest.addProcessorToMailBoxRequest.processor.type = $scope.enumprocsrtype[0];

            // Enum for protocol type
            $scope.enumprotocoltype = [
                'FTP',
                'HTTP',
                'HTTPS'
            ];

            addRequest.addProcessorToMailBoxRequest.processor.protocol = $scope.enumprotocoltype[0];

            // Procsr Dynamic Props
            $scope.processorProperties = [{
                name: '',
                value: '',
                allowAdd: true
            }];

            // Procsr Folder Props
            $scope.processorFolderProperties = [{
                folderURI: '',
                folderType: '',
                folderDesc: '',
                allowAdd: true
            }];

            // Procsr Credential Props
            $scope.processorCredProperties = [{
                credentialURI: '',
                credentialType: '',
                userId: '',
                password: '',
                idpType: '',
                idpURI: '',
                allowAdd: true
            }];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYet = ['add new -->', 'httpVersion', 'httpVerb', 'socketTimeout', 'connectionTimeout', 'url', 'port', 'retryAttempts', 'chunkedEncoding', 'contentType', 'encodingFormat', 'otherRequestHeader'];

            $scope.allStaticProperties = ['httpVersion', 'httpVerb', 'socketTimeout', 'connectionTimeout', 'url', 'port', 'retryAttempts', 'chunkedEncoding', 'contentType', 'encodingFormat', 'otherRequestHeader'];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = ['add new -->', 'PAYLOAD_LOCATION', 'RESPONSE_LOCATION'];

            $scope.allStaticPropertiesForProcessorFolder = ['PAYLOAD_LOCATION', 'RESPONSE_LOCATION'];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential = ['add new -->', 'cred1', 'cred2', 'cred3', 'cred4', 'cred5'];

            $scope.allStaticPropertiesForProcessorCredential = ['cred1', 'cred2', 'cred3', 'cred4', 'cred5'];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp = ['add new -->', 'idp1', 'idp2', 'idp3', 'idp4', 'idp5'];

            $scope.allStaticPropertiesForProcessorCredentialIdp = ['idp1', 'idp2', 'idp3', 'idp4', 'idp5'];

            $scope.addedProperty = 'add new';
            $scope.disableAddNewTextBox = 'true';

            $scope.valueSelectedinSelectionBox = {
                name: ''
            };
            /*Folder*/
            $scope.valueSelectedinSelectionBoxForProcessorFolder = {
                name: ''
            };
            /*Credential*/
            $scope.valueSelectedinSelectionBoxForProcessorCredential = {
                name: ''
            };
            /*Credential Idp*/
            $scope.valueSelectedinSelectionBoxForProcessorCredentialIdp = {
                name: ''
            };


            $scope.gridOptionsForProcessor = {
                data: 'processorProperties',
                displaySelectionCheckbox: false,
                canSelectRows: false,
                enablePaging: false,
                showFooter: false,
                rowHeight: 40,
                columnDefs: [{
                    field: "name",
                    width: 500,
                    displayName: "Name",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "value",
                    width: 300,
                    displayName: "Value",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "allowAdd",
                    width: 100,
                    displayName: "Action",
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties)">add</button></div>' +
                        '<div ng-switch-when="false"><button ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties)">remove</button></div>' +
                        '</div>'

                }]
            };

            $scope.gridOptionsForProcessorFolder = {
                data: 'processorFolderProperties',
                displaySelectionCheckbox: false,
                canSelectRows: false,
                enablePaging: false,
                showFooter: false,
                rowHeight: 40,
                columnDefs: [{
                    field: "folderURI",
                    width: 300,
                    displayName: "Uri",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "folderType",
                    width: 500,
                    displayName: "Type",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder" selected-value="valueSelectedinSelectionBoxForProcessorFolder" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "folderDesc",
                    width: 300,
                    displayName: "Description",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "allowAdd",
                    width: 100,
                    displayName: "Action",
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addFolderRow(row,valueSelectedinSelectionBoxForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)">add</button></div>' +
                        '<div ng-switch-when="false"><button ng-click="removeFolderRow(row,allStaticPropertiesForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)">remove</button></div>' +
                        '</div>'

                }]
            };

            // Credentials for Grid Options 
            $scope.gridOptionsForProcessorCredential = {
                data: 'processorCredProperties',
                displaySelectionCheckbox: false,
                canSelectRows: false,
                enablePaging: false,
                showFooter: false,
                rowHeight: 40,
                columnDefs: [{
                    field: "credentialURI",
                    width: 150,
                    displayName: "URI",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "credentialType",
                    width: 500,
                    displayName: "Type",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential" selected-value="valueSelectedinSelectionBoxForProcessorCredential" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "userId",
                    width: 200,
                    displayName: "UserId",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "password",
                    width: 200,
                    displayName: "Password",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "idpType",
                    width: 500,
                    displayName: "IdpType",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp" selected-value="valueSelectedinSelectionBoxForProcessorCredentialIdp" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "idpURI",
                    width: 200,
                    displayName: "IdpURI",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "allowAdd",
                    width: 100,
                    displayName: "Action",
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addCredentialRow(row,valueSelectedinSelectionBoxForProcessorCredential,valueSelectedinSelectionBoxForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)">add</button></div>' +
                        '<div ng-switch-when="false"><button ng-click="removeCredentialRow(row,allStaticPropertiesForProcessorCredential,allStaticPropertiesForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)">remove</button></div>' +
                        '</div>'

                }]
            };

            $scope.initialLoad = function () {
                $scope.readAllProfiles();
            }

            $scope.readAllProfiles = function () {

                $scope.restService.get($scope.base_url +
                    'mailbox/profile', //Get mail box Data
                    function (data) {

                        $scope.allProfiles = data.getProfileResponse.profiles;
                    }
                );
            }

            $scope.initialLoad();


            // Browse Component related stuff.

            /*$scope.getData = function () {
				$scope.restService.get($scope.base_url +
					'mailbox/listFile', //Get mail box Data
					function (data) {
						$scope.roleList = data.ArrayList;
						console.log($scope.roleList);
					}
				);
			};*/


            // Profile Related Stuff.
            $scope.allProfiles = [];

            $scope.selectedProfiles = [];

            $scope.chooseProfile = function () {

                for (var i = 0; i < $scope.profilesSelectedinAllProfile.length; i++) {
                    $scope.selectedProfiles.push($scope.profilesSelectedinAllProfile[i]);
                    var indexOfSelectedElement = $scope.allProfiles.indexOf($scope.profilesSelectedinAllProfile[i]);
                    $scope.allProfiles.splice(indexOfSelectedElement, 1);
                }
                $scope.profilesSelectedinAllProfile = [];
            };

            $scope.removeProfile = function () {

                for (var i = 0; i < $scope.profilesSelectedinProcessorProfile.length; i++) {
                    $scope.allProfiles.push($scope.profilesSelectedinProcessorProfile[i]);
                    var indexOfSelectedElement = $scope.selectedProfiles.indexOf($scope.profilesSelectedinProcessorProfile[i]);
                    $scope.selectedProfiles.splice(indexOfSelectedElement, 1);
                }
                $scope.profilesSelectedinProcessorProfile = [];
            };

            // For Procsr Dynamic Props
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

            // For Procsr Dynamic Props
            $scope.removeRow = function (row, allProps, allPropsWithNovalue, gridData) {
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                var removedProperty = row.getProperty('name');
                var indexOfSelectedElement = allProps.indexOf(removedProperty);
                if (indexOfSelectedElement > -1) {
                    allPropsWithNovalue.push(removedProperty);
                }
            };

            // For Procsr Folder Props
            $scope.addFolderRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData) {

                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    folderURI: row.getProperty('folderURI'),
                    folderType: valueSelectedinSelectionBox.name,
                    folderDesc: row.getProperty('folderDesc'),
                    allowAdd: false
                });
                var indexOfSelectedElement = allPropsWithNovalue.indexOf(valueSelectedinSelectionBox.name);
                if (indexOfSelectedElement != -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }

                //}
                gridData.push({
                    folderURI: '',
                    folderType: '',
                    folderDesc: '',
                    allowAdd: true
                });

            };

            // For Procsr Folder Props
            $scope.removeFolderRow = function (row, allProps, allPropsWithNovalue, gridData) {
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                var removedProperty = row.getProperty('folderType');
                var indexOfSelectedElement = allProps.indexOf(removedProperty);
                if (indexOfSelectedElement > -1) {
                    allPropsWithNovalue.push(removedProperty);
                }
            };

            // For Procsr Credentials Props
            $scope.addCredentialRow = function (row, valueSelectedinSelectionBox, valueSelectedinSelectionBoxIdp, allPropsWithNovalue, allPropsWithNovalueIdp, gridData) {

                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    credentialURI: row.getProperty('credentialURI'),
                    credentialType: valueSelectedinSelectionBox.name,
                    userId: row.getProperty('userId'),
                    password: row.getProperty('password'),
                    idpType: valueSelectedinSelectionBoxIdp.name,
                    allowAdd: false
                });
                var indexOfSelectedElement = allPropsWithNovalue.indexOf(valueSelectedinSelectionBox.name);
                if (indexOfSelectedElement != -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }

                var indexOfSelectedElementIdp = allPropsWithNovalueIdp.indexOf(valueSelectedinSelectionBoxIdp.name);
                if (indexOfSelectedElementIdp != -1) {
                    allPropsWithNovalueIdp.splice(indexOfSelectedElementIdp, 1);
                }

                //}
                gridData.push({
                    credentialURI: '',
                    credentialType: '',
                    userId: '',
                    password: '',
                    idpType: '',
                    allowAdd: true
                });

            };

            // For Procsr Credentials Props
            $scope.removeCredentialRow = function (row, allProps, allPropsIdp, allPropsWithNovalue, allPropsWithNovalueIdp, gridData) {
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);

                var removedProperty = row.getProperty('credentialType');
                var indexOfSelectedElement = allProps.indexOf(removedProperty);
                if (indexOfSelectedElement > -1) {
                    allPropsWithNovalue.push(removedProperty);
                }

                var removedIdpProperty = row.getProperty('idpType');
                var indexOfSelectedElementIdp = allPropsIdp.indexOf(removedIdpProperty);
                if (indexOfSelectedElementIdp > -1) {
                    allPropsWithNovalueIdp.push(removedIdpProperty);
                }
            };

            $scope.doCancel = function () {

            };

            $scope.saveProcessor = function () {

                var lenDynamicProps = $scope.processorProperties.length;
                for (var i = 0; i < lenDynamicProps - 1; i++) {

                    var index = $scope.allStaticProperties.indexOf($scope.processorProperties[i].name);


                    if (index == -1) {
                        $scope.processor.dynamicProperties.push({
                            name: $scope.processorProperties[i].name,
                            value: $scope.processorProperties[i].value

                        });
                    } else {
                        var name = $scope.processorProperties[i].name;
                        if (name === 'otherRequestHeader') {

                            $scope.processor.remoteProcessorProperties.otherRequestHeader.push({
                                name: 'otherRequestHeader',
                                value: $scope.processorProperties[i].value

                            });
                        } else {
                            $scope.processor.remoteProcessorProperties[name] = $scope.processorProperties[i].value;
                        }

                    }

                }

                var lenFolderProps = $scope.processorFolderProperties.length;
                for (var i = 0; i < lenFolderProps - 1; i++) {
                    $scope.processor.folders.push({
                        folderURI: $scope.processorFolderProperties[i].folderURI,
                        folderType: $scope.processorFolderProperties[i].folderType,
                        folderDesc: $scope.processorFolderProperties[i].folderDesc

                    });
                }

                var lenCredentialProps = $scope.processorCredProperties.length;
                for (var i = 0; i < lenCredentialProps - 1; i++) {
                    $scope.processor.credentials.push({
                        credentialURI: $scope.processorCredProperties[i].credentialURI,
                        credentialType: $scope.processorCredProperties[i].credentialType,
                        userId: $scope.processorCredProperties[i].userId,
                        password: $scope.processorCredProperties[i].password,
                        idpType: $scope.processorCredProperties[i].idpType,
                        idpURI: $scope.processorCredProperties[i].idpURI

                    });
                }

                $scope.processor.linkedMailboxId = 'C6A30A26C0A800FE0B62C7C85060484E';

                //$scope.processor.linkedProfiles = $scope.selectedProfiles;

                var profileLen = $scope.selectedProfiles.length;
                for (var i = 0; i < profileLen; i++) {
                    $scope.processor.linkedProfiles[i] = $scope.selectedProfiles[i].name
                }

                addRequest.addProcessorToMailBoxRequest.processor = $scope.processor;

                $log.info($filter('json')(addRequest));
                $scope.restService.post($scope.base_url + 'mailbox/' + $scope.processor.linkedMailboxId + '/processor', $filter('json')(addRequest),
                    function (data, status) {

                        if (status === 200) {
                            alert(data.addProcessorToMailBoxResponse.response.message);
                        }

                        $scope.processor.dynamicProperties = [];
                        $scope.processor.folders = [];
                        $scope.processor.credentials = [];
                        $scope.processor.remoteProcessorProperties.otherRequestHeader = [];

                    }
                );

                console.log(addRequest.addProcessorToMailBoxRequest.processor);

            };

        }


    ]);
/*
    angular.treeview.js
*/
(function (l) {
    l.module("angularTreeview", []).directive(
        "treeModel", function ($compile) {
            return {
                restrict: "A",
                link: function (a, g, c) {
                    var e = c.treeModel,
                        h = c.nodeLabel || "label",
                        d = c.nodeChildren || "children",
                        k =
                            '<ul><li data-ng-repeat="node in ' +
                            e +
                            '"><i class="collapsed" data-ng-show="node.' +
                            d +
                            '.length && node.collapsed" data-ng-click="selectNodeHead(node, $event)"></i><i class="expanded" data-ng-show="node.' +
                            d +
                            '.length && !node.collapsed" data-ng-click="selectNodeHead(node, $event)"></i><i class="normal" data-ng-hide="node.' +
                            d +
                            '.length"></i> <span data-ng-class="node.selected" data-ng-click="selectNodeLabel(node, $event)">{{node.' +
                            h +
                            '}}</span><div data-ng-hide="node.collapsed" data-tree-model="node.' +
                            d + '" data-node-id=' + (c.nodeId ||
                                "id") + " data-node-label=" +
                            h + " data-node-children=" + d +
                            "></div></li></ul>";
                    e && e.length && (c.angularTreeview ?
                        (a.$watch(e, function (m, b) {
                                g.empty().html($compile(k)(a))
                            }, !1), a.selectNodeHead = a.selectNodeHead ||
                            function (a, b) {
                                b.stopPropagation && b.stopPropagation();
                                b.preventDefault && b.preventDefault();
                                b.cancelBubble = !0;
                                b.returnValue = !1;
                                a.collapsed = !a.collapsed
                            }, a.selectNodeLabel = a.selectNodeLabel ||
                            function (c, b) {
                                b.stopPropagation && b.stopPropagation();
                                b.preventDefault && b.preventDefault();
                                b.cancelBubble = !0;
                                b.returnValue = !1;
                                a.currentNode && a.currentNode
                                    .selected && (a.currentNode.selected =
                                        void 0);
                                c.selected = "selected";
                                a.currentNode = c
                            }) : g.html($compile(k)(a)))
                }
            }
        })
})(angular);