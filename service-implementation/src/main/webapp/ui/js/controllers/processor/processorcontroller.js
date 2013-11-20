var rest = myApp.controller(
    'ProcessorCntrlr', ['$scope',
        '$filter', '$location', '$log', '$blockUI',
        function ($scope, $filter,
            $location, $log, $blockUI) {
            // To be Populated
            $scope.mailBoxId;
            var block = $blockUI.createBlockUI();
            
            $scope.loadOrigin = function () {
                //          
                $scope.isEdit = false;

                $scope.mailboxName = $location.search().mbxname;
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
                $scope.modal = {
                    "roleList": '',
                    "uri": ''
                };
                $scope.processor.remoteProcessorProperties = {
                    otherRequestHeader: []
                };
                $scope.enumstats = [
                    'ACTIVE',
                    'INACTIVE'
                ];
                $scope.processor.status = $scope.enumstats[0];
                // Enum for procsr type
                $scope.enumprocsrtype = [
                    'REMOTEDOWNLOADER',
                    'REMOTEUPLOADER',
                    'SWEEPER'
                ];
                $scope.processor.type = $scope.enumprocsrtype[0];
                // Enum for protocol type
                $scope.enumprotocoltype = [
                    'FTPS',
                    'HTTP',
                    'HTTPS',
                    'SFTP',
                    'SWEEPER'
                ];
                $scope.enumHttpVerb = [
                    'GET',
                    'PUT',
                    'POST',
                    'DELETE'
                ];
                $scope.verb = $scope.enumHttpVerb[0];
                $scope.processor.protocol = $scope.enumprotocoltype[0];
                // Procsr Dynamic Props
                $scope.ftpMandatoryProperties = [{
                    name: 'url',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
                $scope.httpMandatoryProperties = [{
                    name: 'url',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'httpVersion',
                    value: '1.1',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'httpVerb',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: 'contentType',
                    value: '',
                    allowAdd: false,
                    isMandatory: true
                }, {
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
                $scope.processorProperties = $scope.ftpMandatoryProperties;
                // Procsr Folder Props
                $scope.processorFolderProperties = [{
                    folderURI: '',
                    folderType: '',
                    folderDesc: '',
                    isMandatory: false,
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
                    allowAdd: 'showNoAddBox',
                    passwordDirtyState: ''
                }];
                $scope.allStaticPropertiesThatAreNotAssignedValuesYet = ['add new -->', 'socketTimeout', 'connectionTimeout', 'retryAttempts', 'chunkedEncoding', 'encodingFormat', 'port', 'otherRequestHeader'];
                $scope.allStaticProperties = ['socketTimeout', 'connectionTimeout', 'retryAttempts', 'chunkedEncoding', 'encodingFormat', 'port', 'otherRequestHeader'];
                $scope.allMandatoryFtpProperties = ['url'];
                $scope.allMandatoryHttpProperties = ['httpVersion', 'httpVerb', 'url', 'contentType'];
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = ['PAYLOAD_LOCATION', 'RESPONSE_LOCATION'];
                $scope.allStaticPropertiesForProcessorFolder = ['PAYLOAD_LOCATION', 'RESPONSE_LOCATION'];
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential = ['TRUST_STORE', 'KEY_STORE', 'LOGIN_CREDENTIAL'];
                $scope.allStaticPropertiesForProcessorCredential = ['TRUST_STORE', 'KEY_STORE', 'LOGIN_CREDENTIAL'];
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp = ['FTPS', 'SFTP'];
                $scope.allStaticPropertiesForProcessorCredentialIdp = ['FTPS', 'SFTP'];
                $scope.disableAddNewTextBox = 'true';
                $scope.valueSelectedinSelectionBox = {
                    name: ''
                };
                $scope.showAddNew = {
                    value: 'false'
                };
                $scope.addedProperty = {
                    value: ''
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
                // Profile Related Stuff.
                $scope.allProfiles = [];
                $scope.selectedProfiles = [];
            };
            
            $scope.loadOrigin();
            $scope.gridOptionsForProcessor = {
                data: 'processorProperties',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 60,
                columnDefs: [{
                    field: "name",
                    width: "40%",
                    displayName: "Name*",
                    enableCellEdit: false,
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} add-new="showAddNew" added-property="addedProperty" />'
                }, {
                    field: "value",
                    width: "53%",
                    displayName: "Value*",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(\'name\')">\n\
                                    <div ng-switch-when="">\n\
                                         <textarea ng-model="COL_FIELD"  style="width:94%" row="4" placeholder="required" />\n\
                                         <a ng-click="isModal(row)" data-toggle="modal" data-backdrop="static" data-keyboard="false" href="#valueModal" class = "right">\n\
                                             <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                    </div>\n\
                                    <div ng-switch-default>\n\
                                         <textarea ng-model="COL_FIELD"  required style="width:94%" row="4" placeholder="required"/>\n\
                                         <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class = "right">\n\
                                        <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                    </div>\n\
                                    <div ng-switch-when="httpVerb">\n\
                                         <select ng-model="verb" ng-change="onVerbChange(verb)"  ng-options="property for property in enumHttpVerb"></select>\n\
                                     </div>\n\
                                  </div>'
                }, {
                    field: "allowAdd",
                    width: "7%",
                    enableCellEdit: false,
                    displayName: "Action",
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties,addedProperty)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                        '<div ng-switch-when="false"><div ng-switch on="row.getProperty(\'isMandatory\')">' +
                        '<div ng-switch-when="true">-NA-</div>' +
                        '<div ng-switch-when="false"><button ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties,valueSelectedinSelectionBox)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                        '</div></div></div>'
                }]
            };
            
            $scope.gridOptionsForProcessorFolder = {
                data: 'processorFolderProperties',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 60,
                columnDefs: [{
                    field: "folderURI",
                    width: "33%",
                    displayName: "URI*",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')"><div ng-switch-when="false"><textarea ng-model="COL_FIELD"  style="width:95%" required  placeholder="required" /></div><div ng-switch-default><textarea ng-model="COL_FIELD" style="width:95%"   placeholder="required"/></div></div>'
                }, {
                    field: "folderType",
                    width: "20%",
                    displayName: "Type*",
                    enableCellEdit: false,
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder" selected-value="valueSelectedinSelectionBoxForProcessorFolder" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "folderDesc",
                    width: "40%",
                    displayName: "Description",
                    enableCellEdit: false,
                    cellTemplate: '<textarea style="width:95%" ng-model="COL_FIELD" ></textarea>'
                }, {
                    field: "allowAdd",
                    width: "7%",
                    displayName: "Action",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addFolderRow(row,valueSelectedinSelectionBoxForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                        '<div ng-switch-when="showNoAddBox"><button ng-click="addFolderRow(row,valueSelectedinSelectionBoxForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                        '<div ng-switch-when="false"><div ng-switch on="row.getProperty(\'isMandatory\')"><div ng-switch-when="false"><button ng-click="removeFolderRow(row,allStaticPropertiesForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div><div ng-switch-when="true">-NA-</div></div></div>' +
                        '</div>'
                }]
            };
             // Credentials for Grid Options 
            $scope.gridOptionsForProcessorCredential = {
                data: 'processorCredProperties',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 100,
                columnDefs: [{
                    field: "credentialURI",
                    width: "20%",
                    displayName: "URI",
                    enableCellEdit: false,
                    cellTemplate: '<textarea style="width:98%" rows="4" ng-model="COL_FIELD" />'
                }, {
                    field: "credentialType",
                    width: "17%",
                    displayName: "Type*",
                    enableCellEdit: false,
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential" selected-value="valueSelectedinSelectionBoxForProcessorCredential" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "userId",
                    width: "14%",
                    displayName: "UserId",
                    enableCellEdit: false,
                    cellTemplate: '<input type="text" ng-model="COL_FIELD" class="textboxingrid">'
                }, {
                    field: "password",
                    width: "14%",
                    displayName: "Password",
                    enableCellEdit: false,
                    cellTemplate: '<div class="passwordDirective" password={{row.getProperty(col.field)}} row-entity="row.entity" col-filed="col.field"  />'
                }, {
                    field: "idpType",
                    width: "10%",
                    displayName: "IdpType",
                    enableCellEdit: false,
                    cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp" selected-value="valueSelectedinSelectionBoxForProcessorCredentialIdp" prop-name={{row.getProperty(col.field)}} />'
                }, {
                    field: "idpURI",
                    width: "20%",
                    displayName: "IdpURI",
                    enableCellEdit: false,
                    cellTemplate: '<textarea style="width:98%" rows="4" ng-model="COL_FIELD" />'
                }, {
                    field: "allowAdd",
                    width: "7%",
                    displayName: "Action",
                    enableCellEdit: false,
                    cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                        '<div ng-switch-when="true"><button ng-click="addCredentialRow(row,valueSelectedinSelectionBoxForProcessorCredential,valueSelectedinSelectionBoxForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                        '<div ng-switch-when="showNoAddBox"><button ng-click="addCredentialRow(row,valueSelectedinSelectionBoxForProcessorCredential,valueSelectedinSelectionBoxForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                        '<div ng-switch-when="false"><button ng-click="removeCredentialRow(row,allStaticPropertiesForProcessorCredential,allStaticPropertiesForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                        '</div>'
                }]
            };
            $scope.onVerbChange = function (httpVerb) {
                $scope.verb = httpVerb;
            };
            $scope.initialLoad = function () {
                $scope.readAllProcessors();
                $scope.readAllProfiles();
            };
            //$scope.readOnlyProcessors = false;
            // Grid Setups
            $scope.filterOptions = {
                filterText: "",
                useExternalFilter: true
            };
            //Paging set up
            $scope.totalServerItems = 0;
            $scope.pagingOptions = {
                pageSizes: [5, 10, 50],
                pageSize: 5,
                currentPage: 1
            };
            $scope.readAllProcessors = function () {
                $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId, //Get mail box Data
                    function (data) {
                        $scope.getPagedDataAsync(data,
                            $scope.pagingOptions.pageSize,
                            $scope.pagingOptions.currentPage);
                    }
                );
            };
            $scope.getPagedDataAsync = function (largeLoad, pageSize, page) {
                setTimeout(function () {
                    $scope.setPagingData(largeLoad.getMailBoxResponse.mailBox.processors, page, pageSize);
                }, 100);
            };
            // Set the paging data to grid from server object
            $scope.setPagingData = function (data, page, pageSize) {
                if (data === null || data.length <= 0) {
                    $scope.message = 'No results found.';
                    $scope.openMessage();
                }
                var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
                $scope.processorList = pagedData;
                $scope.totalServerItems = data.length;
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            };
            $scope.$watch('pagingOptions', function (newVal, oldVal) {
                if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                    $scope.readAllProcessors();
                }
                if (newVal !== oldVal && newVal.pageSize !== oldVal.pageSize) {
                    $scope.readAllProcessors();
                }
            }, true);
            $scope.$watch('filterOptions', function (newVal, oldVal) {
                if (newVal !== oldVal) {
                    $scope.readAllProcessors();
                }
            }, true);
            $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="editProcessor(row)"><i class="glyphicon glyphicon-pencil"></i></button>';
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
                }],
                data: 'processorList',
                //rowTemplate: customRowTemplate,
                enablePaging: true,
                showFooter: true,
                canSelectRows: true,
                multiSelect: false,
                jqueryUITheme: false,
                displaySelectionCheckbox: false,
                pagingOptions: $scope.pagingOptions,
                filterOptions: $scope.filterOptions,
                totalServerItems: 'totalServerItems'
            };
            $scope.setRemotePropData = function (reqHeaderArray, value) {
                if (value === 'otherRequestHeader') {
                    var colonArray = [];
                    for (var i = 0; i < reqHeaderArray.length; i++) {
                        colonArray.push(reqHeaderArray[i].name + ':' + reqHeaderArray[i].value);
                    }
                    return colonArray.toString();
                } else {
                    $scope.verb = reqHeaderArray;
                }
            };
            $scope.editProcessor = function (row) {
                block.blockUI();
                $scope.loadOrigin();
                $scope.isEdit = true;
                var procsrId = row.getProperty('guid');
                $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + procsrId, //Get mail box Data
                    function (data) {
                        $log.info($filter('json')(data));
                        //Fix: Reading profile in procsr callback
                        $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                            function (profData) {
                                $log.info($filter('json')(profData));
                                block.unblockUI();
                                $scope.allProfiles = profData.getProfileResponse.profiles;
                                $scope.clearProps();
                                $scope.processor.guid = data.getProcessorResponse.processor.guid;
                                $scope.processor.name = data.getProcessorResponse.processor.name;
                                $scope.processor.type = data.getProcessorResponse.processor.type;
                                $scope.modal.uri = data.getProcessorResponse.processor.javaScriptURI;
                                $scope.processor.description = data.getProcessorResponse.processor.description;
                                $scope.processor.status = data.getProcessorResponse.processor.status;
                                $scope.processor.protocol = data.getProcessorResponse.processor.protocol;
                                $scope.processor.protocol = data.getProcessorResponse.processor.protocol;
                                $scope.selectedProfiles = data.getProcessorResponse.processor.profiles;
                                //Schedules
                                for (var i = 0; i < $scope.selectedProfiles.length; i++) {
                                    // To remove $$hashKey
                                    //var profs = angular.fromJson(angular.toJson($scope.allProfiles));
                                    for (var j = 0; j < $scope.allProfiles.length; j++) {
                                        if ($scope.selectedProfiles[i].id === $scope.allProfiles[j].id) {
                                            $scope.allProfiles.splice(j, 1);
                                            break;
                                        }
                                    }
                                }
                                // Pushing out dynamis props
                                $scope.processorProperties = []; //Removing now so that the add new option always shows below the available properties
                                $scope.httpMandatoryProperties = [];
                                $scope.ftpMandatoryProperties = [];
                                var json_data = data.getProcessorResponse.processor.remoteProcessorProperties;
                                var otherReqIndex = -1;
                                var i = 0;
                                for (var prop in json_data) {
                                    if (json_data[prop] !== 0 && json_data[prop] !== false && json_data[prop] !== null && json_data[prop] !== '') {
                                        i++;
                                        if (prop === 'otherRequestHeader' && json_data[prop].length === 0) {
                                            otherReqIndex = i;
                                        }
                                        if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                                            $scope.httpMandatoryProperties.push({
                                                name: prop,
                                                value: (prop === 'otherRequestHeader' || prop === 'httpVerb') ? $scope.setRemotePropData(json_data[prop], prop) : json_data[prop],
                                                allowAdd: false,
                                                isMandatory: ($scope.allMandatoryHttpProperties.indexOf(prop) === -1) ? false : true
                                            });
                                        } else {
                                            $scope.ftpMandatoryProperties.push({
                                                name: prop,
                                                value: (prop === 'otherRequestHeader') ? $scope.setRemotePropData(json_data[prop], prop) : json_data[prop],
                                                allowAdd: false,
                                                isMandatory: ($scope.allMandatoryFtpProperties.indexOf(prop) === -1) ? false : true
                                            });
                                        }
                                        var indexOfElement = $scope.allStaticPropertiesThatAreNotAssignedValuesYet.indexOf(prop);
                                        if (indexOfElement !== -1) {
                                            $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
                                        }
                                    }
                                }
                                if (otherReqIndex !== -1) {
                                    if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                                        $scope.httpMandatoryProperties.splice(otherReqIndex - 1, 1);
                                    } else {
                                        $scope.ftpMandatoryProperties.splice(otherReqIndex - 1, 1);
                                    }
                                    $scope.allStaticPropertiesThatAreNotAssignedValuesYet.push('otherRequestHeader');
                                }
                                for (var i = 0; i < data.getProcessorResponse.processor.dynamicProperties.length; i++) {
                                    if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                                        $scope.httpMandatoryProperties.push({
                                            name: data.getProcessorResponse.processor.dynamicProperties[i].name,
                                            value: data.getProcessorResponse.processor.dynamicProperties[i].value,
                                            allowAdd: false,
                                            isMandatory: false
                                        });
                                    } else {
                                        $scope.ftpMandatoryProperties.push({
                                            name: data.getProcessorResponse.processor.dynamicProperties[i].name,
                                            value: data.getProcessorResponse.processor.dynamicProperties[i].value,
                                            allowAdd: false,
                                            isMandatory: false
                                        });
                                    }
                                }
                                if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                                    $scope.httpMandatoryProperties.push({ //Adding now so that the add new option always shows below the available properties
                                        name: '',
                                        value: '',
                                        allowAdd: true,
                                        isMandatory: false
                                    });
                                    $scope.processorProperties = $scope.httpMandatoryProperties;
                                } else {
                                    $scope.ftpMandatoryProperties.push({ //Adding now so that the add new option always shows below the available properties
                                        name: '',
                                        value: '',
                                        allowAdd: true,
                                        isMandatory: false
                                    });
                                    $scope.processorProperties = $scope.ftpMandatoryProperties;
                                }
                                $scope.processorFolderProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                                for (var i = 0; i < data.getProcessorResponse.processor.folders.length; i++) {
                                    $scope.processorFolderProperties.push({
                                        folderURI: data.getProcessorResponse.processor.folders[i].folderURI,
                                        folderType: data.getProcessorResponse.processor.folders[i].folderType,
                                        folderDesc: data.getProcessorResponse.processor.folders[i].folderDesc,
                                        isMandatory: ($scope.processor.protocol === 'SWEEPER' && data.getProcessorResponse.processor.folders[i].folderType === 'PAYLOAD_LOCATION') ? true : false,
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
                );
            };
            $scope.readAllProfiles = function () {
                $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                    function (data) {
                        $scope.allProfiles = data.getProfileResponse.profiles;
                        $scope.loadBrowseData();
                    }
                );
            };
            $scope.loadBrowseData = function () {
                $scope.restService.get($scope.base_url + '/listFile', //Get mail box Data
                    function (data) {
                        $scope.roleList = data.ArrayList;
                        $log.info($scope.roleList);
                        $scope.modal.roleList = $scope.roleList;
                    }
                );
            };
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
            $scope.addRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData, addedProperty) {
                // validation
                $log.info(valueSelectedinSelectionBox.name);
                $log.info(row.getProperty('value'));                
                if (valueSelectedinSelectionBox.name === 'add new -->' && addedProperty.value !== '') {
                    valueSelectedinSelectionBox.name = addedProperty.value;
                    addedProperty.value = '';
                }
                if (!valueSelectedinSelectionBox.name || valueSelectedinSelectionBox.name === 'add new -->' || !row.getProperty('value')) {
                    showAlert('It is mandatory to set the name and value of the property being added.');
                    return;
                }
                $scope.informer.inform("error message", "error");
                $scope.informer.inform("info message", "info");
                $scope.allInfos = $scope.informer.allInfos;
                $scope.remove = $scope.informer.remove;
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    name: valueSelectedinSelectionBox.name,
                    value: row.getProperty('value'),
                    allowAdd: false,
                    isMandatory: false
                });
                var indexOfSelectedElement = allPropsWithNovalue.indexOf(valueSelectedinSelectionBox.name);
                if (indexOfSelectedElement !== -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }
                //}
                gridData.push({
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                });
                valueSelectedinSelectionBox.name = '';
            };
            // For Procsr Dynamic Props
            $scope.removeRow = function (row, allProps, allPropsWithNovalue, gridData, valueSelectedinSelectionBox) {
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                var removedProperty = row.getProperty('name');
                var indexOfSelectedElement = allProps.indexOf(removedProperty);
                if (indexOfSelectedElement > -1) {
                    allPropsWithNovalue.push(removedProperty);
                }
                //alert(valueSelectedinSelectionBox.name);
                //valueSelectedinSelectionBox.name=''
                //alert(valueSelectedinSelectionBox.name);
            };
            // For Procsr Folder Props
            $scope.addFolderRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData) {
                $log.info(valueSelectedinSelectionBox.name);
                $log.info(row.getProperty('folderURI'));
                if (!valueSelectedinSelectionBox.name || !row.getProperty('folderURI')) {
                    showAlert('It is mandatory to set the folder URI and Type.');
                    return;
                }
                /*if (valueSelectedinSelectionBox.name === '' || row.getProperty('folderURI') === '' || typeof row.getProperty('folderURI') === 'undefined') {
                    showAlert('It is mandatory to set the folder URI and Type.');
                    return;
                }*/
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    folderURI: row.getProperty('folderURI'),
                    folderType: valueSelectedinSelectionBox.name,
                    folderDesc: row.getProperty('folderDesc'),
                    isMandatory: false,
                    allowAdd: false
                });
                var indexOfSelectedElement = allPropsWithNovalue.indexOf(valueSelectedinSelectionBox.name);
                if (indexOfSelectedElement !== -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }
                //}
                gridData.push({
                    folderURI: '',
                    folderType: '',
                    folderDesc: '',
                    allowAdd: 'showNoAddBox'
                });
                valueSelectedinSelectionBox.name = '';
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
                if (valueSelectedinSelectionBox.name === '') {
                    showAlert('It is mandatory to set credential type');
                    return;
                }
                if (row.getProperty('passwordDirtyState') === "nomatch") {
                    showAlert('The password and confirm password do not match');
                    return;
                }
                var index = gridData.indexOf(row.entity);
                gridData.splice(index, 1);
                gridData.push({
                    credentialURI: row.getProperty('credentialURI'),
                    credentialType: valueSelectedinSelectionBox.name,
                    userId: row.getProperty('userId'),
                    password: row.getProperty('password'),
                    idpType: valueSelectedinSelectionBoxIdp.name,
                    idpURI: row.getProperty('idpURI'),
                    allowAdd: false
                });
                var indexOfSelectedElement = allPropsWithNovalue.indexOf(valueSelectedinSelectionBox.name);
                if (indexOfSelectedElement !== -1) {
                    allPropsWithNovalue.splice(indexOfSelectedElement, 1);
                }
                var indexOfSelectedElementIdp = allPropsWithNovalueIdp.indexOf(valueSelectedinSelectionBoxIdp.name);
                if (indexOfSelectedElementIdp !== -1) {
                    allPropsWithNovalueIdp.splice(indexOfSelectedElementIdp, 1);
                }
                //}
                gridData.push({
                    credentialURI: '',
                    credentialType: '',
                    userId: '',
                    password: '',
                    idpType: '',
                    idpURI: '',
                    allowAdd: 'showNoAddBox'
                });
                valueSelectedinSelectionBox.name = '';
                valueSelectedinSelectionBoxIdp.name = '';
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
                var response = confirm("Are you  sure you want to cancel the Operation? Any unsaved changes will be lost");
                if (response === true) {
                    $location.$$search = {};
                    $location.path('/mailbox/getMailBox');
                }
            };
            $scope.backToMailbox = function () {
                var response = confirm("Are you  sure you want to leave this page? Any unsaved changes will be lost");
                if (response === true) {
                    var redirectToId = $location.search().mailBoxId;
                    $location.$$search = {};
                    $location.path('/mailbox/addMailBox').search('mailBoxId', redirectToId);
                }
            };
            $scope.processOtherHeaderValue = function (value) {
                var commaSplit = val.split(",");
                var colonSplit;
            };
            $scope.saveProcessor = function () {
                var lenDynamicProps = $scope.processorProperties.length;
                var commaSplit = [];
                var mandatoryArray = [];
                for (var i = 0; i < lenDynamicProps - 1; i++) {
                    var name = $scope.processorProperties[i].name;
                    var value = $scope.processorProperties[i].value;
                    if (name === 'url' || name === 'httpVersion' || name === 'contentType') {
                        mandatoryArray.push({
                            name: name,
                            value: value
                        });
                    }
                    if (name === 'httpVerb') {
                        mandatoryArray.push({
                            name: name,
                            value: $scope.verb
                        });
                    }
                    var index = $scope.allStaticProperties.indexOf($scope.processorProperties[i].name);
                    var indexMandatory;
                    if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                        indexMandatory = $scope.allMandatoryHttpProperties.indexOf($scope.processorProperties[i].name);
                    } else indexMandatory = $scope.allMandatoryFtpProperties.indexOf($scope.processorProperties[i].name);
                    if (index === -1 && indexMandatory === -1) {
                        $scope.processor.dynamicProperties.push({
                            name: $scope.processorProperties[i].name,
                            value: $scope.processorProperties[i].value
                        });
                    } else {
                        if (name === 'otherRequestHeader') {
                            commaSplit = $scope.processorProperties[i].value.split(",");
                        } else {
                            $scope.processor.remoteProcessorProperties[name] = $scope.processorProperties[i].value;
                        }
                    }
                }
                for (var i = 0; i < mandatoryArray.length; i++) {
                    if (mandatoryArray[i].value === '' || typeof mandatoryArray[i].value === 'undefined') {
                        showAlert('Enter MandatoryProperties');
                        return;
                    } else $scope.processor.remoteProcessorProperties[mandatoryArray[i].name] = mandatoryArray[i].value;
                }
                for (var i = 0; i < commaSplit.length; i++) {
                    var colonSplit = commaSplit[i].split(":");
                    $scope.processor.remoteProcessorProperties.otherRequestHeader.push({
                        name: colonSplit[0],
                        value: colonSplit[1]
                    });
                }
                console.log(commaSplit);
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
                    $scope.processor.linkedProfiles[i] = $scope.selectedProfiles[i].name;
                }
                $scope.processor.javaScriptURI = $scope.modal.uri;
                block.blockUI();
                if ($scope.isEdit) {
                    editRequest.reviseProcessorRequest.processor = $scope.processor;
                    $log.info($filter('json')(editRequest));
                    $scope.restService.put($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + $scope.processor.guid, $filter('json')(editRequest),
                        function (data, status) {
                            block.unblockUI();
                            if (status === 200) {
                                alert(data.reviseProcessorResponse.response.message);
                                //$scope.readOnlyProcessors = true;
                                $scope.readAllProcessors();
                                //$scope.readAllProfiles();
                            }
                            $scope.clearProps();
                        }
                    );
                } else {
                    addRequest.addProcessorToMailBoxRequest.processor = $scope.processor;
                    $log.info($filter('json')(addRequest));
                    $scope.restService.post($scope.base_url + '/' + $location.search().mailBoxId + '/processor', $filter('json')(addRequest),
                        function (data, status) {
                            block.unblockUI();
                            if (status === 200) {
                                alert(data.addProcessorToMailBoxResponse.response.message);
                                //$scope.readOnlyProcessors = true;
                                $scope.readAllProcessors();
                                //$scope.readAllProfiles();
                                $scope.isEdit = true;
                                $scope.processor.guid = data.addProcessorToMailBoxResponse.processor.guId;
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
            $scope.addNew = function () {
                $scope.loadOrigin();
                $scope.readAllProfiles();
            };
            $scope.resetProcessorType = function (model) {
                if (model.type === 'SWEEPER') {
                    $scope.processor.protocol = $scope.enumprotocoltype[4];
                    $scope.setFolderData(true);
                } else {
                    
                    $scope.processor.protocol = $scope.enumprotocoltype[0];
                    $scope.processorProperties = $scope.ftpMandatoryProperties;
                    $scope.setFolderData(false);
                }
            };
            $scope.resetProtocol = function (model) {
                console.log(model);
                //alert("iam called");
                if ($scope.isEdit) {
                    return;
                }
                if ($scope.processor.protocol === "FTPS" || $scope.processor.protocol === "SFTP") {
				
                    if ($scope.processor.type === "SWEEPER") $scope.processor.type = $scope.enumprocsrtype[0];
                    $scope.processorProperties = $scope.ftpMandatoryProperties;
                    $scope.setFolderData(false);
                } else if ($scope.processor.protocol === "HTTP" || $scope.processor.protocol === "HTTPS") {
				
					if ($scope.processor.type === "SWEEPER") $scope.processor.type = $scope.enumprocsrtype[0];
                    $scope.processorProperties = $scope.httpMandatoryProperties;
                    $scope.setFolderData(false);
                } else {
                    $scope.setFolderData(true);
                    $scope.processor.type = $scope.enumprocsrtype[2];

                }
            };
            $scope.setFolderData = function (mandatory) {
                if (mandatory) {
                    $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = ['RESPONSE_LOCATION'];
                    $scope.processorFolderProperties = [{
                        folderURI: '',
                        folderType: 'PAYLOAD_LOCATION',
                        folderDesc: '',
                        isMandatory: true,
                        allowAdd: false
                    }, {
                        folderURI: '',
                        folderType: '',
                        folderDesc: '',
                        isMandatory: false,
                        allowAdd: 'showNoAddBox'
                    }];
                } else {
                    $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = $scope.allStaticPropertiesForProcessorFolder;
                    $scope.processorFolderProperties = [{
                        folderURI: '',
                        folderType: '',
                        folderDesc: '',
                        isMandatory: false,
                        allowAdd: 'showNoAddBox'
                    }];
                    $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = $scope.allStaticPropertiesForProcessorFolder;
                }
            };
            // Editor Section Begins
            var editor;
            var rowObj;
            $scope.loadValueData = function (_editor) {
                editor = _editor;
                _editor.getSession().setUseWorker(false);
            };
            $scope.isModal = function (row) {
                rowObj = row;
                editor.setValue(row.getProperty('value'));
            };
            $scope.close = function () {
                rowObj.entity.value = editor.getValue();
            };
            // Editor Section Ends
        }
    ]);