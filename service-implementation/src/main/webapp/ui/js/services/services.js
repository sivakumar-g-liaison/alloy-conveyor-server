'use strict';

// simple stub that could use a lot of work...
myApp.factory('RESTService',
    function ($http, $rootScope) {
        return {
            get: function (url, callback, params) {
                return $http({
                    method: 'GET',
                    url: url,
                    params: params
                }).
                success(function (data, status, headers, config) {
					
                    callback(data, status);
                }).
                error(function (data, status, headers, config) {
                    if(status === 403) {
						$rootScope.block.unblockUI();
						showSaveMessage('You do not have sufficient privilege to invoke the service', 'error');
					} else {
						callback(data, status);
					}
                });
            },
            post: function (url, body, callback, headers) {

                //alert(body);
                return $http({
                    method: 'POST',
                    url: url,
                    data: body,
					headers: headers
                }).
                success(function (data, status, headers, config) {
                    callback(data, status);
                }).
                error(function (data, status, headers, config) {
					if(status === 403) {
						$rootScope.block.unblockUI();
						showSaveMessage('You do not have sufficient privilege to invoke the service', 'error');
					} else {
						callback(data, status);
					}
                });
            },
            put: function (url, body, callback, headers) {

                //alert(body);
                return $http({
                    method: 'PUT',
                    url: url,
                    data: body,
					headers: headers
                }).
                success(function (data, status, headers, config) {
                    callback(data, status);
                }).
                error(function (data, status, headers, config) {
					if(status === 403) {
						$rootScope.block.unblockUI();
						showSaveMessage('You do not have sufficient privilege to invoke the service', 'error');
					} else {
						callback(data, status);
					}
                });
            },
            delete: function (url, callback) {
                var body = "[{}]"; //Dummy body
                return $http({
                    method: 'DELETE',
                    url: url,
                    data: body
                }).
                success(function (data, status, headers, config) {
                    callback(data, status);
                }).
                error(function (data, status, headers, config) {
					if(status === 403) {
						$rootScope.block.unblockUI();
						showSaveMessage('You do not have sufficient privilege to invoke the service', 'error');
					} else {
						callback(data, status);
					}
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

        // initMaybe it wasn't meant to work for mpm?ial state says we haven't logged in or out yet...
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