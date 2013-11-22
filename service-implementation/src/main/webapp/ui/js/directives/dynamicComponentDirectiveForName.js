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
                    iconColor: '='
                },
                template: '<div ng-switch on="allowAdd">' +
                    '<div ng-switch-when="false">{{propName}}</div>' +
                    '<div ng-switch-when="true">\n\
                    <div ng-form name="form">\n\
                        <select ng-change="shouldIShowAddTextBox()" ng-model="selectedValue.name" ng-options="property for property in allStaticProperties">\n\
                             <option value="">-- select--</option>\n\
                        </select> <i>&nbsp</i>\n\
                        <textarea ng-show="addNew.value"  ng-model="addedProperty.value"   placeholder="required" style="width:60%"></textarea></div>\n\
                        <div ng-show="form.name.$dirty && form.name.$invalid">\n\
                            <span class="help-block-custom" ng-show="form.$error.pattern"><strong>Invalid Property Name.</strong></span>\n\
                            <span class="help-block-custom" ng-show="form.$error.minlength"><strong>Property Name cannot be shorter than 3 characters.</strong></span>\n\
                            <span class="help-block-custom" ng-show="form.$error.maxlength"><strong>Property Name cannot be longer than 128 characters.</strong></span>\n\
                         </div>\n\
                      </div>',
                link: function (scope) {
                    //scope.showAddnew = false;
                    scope.$watch("allProps", function (
                        newValue) {

                        scope.allStaticProperties = angular.fromJson(newValue);
                        if (scope.selectedValue.name === "add new -->") {
                            return;
                        }
                        scope.addNew.value = false;
                    }, true);

                    scope.shouldIShowAddTextBox =
                        function () {

                            /* if(scope.selectedValue.name !== null){
                                scope.iconColor.color="glyphicon-red";
                            }else{
                                scope.iconColor.color="glyphicon-white";
                            } */
                            if (scope.selectedValue.name === "add new -->") {
                                scope.addNew.value = true;
                                scope.addedProperty.value = '';
                            } else {
                                scope.addNew.value = false;
                            }
                    };
                }
            };
        });