'use strict';

angular.module('myApp.directiveSwapPropInput', []).directive('swapInputOrText', function() {
  
  return {
    restrict: 'C',
    replace: true,
    transclude: true,
    scope: { myData: '@myData' },
	
    template: '<div ng-switch on="myData">' +
                '<div ng-switch-when="-1"><input type=text required></div>' +
                '<div ng-switch-when="5">{{myData}}</div>' +
              '</div>'
  }
  
});