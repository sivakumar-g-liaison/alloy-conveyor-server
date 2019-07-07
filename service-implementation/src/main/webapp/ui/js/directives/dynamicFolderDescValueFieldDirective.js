/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
angular.module(
    'myApp.dynamicFolderDescValueFieldDirective', []
    ).directive (
       'dynamicFolderDescValueFieldDirective', 
        function($compile, $rootScope, $timeout) {            
          return {		  
			restrict: 'E',
			replace: true,
			scope : {
				currentRowObject: '='
			},
			link: function(scope, elem, attrs) { 
                scope.$watch("currentRowObject.folderType", function() {
                var templateUrl = 'partials/directive-templates/folderDescription.html';                    
                    $rootScope.restService.get(templateUrl, function (data) {
                          elem.html(data);
                          $compile(elem.contents())(scope);
                    }); 
                });
			   scope.infoIconImgUrl = 'img/alert-triangle-red.png';		   
			}
         };   
       }
   ); 