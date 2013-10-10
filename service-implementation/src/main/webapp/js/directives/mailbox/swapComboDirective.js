'use strict';

// this is the angular way to stop even propagation
angular.module('myApp.directiveSwapPropCombo', []).directive('swapComboOrText', function() {
  
  return {
    restrict: 'C',
    replace: true,
    scope: { myData: '@myData', propData: '@propData', propModel: '@propModel'},
	
	/*loading the required template based upon the model value*/
    template: '<div ng-switch on="myData">' +
                '<div ng-switch-default><input type=text value="{{propModel}}"></div>' +
                '<div ng-switch-when="-1"><select ng-model="propModel" ng-options="c for c in propData"></select></div>' +
              '</div>'
  }
  
});