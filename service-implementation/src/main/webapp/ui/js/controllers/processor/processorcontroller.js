var rest = myApp.controller(
    'ProcessorCntrlr', ['$scope',
        '$filter', '$location', '$log', '$blockUI',
        function ($scope, $filter,
                  $location, $log, $blockUI) {
            // To be Populated
        $scope.mailBoxId;
        var block = $blockUI.createBlockUI();

        // Function to modify the static properties to have additional properties of "binary"
        // and "passive" for FTP & FTPS protocols.
        $scope.modifyStaticPropertiesBasedOnProtocol = function() {
            if ($scope.processor.protocol == "FTP" || $scope.processor.protocol == "FTPS") {
                $scope.allStaticPropertiesThatAreNotAssignedValuesYet.push({"name":"Binary","id":"binary"},
                    {"name":"Passive","id":"passive"});
                $scope.allStaticProperties.push({"name":"Binary","id":"binary"},
                    {"name":"Passive","id":"passive"});
            } else {
                // Remove binary and passive properties from the array allStaticPropertiesThatAreNotAssignedValuesYet
                var indexOfBinary = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'binary');
                if(indexOfBinary !== -1 ) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfBinary, 1);

                var indexOfPassive = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet, 'passive');
                if (indexOfPassive !== -1) $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfPassive, 1);

                // Remove binary and passive properties from the array allStaticProperties
                indexOfBinary = getIndexOfId($scope.allStaticProperties, 'binary');
                if(indexOfBinary !== -1 ) $scope.allStaticProperties.splice(indexOfBinary, 1);
                indexOfPassive = getIndexOfId($scope.allStaticProperties, 'passive');
                if (indexOfPassive !== -1) $scope.allStaticProperties.splice(indexOfPassive, 1);

            }
        }

        $scope.loadOrigin = function () {
            //
            $scope.isEdit = false;
            $scope.isProcessorTypeSweeper = false;
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

            $scope.glyphIconColorForProcessorProperties = {
                color: "glyphicon-white"
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
            $scope.enumstats = [{"name":"Active","id":"ACTIVE"},
                {"name":"InActive","id":"INACTIVE"}];

            $scope.status = $scope.enumstats[0];

            // Enum for procsr type
            $scope.enumprocsrtype = [

                {"name":"Remote Downloader","id":"REMOTEDOWNLOADER"},
                {"name":"Remote Uploader","id":"REMOTEUPLOADER"},
                {"name":"Directory Sweeper","id":"SWEEPER"}
            ];
            $scope.procsrType = $scope.enumprocsrtype[0];
            // Enum for protocol type
            $scope.enumprotocoltype = [
                'FTP',
                'FTPS',
                'HTTP',
                'HTTPS',
                'SFTP'
            ];
            $scope.enumHttpVerb = [
                'GET',
                'PUT',
                'POST',
                'DELETE'
            ];
            $scope.verb = $scope.enumHttpVerb[0];
            $scope.processor.protocol = $scope.enumprotocoltype[0];

            // applying boolean value for chunked encoding
            $scope.booleanValues = [
                true,
                false
            ];
            // Procsr Dynamic Props
            $scope.ftpMandatoryProperties = [{
                name: 'URL',
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
                name: 'URL',
                value: '',
                allowAdd: false,
                isMandatory: true
            }, {
                name: 'HTTP Version',
                value: '1.1',
                allowAdd: false,
                isMandatory: true
            }, {
                name: 'HTTP Verb',
                value: '',
                allowAdd: false,
                isMandatory: true
            }, {
                name: 'Content Type',
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
                allowAdd: 'true'
            }];
            // Procsr Credential Props
            $scope.processorCredProperties = [{
                credentialURI: '',
                credentialType: '',
                userId: '',
                password: '',
                idpType: '',
                idpURI: '',
                allowAdd: 'true',
                passwordDirtyState: ''
            }];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYet = [{"name":"add new -->","id":"add new -->"},
                {"name":"Socket Timeout","id":"socketTimeout"}, {"name":"Connection Timeout","id":"connectionTimeout"},
                {"name":"Retry Attempts","id":"retryAttempts"}, {"name":"Chunked Encoding","id":"chunkedEncoding"},
                {"name":"Encoding Format","id":"encodingFormat"}, {"name":"Port","id":"port"},
                {"name":"OtherRequest Header","id":"otherRequestHeader"}, {"name":"Processed File Location","id":"processedfilelocation"}] ;

            $scope.allStaticProperties = [{"name":"Socket Timeout","id":"socketTimeout"}, {"name":"Connection Timeout","id":"connectionTimeout"},
                {"name":"Retry Attempts","id":"retryAttempts"}, {"name":"Chunked Encoding","id":"chunkedEncoding"},
                {"name":"Encoding Format","id":"encodingFormat"}, {"name":"Port","id":"port"},
                {"name":"OtherRequest Header","id":"otherRequestHeader"}, {"name":"Processed File Location","id":"processedfilelocation"}] ;

            // function to modify the static properties if the protocol is FTP or FTPS
            $scope.modifyStaticPropertiesBasedOnProtocol();

            $scope.allMandatoryFtpProperties = [{"name":"URL","id":"url"}];

            $scope.allMandatoryHttpProperties = [{"name":"HTTP Version","id":"httpVersion"},
                {"name":"HTTP Verb","id":"httpVerb"},
                {"name":"URL","id":"url"},
                {"name":"Content Type","id":"contentType"}];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = [{"name":"Payload Location","id":"PAYLOAD_LOCATION"},
                {"name":"Response Location","id":"RESPONSE_LOCATION"},
                {"name":"Target Location","id":"TARGET_LOCATION"}];

            $scope.allStaticPropertiesForProcessorFolder = [{"name":"Payload Location","id":"PAYLOAD_LOCATION"},
                {"name":"Response Location","id":"RESPONSE_LOCATION"},
                {"name":"Target Location","id":"TARGET_LOCATION"}];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential = [{"name":"Trust Store","id":"TRUST_STORE"},
                {"name":"Key Store","id":"KEY_STORE"}, {"name":"Login Credential","id":"LOGIN_CREDENTIAL"}];

            $scope.allStaticPropertiesForProcessorCredential = [{"name":"Trust Store","id":"TRUST_STORE"},
                {"name":"Key Store","id":"KEY_STORE"}, {"name":"Login Credential","id":"LOGIN_CREDENTIAL"}];

            $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp = [{name: "FTPS", id: "FTPS"},
                {name: "SFTP", id: "SFTP"}];

            $scope.allStaticPropertiesForProcessorCredentialIdp = [{name: "FTPS", id: "FTPS"},
                {name: "SFTP", id: "SFTP"}];

            $scope.disableAddNewTextBox = 'true';

            $scope.valueSelectedinSelectionBox = {
                value: ''
            };
            $scope.showAddNew = {
                value: 'false'
            };
            $scope.addedProperty = {
                value: ''
            };
            /*Folder*/
            $scope.valueSelectedinSelectionBoxForProcessorFolder = {
                value: ''
            };
            /*Credential*/
            $scope.valueSelectedinSelectionBoxForProcessorCredential = {
                value: ''
            };
            /*Credential Idp*/
            $scope.valueSelectedinSelectionBoxForProcessorCredentialIdp = {
                value: ''
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
            rowHeight: 80,
            columnDefs: [{
                field: "name",
                width: "40%",
                displayName: "Name*",
                enableCellEdit: false,
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYet" selected-value="valueSelectedinSelectionBox" prop-name={{row.getProperty(col.field)}} add-new="showAddNew" added-property="addedProperty" icon-color="glyphIconColorForProcessorProperties" />'
            }, {
                field: "value",
                width: "53%",
                displayName: "Value*",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="getId(allStaticProperties, row)">\n\
                    	<div ng-switch-when="">\n\
                            <div ng-switch on="valueSelectedinSelectionBox.value.id">\n\
                                <div ng-switch-when="">\n\
                                    <textarea ng-model="COL_FIELD" style="width:94%;height:45px" ng-maxLength=512 placeholder="required" />\n\
                                    <a ng-click="isModal(row)" data-toggle="modal" data-backdrop="static" data-keyboard="false" href="#valueModal" class="right">\n\
                                    <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                </div>\n\
                                <div ng-switch-when="chunkedEncoding">\n\
                                    <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD=false" ng-options="property for property in booleanValues"></select>\n\
                                </div>\n\
                                 <div ng-switch-when="binary">\n\
                                    <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD=false" ng-options="property for property in booleanValues"></select>\n\
                                </div>\n\
                                 <div ng-switch-when="passive">\n\
                                    <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD=false" ng-options="property for property in booleanValues"></select>\n\
                                </div>\n\
                                <div ng-switch-default>\n\
                                    <textarea ng-model="COL_FIELD" ng-init="COL_FIELD=null" ng-maxLength=2048 style="width:94%;height: 45px" placeholder="required" />\n\
                                    <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                                    <i class="glyphicon glyphicon-new-window"></i></a>\n\
                                </div>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-default>\n\
                            <textarea ng-model="COL_FIELD" ng-maxLength=2048 required style="width:94%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                        </div>\n\
                        <div ng-switch-when="otherRequestHeader">\n\
                            <textarea ng-model="COL_FIELD" ng-maxLength=512 required style="width:94%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                        </div>\n\
                        <div ng-switch-when="encodingFormat">\n\
                            <textarea ng-model="COL_FIELD" ng-maxLength=512 required style="width:94%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                        </div>\n\
                        <div ng-switch-when="chunkedEncoding">\n\
                            <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-options="property for property in booleanValues"></select>\n\
                        </div>\n\
                        <div ng-switch-when="binary">\n\
                            <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-options="property for property in booleanValues"></select>\n\
                        </div>\n\
                        <div ng-switch-when="passive">\n\
                            <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-options="property for property in booleanValues"></select>\n\
                        </div>\n\
                        <div ng-switch-when="httpVerb">\n\
                            <select ng-model="verb" ng-change="onVerbChange(verb)" ng-options="property for property in enumHttpVerb"></select>\n\
                        </div>\n\
                        <div ng-switch-when="httpVersion">\n\
                            <textarea ng-model="COL_FIELD" name="httpVersion" ng-pattern="' + $scope.httpVersionPattern + '" required style="width:94%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.httpVersion.$dirty && formAddPrcsr.httpVersion.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.httpVersion.$error.pattern><strong>Version should be 1.1</strong></span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="url">\n\
                            <textarea ng-model="COL_FIELD" name="propUrl" ng-pattern="' + $scope.inputPatternForURL + '" required style="width:94%;height: 45px" placeholder="required" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.propUrl.$dirty && formAddPrcsr.propUrl.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.propUrl.$error.pattern><strong>Enter valid URL</strong></span>\n\
                            </div></div>\n\
                        <div ng-switch-when="socketTimeout">\n\
                            <textarea ng-model="COL_FIELD" name="socketTimeout" required style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.socketTimeout.$dirty && formAddPrcsr.socketTimeout.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.socketTimeout.$error.pattern><strong>Invalid Data.</strong></span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="connectionTimeout">\n\
                            <textarea ng-model="COL_FIELD" name="connectionTimeout" required style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.connectionTimeout.$dirty && formAddPrcsr.connectionTimeout.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.connectionTimeout.$error.pattern><strong>Invalid Data.</strong></span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="retryAttempts">\n\
                            <textarea ng-model="COL_FIELD" name="retryAttempts" required style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.retryAttempts.$dirty && formAddPrcsr.retryAttempts.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.retryAttempts.$error.pattern><strong>Invalid Data.</strong></span>\n\
                            </div>\n\
                        </div>\n\
                        <div ng-switch-when="port">\n\
                            <textarea ng-model="COL_FIELD" name="port" required style="width:94%;height: 45px" placeholder="required" ng-pattern="' + $scope.numberPattern + '" />\n\
                            <a ng-click="isModal(row)" data-toggle="modal" href="#valueModal" class="right">\n\
                            <i class="glyphicon glyphicon-new-window"></i></a>\n\
                            <div ng-show="formAddPrcsr.port.$dirty && formAddPrcsr.port.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.port.$error.pattern><strong>Invalid Data.</strong></span>\n\
                            </div>\n\
                        </div>\n\
                    </div>'
            },  {

                field: "allowAdd",
                width: "7%",
                enableCellEdit: false,
                displayName: "Action",
                sortable: false,
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true">\n\
                                            <button   ng-click="addRow(row,valueSelectedinSelectionBox,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties,addedProperty)"><i class="glyphicon glyphicon-plus-sign" ng-class="glyphIconColorForProcessorProperties.color"></i></button></div>' +
                    '<div ng-switch-when="false">\n\
                                           <div ng-switch on="row.getProperty(\'isMandatory\')">' +
                    '<div ng-switch-when="true">-NA-</div>' +
                    '<div ng-switch-when="false"><button ng-click="removeRow(row,allStaticProperties,allStaticPropertiesThatAreNotAssignedValuesYet,processorProperties,valueSelectedinSelectionBox)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                    '</div>\n\
                                    </div>\n\
                             </div>'
            }]
        };

        $scope.getFolderId = function(objArray, row) {

            return getId(objArray, row.getProperty('folderType'));
        };

        $scope.getCredentialId = function(objArray, row) {

            return getId(objArray, row.getProperty('credentialType'));
        };

        $scope.getId = function(objArray, row) {

            if (row.getProperty('name') === '') {
                return '';
            }

            var ftpVal = getId($scope.allMandatoryFtpProperties, row.getProperty('name'));
            if (ftpVal.length > 0) {
                return ftpVal;
            }

            var httpVal = getId($scope.allMandatoryHttpProperties, row.getProperty('name'));
            if (httpVal.length > 0) {
                return httpVal;
            }

            var val = getId(objArray, row.getProperty('name'));
            if (val.length > 0) {
                return val;
            }

            return "Dor%^7#@"
        };

        $scope.getIdValue = function(name) {

            var ftpVal = getId($scope.allMandatoryFtpProperties, name);
            if (ftpVal.length > 0) {
                return ftpVal;
            }

            var httpVal = getId($scope.allMandatoryHttpProperties, name);
            if (httpVal.length > 0) {
                return httpVal;
            }

            return getId($scope.allStaticProperties, name);
        };

        $scope.getNameValue = function(id) {

            var ftpVal = getName($scope.allMandatoryFtpProperties, id);
            if (ftpVal.length > 0) {
                return ftpVal;
            }

            var httpVal = getName($scope.allMandatoryHttpProperties, id);
            if (httpVal.length > 0) {
                return httpVal;
            }

            return getName($scope.allStaticProperties, id);
        };

        $scope.gridOptionsForProcessorFolder = {
            data: 'processorFolderProperties',
            displaySelectionCheckbox: false,
            enableRowSelection: false,
            enableCellEditOnFocus: true,
            enablePaging: false,
            showFooter: false,
            rowHeight: 80,
            columnDefs: [{
                field: "folderURI",
                width: "33%",
                displayName: "URI*",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">' +
                    '<div ng-switch-when="false">' +
                    '<div ng-switch on="getFolderId(allStaticPropertiesForProcessorFolder, row)">\n\
                        <div ng-switch-when="PAYLOAD_LOCATION"><textarea ng-model="COL_FIELD"  style="width:95%;height:45px" required  placeholder="required" name="folderuripayload" ng-pattern="' + $scope.inputPatternForFolderURI + '" ng-maxLength=250 />\n\
                                <div ng-show="formAddPrcsr.folderuripayload.$dirty && formAddPrcsr.folderuripayload.$invalid">\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.folderuripayload.$error.pattern><strong>Invalid Folder URI.</strong></span>\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.folderuripayload.$error.maxlength><strong>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span>\n\
                           </div></div>\n\
                        <div ng-switch-when="RESPONSE_LOCATION"><textarea ng-model="COL_FIELD"  style="width:95%;height:45px" required  placeholder="required" name="folderuriresponse" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                            <div ng-show="formAddPrcsr.folderuriresponse.$dirty && formAddPrcsr.folderuriresponse.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.folderuriresponse.$error.pattern><strong>Invalid Folder URI.</strong></span>\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.folderuriresponse.$error.maxlength><strong>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span></div></div>\n\
						<div ng-switch-when="TARGET_LOCATION"><textarea ng-model="COL_FIELD"  style="width:95%;height:45px" required  placeholder="required" name="folderuritarget" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                            <div ng-show="formAddPrcsr.folderuritarget.$dirty && formAddPrcsr.folderuritarget.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.folderuritarget.$error.pattern><strong>Invalid Folder URI.</strong></span>\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.folderuritarget.$error.maxlength><strong>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span></div></div>\n\
                        </div></div>' +
                    '<div ng-switch-when="true">' +
                    '<textarea name="folderuridefault" ng-model="COL_FIELD" style="width:95%;height:45px" placeholder="required" ng-maxLength=250 ng-pattern="' + $scope.inputPatternForFolderURI + '"/>\n\
                    <div ng-show="formAddPrcsr.folderuridefault.$dirty && formAddPrcsr.folderuridefault.$invalid">\n\
                             <span class="help-block-custom" ng-show=formAddPrcsr.folderuridefault.$error.pattern><strong>Invalid Folder URI.</strong></span>\n\
                             <span class="help-block-custom" ng-show=formAddPrcsr.folderuridefault.$error.maxlength><strong>Folder URI cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span>\n\
                        </div></div></div>'
            }, {
                field: "folderType",
                width: "20%",
                displayName: "Type*",
                enableCellEdit: false,
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder" selected-value="valueSelectedinSelectionBoxForProcessorFolder" prop-name={{row.getProperty(col.field)}} add-new="showAddNew"/>'

            }, {
                field: "folderDesc",
                width: "40%",
                displayName: "Description",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">' +
                    '<div ng-switch-when="false">' +
                    '<div ng-switch on="getFolderId(allStaticPropertiesForProcessorFolder, row)">\n\
                        <div ng-switch-when="PAYLOAD_LOCATION"><textarea ng-model="COL_FIELD"  style="width:95%;height:45px" name="descriptionpayload" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250 />\n\
                                <div ng-show="formAddPrcsr.descriptionpayload.$dirty && formAddPrcsr.descriptionpayload.$invalid">\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.descriptionpayload.$error.pattern><strong>Invalid Description.</strong></span>\n\
                                    <span class="help-block-custom" ng-show=formAddPrcsr.descriptionpayload.$error.maxlength><strong>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span>\n\
                           </div></div>\n\
                        <div ng-switch-when="RESPONSE_LOCATION"><textarea ng-model="COL_FIELD"  style="width:95%;height:45px" name="descriptionresponse" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250 />\n\
                            <div ng-show="formAddPrcsr.descriptionresponse.$dirty && formAddPrcsr.descriptionresponse.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.descriptionresponse.$error.pattern><strong>Invalid Description.</strong></span>\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.descriptionresponse.$error.maxlength><strong>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span></div></div>\n\
						<div ng-switch-when="TARGET_LOCATION"><textarea ng-model="COL_FIELD"  style="width:95%;height:45px" name="descriptiontarget" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250 />\n\
                            <div ng-show="formAddPrcsr.descriptiontarget.$dirty && formAddPrcsr.descriptiontarget.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.descriptiontarget.$error.pattern><strong>Invalid Description.</strong></span>\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.descriptiontarget.$error.maxlength><strong>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span></div></div>\n\
                        </div></div>' +
                    '<div ng-switch-when="true">' +
                    '<textarea name="descriptiondefault" ng-model="COL_FIELD" style="width:95%;height:45px" ng-pattern="' + $scope.userInputPattern + '" ng-maxLength=250/>\n\
                    <div ng-show="formAddPrcsr.descriptiondefault.$dirty && formAddPrcsr.descriptiondefault.$invalid">\n\
                        <span class="help-block-custom" ng-show=formAddPrcsr.descriptiondefault.$error.pattern><strong>Invalid Description.</strong></span>\n\
                        <span class="help-block-custom" ng-show=formAddPrcsr.descriptiondefault.$error.maxlength><strong>Description cannot be longer than {{maximumLengthAllowedInGridForFolderDetails}} characters.</strong></span>\n\
                        </div></div></div>'
            }, {
                field: "allowAdd",
                width: "7%",
                displayName: "Action",
                enableCellEdit: false,
                sortable: false,
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true"><button ng-click="addFolderRow(row,valueSelectedinSelectionBoxForProcessorFolder,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,processorFolderProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
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
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">' +
                    '<div ng-switch-when="false">' +
                    '<div ng-switch on="getCredentialId(allStaticPropertiesForProcessorCredential, row)">\n\
                        <div ng-switch-when="TRUST_STORE"><textarea ng-model="COL_FIELD"  style="width:98%" name="credentialuritrust" ng-maxLength=128  row="3" />\n\
                          <div ng-show="formAddPrcsr.credentialuritrust.$dirty && formAddPrcsr.credentialuritrust.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.credentialuritrust.$error.maxlength><strong>Credential URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span>\n\
                       </div></div>\n\
                    <div ng-switch-when="KEY_STORE"><textarea ng-model="COL_FIELD"  style="width:95%" name="credentialurikey" ng-maxLength=128 />\n\
                        <div ng-show="formAddPrcsr.credentialurikey.$dirty && formAddPrcsr.credentialurikey.$invalid">\n\
                            <span class="help-block-custom" ng-show=formAddPrcsr.credentialurikey.$error.maxlength><strong>Credential URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span></div></div>\n\
                    <div ng-switch-when="LOGIN_CREDENTIAL"><textarea ng-model="COL_FIELD"  style="width:98%" name="credentialurilogin" ng-maxLength=128 row="3" />\n\
                            <div ng-show="formAddPrcsr.credentialurilogin.$dirty && formAddPrcsr.credentialurilogin.$invalid">\n\
                                <span class="help-block-custom" ng-show=formAddPrcsr.credentialurilogin.$error.maxlength><strong>Credential URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span>\n\
                       </div></div>\n\
                    </div></div>' +
                    '<div ng-switch-when="true">' +
                    '<textarea name="credentialdefault" ng-model="COL_FIELD" style="width:95%" ng-maxLength=128 />\n\
                <div ng-show="formAddPrcsr.credentialdefault.$dirty && formAddPrcsr.credentialdefault.$invalid">\n\
                     <span class="help-block-custom" ng-show=formAddPrcsr.credentialdefault.$error.maxlength><strong>Credential URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span></div>\n\
                </div></div>'
            }, {
                field: "credentialType",
                width: "17%",
                displayName: "Type*",
                enableCellEdit: false,
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential" selected-value="valueSelectedinSelectionBoxForProcessorCredential" prop-name={{row.getProperty(col.field)}} add-new="showAddNew"/>'
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
                cellTemplate: '<div class="dynamicComponentDirectiveForName" allow-add={{row.getProperty(\'allowAdd\')}} all-props="allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp" selected-value="valueSelectedinSelectionBoxForProcessorCredentialIdp" prop-name={{row.getProperty(col.field)}} add-new="showAddNew"/>'
            }, {
                field: "idpURI",
                width: "20%",
                displayName: "IdpURI",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')">' +
                    '<div ng-switch-when="false">' +
                    '<div ng-switch on="getCredentialId(allStaticPropertiesForProcessorCredential, row)">\n\
                        <div ng-switch-when="TRUST_STORE"><textarea ng-model="COL_FIELD"  style="width:98%" name="idpuritrust" ng-maxLength=128  row="3" />\n\
                            <div ng-show="formAddPrcsr.idpuritrust.$dirty && formAddPrcsr.idpuritrust.$invalid">\n\
                               <span class="help-block-custom" ng-show=formAddPrcsr.idpuritrust.$error.maxlength><strong>IDP URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span>\n\
                       </div></div>\n\
                       <div ng-switch-when="KEY_STORE"><textarea ng-model="COL_FIELD"  style="width:95%" name="idpurikey" ng-maxLength=128/>\n\
                        <div ng-show="formAddPrcsr.idpurikey.$dirty && formAddPrcsr.idpurikey.$invalid">\n\
                             <span class="help-block-custom" ng-show=formAddPrcsr.idpurikey.$error.maxlength><strong>IDP URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span></div></div>\n\
                    <div ng-switch-when="LOGIN_CREDENTIAL"><textarea ng-model="COL_FIELD"  style="width:98%" name="idpurilogin" ng-maxLength=128  row="3" />\n\
                            <div ng-show="formAddPrcsr.idpurilogin.$dirty && formAddPrcsr.idpurilogin.$invalid">\n\
                               <span class="help-block-custom" ng-show=formAddPrcsr.idpurilogin.$error.maxlength><strong>IDP URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span>\n\
                       </div></div>\n\
                    </div></div>' +
                    '<div ng-switch-when="true">' +
                    '<textarea name="idpuridefault" ng-model="COL_FIELD" style="width:95%" ng-maxLength=128 "/>\n\
                <div ng-show="formAddPrcsr.idpuridefault.$dirty && formAddPrcsr.idpuridefault.$invalid">\n\
                     <span class="help-block-custom" ng-show=formAddPrcsr.idpuridefault.$error.maxlength><strong>IDP URI cannot be longer than {{maximumLengthAllowedInGridForCredentialDetails}} characters.</strong></span></div>\n\
                </div></div>'
            }, {
                field: "allowAdd",
                width: "7%",
                displayName: "Action",
                enableCellEdit: false,
                sortable: false,
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true"><button ng-click="addCredentialRow(row,valueSelectedinSelectionBoxForProcessorCredential,valueSelectedinSelectionBoxForProcessorCredentialIdp,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,processorCredProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
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
                newVal.currentPage = 1;
            }
        }, true);
        $scope.$watch('filterOptions', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.readAllProcessors();
            }
        }, true);
        $scope.editableInPopup = '<button class="btn btn-default btn-xs" ng-click="editProcessor(row.getProperty(\'guid\'),true)"><i class="glyphicon glyphicon-pencil"></i></button>';
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
                sortable: false,
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
        $scope.editProcessor = function (processorId, blockuiFlag) {
            if (blockuiFlag === true) {
                block.blockUI();
            }
            $scope.loadOrigin();
            $scope.isEdit = true;
            var procsrId = processorId;;
            $scope.restService.get($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + procsrId, //Get mail box Data
                function (data) {
                    $log.info($filter('json')(data));
                    //Fix: Reading profile in procsr callback
                    $scope.restService.get($scope.base_url + '/profile', //Get mail box Data
                        function (profData) {
                            $log.info($filter('json')(profData));
                            if (blockuiFlag === true) {
                                block.unblockUI();
                            }
                            $scope.allProfiles = profData.getProfileResponse.profiles;
                            $scope.clearProps();
                            $scope.processor.guid = data.getProcessorResponse.processor.guid;
                            $scope.processor.name = data.getProcessorResponse.processor.name;
                            //$scope.processor.type = data.getProcessorResponse.processor.type;
                            $scope.modal.uri = data.getProcessorResponse.processor.javaScriptURI;
                            $scope.processor.description = data.getProcessorResponse.processor.description;

                            (data.getProcessorResponse.processor.status === 'ACTIVE') ? $scope.status = $scope.enumstats[0]
                                : $scope.status = $scope.enumstats[1];

                            $scope.setTypeDuringProcessorEdit(data.getProcessorResponse.processor.type);
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
                                            name: $scope.getNameValue(prop),
                                            value: (prop === 'otherRequestHeader' || prop === 'httpVerb') ? $scope.setRemotePropData(json_data[prop], prop) : json_data[prop],
                                            allowAdd: false,
                                            isMandatory: ($scope.allMandatoryHttpProperties.indexOf(prop) === -1) ? false : true
                                        });
                                    } else {
                                        $scope.ftpMandatoryProperties.push({
                                            name: $scope.getNameValue(prop),
                                            value: (prop === 'otherRequestHeader') ? $scope.setRemotePropData(json_data[prop], prop) : json_data[prop],
                                            allowAdd: false,
                                            isMandatory: ($scope.allMandatoryFtpProperties.indexOf(prop) === -1) ? false : true
                                        });
                                    }
                                    var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYet,prop);
                                    if (indexOfElement !== -1) {
                                        $scope.allStaticPropertiesThatAreNotAssignedValuesYet.splice(indexOfElement, 1);
                                    }
                                }
                            }

                            // Condition which executes only if OtherRequest Headers comes with an empty value
                            // So that there is no need to show it in the UI
                            if (otherReqIndex !== -1) {
                                if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                                    $scope.httpMandatoryProperties.splice(otherReqIndex - 1, 1);
                                } else {
                                    $scope.ftpMandatoryProperties.splice(otherReqIndex - 1, 1);
                                }
                                $scope.allStaticPropertiesThatAreNotAssignedValuesYet.push({"name":"OtherRequest Header","id":"otherRequestHeader"});
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
                                    folderType: getName($scope.allStaticPropertiesForProcessorFolder, data.getProcessorResponse.processor.folders[i].folderType),
                                    folderDesc: data.getProcessorResponse.processor.folders[i].folderDesc,
                                    isMandatory: ($scope.processor.protocol === 'SWEEPER' && data.getProcessorResponse.processor.folders[i].folderType === 'PAYLOAD_LOCATION') ? true : false,
                                    allowAdd: false
                                });
                                var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder,
                                    data.getProcessorResponse.processor.folders[i].folderType);

                                if (indexOfElement !== -1) {

                                    $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder.splice(indexOfElement, 1);
                                }
                            };
                            $scope.processorFolderProperties.push({
                                folderURI: '',
                                folderType: '',
                                folderDesc: '',
                                allowAdd: 'true'
                            });
                            $scope.processorCredProperties.splice(0, 1); //Removing now so that the add new option always shows below the available properties
                            for (var i = 0; i < data.getProcessorResponse.processor.credentials.length; i++) {
                                $scope.processorCredProperties.push({
                                    credentialURI: data.getProcessorResponse.processor.credentials[i].credentialURI,
                                    credentialType: getName($scope.allStaticPropertiesForProcessorCredential, data.getProcessorResponse.processor.credentials[i].credentialType),
                                    userId: data.getProcessorResponse.processor.credentials[i].userId,
                                    password: data.getProcessorResponse.processor.credentials[i].password,
                                    idpType: getName($scope.allStaticPropertiesForProcessorCredentialIdp, data.getProcessorResponse.processor.credentials[i].idpType),
                                    idpURI: data.getProcessorResponse.processor.credentials[i].idpURI,
                                    allowAdd: false
                                });

                                var indexOfElement = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential,
                                    data.getProcessorResponse.processor.credentials[i].credentialType);

                                if (indexOfElement !== -1) {
                                    $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredential.splice(indexOfElement, 1);
                                }

                                var indexOfElementIdp = getIndexOfId($scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp,
                                    data.getProcessorResponse.processor.credentials[i].idpType);

                                if (indexOfElementIdp !== -1) {
                                    $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorCredentialIdp.splice(indexOfElementIdp, 1);
                                }
                            };
                            $scope.processorCredProperties.push({
                                credentialURI: '',
                                credentialType: '',
                                userId: '',
                                password: '',
                                idpType: '',
                                idpURI: '',
                                allowAdd: 'true'
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


            $scope.setTypeDuringProcessorEdit = function(protoId) {

                console.log(protoId);
                console.log(getIndexOfId($scope.enumprocsrtype, protoId))
                $scope.procsrType = $scope.enumprocsrtype[getIndexOfId($scope.enumprocsrtype, protoId)];
            };

        // For Procsr Dynamic Props
        $scope.addRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData, addedProperty) {
            // validation
            //$log.info(valueSelectedinSelectionBox.value.id);
            //$log.info(row.getProperty('value'));

            if (valueSelectedinSelectionBox.value === null) {
                showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                return;
            }

            var attrName = '';

            if (valueSelectedinSelectionBox.value.id === 'socketTimeout' || valueSelectedinSelectionBox.value.id === 'connectionTimeout'
                || valueSelectedinSelectionBox.value.id === 'retryAttempts' || valueSelectedinSelectionBox.value.id === 'port') {

                if (!($scope.numberPattern.test(row.getProperty('value')))) {

                    showAlert('Value should be a number.', 'error');
                    return;
                }
            }

            if (valueSelectedinSelectionBox.value.id !== 'add new -->') {

                attrName = valueSelectedinSelectionBox.value.name;
            } else if (addedProperty.value !== '') {
                attrName = addedProperty.value;
            }

            // row.getProperty('value') is converted to string since the Propertyvalue may contain boolean value for the property
            // "chunkedEncoding" if the user has entered the value of "false", then checking the !row.getProperty('value')
            // will always be true and the below alert will be displayed.

            console.log(row.getProperty('value'));
            // console.log(row.getProperty('value').toString());

            var rowVal;

            if (row.getProperty('value') === null) {
                rowVal = ''
            } else {
                rowVal = row.getProperty('value').toString();
            }

            if (!attrName || !rowVal) {

                showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                return;
            }

            if (checkNameDuplicate(gridData, attrName)) {

                showAlert('Name already added.', 'error');
                return;
            }

            var indexOfSelectedElement = getIndex(allPropsWithNovalue, attrName);

            // Displays an alert if the dynamic property entered by user is already in static properties provided
            if ((valueSelectedinSelectionBox.value.name == 'add new -->') && (indexOfSelectedElement !== -1) ) {
                showAlert('The property is already available in dropdown provided.Please use the appropriate property from dropdown menu','error');
                return;
            }
            /*$scope.informer.inform("error message", "error");
             $scope.informer.inform("info message", "info");
             $scope.allInfos = $scope.informer.allInfos;
             $scope.remove = $scope.informer.remove;*/

            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            gridData.push({
                name: attrName,
                value: row.getProperty('value'),
                allowAdd: false,
                isMandatory: false
            });

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

            valueSelectedinSelectionBox.value = '';
            addedProperty.value = '';
        };

        // For Procsr Dynamic Props
        $scope.removeRow = function (row, allProps, allPropsWithNovalue, gridData, valueSelectedinSelectionBox) {

            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            var removedProperty = row.getProperty('name');
            var indexOfSelectedElement = getIndex(allProps, removedProperty);
            if (indexOfSelectedElement > -1) {
                allPropsWithNovalue.push(allProps[indexOfSelectedElement]);
            }
        };

        // For Procsr Folder Props
        $scope.addFolderRow = function (row, valueSelectedinSelectionBox, allPropsWithNovalue, gridData) {

            //$log.info(valueSelectedinSelectionBox.value.id);
           // $log.info(row.getProperty('folderURI'));

            if (valueSelectedinSelectionBox.value === null) {
                showAlert('It is mandatory to set the folder URI and Type.', 'error');
                return;
            }

            if (!valueSelectedinSelectionBox.value.id || !row.getProperty('folderURI')) {
                showAlert('It is mandatory to set the folder URI and Type.', 'error');
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
                folderType: valueSelectedinSelectionBox.value.name,
                folderDesc: row.getProperty('folderDesc'),
                isMandatory: false,
                allowAdd: false
            });

            var indexOfSelectedElement = getIndex(allPropsWithNovalue, valueSelectedinSelectionBox.value.name);
            if (indexOfSelectedElement !== -1) {
                allPropsWithNovalue.splice(indexOfSelectedElement, 1);
            }
            //}
            gridData.push({
                folderURI: '',
                folderType: '',
                folderDesc: '',
                allowAdd: 'true'
            });
            valueSelectedinSelectionBox.value = '';
        };

        // For Procsr Folder Props
        $scope.removeFolderRow = function (row, allProps, allPropsWithNovalue, gridData) {
            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            var removedProperty = row.getProperty('folderType');
            var indexOfSelectedElement = getIndex(allProps, removedProperty);
            if (indexOfSelectedElement > -1) {
                allPropsWithNovalue.push(allProps[indexOfSelectedElement]);
            }
        };

        // For Procsr Credentials Props
        $scope.addCredentialRow = function (row, valueSelectedinSelectionBox, valueSelectedinSelectionBoxIdp, allPropsWithNovalue, allPropsWithNovalueIdp, gridData) {

            if (valueSelectedinSelectionBox.value === null) {
                showAlert('It is mandatory to set the folder URI and Type.', 'error');
                return;
            }

            if (!valueSelectedinSelectionBox.value.id) {
                showAlert('It is mandatory to set credential type', 'error');
                return;
            }

            if (row.getProperty('passwordDirtyState') === "nomatch") {
                showAlert('The password and confirm password do not match', 'error');
                return;
            }

            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            gridData.push({
                credentialURI: row.getProperty('credentialURI'),
                credentialType: valueSelectedinSelectionBox.value.name,
                userId: row.getProperty('userId'),
                password: row.getProperty('password'),
                idpType: valueSelectedinSelectionBoxIdp.value.name,
                idpURI: row.getProperty('idpURI'),
                allowAdd: false
            });
            var indexOfSelectedElement = getIndex(allPropsWithNovalue, valueSelectedinSelectionBox.value.name);
            if (indexOfSelectedElement !== -1) {
                allPropsWithNovalue.splice(indexOfSelectedElement, 1);
            }
            var indexOfSelectedElementIdp = getIndex(allPropsWithNovalueIdp, valueSelectedinSelectionBoxIdp.value.name);
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
                allowAdd: 'true'
            });
            valueSelectedinSelectionBox.value = '';
            valueSelectedinSelectionBoxIdp.value = '';
        };
        // For Procsr Credentials Props
        $scope.removeCredentialRow = function (row, allProps, allPropsIdp, allPropsWithNovalue, allPropsWithNovalueIdp, gridData) {
            var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            var removedProperty = row.getProperty('credentialType');
            var indexOfSelectedElement = getIndex(allProps, removedProperty);
            if (indexOfSelectedElement > -1) {
                allPropsWithNovalue.push(allProps[indexOfSelectedElement]);
            }
            var removedIdpProperty = row.getProperty('idpType');
            var indexOfSelectedElementIdp = getIndex(allPropsIdp, removedIdpProperty);
            if (indexOfSelectedElementIdp > -1) {
                allPropsWithNovalueIdp.push(allPropsIdp[indexOfSelectedElementIdp]);
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


        $scope.getIndex = function(name) {

            var ftpVal = getIndex($scope.allMandatoryFtpProperties, name);
            if (ftpVal !== -1) {
                return ftpVal;
            }

            var httpVal = getIndex($scope.allMandatoryHttpProperties, name);
            if (httpVal !== -1) {
                return httpVal;
            }

            return getId($scope.allStaticProperties, name);
        };

        $scope.saveProcessor = function () {

            var lenDynamicProps = $scope.processorProperties.length;
            var commaSplit = [];
            var mandatoryArray = [];

            for (var i = 0; i < lenDynamicProps - 1; i++) {

                var index =  $scope.getIndex($scope.processorProperties[i].name);

                var name = (index == -1) ? $scope.processorProperties[i].name : $scope.getIdValue($scope.processorProperties[i].name);
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

                var index = getIndex($scope.allStaticProperties, $scope.processorProperties[i].name);
                var indexMandatory;

                if ($scope.processor.protocol === 'HTTP' || $scope.processor.protocol === 'HTTPS') {
                    indexMandatory = getIndex($scope.allMandatoryHttpProperties, $scope.processorProperties[i].name);
                } else indexMandatory = getIndex($scope.allMandatoryFtpProperties, $scope.processorProperties[i].name);

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
                    showAlert('Enter MandatoryProperties', 'error');
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
                    folderType: getId($scope.allStaticPropertiesForProcessorFolder, $scope.processorFolderProperties[i].folderType),
                    folderDesc: $scope.processorFolderProperties[i].folderDesc
                });
            }

            var lenCredentialProps = $scope.processorCredProperties.length;

            for (var i = 0; i < lenCredentialProps - 1; i++) {

                $scope.processor.credentials.push({
                    credentialURI: $scope.processorCredProperties[i].credentialURI,
                    credentialType: getId($scope.allStaticPropertiesForProcessorCredential, $scope.processorCredProperties[i].credentialType),
                    userId: $scope.processorCredProperties[i].userId,
                    password: $scope.processorCredProperties[i].password,
                    idpType:  getId($scope.allStaticPropertiesForProcessorCredentialIdp, $scope.processorCredProperties[i].idpType),
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
                editRequest.reviseProcessorRequest.processor.status = $scope.status.id;
                editRequest.reviseProcessorRequest.processor.type = $scope.procsrType.id;

                $log.info($filter('json')(editRequest));
                $scope.restService.put($scope.base_url + '/' + $location.search().mailBoxId + '/processor/' + $scope.processor.guid, $filter('json')(editRequest),
                    function (data, status) {

                        if (status === 200) {
                            $scope.editProcessor($scope.processor.guid, false);
                            if (data.reviseProcessorResponse.response.status === 'success') {
                                showSaveMessage(data.reviseProcessorResponse.response.message, 'success');
                            } else {
                                showSaveMessage(data.reviseProcessorResponse.response.message, 'error');
                            }
                            //$scope.readOnlyProcessors = true;
                            $scope.readAllProcessors();
                            //$scope.readAllProfiles();
                        } else {
                            showSaveMessage("Error while saving processor", 'error');
                        }
                        block.unblockUI();
                        $scope.clearProps();
                    }
                );
            } else {
                addRequest.addProcessorToMailBoxRequest.processor = $scope.processor;
                addRequest.addProcessorToMailBoxRequest.processor.status = $scope.status.id;
                addRequest.addProcessorToMailBoxRequest.processor.type = $scope.procsrType.id;

                $log.info($filter('json')(addRequest));
                $scope.restService.post($scope.base_url + '/' + $location.search().mailBoxId + '/processor', $filter('json')(addRequest),
                    function (data, status) {

                        if (status === 200) {

                            //$scope.readOnlyProcessors = true;
                            $scope.readAllProcessors();
                            //$scope.readAllProfiles();
                            $scope.isEdit = true;
                            $scope.processor.guid = data.addProcessorToMailBoxResponse.processor.guId;
                            $scope.editProcessor($scope.processor.guid, false);
                            if (data.addProcessorToMailBoxResponse.response.status === 'success') {
                                showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'success');
                            } else {
                                showSaveMessage(data.addProcessorToMailBoxResponse.response.message, 'error');
                            }
                        } else {
                            showSaveMessage("Error while saving processor", 'error');
                        }
                        $scope.clearProps();
                        block.unblockUI();
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

            $scope.resetStaticAndMandatoryProps();
            if (model.id === 'SWEEPER') {
                $scope.isProcessorTypeSweeper = true;
                $scope.processor.protocol = "SWEEPER"
                $scope.setFolderData(true);
                $scope.processorProperties = [{
                    name: '',
                    value: '',
                    allowAdd: true,
                    isMandatory: false
                }];
            } else {
                $scope.isProcessorTypeSweeper = false;
                $scope.processor.protocol = $scope.enumprotocoltype[0];
                $scope.processorProperties = $scope.ftpMandatoryProperties;
                $scope.setFolderData(false);
            }
        };
        $scope.resetProtocol = function (model) {

            console.log(model);
            $scope.resetStaticAndMandatoryProps();
            if ($scope.processor.protocol === "FTP" || $scope.processor.protocol === "FTPS" || $scope.processor.protocol === "SFTP") {

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
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = [{name:'Response Location', id:'RESPONSE_LOCATION'}];
                $scope.processorFolderProperties = [{
                    folderURI: '',
                    folderType: 'Payload Location',
                    folderDesc: '',
                    isMandatory: true,
                    allowAdd: false
                }, {
                    folderURI: '',
                    folderType: '',
                    folderDesc: '',
                    isMandatory: false,
                    allowAdd: 'true'
                }];
            } else {
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = $scope.allStaticPropertiesForProcessorFolder;
                $scope.processorFolderProperties = [{
                    folderURI: '',
                    folderType: '',
                    folderDesc: '',
                    isMandatory: false,
                    allowAdd: 'true'
                }];
                $scope.allStaticPropertiesThatAreNotAssignedValuesYetInProcessorFolder = $scope.allStaticPropertiesForProcessorFolder;
            }
        };

        $scope.resetStaticAndMandatoryProps = function () {


            $scope.allStaticPropertiesThatAreNotAssignedValuesYet = [{"name":"add new -->","id":"add new -->"},
                {"name":"Socket Timeout","id":"socketTimeout"}, {"name":"Connection Timeout","id":"connectionTimeout"},
                {"name":"Retry Attempts","id":"retryAttempts"}, {"name":"Chunked Encoding","id":"chunkedEncoding"},
                {"name":"Encoding Format","id":"encodingFormat"}, {"name":"Port","id":"port"},
                {"name":"OtherRequest Header","id":"otherRequestHeader"}, {"name":"Processed File Location","id":"processedfilelocation"}] ;

            // function to modify the static properties if the protocol is FTP or FTPS
            $scope.modifyStaticPropertiesBasedOnProtocol();

            $scope.ftpMandatoryProperties = [{
                name: 'URL',
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
                name: 'URL',
                value: '',
                allowAdd: false,
                isMandatory: true
            }, {
                name: 'HTTP Version',
                value: '1.1',
                allowAdd: false,
                isMandatory: true
            }, {
                name: 'HTTP Verb',
                value: '',
                allowAdd: false,
                isMandatory: true
            }, {
                name: 'Content Type',
                value: '',
                allowAdd: false,
                isMandatory: true
            }, {
                name: '',
                value: '',
                allowAdd: true,
                isMandatory: false
            }];
        }

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

        $scope.changeGlyphIconColor = function (icon, currentValue) {

            if (currentValue !== '') {
                icon.color = "glyphicon-red";
            } else {
                icon.color = "glyphicon-white";
            };
        };

    }
]);