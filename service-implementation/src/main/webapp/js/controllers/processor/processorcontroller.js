var rest = myApp.controller(
    'ProcessorCntrlr', ['$scope',
        '$filter', '$location', '$log',
        function ($scope, $filter,
            $location, $log) {

            // To be Populated
            $scope.mailBoxId;

            //
            $scope.isEdit = false;

            //Model for Add MB
            addRequest = $scope.addRequest = {
                addProcessorToMailBoxRequest: {
                    processor: {}
                }
            };

            editRequest = $scope.editRequest = {
                reviseProcessorRequest: {
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
                allowAdd: 'showNoAddBox'
            }];

            // Procsr Credential Props
            $scope.processorCredProperties = [{

                credentialURI: '',
                credentialType: '',
                userId: '',
                password: '',
                idpType: '',
                idpURI: '',
                allowAdd: 'showNoAddBox'
            }];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYet = ['add new -->', 'httpVersion', 'httpVerb', 'socketTimeout', 'connectionTimeout', 'url', 'port', 'retryAttempts', 'chunkedEncoding', 'contentType', 'encodingFormat', 'otherRequestHeader'];

            $scope.allStaticProperties = ['httpVersion', 'httpVerb', 'socketTimeout', 'connectionTimeout', 'url', 'port', 'retryAttempts', 'chunkedEncoding', 'contentType', 'encodingFormat', 'otherRequestHeader'];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = ['PAYLOAD_LOCATION', 'RESPONSE_LOCATION'];

            $scope.allStaticPropertiesForProcessorFolder = ['PAYLOAD_LOCATION', 'RESPONSE_LOCATION'];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential = ['cred1', 'cred2', 'cred3', 'cred4', 'cred5'];

            $scope.allStaticPropertiesForProcessorCredential = ['cred1', 'cred2', 'cred3', 'cred4', 'cred5'];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp = ['idp1', 'idp2', 'idp3', 'idp4', 'idp5'];

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
                    width: "45%",
                    displayName: "Name",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "value",
                    width: "45%",
                    displayName: "Value",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "allowAdd",
                    width: "10%",
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
                    width: "30%",
                    displayName: "Uri",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "folderType",
                    width: "30%",
                    displayName: "Type",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder" selected-value="valueSelectedinSelectionBoxForProcessorFolder" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "folderDesc",
                    width: "30%",
                    displayName: "Description",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "allowAdd",
                    width: "10%",
                    displayName: "Action",
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addFolderRow(row,valueSelectedinSelectionBoxForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)">add</button></div>' +
                        '<div ng-switch-when="showNoAddBox"><button ng-click="addFolderRow(row,valueSelectedinSelectionBoxForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)">add</button></div>' +
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
                rowHeight: 100,
                columnDefs: [{
                    field: "credentialURI",
                    width: "15%",
                    displayName: "URI",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "credentialType",
                    width: "12%",
                    displayName: "Type",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential" selected-value="valueSelectedinSelectionBoxForProcessorCredential" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "userId",
                    width: "15%",
                    displayName: "UserId",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "password",
                    width: "20%",
                    displayName: "Password",
                    cellTemplate: '<div class="passwordDirective" row-entity="row.entity" col-filed="col.field" />'
                }, {
                    field: "idpType",
                    width: "12%",
                    displayName: "IdpType",
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp" selected-value="valueSelectedinSelectionBoxForProcessorCredentialIdp" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "idpURI",
                    width: "15%",
                    displayName: "IdpURI",
                    enableCellEdit: true,
                    enableCellSelection: true,
                    enableFocusedCellEdit: true
                }, {
                    field: "allowAdd",
                    width: "10%",
                    displayName: "Action",
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addCredentialRow(row,valueSelectedinSelectionBoxForProcessorCredential,valueSelectedinSelectionBoxForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)">add</button></div>' +
                        '<div ng-switch-when="showNoAddBox"><button ng-click="addCredentialRow(row,valueSelectedinSelectionBoxForProcessorCredential,valueSelectedinSelectionBoxForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)">add</button></div>' +
                        '<div ng-switch-when="false"><button ng-click="removeCredentialRow(row,allStaticPropertiesForProcessorCredential,allStaticPropertiesForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)">remove</button></div>' +
                        '</div>'

                }]
            };

            $scope.initialLoad = function () {
                $scope.readAllProcessors();
            }


            $scope.readOnlyProcessors = false;

            $scope.readAllProcessors = function () {

                $scope.restService.get($scope.base_url + 'mailbox/' + $location.search().mailBoxId, //Get mail box Data
                    function (data) {

                        $scope.processorList = data.getMailBoxResponse.mailBox.processors;
                        if (!$scope.readOnlyProcessors) {
                            $scope.readAllProfiles();
                        }

                    }
                );
            }

            $scope.editableInPopup = '<button class="btn" ng-click="editProcessor(row)"><i class="icon-pencil"></i></button>';

            $scope.gridOptionsForProcessorList = {
                columnDefs: [{
                        field: 'name',
                        displayName: 'Name'
                    }, {
                        field: 'type',
                        displayName: 'Type'
                    }, {
                        field: 'status',
                        displayName: 'Status'
                    }, {
                        displayName: 'Action',
                        cellTemplate: $scope.editableInPopup
                    }, {
                        field: 'guid',
                        visible: false
                    }

                ],
                data: 'processorList',
                //rowTemplate: customRowTemplate,
                enablePaging: true,
                showFooter: true,
                canSelectRows: true,
                multiSelect: false,
                jqueryUITheme: false,
                displaySelectionCheckbox: false,
                pagingOptions: $scope.pagingOptions,
                filterOptions: $scope.filterOptions
            };

            $scope.editProcessor = function (row) {

                $scope.isEdit = true;
                var procsrId = row.getProperty('guid');

                $scope.restService.get($scope.base_url +
                    'mailbox/' + $location.search().mailBoxId + '/processor/' + procsrId, //Get mail box Data
                    function (data) {

                        $log.info($filter('json')(data));

                        $scope.clearProps();

                        $scope.processor.guid = data.getProcessorResponse.processor.guid;
                        $scope.processor.name = data.getProcessorResponse.processor.name;
                        $scope.processor.type = data.getProcessorResponse.processor.type;
                        $scope.modal.uri = data.getProcessorResponse.processor.javaScriptURI;
                        $scope.processor.description = data.getProcessorResponse.processor.description;
                        $scope.processor.status = data.getProcessorResponse.processor.status;
                        $scope.processor.protocol = data.getProcessorResponse.processor.protocol;


                        // Pushing out dynamis props

                        /*$scope.processorProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                        for (var i = 0; i < data.getProcessorResponse.mailBox.properties.length; i++) {
                            $scope.processorProperties.push({
                                name: data.getMailBoxResponse.mailBox.properties[i].name,
                                value: data.getMailBoxResponse.mailBox.properties[i].value,
                                allowAdd: false
                            });

                            var indexOfElement = $scope.allStaticPropertiesThatAreNotAssignedValuesYet.indexOf(data.getMailBoxResponse.mailBox.properties[i].name);
                            $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
                        };*/

                        $scope.processorProperties.splice(0, 1);

                        $scope.processorProperties.push({ //Adding now so that the add new option always shows below the available properties
                            name: '',
                            value: '',
                            allowAdd: true
                        });

                        $scope.processorFolderProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                        for (var i = 0; i < data.getProcessorResponse.processor.folders.length; i++) {
                            $scope.processorFolderProperties.push({
                                folderURI: data.getProcessorResponse.processor.folders[i].folderURI,
                                folderType: data.getProcessorResponse.processor.folders[i].folderType,
                                folderDesc: data.getProcessorResponse.processor.folders[i].folderDesc,
                                allowAdd: false
                            });

                            var indexOfElement = $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder.indexOf(data.getProcessorResponse.processor.folders[i].folderType);
                            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder.splice(indexOfElement, 1);
                        };

                        $scope.processorFolderProperties.push({
                            folderURI: '',
                            folderType: '',
                            folderDesc: '',
                            allowAdd: 'showNoAddBox'
                        });

                        $scope.processorCredProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                        for (var i = 0; i < data.getProcessorResponse.processor.credentials.length; i++) {
                            $scope.processorCredProperties.push({
                                credentialURI: data.getProcessorResponse.processor.credentials[i].credentialURI,
                                credentialType: data.getProcessorResponse.processor.credentials[i].credentialType,
                                userId: data.getProcessorResponse.processor.credentials[i].userId,
                                password: data.getProcessorResponse.processor.credentials[i].password,
                                idpType: data.getProcessorResponse.processor.credentials[i].idpType,
                                idpURI: data.getProcessorResponse.processor.credentials[i].idpURI,
                                allowAdd: false
                            });

                            var indexOfElement = $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential.indexOf(data.getProcessorResponse.processor.credentials[i].credentialType);
                            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential.splice(indexOfElement, 1);

                            var indexOfElementIdp = $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp.indexOf(data.getProcessorResponse.processor.credentials[i].idpType);
                            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp.splice(indexOfElementIdp, 1);
                        };

                        $scope.processorCredProperties.push({

                            credentialURI: '',
                            credentialType: '',
                            userId: '',
                            password: '',
                            idpType: '',
                            idpURI: '',
                            allowAdd: 'showNoAddBox'
                        });
                    }
                );
            }


            $scope.readAllProfiles = function () {

                $scope.restService.get($scope.base_url +
                    'mailbox/profile', //Get mail box Data
                    function (data) {

                        $scope.allProfiles = data.getProfileResponse.profiles;
                        $scope.loadBrowseData();
                    }
                );
            }

            $scope.loadBrowseData = function () {

                $scope.restService.get($scope.base_url +
                    'mailbox/listFile', //Get mail box Data
                    function (data) {

                        $scope.roleList = data.ArrayList;
                        $log.info($scope.roleList);
                        $scope.modal = {
                            "roleList": $scope.roleList,
                            "url": ''
                        }
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
                    allowAdd: 'showNoAddBox'
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
                    allowAdd: 'showNoAddBox'
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

                $scope.processor.linkedMailboxId = $location.search().mailBoxId;

                //$scope.processor.linkedProfiles = $scope.selectedProfiles;

                var profileLen = $scope.selectedProfiles.length;
                for (var i = 0; i < profileLen; i++) {
                    $scope.processor.linkedProfiles[i] = $scope.selectedProfiles[i].name
                }

                $scope.processor.javaScriptURI = $scope.modal.uri;

                if ($scope.isEdit) {

                    editRequest.reviseProcessorRequest.processor = $scope.processor;

                    $log.info($filter('json')(editRequest));
                    $scope.restService.put($scope.base_url + 'mailbox/' + $location.search().mailBoxId + '/processor/' + $scope.processor.guid, $filter('json')(editRequest),
                        function (data, status) {

                            if (status === 200) {
                                alert(data.reviseProcessorResponse.response.message);
                                $scope.readOnlyProcessors = true;
                                $scope.readAllProcessors();
                            }

                            $scope.clearProps();
                        }
                    );

                } else {
                    addRequest.addProcessorToMailBoxRequest.processor = $scope.processor;

                    $log.info($filter('json')(addRequest));
                    $scope.restService.post($scope.base_url + 'mailbox/' + $location.search().mailBoxId + '/processor', $filter('json')(addRequest),
                        function (data, status) {

                            if (status === 200) {
                                alert(data.addProcessorToMailBoxResponse.response.message);
                                $scope.readOnlyProcessors = true;
                                $scope.readAllProcessors();
                            }

                            $scope.clearProps();
                        }
                    );
                }

            };

            $scope.clearProps = function () {
                $scope.processor.dynamicProperties = [];
                $scope.processor.folders = [];
                $scope.processor.credentials = [];
                $scope.processor.remoteProcessorProperties.otherRequestHeader = [];
            };

        }


    ]);