'use strict';

// this is the angular way to stop even propagation
angular.module('myApp.directiveSwapPropCombo', []).directive('swapComboOrText', function() {
  
  return {
    restrict: 'C',
    replace: true,
    scope: { myData: '@myData', propData: '@propData', propModel: '@propModel', isOpen: "="},
	
	/*loading the required template based upon the model value*/
    template: '<div ng-switch on="myData">' +
                '<div ng-switch-default><input type=text value="{{propModel}}"></div>' +
                '<div ng-switch-when="-1"><select ng-model="propModel" ng-options="c.name for c in propData"></select></div>' +
              '</div>',
			  
      // The linking function will add behavior to the template
      link: function(scope, element, attrs) {
	  
        scope.$watch('myData', function (v) {
				
				if (v === '-1') {
				
					scope.propModel = angular.fromJson(scope.propModel);
					scope.propData = angular.fromJson(scope.propData);
				}
                
        });
		
		scope.$watch('propModel', function (v) {
			
			console.log('Model val '+scope.isOpen)
			scope.isOpen = 'true';
			console.log('Model val '+scope.isOpen)
		});
		
	}
}
  
});