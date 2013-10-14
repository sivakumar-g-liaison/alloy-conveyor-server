'use strict';

// simple stub that could use a lot of work...
myApp.factory('RESTService',
    function ($http) {
        return {
            get:function (url, callback) {
                return $http({method:'GET', url:url}).
                    success(function (data, status, headers, config) {
                        callback(data);
                    }).
                    error(function (data, status, headers, config) {
                        alert("failed to retrieve data");
                        callback(data);
                    });
            }, post:function (url, body, callback) {
                
            	//alert(body);
                return $http({method:'POST', url:url, data:body}).
                success(function (data, status, headers, config) {
                    callback(data, status);
                }).
                error(function (data, status, headers, config) {
                    callback(data, status);
                });
           }, delete:function (url, callback) {
               var body = "[{}]";//Dummy body
               return $http({method:'DELETE', url:url, data:body}).
                success(function (data, status, headers, config) {
                    callback(data, status);
                }).
                error(function (data, status, headers, config) {
                    callback(data, status);
                });
           }
        
        };
    }
);


// simple auth service that can use a lot of work... 
myApp.factory('AuthService',
    function () {
        var currentUser = null;
        var authorized = false;

        // initial state says we haven't logged in or out yet...
        // this tells us we are in public browsing
        var initialState = true;

        return {
            initialState:function () {
                return initialState;
            },
            login:function (name, password) {
                currentUser = name;
                authorized = true;
                //console.log("Logged in as " + name);
                initialState = false;
            },
            logout:function () {
                currentUser = null;
                authorized = false;
            },
            isLoggedIn:function () {
                return authorized;
            },
            currentUser:function () {
                return currentUser;
            },
            authorized:function () {
                return authorized;
            }
        };
    }
);

