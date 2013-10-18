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
					
                                        rowEntity:'=',
                                        colFiled:'='
				},
				template: '<div><input type="password"  ng-model="password" ng-change="showConfirmBox()"  /> </br>\n\
                                                <label ng-show=showconfirmpassword >confirm password:</label>\n\
                                                <input type="password"  ng-show=showconfirmpassword ng-change="validate()" ng-model="repeatepassword" required />\n\
                                                <div>',
				link: function (scope) {
					
                                                scope.showconfirmpassword=false;
                                                scope.password=scope.rowEntity[scope.colFiled];
                                                scope.showConfirmBox =
						function () {
                                                    scope.showconfirmpassword=true; 
                                                         },
                                               	scope.validate = 
                                                function () {
                                                   
							if(scope.password === scope.repeatepassword){                                                            
                                                            scope.showconfirmpassword=false;
                                                            scope.rowEntity[scope.colFiled]=scope.password;
                                                            scope.repeatepassword='';
                                                        }
						};
					}
				};
			});