/* 
 *open the template in the editor.
 */
angular.module(
    'myApp.dynamicValidationDirective', []
).directive('dynamicValidationDirective', 
 function($compile, $rootScope, $parse) {
  return {
      	restrict: 'A',
		terminal: true,
		priority: 100000,
     link: function(scope,element,attrs){ 	 	
         var name = $parse(element.attr('dynamic-validation-directive'))(scope);
			element.removeAttr('dynamic-validation-directive');
			element.attr('name', name);
			$compile(element)(scope);
      }
   };
});