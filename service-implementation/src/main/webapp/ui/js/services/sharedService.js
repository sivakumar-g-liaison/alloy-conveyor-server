'use strict';

myApp.factory('SharedService',
    function () {
        var guid = 'test';

    return {
        getProperty: function () {
            return guid;
        },
        setProperty: function(value) {
            guid = value;
        }
    };
    }
);
