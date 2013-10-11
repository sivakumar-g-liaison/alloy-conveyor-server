'use strict';

//TODO move to individual file
angular.module('myApp.directiveCustomCell', []).directive('customCell',  function () {

    return {
        restrict: 'C',
        replace: true,
        transclude: true,
        scope: {
            status: '@status',
            name: '@name'
        },

        /*loading the required template based upon the model value*/
        template: '<div ng-switch on="status"><div ng-switch-when="INCOMPLETE"><i class="icon-warning-sign"></i> {{name}}</div><div ng-switch-default>{{name}}</div></div>'
    }

});