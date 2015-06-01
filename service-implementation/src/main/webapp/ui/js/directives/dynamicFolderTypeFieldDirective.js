/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
angular.module(
        'myApp.dynamicFolderTypeFieldDirective', []
    )
    .directive(
    'dynamicFolderTypeFieldDirective',
    function ($rootScope, $timeout) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                allProps: '=',
                selectedValue: '=',                
                currentRowObject: '=',
                initialStateObject: '@',
				sortName: '='
            },
            templateUrl: 'partials/directive-templates/folderTypeDirectiveTemplate.html',
            link: function (scope) {
			
                 // function that determines whether selected option in the dropdown 
				 // or currentRowObject to be displayed
                 scope.isFolderTypeAllowed = function() {                 
                    var initialStateObject = angular.copy(angular.fromJson(scope.initialStateObject));
                    if(!initialStateObject.mandatory && !initialStateObject.valueProvided) return true;
                    return false;
                 };
                 
                 // function that constructs the currentRowObject correctly according to 
                 // the selected option in the dropdown
                 scope.constructProperty = function(property) {
                 
                    if (property === null || typeof property === 'undefined') {
                        return;
                    }
                    property.folderURI = scope.currentRowObject.folderURI;
                    property.folderDesc = scope.currentRowObject.folderDesc;
                    angular.copy(property, scope.currentRowObject);                 	
                 };          
			}
        };
    });