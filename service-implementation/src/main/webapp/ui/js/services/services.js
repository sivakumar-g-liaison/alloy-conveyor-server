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
					} else if (status === 401) {
						$rootScope.block.unblockUI();
                        showSaveMessage('Session expired, please log in to SOA Proxy to re-activate session', 'error');
                    } else {
						callback(data, status);
					}
                });
            },
            post: function (url, body, callback, headers) {

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
					} else if (status === 401) {
						$rootScope.block.unblockUI();
                        showSaveMessage('Session expired, please log in to SOA Proxy to re-activate session', 'error');
                    } else {
						callback(data, status);
					}
                });
            },
            put: function (url, body, callback, headers) {

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
					} else if (status === 401) {
						$rootScope.block.unblockUI();
                        showSaveMessage('Session expired, please log in to SOA Proxy to re-activate session', 'error');
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
					} else if (status === 401) {
						$rootScope.block.unblockUI();
                        showSaveMessage('Session expired, please log in to SOA Proxy to re-activate session', 'error');
                    } else {
						callback(data, status);
					}
                });
            }

        };
    }
);
