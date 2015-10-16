myApp.controller(
    'CredentialHanlderCntrlr', ['$rootScope', '$scope', '$blockUI', '$timeout',
        function ($rootScope, $scope, $blockUI, $timeout) {
        
            $scope.processCredentialDetails = function() {
                  for (var i = 0; i <  $scope.$parent.processorCredProperties.length; i++) {
                    var credObj =  $scope.$parent.processorCredProperties[i];
                    if(credObj.credentialType == 'SSH_KEYPAIR') {
                        $scope.sshkeyModal.sshKeyPairGroupId = credObj.idpURI;
                    }
                    if (credObj.credentialType == 'TRUSTSTORE_CERT') {
                        $scope.truststoreModal.trustStoreGroupId = credObj.idpURI;
                    }
                }
            }
            $scope.addTruststoreDetails = function() {
                $scope.removeTruststoreDetails();
                $scope.$parent.processorCredProperties.splice(0,0,{
                                credentialURI: '',
                                credentialType: 'TRUSTSTORE_CERT',
                                credentialDisplayType: 'TrustStore Certificate',
                                userId: '',
                                password:'',
                                idpURI: $scope.truststoreModal.trustStoreGroupId,
                                idpType: '',
                                valueProvided: true
                            });
                 $scope.$parent.processorCredProperties= $scope.$parent.processorCredProperties.slice();
                 $scope.resetCredentialModal();
                 $scope.trustoreAdditonEnabler();
            }
            $scope.removeTruststoreDetails = function() {
                for(var i = ($scope.$parent.processorCredProperties.length - 1); i >= 0; i--) {
                     var cred = $scope.$parent.processorCredProperties[i];
                    if((cred.credentialType == 'TRUSTSTORE_CERT')) {
                        $scope.$parent.processorCredProperties.splice(i, 1);
                    }
                }            
            }

            $scope.addSSHKeyDetails = function() {
                 $scope.removeSSHKeyDetails();
                 $scope.$parent.processorCredProperties.splice(0,0,{
                    credentialURI: '',
                    credentialType: 'SSH_KEYPAIR',
                    credentialDisplayType: 'SSH KeyPair',
                    userId: '',
                    password: '',
                    idpURI: $scope.sshkeyModal.sshKeyPairGroupId,
                    idpType: '',
                    valueProvided: true
                });
                $scope.$parent.processorCredProperties = $scope.$parent.processorCredProperties.slice();
                if (!$scope.$$phase) {
                    $scope.$apply();
                };
                $scope.resetCredentialModal();
                $scope.sshKeysAdditonEnabler();
            }
            $scope.removeSSHKeyDetails = function () {
                for(var i = ($scope.$parent.processorCredProperties.length - 1); i >= 0; i--) {
                    var cred = $scope.$parent.processorCredProperties[i];
                    if((cred.credentialType == 'SSH_KEYPAIR')) {
                        $scope.$parent.processorCredProperties.splice(i, 1);
                    }
                }
              }
              
            $scope.removeLoginCredentials = function () {
                for(var i = ($scope.$parent.processorCredProperties.length - 1); i >= 0; i--) {
                    var cred = $scope.$parent.processorCredProperties[i];
                    if((cred.credentialType == 'LOGIN_CREDENTIAL')) {
                        $scope.$parent.processorCredProperties.splice(i, 1);
                    }
                }
              }
            
             $scope.deleteTruststoreCredential = function() {			
               $scope.truststoreModal.trustStoreGroupId = '';
               $scope.removeTruststoreDetails();
  			}
             $scope.deleteSSHKeyCredential = function() {			
  			   $scope.sshkeyModal.sshKeyPairGroupId = "";
               $scope.removeSSHKeyDetails();
  			}
            
            $scope.resetProcessorCredentialDetails = function() {
                  $scope.$parent.processorCredProperties = [{
                    credentialURI: '',
                    credentialType: 'LOGIN_CREDENTIAL',
                    credentialDisplayType:'Login Credential',
                    userId: '',
                    password: '',
                    idpType: '',
                    idpURI: '',
                    valueProvided: false
                }];    
            }
            $scope.resetCredentialModal = function() {
                    $scope.truststoreModal = {
                    "trustStoreGroupId": ''
                };
                
                $scope.sshkeyModal = {
                    "sshKeyPairGroupId": '',
                };
            }
                     
            $scope.gridOptionsForProcessorCredential = {
                data: 'processorCredProperties',
                displaySelectionCheckbox: false,
                enableRowSelection: false,
                enableCellEditOnFocus: true,
                enablePaging: false,
                showFooter: false,
                rowHeight: 100,
				enableColumnResize : true,
				enableSorting : false,
				plugins: [new ngGridFlexibleHeightPlugin()],
                columnDefs: [{
                    field: "idpURI",
                    width: "30%",
                    displayName: "Name",
                    enableCellEdit: false
                }, {
                    field: "credentialType",
                    width: "20%",
                    displayName: "Type*",
                    enableCellEdit: false,
                    cellTemplate: 'partials/processor/credential-section-templates/grid_field_template.html'
               }, {
                    field: "userId",
                    width: "20%",
                    displayName: "UserId",
                    enableCellEdit: false,
                   cellTemplate: 'partials/processor/credential-section-templates/username_template.html'
                }, {
                    field: "password",
                    width: "20%",
                    displayName: "Password",
                    enableCellEdit: false,
                    cellTemplate: 'partials/processor/credential-section-templates/password_template.html'
               }, {
                    field: "valueProvided",
                    width: "10%",
                    displayName: "Action",
                    enableCellEdit: false,
                    sortable: false,
                    cellTemplate: 'partials/processor/credential-section-templates/credential_action_field_template.html'
                }]
            };	
            
          $scope.addLoginCredentials = function () {
             $scope.$parent.processorCredProperties.push({
                credentialURI: '',
                credentialType: 'LOGIN_CREDENTIAL',
                credentialDisplayType:'Login Credential',
                userId: '',
                password: '',
                idpType: '',
                idpURI: '',
                valueProvided: false
            });    
         }

            
            $scope.addCredentialObject = function(credObj) {
            
                if (credObj.credentialType === null || credObj.credentialType === "" || typeof credObj.credentialType === 'undefined') {
                     showAlert('It is mandatory to set credential type', 'error');
                     return;
                }
                
                if (credObj.credentialType === "LOGIN_CREDENTIAL" && (credObj.userId === "" || credObj.userId === null || typeof credObj.userId === 'undefined') || (!$scope.isSSHKeysAvailable() && (credObj.password === "" || credObj.password === null || typeof credObj.password === 'undefined'))) {
                    showAlert('It is mandatory to provide userId and password', 'error');
                    return;
                }
                credObj.valueProvided = true;
            };
            $scope.removeCredentialObject = function(credObj,index, row) {
                if (credObj.credentialType === "LOGIN_CREDENTIAL") {
                    $scope.removeLoginCredentials();
                    $scope.addLoginCredentials();
                    $scope.$parent.processorCredProperties = $scope.$parent.processorCredProperties.slice();
                    if (!$scope.$$phase) {
                        $scope.$apply();
                    };
                }
                 if (credObj.credentialType == "SSH_KEYPAIR") {
                    $scope.deleteSSHKeyCredential();
                }
                if (credObj.credentialType == "TRUSTSTORE_CERT") {
                    $scope.deleteTruststoreCredential();
                }
            };
            $scope.credentialCleanup = function() {
                $scope.resetCredentialModal();
                $scope.resetProcessorCredentialDetails();
            }
            $scope.$on("resetCredentialSection", function(event){ $scope.credentialCleanup()});
            
         // enable or disable turststore addition button
            $scope.trustoreAdditonEnabler = function() {
				$scope.disableTrustoreAddition = ($scope.truststoreModal.trustStoreGroupId !== '' && 
            									typeof $scope.truststoreModal.trustStoreGroupId !== 'undefined') ? false : true;
            	$scope.trustStoreUrl = $scope.fetchTrustStore + $scope.truststoreModal.trustStoreGroupId;
				if (!$scope.disableTrustoreAddition) {
					$scope.restService.get($scope.trustStoreUrl,
						function (data, status) {
							if (status != 200) {
								$scope.disableTrustoreAddition = true;
								showSaveMessage("The given trustStore group id does not exist in the Keymanagement system", 'error');
							}
					});
				}
            }

            // enable or disable ssh keys addition button
            $scope.sshKeysAdditonEnabler = function() {
				$scope.disableSSHKeysAddition = ($scope.sshkeyModal.sshKeyPairGroupId !== '' && 
            									typeof $scope.sshkeyModal.sshKeyPairGroupId !== 'undefined') ? false : true;
            	$scope.fetchSshKeypairUrl = $scope.fetchSshKeyPair + $scope.sshkeyModal.sshKeyPairGroupId;
    			if (!$scope.disableSSHKeysAddition) {
    				$scope.restService.get($scope.fetchSshKeypairUrl,
    					function (data, status) {
    						if (status != 200) {
								$scope.disableSSHKeysAddition = true;
    							showSaveMessage("The given SSH keypair group id does not exist in the Keymanagement system", 'error');
    						}
    				});		
				}
			}
            
            // helper function to determine is sshkey pair present in credentials of processorCredProperties
            $scope.isSSHKeysAvailable = function() {
                for(var i = ($scope.$parent.processorCredProperties.length - 1); i >= 0; i--) {
                     var cred = $scope.$parent.processorCredProperties[i];
                     if((cred.credentialType == 'SSH_KEYPAIR')) {
                         return true;
                     }
                 }
                 return false;
            } 
            }])