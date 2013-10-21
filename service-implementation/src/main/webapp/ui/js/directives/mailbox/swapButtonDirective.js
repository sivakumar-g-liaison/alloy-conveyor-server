'use strict';

angular.module('myApp.directiveSwapPropButton', []).directive('swapButton', function() {
  
  return {
    restrict: 'C',
    replace: true,
    transclude: true,
    scope: { val: '@getBtn', addRow:'&' , delRow:'&deleteRow'},
    template: '<div ng-switch on="val">' +
                '<div ng-switch-when="true"><button ng-click="addRow()">Add Row</button></div>' +
                '<div ng-switch-when="false"><button ng-click="delRow(row)">Delete Row</button></div>' +
              '</div>'
  }
  
});