myApp.controller(
    'CredentialHanlderCntrlr', ['$rootScope', '$scope', '$blockUI', '$timeout',
        function ($rootScope, $scope, $blockUI, $timeout) {
        
             // SSHkeys Uploading section begins
             $scope.setSSHPrivateKey = function (element) {
                $scope.$apply(function ($scope) {
                    // Turn the FileList object into an Array
                    for (var i = 0; i < element.files.length; i++) {
                         $scope.sshKeys.privatekey = element.files[i];
                    }
                    $scope.sshkeyModal.sshPrivateKeyURI = element.files[0].name;
                    $scope.isPrivateKeySelected = true;
                    $scope.progressVisible = false;
                });
             };
             
              $scope.setSSHPublicKey = function (element) {
               // console.log(element.value);
                $scope.$apply(function ($scope) {
                    // Turn the FileList object into an Array
                    for (var i = 0; i < element.files.length; i++) {
                       $scope.sshKeys.publickey = element.files[i];
                    }
                    $scope.sshkeyModal.sshPublicKeyURI = element.files[0].name;
                    $scope.isPublicKeySelected = true;
                    $scope.progressVisible = false;
                });
             };
            
              $scope.uploadSSHKey = function () {
                //console.log('Entering upload event of ssh keys');
                if ($scope.sshkeyModal.sshKeyPairPassphrase !== $scope.sshkeyModal.sshKeyPairConfirmPassphrase) {
    				showSaveMessage("Passwords does not match.", 'error');
    				$(sshkeypassphrase).focus();
    				return;
    			}
                $scope.block.blockUI();
                formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';
                var fd = new FormData();
                $scope.sshKeyObj['serviceInstanceId'] = Date.now().toString();
				$scope.sshKeyObj.dataTransferObject['name'] = $scope.processor.name.concat('_',$scope.procsrType.value,'_',$scope.sshkeyModal.sshkeyURI);
				var currentDate = new Date();
				var afterOneYear = new Date();
				afterOneYear.setYear(currentDate.getFullYear() + 1);
				$("#yearFromNow").append(afterOneYear.toString());
				$scope.sshKeyObj.dataTransferObject['createdDate'] = currentDate.toISOString();
				$scope.sshKeyObj.dataTransferObject['validityDateFrom'] = currentDate.toISOString();
				$scope.sshKeyObj.dataTransferObject['validityDateTo'] = afterOneYear.toISOString();
                $scope.sshKeyObj.dataTransferObject['custodianPassphrase'] = $scope.sshkeyModal.sshKeyPairPassphrase;
                fd.append("json", angular.toJson($scope.sshKeyObj));
                fd.append($scope.sshKeys.privatekey.name, $scope.sshKeys.privatekey);
                fd.append($scope.sshKeys.publickey.name, $scope.sshKeys.publickey);
                var xhr = new XMLHttpRequest();
                xhr.addEventListener("load", sshkeyUploadComplete, false);
                xhr.addEventListener("error", sshkeyUploadFailed, false);
                xhr.addEventListener("abort", sshkeyUploadCanceled, false);
                xhr.open("POST", $scope.url_ssh_upload_key);
                xhr.send(fd);
            };
              function sshkeyUploadComplete(evt) {
                // To reset the values in the file browser window
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
                /* This event is raised when the server send back a response */
                if (evt.target.status === 201) {
                   // console.log('ssh key uploaded successfully');
                    var resp = angular.fromJson(evt.target.responseText);
                    var pkGuid = resp['dataTransferObject']['pguid'];
                    // Keygroup guid 
                    pkGuid = pkGuid.toString();
                    $scope.sshkeyModal.sshKeyPairPguid = pkGuid;
                    $scope.addSSHKeyDetails();
                    $scope.resetCredentialModal();

               } else {
                    $scope.block.unblockUI();
                    showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                    return;
                }
            }
            
             function sshkeyUploadFailed(evt) {
                // To reset the values in the file browser window
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
                $scope.block.unblockUI();
                showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                return;
            }

            function sshkeyUploadCanceled(evt) {
                // To reset the values in the file browser window
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
                $scope.block.unblockUI();
                showSaveMessage('SSH KeyPair Uploading Failed', 'error');
                return;
            }
           
            $scope.resetSSHKeys = function (element) {
                element.value = null;
            }
            // SSH Key Upload Section Ends
 
            $scope.processCredentialDetails = function() {
                  for (var i = 0; i <  $scope.$parent.processorCredProperties.length; i++) {
                    var credObj =  $scope.$parent.processorCredProperties[i];
                    if(credObj.credentialType == 'SSH_KEYPAIR') {
                        $scope.sshkeyModal.sshKeyPairPguid = credObj.idpURI;
                        $scope.sshkeyModal.sshKeyPairPassphrase = '';
                        $scope.sshkeyModal.sshKeyPairConfirmPassphrase = '';
                        if(credObj.idpType == 'PRIVATE') {
                             $scope.sshkeyModal.sshPrivateKeyURI = ''; 
                        } else {
                             $scope.sshkeyModal.sshPublicKeyURI = '';
                        }                                                                        
                    }
                    if (credObj.credentialType == 'TRUSTSTORE_CERT') {
                        $scope.certificateModal.certificateURI = '';
                        $scope.certificateModal.trustStoreGroupId = credObj.idpURI;
                        $scope.certificateModal.isGlobalTrustore = (credObj.idpType == 'GLOBAL')?"1":"0";
                    }
                }
            }
            $scope.addCertificateDetails = function() {
                $scope.removeCertificateDetails();
                var idpType = ($scope.certificateModal.isGlobalTrustore == '1' || $scope.certificateModal.isGlobalTrustore == 1)?'GLOBAL':'SELFSIGNED';
                $scope.$parent.processorCredProperties.splice(0,0,{
                                credentialURI: $scope.certificateModal.certificateURI,
                                credentialType: 'TRUSTSTORE_CERT',
                                credentialDisplayType: 'TrustStore Certificate',
                                userId: '',
                                password:'',
                                idpURI: $scope.certificateModal.trustStoreGroupId,
                                idpType: idpType,
                                valueProvided: true
                            });
                 $scope.$parent.processorCredProperties= $scope.$parent.processorCredProperties.slice();
                 $scope.block.unblockUI();
                
            }
            $scope.removeCertificateDetails = function() {
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
                    credentialURI: $scope.sshkeyModal.sshPrivateKeyURI,
                    credentialType: 'SSH_KEYPAIR',
                    credentialDisplayType: 'SSH Private Key',
                    userId: '',
                    password: '',
                    idpURI: $scope.sshkeyModal.sshKeyPairPguid,
                    idpType: 'PRIVATE',
                    valueProvided: true
                }, {
                    credentialURI: $scope.sshkeyModal.sshPublicKeyURI,
                    credentialType: 'SSH_KEYPAIR',
                    credentialDisplayType: 'SSH Public Key',
                    userId: '',
                    password: '',
                    idpURI: $scope.sshkeyModal.sshKeyPairPguid,
                    idpType: 'PUBLIC',
                    valueProvided: true
                });
                $scope.$parent.processorCredProperties = $scope.$parent.processorCredProperties.slice();
                if (!$scope.$$phase) {
                    $scope.$apply();
                };
                $scope.block.unblockUI();
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
            
             $scope.deleteCertificateCredential = function() {			
  			   $scope.certificateModal.certificateURI = "";
               $scope.certificateModal.certificates = '';
               $scope.certificateModal.isGlobalTrustore = "1";
               $scope.certificateModal.trustStoreGroupId = '';
               $scope.resetFiles();
               $scope.removeCertificateDetails();
  			}
             $scope.deleteSSHKeyCredential = function() {			
  			   $scope.sshkeyModal.sshKeyPairPguid = "";
               $scope.sshkeyModal.sshPrivateKeyURI = "";
               $scope.sshkeyModal.sshPublicKeyURI = "";
               $scope.sshkeyModal.sshKeyPairPassphrase = "";
               $scope.sshkeyModal.sshKeyPairConfirmPassphrase = "";
               $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
               $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
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
                    $scope.certificateModal = {
                    "certificates": '',
                    "certificateURI": '',
                    "isGlobalTrustore": '1',
                    "trustStoreGroupId": ''
                };
                
                $scope.sshkeyModal = {
                    "sshKeys": '',
                    "sshPrivateKeyURI": '',
                    "sshPublicKeyURI": '',
                    "sshKeyPairPguid": '',
                    "sshKeyPairPassphrase":'',
                    "sshKeyPairConfirmPassphrase":''
                };
                formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';
                $scope.isPrivateKeySelected = false;
                $scope.isPublicKeySelected = false;
            }
            
                        // File Upload Section Begins
            $scope.setFiles = function (element) {
                $scope.$apply(function ($scope) {
                    $scope.files = [];
                    for (var i = 0; i < element.files.length; i++) {
                        $scope.files.push(element.files[i]);
                    }
                    $scope.certificateModal.certificateURI = $scope.files[0].name;
                    $scope.isFileSelected = true;
                    $scope.progressVisible = false;
                });
            };
            $scope.uploadFile = function () {
                
                $scope.block.blockUI();
                var fd = new FormData();
                $scope.pkObj['serviceInstanceId'] = Date.now().toString();
				$scope.pkObj.dataTransferObject['name'] = $scope.processor.name.concat('_',$scope.procsrType.value,'_',$scope.certificateModal.certificateURI);
				var currentDate = new Date();
				var afterOneYear = new Date();
				afterOneYear.setYear(currentDate.getFullYear() + 1);
				$("#yearFromNow").append(afterOneYear.toString());
				$scope.pkObj.dataTransferObject['createdDate'] = currentDate.toISOString();
				$scope.pkObj.dataTransferObject['validityDateFrom'] = currentDate.toISOString();
				$scope.pkObj.dataTransferObject['validityDateTo'] = afterOneYear.toISOString();
                fd.append("json", angular.toJson($scope.pkObj));
                for (var i in $scope.files) {
                    fd.append($scope.files[i].name, $scope.files[i]);
                }
                var xhr = new XMLHttpRequest();
                xhr.addEventListener("load", uploadComplete, false);
                xhr.addEventListener("error", uploadFailed, false);
                xhr.addEventListener("abort", uploadCanceled, false);
                xhr.open("POST", $scope.url_upload_key);
                xhr.send(fd);
            };

            function uploadComplete(evt) {
                /* This event is raised when the server send back a response */
                if (evt.target.status === 201) {
                    var resp = angular.fromJson(evt.target.responseText);
                    var arr = resp['dataTransferObject']['keyGroupMemberships'];
                    pkGuid = arr[0]['keyBase']['pguid'];
                    // Public Key guid 
                    pkGuid = pkGuid.toString();
                    if ($scope.certificateModal.isGlobalTrustore === "0") {
                        $scope.uploadToSelfSignedTrustStore(pkGuid);
                    } else {
                        $scope.linkTrustStoreWithCertificate(pkGuid, $rootScope.javaProperties.globalTrustStoreId, $rootScope.javaProperties.globalTrustStoreGroupId);
                    }
                } else {
                    $scope.block.unblockUI();
                    showSaveMessage('Certificate Uploading Failed', 'error');
                    return;
                }
            }
            $scope.uploadToSelfSignedTrustStore = function (pkGuid) {
                $scope.restService.post($scope.base_url + '/processor/uploadkey','POST Self signed key to key manager',
                    function (data, status) {
                        if (status === 200 && data.getTrustStoreResponse.response.status === 'success') {
                            $scope.linkTrustStoreWithCertificate(pkGuid, data.getTrustStoreResponse.trustStore.trustStoreId,
                                data.getTrustStoreResponse.trustStore.trustStoreGroupId);
                        } else {
                            $scope.block.unblockUI();
                            showSaveMessage('Certificate Uploading Failed', 'error');
                            return;
                        }
                    }
                );
            };
            $scope.linkTrustStoreWithCertificate = function (pkGuid, trustStoreId, trustStoreGroupId) {
            	
            	$scope.linkKeyTs['serviceInstanceId'] = Date.now().toString();
                // To put public key is association json to TrustStore
                $scope.linkKeyTs['dataTransferObject']['trustStoreMemberships'][0]['publicKey']['pguid'] = pkGuid;
                $scope.restService.put($scope.url_link_key_store + trustStoreId, angular.toJson($scope.linkKeyTs),
                    function (data, status) {
                        if (status === 200) {
                            $scope.certificateModal.trustStoreGroupId = trustStoreGroupId;
                            $scope.addCertificateDetails();
                            $scope.resetCredentialModal();
                        } else {
                            $scope.block.unblockUI();
                            showSaveMessage('Certificate Uploading Failed', 'error');
                            return;
                        }
                    }
                );
            };

            function uploadFailed(evt) {
                $scope.block.unblockUI();
                showSaveMessage('Certificate Uploading Failed', 'error');
                return;
            }

            function uploadCanceled(evt) {
                $scope.block.unblockUI();
                showSaveMessage('Certificate Uploading Failed', 'error');
                return;
            }
            // File Upload Section Ends
            
            $scope.resetFiles = function() {
				document.getElementById('mbx-procsr-certificatebrowse').value = null;
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
				plugins: [new ngGridFlexibleHeightPlugin()],
                columnDefs: [{
                    field: "credentialURI",
                    width: "20%",
                    displayName: "Name",
                    enableCellEdit: false
                   // cellTemplate: 'partials/processor/credential-section-templates/grid_field_template.html'
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
                    width: "20%",
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
                
                if (credObj.credentialType === "LOGIN_CREDENTIAL" && ((credObj.userId === "" || credObj.userId === null || typeof credObj.userId === 'undefined') || (credObj.password === "" || credObj.password === null || typeof credObj.password === 'undefined' && (credObj.credentialType !== "SSH_KEYPAIR")))) {
                    showAlert ('It is mandatory to provide userId and password', 'error');
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
                    $scope.deleteCertificateCredential();
                }
            };
            $scope.credentialCleanup = function() {
                $scope.resetFiles();
                $scope.resetCredentialModal();
                $scope.resetProcessorCredentialDetails();
                formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshpublickeyAdd"));
                $scope.resetSSHKeys(document.getElementById("mbx-procsr-sshprivatekeyAdd"));
            }
            $scope.$on("resetCredentialSection", function(event){ $scope.credentialCleanup()});
            
            $scope.confirmPasswordColor = function () {		
  			  if (typeof($scope.sshkeyModal.sshKeyPairConfirmPassphrase) === 'undefined' || $scope.sshkeyModal.sshKeyPairConfirmPassphrase === '') {
  				formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '';	 	
  			  } else if ($scope.sshkeyModal.sshKeyPairPassphrase === $scope.sshkeyModal.sshKeyPairConfirmPassphrase) {
  				formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '#78FA89';	
  			  }	else {
  				formAddPrcsr.sshkeyconfirmpassphrase.style.backgroundColor = '#FA787E';
  			  }
           }

            }])