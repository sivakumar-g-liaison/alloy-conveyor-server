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
                    password: '@'                    
                },
                template: '<div><input type="password"  ng-model="password" ng-change="showConfirmBox()" class="textboxingrid" />\n\
                                <label ng-show=showconfirmpassword >{{matchString}}</label> \n\
                                \n\
                                <input type="password"  ng-show=showconfirmpassword ng-change="validate()" ng-model="repeatepassword" class="textboxingrid"  required placeholder="confirm password"/>\n\
                                \n\
                                 </div>',
                link: function (scope) {
                    scope.showConfirmBox =
                        function () {
                            if (scope.password === '') {
                                scope.showconfirmpassword = false;
                                scope.rowEntity[scope.colFiled] = '';
                                scope.rowEntity.passwordDirtyState = 'matches';
                                return;
                            }
                            scope.showconfirmpassword = true;
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