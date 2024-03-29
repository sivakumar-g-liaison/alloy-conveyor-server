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
    function ($rootScope) {
        return {
            restrict: 'C',
            replace: true,
            scope: {
                allowAdd: '@',
                allProps: '=',
                selectedValue: '=',
                addNew: '=',
                addedProperty: '=',
                propName: '@',
                iconColor: '=',
				sortName: '='
            },
            template: '<div ng-switch on="allowAdd">' +
                '<div ng-switch-when="false">{{propName}}</div>' +
                '<div class="alignDiv" ng-switch-when="true">\n\
                <div ng-form="form">\n\
                    <select ng-change="shouldIShowAddTextBox()" ng-model="selectedValue.value" ng-options="property.name for property in allStaticProperties | orderBy:sortName ">\n\
                         <option value="">-- select--</option>\n\
                    </select> <i>&nbsp</i>\n\
                    <textarea class="form-control alignDynamicTextarea" ng-input="COL_FIELD" ng-show="addNew.value" ng-model="addedProperty.value" placeholder="required" style="width:46%;height:45px;"></textarea></div>\n\
                  </div>',
            link: function (scope) {
                //scope.showAddnew = false;
                scope.$watch("allProps", function (
                    newValue) {

                    scope.allStaticProperties = angular.fromJson(newValue);
                    if (scope.selectedValue.value.id === "add new -->") {
                        return;
                    }
                    if(typeof(scope.addNew) != 'undefined') {
                        scope.addNew.value = false;
                   }
                }, true);

                scope.shouldIShowAddTextBox =
                    function () {

                        /* if(scope.selectedValue.name !== null){
                         scope.iconColor.color="glyphicon-red";
                         }else{
                         scope.iconColor.color="glyphicon-white";
                         } */

                        //console.log(scope.selectedValue);

                        if (scope.selectedValue.value !== null && scope.selectedValue.value.id === "add new -->") {
                            scope.addNew.value = true;
                            scope.addedProperty.value = '';
                        } else {
                        	  if(typeof(scope.addNew) != 'undefined') {
                                  scope.addNew.value = false;
                             }
                        }
                    };
            }
        };
    });