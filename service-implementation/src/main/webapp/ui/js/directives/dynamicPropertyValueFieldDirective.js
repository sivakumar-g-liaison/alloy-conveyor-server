angular.module(
    'myApp.dynamicPropertyValueFieldDirective', []
    ).directive (
       'dynamicPropertyValueFieldDirective', 
        function($compile, $rootScope, $timeout) {
            
            var getTemplateUrl = function(currentRowObject) {
            
                var type = currentRowObject.type;
                var templateUrl = '';

                switch(type) {                 
                    case 'textarea':
                        templateUrl = 'partials/directive-templates/textarea.html';
                        break;
                    case 'select':
                        templateUrl = 'partials/directive-templates/select.html';
                        break;
                }
                return templateUrl;
            };

            return {
            
                restrict: 'E',
                replace: true,
                scope : {
                    currentRowObject: '='
                },        
                link: function(scope, elem, attrs) {
                
                   scope.$watch("currentRowObject.name", function() {
                    var templateUrl = getTemplateUrl(scope.currentRowObject);                    
                        $rootScope.restService.get(templateUrl, function (data) {
                              elem.html(data);
                              $compile(elem.contents())(scope);
                        }); 
                   });
                   scope.infoIconImgUrl = 'img/alert-triangle-red.png'; 
                   scope.sftpDefaultPort = '22';
			       scope.ftpDefaultPort = '21';
			       scope.ftpsDefaultPort = '21';
                   scope.httpDefaultVersion = '1.1';  				   
                                     
                    scope.optionSelected = function(selectedOption) {                       
                        scope.currentRowObject.value = selectedOption;
                    };
                    
                    var editor;	
                    var rowObj;
                    
					scope.loadValueData = function (_editor) {
                       editor = _editor;
                       editor.getSession().setUseWorker(false);
                    };
                    
					scope.isModal = function (currentRowObject) {
					
					    editor.setValue('');
						$timeout(enableAndFocusEditor,500);
						rowObj = currentRowObject;
						
					    if (rowObj.name === 'retryAttempts' && !$rootScope.retryAttemptsPattern.test(rowObj.value)) {
							rowObj.value = '';
						} else if ((rowObj.name === 'socketTimeout' || rowObj.name === 'connectionTimeout')
									 && !$rootScope.numberTimeOutPattern.test(rowObj.value)) {
							rowObj.value = '';
						} else if (rowObj.name === 'url' && !$rootScope.inputPatternForURL.test(rowObj.value)) {
							rowObj.value = '';
						} else if (rowObj.name === 'httpVersion' && !$rootScope.httpVersionPattern.test(rowObj.value)) {
							rowObj.value = '';
						} else if (rowObj.name === 'port' && !$rootScope.inputPatternForPort.test(rowObj.value)) {
							rowObj.value = '';
						}						
						if (typeof rowObj.value === 'undefined' || rowObj.value === '') {
							editor.setValue(" ");
						} else {				
							editor.setValue(rowObj.value.toString());
						}
					};
					scope.close = function () {
						if (rowObj.name === 'url') {
						   scope.onCloseEditor(editor.getValue());
						}
						scope.currentRowObject.value = editor.getSession().getValue();
				    };
					var enableAndFocusEditor = function() {
						if (editor) {
							editor.focus();
							var session = editor.getSession();
							//Get the number of lines
							var count = session.getLength();
							//Go to end of the last line
							
							if((typeof(editor.getValue()) === 'undefined') || (editor.getValue() === "")) {
								editor.setValue(" "); 
								editor.remove(" "); 
								editor.moveCursorTo(0,0);
							} else {
								editor.setValue(editor.getValue(),0);
							}
							
							editor.gotoLine(session.getLine(count-1).length);
						}
					};					
					
					scope.onCloseEditor = function (url) {
            
						if (url !== '' && $rootScope.inputPatternForURL.test(url)) {

							var port = url.split('/')[2].split(':')[1];
							for (var i = 0; i < scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor.length; i++) {
								if (scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].name === 'port') {
									if ($rootScope.inputPatternForPort.test(port)) {
										scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = true;
									} else {
										scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;
									}
									if (typeof port !== 'undefined' && port !== '') {
										scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = port;
									} else {
										scope.$parent.$parent.$parent.$parent.defaultPortValue();
										scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;
									}
									if(port === '') scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;
								}
							}
						} else {
							for(var i = 0; i < scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor.length; i++) {
								if (scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].name === 'port') {
									scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = '';	
                                    //scope.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;						
								}
							}
						}
					};
                    
					//Evaluate url and port
                     scope.OnChangeUrl = function(row) {
               
					   var url = row.value;
					   if(typeof url !== 'undefined') {
							var ip = url.split('/')[2].split(':')[0];
							var port = url.split('/')[2].split(':')[1]; 
							 for(i = 0; i < scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor.length; i++) {
								if (scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].name === 'port') {
									if ($rootScope.inputPatternForPort.test(port)) {										
										scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = true;
									} else {
									  scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;										    
									}
									if (typeof port !== 'undefined' && port !== '') {
										scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = port;
									} else {
										scope.defaultPortValue();
										scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;	
									}
									if(port === '') scope.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;
							   }
							}
						} else {
							for(var i = 0; i < scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor.length; i++) {
								if (scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].name === 'port') {
									scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = '';
									scope.defaultPortValue();
									scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].readOnly = false;	
							   }
							}
						}
									   
				   }
				   //Load default Port Value for required protocal
                   scope.defaultPortValue = function() {
					
						for (var i = 0; i < scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor.length; i++) {							
							if (scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].name === 'port') {								
								if (scope.$parent.$parent.$parent.$parent.processor.protocol.value === "FTP") {
									scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = scope.ftpDefaultPort;
								} else if (scope.$parent.$parent.$parent.$parent.processor.protocol.value === "SFTP") {
									scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = scope.sftpDefaultPort;
								} else if (scope.$parent.$parent.$parent.$parent.processor.protocol.value === "FTPS") {
									scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = scope.ftpsDefaultPort;
								} 
							} else if (scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].name === 'httpVersion') {
								scope.$parent.$parent.$parent.$parent.propertiesAddedToProcessor[i].value = scope.httpDefaultVersion;
							}
						}
					}				    				   
                    					
                   
                }
         };   
       }
    ); 
