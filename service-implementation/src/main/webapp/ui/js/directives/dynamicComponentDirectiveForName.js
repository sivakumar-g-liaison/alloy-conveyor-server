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
                    addNew: '=',
                    addedProperty: '=',
                    propName: '@'
                },
                template: '<div ng-switch on="allowAdd">' +
                    '<div ng-switch-when="false">{{propName}}</div>' +
                    '<div ng-switch-when="true">\n\
                        <select ng-change="shouldIShowAddTextBox()" ng-model="selectedValue.name" ng-options="property for property in allStaticProperties">\n\
                             <option value="">-- select--</option>\n\
                        </select> <i>&nbsp</i>\n\
                        <textarea class="tarea" ng-show="addNew.value"  ng-model="addedProperty.value"   placeholder="required" style="width:60%"></textarea>\n\
                      </div>\n\
                      <div ng-switch-default>\n\
                           <select ng-change="shouldIShowAddTextBox()" ng-model="selectedValue.name" ng-options="property for property in allStaticProperties">\n\
                              <option value="">-- select--</option>\n\
                           </select>\n\
                       </div>',
                link: function (scope) {
                    //scope.showAddnew = false;
                    scope.$watch("allProps", function (
                        newValue) {
                        if (newValue[newValue.length - 1] === "") {
                            newValue.splice(newValue.length - 1, 1);
                        }
                        scope.allStaticProperties = angular.fromJson(newValue);
                        if(scope.selectedValue.name === "add new -->"){
                            return;
                        }
                        scope.addNew.value = false;
                    }, true);
                    scope.shouldIShowAddTextBox =
                        function () {
                            if (scope.selectedValue.name === "add new -->") {
                                scope.addNew.value = true;
                                scope.addedProperty.value ='';
                            } else {
                                scope.addNew.value = false;
                            }
                    };
                }
            };
        });