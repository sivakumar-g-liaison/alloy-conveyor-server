var rest = myApp.controller('AccountCntrlr', ['$scope', '$filter', '$location', '$log', '$blockUI',
    function ($scope, $filter, $location, $log, $blockUI) {

        var block = $blockUI.createBlockUI();
        
         $scope.loadOrigin = function() {
          
             $scope.isAccountEdit = false;
            
            //Model for Add UP
            addRequest = $scope.addRequest = {
                addUserAccountRequest: {
                    account: {},
                    idpProfiles: []
                }
            };
            
            //Model for Add UP
            editRequest = $scope.editRequest = {
                reviseUserAccountRequest: {
                    account: {},
                    idpProfiles: []
                }
            };
            
            $scope.account = {
                guid: "",
                description: "",
                activeState: "",
                loginId: "",
                smsNumber: "",
                tmpPswdHash: "",
                tmpPswdExp: "",
                crmURI: "",
                currencyFormat: "",
                numberFormat: "",
                dateFormat: "",
                timeZone: "",
                accountType: {},
                language: {}
            };
            
            $scope.accountType = {
                name: ""
            }
            
            $scope.language = {
                name: ""
            }
            
            $scope.idpProfiles = [];

            $scope.enumStatus = [
                'ACTIVE',
                'INACTIVE'
            ];

            $scope.enumAccountType = [
                'machine',
                'person'
            ];

            $scope.enumLanguageType = [
                'english'
            ];
            
            $scope.enumGateWayType = [
                'SFTP'
            ];
            
            $scope.enumIdpProvider = [
                'Provider'
            ];

            $scope.account.activeState = $scope.enumStatus[0];
            $scope.accountType.name = $scope.enumAccountType[0];
            $scope.language.name = $scope.enumLanguageType[0];

            // Idp Profile Props
            $scope.idpProfileProperties = [{
                guid: '',
                domain: '',
                gatewayType: '',
                idpProvider: '',
                allowAdd: true
            }];

            $scope.gateproperty = "-- select--";
            $scope.idpproperty = "-- select--";
        }
        
        $scope.loadOrigin();
        
        $scope.load = function() {
            
            if ($location.search().accountId !== '' && typeof $location.search().accountId !== 'undefined') { // Edit Mode On               

                $scope.isAccountEdit = true;
                $scope.accountId = $location.search().accountId;

                block.blockUI();
                $scope.restService.get($scope.base_url + "/account/" + $scope.accountId, //Get mail box Data
                    function (data) {

                        $log.info(data);
                        
                        block.unblockUI();
                        
                        $scope.account.guid = data.getUserAccountResponse.account.guid;
                        $scope.account.activeState = data.getUserAccountResponse.account.activeState;
                        $scope.accountType.name = data.getUserAccountResponse.account.accountType.name;
                        $scope.language.name = data.getUserAccountResponse.account.language.name;
                        $scope.account.crmURI = data.getUserAccountResponse.account.crmURI;
                        $scope.account.currencyFormat = data.getUserAccountResponse.account.currencyFormat;
                        $scope.account.numberFormat = data.getUserAccountResponse.account.numberFormat;
                        $scope.account.timeZone = data.getUserAccountResponse.account.timeZone;
                        $scope.account.dateFormat = data.getUserAccountResponse.account.dateFormat;
                        $scope.account.description = data.getUserAccountResponse.account.description;
                        $scope.account.smsNumber = data.getUserAccountResponse.account.smsNumber;
                        $scope.account.loginId = data.getUserAccountResponse.account.loginId;
                        
                        $scope.clearProps();
                        $scope.idpProfileProperties = [];
                        
                        for (var i = 0; i < data.getUserAccountResponse.idpProfiles.length; i++) {
                        
                            $scope.idpProfileProperties.push({
                            
                                guid: data.getUserAccountResponse.idpProfiles[i].guid,
                                domain: data.getUserAccountResponse.idpProfiles[i].loginDomain,
                                gatewayType: data.getUserAccountResponse.idpProfiles[i].gatewayType,
                                idpProvider: data.getUserAccountResponse.idpProfiles[i].idpProvider,
                                allowAdd: false
                            });
                        }
                        
                        $scope.idpProfileProperties.push({
                        
                            guid: '',
                            domain: '',
                            gatewayType: '',
                            idpProvider: '',
                            allowAdd: true
                        });
                    }
                );
            }
        }
        
        $scope.load();

        $scope.gridOptionsForProcessorIdp = {
            data: 'idpProfileProperties',
            displaySelectionCheckbox: false,
            enableRowSelection: false,
            enableCellEditOnFocus: true,
            enablePaging: false,
            showFooter: false,
            rowHeight: 40,
            columnDefs: [{
                field: "domain",
                width: "30%",
                displayName: "Domain",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')"><div ng-switch-when="false"><input type="text" ng-model="COL_FIELD" class="textboxingrid" placeholder="required" readonly></div><div ng-switch-default><input type="text" ng-model="COL_FIELD" class="textboxingrid" placeholder="required"></div></div>'

            }, {
                field: "gatewayType",
                width: "30%",
                displayName: "Gateway Type",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')"><div ng-switch-when="false">{{COL_FIELD}}</div><div ng-switch-when="true"><select ng-model="COL_FIELD" ng-options="property for property in enumGateWayType"><option value="">-- select--</option><select></div></div>'
            }, {
                field: "idpProvider",
                width: "30%",
                displayName: "Provider",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="row.getProperty(\'allowAdd\')"><div ng-switch-when="false">{{COL_FIELD}}</div><div ng-switch-when="true"><select ng-model="COL_FIELD" ng-options="property for property in enumIdpProvider"><option value="">-- select--</option><select></div></div>'

            }, {
                field: "allowAdd",
                width: "10%",
                displayName: "Action",
                enableCellEdit: false,
                cellTemplate: '<div ng-switch on="row.getProperty(col.field)">' +
                    '<div ng-switch-when="true"><button ng-click="addRow(row, gateproperty, idpproperty, idpProfileProperties)"><i class="glyphicon glyphicon-plus-sign glyphicon-white"></i></button></div>' +
                    '<div ng-switch-when="false"><button ng-click="removeRow(row, idpProfileProperties)"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                    '</div>'

            }]
        };
		
		$scope.saveAccount = function () {
			
			var len = $scope.idpProfileProperties.length - 1;
            
            if (!len) {
               
               showAlert('AtLeast one profile is required');
               return;
            }
			
			for (var i = 0; i < len; i++) {

                    var guid = $scope.idpProfileProperties[i].guid;
                    var domain = $scope.idpProfileProperties[i].domain;
                    var gatewayType = $scope.idpProfileProperties[i].gatewayType;
					var idpProvider = $scope.idpProfileProperties[i].idpProvider;
					
				$scope.idpProfiles.push({
                    
                    guid: guid,
					loginDomain: domain,
					gatewayType: gatewayType,
					idpProvider: idpProvider
				});
			}
			
			block.blockUI();
			
			if ($scope.isAccountEdit) {
                
                editRequest.reviseUserAccountRequest.account = $scope.account;
               editRequest.reviseUserAccountRequest.idpProfiles = $scope.idpProfiles;
                editRequest.reviseUserAccountRequest.account.accountType = $scope.accountType;
                editRequest.reviseUserAccountRequest.account.language = $scope.language;
                
                $log.info($filter('json')(editRequest));
                $scope.restService.put($scope.base_url + '/account/' +$scope.accountId, $filter('json')(editRequest),
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200) {
                            alert(data.reviseUserAccountResponse.response.message);
                        }

                        $scope.idpProfiles = [];
                        $scope.idpProfileProperties.splice(idpProfileProperties.length - 1,1)
                        
                        $scope.idpProfileProperties.push({
                            domain: '',
                            gatewayType: '',
                            idpProvider: '',
                            allowAdd: true
                        });
                    }
                );
                
            } else {
                
               addRequest.addUserAccountRequest.account = $scope.account;
               addRequest.addUserAccountRequest.idpProfiles = $scope.idpProfiles;
                addRequest.addUserAccountRequest.account.accountType = $scope.accountType;
                addRequest.addUserAccountRequest.account.language = $scope.language;
                
                $log.info($filter('json')(addRequest));
                $scope.restService.post($scope.base_url + '/account', $filter('json')(addRequest),
                    function (data, status) {

                        block.unblockUI();
                        if (status === 200) {
                            alert(data.addUserAccountResponse.response.message);
                        }

                        $scope.clearProps();
                        
                        $scope.idpProfileProperties.push({
                            domain: '',
                            gatewayType: '',
                            idpProvider: '',
                            allowAdd: true
                        });
                    }
                ); 
            }
            	
		}

        $scope.addRow = function (row, gateproperty, idpproperty, gridData) {
            
        	//alert(prop);
            console.log(row.getProperty('domain'));
            console.log(row.getProperty('gatewayType'));
            
            if (!row.getProperty('domain') || !row.getProperty('gatewayType') || !row.getProperty('idpProvider')) {
                showAlert('Enter Values');
                return;
            }
            
        	var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
            
            gridData.push({
            
                guid: '',
            	domain: row.getProperty('domain'),
            	gatewayType: row.getProperty('gatewayType'),
            	idpProvider: row.getProperty('idpProvider'),
                allowAdd: false
            });
            
            gridData.push({
            
                guid: '',
            	domain: '',
                gatewayType: '',
                idpProvider: '',
                allowAdd: true
            });
            
        }
        
        $scope.removeRow = function (row, gridData) {
        	
        	var index = gridData.indexOf(row.entity);
            gridData.splice(index, 1);
        }


        $scope.clearProps = function() {
            $scope.idpProfiles = [];
            $scope.idpProfileProperties = [];
        }
    }
]);