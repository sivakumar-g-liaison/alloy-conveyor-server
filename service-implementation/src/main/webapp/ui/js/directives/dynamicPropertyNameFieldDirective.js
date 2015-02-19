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
    function ($rootScope, $timeout) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                allProps: '=',
                addedProps: '=',
                selectedValue: '=',
                showAddNewComponent: '=',
                currentRowObject: '=',
                initialStateObject: '@',
				sortName: '='
            },
            templateUrl: 'partials/directive-templates/propertyNameDirectiveTemplate.html',
            link: function (scope) { 
                
                // function to handle display of readonly property
                // currently pipelineId is the only readonly property available
                 scope.handleReadOnlyProperty = function() {                 
                    switch(scope.currentRowObject.name) {
                        case 'pipelineId':
                        scope.currentRowObject.value = $rootScope.pipelineId;
                        break;
                    }
                 };
                
                scope.handleReadOnlyProperty();
                
                // function that determines whether + icon to be displayed or not in the Action column
                 scope.isAdditionAllowed = function() {
                    var initialStateObject = angular.copy(angular.fromJson(scope.initialStateObject));
                    if(!initialStateObject.isMandatory && !initialStateObject.isValueProvided) return true;
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
                        // if type of currentRow object changed then grid is not getting updated
                        // but if the length of the grid data array gets modified grid is getting updated
                        // used $timeout as a hack to push object to grid data array after the 
                        // UI rendering cycle gets completed                        
                        if (scope.currentRowObject.type !== property.type) {
                        	angular.copy(property, scope.currentRowObject);
                            scope.addedProps.pop();
                            $timeout(function() {
                                scope.addedProps.push(scope.currentRowObject);
                            });
                            
                         } else {
                        	angular.copy(property, scope.currentRowObject);
                        }
                        
                    }
                    console.log("currentRowObject"+scope.currentRowObject);
                 };
                 
                 // function that constructs the currentRowObject correctly according to 
                 // the value added in the text area through add new --> option
                 scope.handlePropertyConstructionForAddNew = function(attrName) {
                       scope.currentRowObject = angular.copy(scope.selectedValue.value)
                       scope.currentRowObject.name = attrName;
                       scope.currentRowObject.displayName = attrName;
                       scope.currentRowObject.isDynamic = true;
                       console.log("currentRowObject in onblur"+scope.currentRowObject);
                 };
            }
        };
    });