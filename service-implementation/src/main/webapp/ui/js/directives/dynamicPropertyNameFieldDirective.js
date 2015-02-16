/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
angular.module(
        'myApp.dynamicPropertyNameFieldDirective', []
    )
    .directive(
    'dynamicPropertyNameFieldDirective',
    function ($rootScope) {
        return {
            restrict: 'C',
            replace: true,
            scope: {
                allProps: '=',
                selectedValue: '=',
                showAddNewComponent: '=',
                addedProperty: '=',
                currentRowObject: '@',
				sortName: '=',
                propName: '='
            },
            template: '<div ng-switch on="isAdditionAllowed()">' +
                '<div ng-switch-when="false">{{propName}}</div>' +
                '<div class="alignDiv" ng-switch-when="true">\n\
                <div ng-form="form">\n\
                    <select ng-change="constructProperty()" ng-model="selectedValue.value" ng-options="property.name for property in allProps | orderBy:sortName ">\n\
                         <option value="">-- select--</option>\n\
                    </select><i>&nbsp</i>\n\
                    <textarea class="form-control alignDynamicTextarea" ng-input="COL_FIELD" ng-show="showAddNewComponent.value" ng-model="addedProperty.value.name" placeholder="required" ng-blur="handlePropertyConstructionForAddNew(addedProperty.value.name)" style="width:47%;height:45px;"></textarea></div>\n\
                  </div>',
            link: function (scope) {
            
                
                scope.isAdditionAllowed = function() {  
                    var currentRowObj = angular.copy(angular.fromJson(scope.currentRowObject));
                    if(!currentRowObj.isMandatory && currentRowObj.value === "") return true;
                    return false;
                 };
                 
                 scope.constructProperty = function() {
                    if (scope.selectedValue.value !== null && scope.selectedValue.value.name === "add new -->") {
                        scope.showAddNewComponent.value = true;
                        scope.addedProperty.value = '';
                    } else {
                        scope.showAddNewComponent.value = false;
                        scope.addedProperty.value = angular.copy(scope.selectedValue.value);
                    }
                 };
                 scope.handlePropertyConstructionForAddNew = function(attrName) {
                       scope.addedProperty.value = angular.copy(scope.selectedValue.value);
                       scope.addedProperty.value.name = attrName;
                       scope.addedProperty.value.displayName = attrName;                        
                       console.log("newly added property"+scope.addedProperty);
                 };
            }
        };
    });