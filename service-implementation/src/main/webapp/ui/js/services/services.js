'use strict';

// simple stub that could use a lot of work...
myApp.factory('RESTService',
    function ($http, $rootScope) {
        var headers = {'acl-manifest':'H4sIAAAAAAAAAFVQYU/CMBT8L++jAcPKFNgnC0xdVEjGoonGmLo+SbVrl7YsLoT/bgsTNEuWvHf37q63BVQNSl0jJFsoDTKHfO5/kAAZRBf9QdQnpIhIEo+Ti/h8OI6eoQdrqd+ZzDgkaiNlD2pmULk/s9ElWntabCyaMAHHSofhkylCrqRgwmp1zrGBA+laGOsWrMK/l/fs/67LOW1pXUtRMie0OmA77y2Z+9CmguRlexwO13BDvI3REqfMIqdlCDnTyvnVns51xYTqyKs0f8xm6ds0X96luT88oEUbyoJlfkMX2TMtsuXiiGXKoVFMdgpzMhrRdBoP6ISQyeUwiqP4kl7PhnQ8Gg8naRfGW8Mdtg9MsTVWvkjKK6HgNfQoGiFxjft0Bq3emDIon4XTbuwC5Whd/ss4oeF5+O084QvbfnX08JQaTSWs3Zfn5RsmN7/aJ6hTf7rNivQ+WxWwew1fD5T3Q75C04gS51ij4qjKdl+nr8I7vgTmDzsGSP9iAgAA'}; // Dummy Headers
        return {
            get: function (url, callback, params) {
                return $http({
                    method: 'GET',
                    url: url,
                    params: params,
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
            post: function (url, body, callback, headers) {
                if (typeof headers == 'undefined') {
                     headers = {'acl-manifest':'H4sIAAAAAAAAAFVQYU/CMBT8L++jAcPKFNgnC0xdVEjGoonGmLo+SbVrl7YsLoT/bgsTNEuWvHf37q63BVQNSl0jJFsoDTKHfO5/kAAZRBf9QdQnpIhIEo+Ti/h8OI6eoQdrqd+ZzDgkaiNlD2pmULk/s9ElWntabCyaMAHHSofhkylCrqRgwmp1zrGBA+laGOsWrMK/l/fs/67LOW1pXUtRMie0OmA77y2Z+9CmguRlexwO13BDvI3REqfMIqdlCDnTyvnVns51xYTqyKs0f8xm6ds0X96luT88oEUbyoJlfkMX2TMtsuXiiGXKoVFMdgpzMhrRdBoP6ISQyeUwiqP4kl7PhnQ8Gg8naRfGW8Mdtg9MsTVWvkjKK6HgNfQoGiFxjft0Bq3emDIon4XTbuwC5Whd/ss4oeF5+O084QvbfnX08JQaTSWs3Zfn5RsmN7/aJ6hTf7rNivQ+WxWwew1fD5T3Q75C04gS51ij4qjKdl+nr8I7vgTmDzsGSP9iAgAA'} ;
                }              
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
                if (typeof headers == 'undefined') {
                     headers = {'acl-manifest':'H4sIAAAAAAAAAFVQYU/CMBT8L++jAcPKFNgnC0xdVEjGoonGmLo+SbVrl7YsLoT/bgsTNEuWvHf37q63BVQNSl0jJFsoDTKHfO5/kAAZRBf9QdQnpIhIEo+Ti/h8OI6eoQdrqd+ZzDgkaiNlD2pmULk/s9ElWntabCyaMAHHSofhkylCrqRgwmp1zrGBA+laGOsWrMK/l/fs/67LOW1pXUtRMie0OmA77y2Z+9CmguRlexwO13BDvI3REqfMIqdlCDnTyvnVns51xYTqyKs0f8xm6ds0X96luT88oEUbyoJlfkMX2TMtsuXiiGXKoVFMdgpzMhrRdBoP6ISQyeUwiqP4kl7PhnQ8Gg8naRfGW8Mdtg9MsTVWvkjKK6HgNfQoGiFxjft0Bq3emDIon4XTbuwC5Whd/ss4oeF5+O084QvbfnX08JQaTSWs3Zfn5RsmN7/aJ6hTf7rNivQ+WxWwew1fD5T3Q75C04gS51ij4qjKdl+nr8I7vgTmDzsGSP9iAgAA'} ;
                }    
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
                var headers = {'acl-manifest':'H4sIAAAAAAAAAFVQYU/CMBT8L++jAcPKFNgnC0xdVEjGoonGmLo+SbVrl7YsLoT/bgsTNEuWvHf37q63BVQNSl0jJFsoDTKHfO5/kAAZRBf9QdQnpIhIEo+Ti/h8OI6eoQdrqd+ZzDgkaiNlD2pmULk/s9ElWntabCyaMAHHSofhkylCrqRgwmp1zrGBA+laGOsWrMK/l/fs/67LOW1pXUtRMie0OmA77y2Z+9CmguRlexwO13BDvI3REqfMIqdlCDnTyvnVns51xYTqyKs0f8xm6ds0X96luT88oEUbyoJlfkMX2TMtsuXiiGXKoVFMdgpzMhrRdBoP6ISQyeUwiqP4kl7PhnQ8Gg8naRfGW8Mdtg9MsTVWvkjKK6HgNfQoGiFxjft0Bq3emDIon4XTbuwC5Whd/ss4oeF5+O084QvbfnX08JQaTSWs3Zfn5RsmN7/aJ6hTf7rNivQ+WxWwew1fD5T3Q75C04gS51ij4qjKdl+nr8I7vgTmDzsGSP9iAgAA'}; 
                return $http({
                    method: 'DELETE',
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