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
                showAddNewComponent: '=',
                currentRowObject: '=',
                initialStateObject: '@',
				sortName: '='
            },
            templateUrl: 'partials/directive-templates/folderTypeDirectiveTemplate.html',
            link: function (scope) {
			
                // function that determines whether + icon to be displayed or not in the Action column
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
                    angular.copy(property, scope.currentRowObject);
                    console.log("currentRowObject"+scope.currentRowObject);                  	
                 };          
			}
        };
    });