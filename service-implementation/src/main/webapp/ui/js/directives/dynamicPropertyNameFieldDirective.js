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
                currentRowObject: '=',
                initialStateObject: '@',
				sortName: '='
            },
            templateUrl: 'partials/directive-templates/propertyNameDirectiveTemplate.html',
            link: function (scope) {          
                
                // funtion that determines whether + icon to be displayed or not in the Action column
                 scope.isAdditionAllowed = function() {
                    var initialStateObject = angular.copy(angular.fromJson(scope.initialStateObject));
                    if(!initialStateObject.isMandatory &&  initialStateObject.value === "") return true;
                    return false;
                 };
                 
                 // function that constructs the currentRowObject correctly according to 
                 // the selected option in the dropdown
                 scope.constructProperty = function(property) {
                 
                    if (property === null || typeof property === 'undefined') {
                        return;
                    }
                    if (property.name === "add new -->") {
                        scope.showAddNewComponent.value = true;
                    } else {
                        scope.showAddNewComponent.value = false;
                        scope.currentRowObject = angular.copy(scope.selectedValue.value);
                    }
                    console.log("currentRowObject"+scope.currentRowObject);
                 };
                 
                 // function that constructs the currentRowObject correctly according to 
                 // the value added in the text area through add new --> option
                 scope.handlePropertyConstructionForAddNew = function(attrName) {
                 
                       scope.currentRowObject = angular.copy(scope.selectedValue.value)
                       scope.currentRowObject.name = attrName;
                       scope.currentRowObject.displayName = attrName;
                       scope.currentRowObject.isCustomized = true;
                       console.log("currentRowObject in onblur"+scope.currentRowObject);
                 };
            }
        };
    });