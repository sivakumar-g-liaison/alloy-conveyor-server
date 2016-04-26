/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
angular.module(
    'myApp.passwordDirective', []
)
    .directive(
        'passwordDirective',
        function () {
            return {
                restrict: 'C',
                replace: true,
                scope: {

                    rowEntity: '=',
                    colFiled: '=',
                    password: '@',
                 },
                template: '<div><input type="password"  ng-model="password" ng-input="password" ng-change="showConfirmBox()" class="textboxingrid" />\n\
                                <label ng-show=showconfirmpassword ng-class = "{\'error_msg_color\':showconfirmpassword}">{{matchString}}</label> \n\
                                \n\
                                <input type="password"  ng-show=showconfirmpassword ng-change="validate()" ng-model="repeatepassword" ng-input="repeatepassword" class="textboxingrid"  placeholder="confirm password"/>\n\
                                \n\
                    			<span id="procsr-cred-invalid-password" class="customHide" ng-class = "{\'help-block-custom\':showerrormessage}" ng-show="formAddPrcsr.$error.pwd">{{errorMessage}}</span>\n\
                                 </div>',
                link: function (scope) {
				/*The event is triggered in processorController to clear password and error message in UI*/
            	scope.$on('clearPassword', function(event){
            			scope.showconfirmpassword = false;
    					scope.showerrormessage = false;
    					scope.password = '';
    					scope.repeatepassword = '';
    					scope.$parent.formAddPrcsr.$setValidity('pwd', true); 
    					scope.errorMessage = '';
    				})
					/*The event is triggered in processorController to clear password  whenever there is an error in processor creation or revision.
					 * The password is mandated after that*/
            	scope.$on('mandatePassword', function(event){
            			scope.showconfirmpassword = false;
    					scope.showerrormessage = true;
    					scope.password = '';
    					scope.repeatepassword = '';
    				})
                scope.showConfirmBox =
                        function () {
                            if (scope.password === '') {
                                scope.showconfirmpassword = false;
                                scope.showerrormessage = false;
                                scope.rowEntity[scope.colFiled] = '';
                                scope.rowEntity.passwordDirtyState = 'matches';
								scope.$parent.formAddPrcsr.$setValidity('pwd', true);
                                return;
                            }
                          //This condition is added to check the password length and show corresponding error message 
							if (scope.password.length >= 64) {
                                scope.showerrormessage = true;
								scope.showconfirmpassword = false;
                                scope.rowEntity.passwordDirtyState = 'maxlengthError';
								scope.$parent.formAddPrcsr.$setValidity('pwd', false);
								scope.errorMessage = "Password cannot be longer than 63 characters.";
                                scope.infoIconImgUrl = 'img/alert-triangle-red.png';
                                return;
                            }
                            scope.showconfirmpassword = true;
                            scope.showerrormessage = false;
                            scope.matchString = "doesn't match";
							scope.$parent.formAddPrcsr.$setValidity('pwd', false);
                            scope.repeatepassword = '';
                            scope.rowEntity.passwordDirtyState = 'nomatch';
                            
                    },
                    scope.validate =
                        function () {

                            if (scope.password === scope.repeatepassword) {
                                scope.matchString = "matches";
                                scope.showconfirmpassword = false;
                                scope.rowEntity[scope.colFiled] = scope.password;
                                scope.repeatepassword = '';
                                scope.rowEntity.passwordDirtyState = 'matches';
								scope.$parent.formAddPrcsr.$setValidity('pwd', true);
                            } else {
                                scope.matchString = "doesn't match";
                                scope.rowEntity.passwordDirtyState = 'nomatch';
								scope.$parent.formAddPrcsr.$setValidity('pwd', false);
                            }
                    };
                }
            };
        });