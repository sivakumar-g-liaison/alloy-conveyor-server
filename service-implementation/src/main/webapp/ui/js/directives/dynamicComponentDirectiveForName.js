/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
angular.module(
	'myApp.dynamicComponentDirectiveForName', []
)
	.directive(
		'dynamicComponentDirectiveForName',
		function () {
			return {
				restrict: 'C',
				replace: true,
				scope: {
					allowAdd: '@',
					allProps: '=',
					enableTextbox: '&',
                                        selectedValue: '=',
                                        propName:'@'
				},
				template: '<div ng-switch on="allowAdd">' +
					'<div ng-switch-when="false">{{propName}}</div>' +
					'<div ng-switch-when="true">\n\
                                        <select ng-change="shouldIShowAddTextBox(selectedproperty)" ng-model="selectedproperty" ng-options="property for property in allStaticProperties">\n\
                                        <option value="">-- select--</option>\n\
                                        </select> <i><br/></i>\n\
                                        <input type="text" ng-show=showAddnew  ng-model="addedProperty" ng-change="setScopeValue(addedProperty)" required class="textboxingrid"></input>\n\
                                        </div>\n\
                                        <div ng-switch-default>\n\
                                        <select ng-change="shouldIShowAddTextBox(selectedproperty)" ng-model="selectedproperty" ng-options="property for property in allStaticProperties">\n\
                                        <option value="">-- select--</option>\n\
                                        </select></div>',
				link: function (scope, elem, attrs) {
                                        scope.showAddnew=false;
					scope.$watch("allProps", function (
							newValue) {
                                                   
							scope.allStaticProperties =
								angular.fromJson(newValue);
							scope.showAddnew = false;
							scope.addedProperty = '';
						});
                                                
						scope.shouldIShowAddTextBox =
						function (selectedproperty) {
                                                    
							scope.selectedValue.name = selectedproperty;
							if (selectedproperty ==="add new -->") {                                                            
							scope.addedProperty = '';	
                                                        scope.showAddnew = true;
								
                                                                
							} else {
								scope.addedProperty = 'add new';
								scope.showAddnew = false;
							}
						},
                                              
					  scope.setScopeValue = function (value) {
							scope.selectedValue.name = value;
									  };
					
				}
			};
		});