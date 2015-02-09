/* 
 *open the template in the editor.
 */
angular.module(
    'myApp.urlValidation', []
).directive('urlValidation', 
 function($rootScope) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, elem, attrs, ngModel) {
            elem.bind('blur', function() {

                scope.scriptURL = ngModel.$modelValue;                
				if(typeof scope.scriptURL == "undefined" || scope.scriptURL.length == 0) {
				   ngModel.$setValidity('allowed', true);
					    scope.$parent.scriptUrlIsValid = false;
					    scope.$parent.disable = true;	
                        scope.$apply();
                        return undefined;			  
				} else {				
				  
                    var res = scope.scriptURL.split(":/");

                    scope.allowedProtocols = ["gitlab", "Gitlab", "GITLAB"];

                    if (scope.allowedProtocols.indexOf(res[0]) > -1 && res[1].charAt(0) != '/' && res[1] != "") {
					
					if(attrs.id == "modelscriptUrlName") {
					    scope.$parent.loader = true;
						 $rootScope.restService.get($rootScope.base_url + "/git/content/" + res[1],
							function (data, status) {
							scope.$parent.loader = false;
							  if (status === 200 || status === 400) { 			
								if (data.scriptserviceResponse.response.status === 'success') {
								   scope.$parent.scriptTemplateIsExist = true;
								   scope.$parent.editor.getSession().setValue(data.scriptserviceResponse.script);
								} else {
								    scope.$parent.scriptTemplateIsExist = false;
								    scope.$parent.editor.getSession().setValue("");
								} 
							} else {
							 showSaveMessage("Error while retrieving file from GitLab", 'error');
							}          	
						   }
						);	
					}
					scope.$parent.disable = false;
					ngModel.$setValidity('allowed', true);
					    scope.$parent.scriptUrlIsValid = true;
                        scope.$apply();
                        return ngModel.$modelValue;
				    }
                    else {
                    	scope.$parent.disable = true;
                        ngModel.$setValidity('allowed', false);
						scope.$parent.scriptUrlIsValid = false;
                        scope.$apply();
                        return undefined;
                    }
                }
            });
        }
    };
  });