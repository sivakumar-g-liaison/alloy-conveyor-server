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
                    hide: '=',
                    password: '@'                    
                },
                template: '<div><input type="password"  ng-model="password" ng-change="showConfirmBox()" class="textboxingrid" />\n\
                                <label ng-show=showconfirmpassword >{{matchString}}</label> \n\
                                \n\
                                <input type="password"  ng-show=showconfirmpassword ng-change="validate()" ng-model="repeatepassword" class="textboxingrid"  required placeholder="confirm password"/>\n\
                                \n\
            					<span class="custom-info-block" ng-show=showerrormessage>Password cannot be longer than 63 characters.</span>\n\
                                 </div>',
                link: function (scope) {
				/*GMB-197 Fix : The variable is changed in removeCredentialRow() and save() 
					and the error message and repeat password text box is not shown*/
				 scope.$watch("hide", function (
                    newValue) {
					scope.showconfirmpassword = false;
					scope.showerrormessage = false;
				}, true);
                scope.showConfirmBox =
                        function () {
                            if (scope.password === '') {
                                scope.showconfirmpassword = false;
                                scope.showerrormessage = false;
                                scope.rowEntity[scope.colFiled] = '';
                                scope.rowEntity.passwordDirtyState = 'matches';
                                return;
                            }
                          //This condition is added to check the password length and show corresponding error message 
							if (scope.password.length >= 64) {
                                scope.showerrormessage = true;
								scope.showconfirmpassword = false;
                                scope.rowEntity.passwordDirtyState = 'maxlengthError';
                                return;
                            }
                            scope.showconfirmpassword = true;
                            scope.showerrormessage = false;
                            scope.matchString = "doesn't match";
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
                            } else {
                                scope.matchString = "doesn't match";
                                 scope.rowEntity.passwordDirtyState = 'nomatch';
                            }
                    };
                }
            };
        });