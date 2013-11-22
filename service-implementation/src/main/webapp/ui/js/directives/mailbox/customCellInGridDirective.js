'use strict';

//TODO move to individual file
angular.module('myApp.directiveCustomCell', []).directive('customCell', function () {

    return {
        restrict: 'C',
        replace: true,
        transclude: true,
        scope: {
            status: '@status',
            name: '@name'
        },

        /*loading the required template based upon the model value*/
        template: '<div style="display: flex" ng-switch on="status"><div style="overflow:hidden;white-space:nowrap;text-overflow: ellipsis;" ng-switch-when="INCOMPLETE"><i class="glyphicon glyphicon-warning-sign"></i> {{name}}</div><div ng-switch-default style="overflow:hidden;white-space:nowrap;text-overflow: ellipsis;">{{name}}</div></div>'
    }

});