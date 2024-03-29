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
                        case 'pipeLineID':
                        scope.currentRowObject.value = $rootScope.pipelineId;
                        break;
                        case 'httpListenerPipeLineId':
                        scope.currentRowObject.value = $rootScope.pipelineId;
                        break;
                    }
                 };
                
                scope.handleReadOnlyProperty();
                
                // function that determines whether + icon to be displayed or not in the Action column
                 scope.isAdditionAllowed = function() {
                 
                    var initialStateObject = angular.copy(angular.fromJson(scope.initialStateObject));
                    if(!initialStateObject.mandatory && !initialStateObject.valueProvided) return true;
                    return false;
                 };
                 
                 // function that constructs the currentRowObject correctly according to 
                 // the selected option in the dropdown
                 scope.constructProperty = function(property) {
                	 
                	scope.showAddNewComponent.value = false;
                    if (property === null || typeof property === 'undefined') {
                        return;
                    }
                    if (property.name === "pipeLineID") {
                        property.value = $rootScope.pipelineId;
                    }
                    if (property.name === "add new -->") {
                        scope.showAddNewComponent.value = true;
                        document.getElementById("addNewText").value = "";
                    } else {
                    	
                    	if (property.defaultValue && !property.value) {
 					       property.value = property.defaultValue;
 					    }
                        scope.showAddNewComponent.value = false;                      
                    }
                    angular.copy(property, scope.currentRowObject);
                 };
                 
                 // function that constructs the currentRowObject correctly according to 
                 // the value added in the text area through add new --> option
                 scope.handlePropertyConstructionForAddNew = function(attrName) {
                 
                       angular.copy(scope.selectedValue.value, scope.currentRowObject);
                       scope.currentRowObject.name = attrName;
                       scope.currentRowObject.displayName = attrName;
                       scope.currentRowObject.dynamic = true;
                 };
            }
        };
    });