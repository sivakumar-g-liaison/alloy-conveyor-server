/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
angular.module(
    'myApp.dynamicFolderUriValueFieldDirective', []
    ).directive (
       'dynamicFolderUriValueFieldDirective', 
        function($compile, $rootScope, $timeout) {     
          return {
            
			restrict: 'E',
			replace: true,
			scope : {
				currentRowObject: '='
			},
			templateUrl: 'partials/directive-templates/folderUri.html',				
			link: function(scope, elem, attrs) {        
			   scope.infoIconImgUrl = 'img/alert-triangle-red.png';                   
			}
         };   
       }
    ); 