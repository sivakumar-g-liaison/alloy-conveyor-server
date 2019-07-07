/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
angular.module(
	'myApp.cellWithTextBox', []
)
	.directive(
		'cellWithTextBoxDirective',
		function () {
			return {
				restrict: 'C',
				replace: true,
				scope: {
					
                                        rowEntity:'=',
                                        colFiled:'=',
                                        isRequired:'@',
                                        propertyValue:'@'
				},
                                
                                
				template: '<input type="text" ng-model="propertyValue" ng-change="setValue()" required="" class="textboxingrid" placeholder="required">',
				link: function (scope) {					
                                                
                                               	scope.setValue = 
                                                function () {
                                                   
							
                                                            scope.rowEntity[scope.colFiled]=scope.propertyValue;
                                                            
                                                       
						};
					}
				};
			});