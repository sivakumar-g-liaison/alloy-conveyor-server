'use strict';
// declare top-level module which depends on filters,and services
var myApp = angular.module('myApp', ['myApp.filters',
    'myApp.directiveSwapPropInput',
    'myApp.directiveSwapPropButton',
    'myApp.directiveSwapPropCombo',
    'myApp.directiveCustomCell', // custom directives
    'myApp.dynamicComponentDirectiveForName',
    'myApp.passwordDirective',
    'myApp.dynamicPropertyNameFieldDirective',
    'myApp.dynamicPropertyValueFieldDirective',
    'myApp.dynamicActionFieldDirective',
    'myApp.urlValidation',
    'myApp.cellWithTextBox',
    'ngGrid', // angular grid
    'ngSanitize', // for html-bind in ckeditor
    'ui.ace', // ace code editor
    'ui.bootstrap', // jquery ui bootstrap
    'ngRoute',
    'angularTreeview', // for tree view
    'BlockUI'
]);
var filters = angular.module('myApp.filters', []);
var directives = angular.module('myApp.directives', []);
// bootstrap angular
myApp.config(['$routeProvider', '$locationProvider', '$httpProvider',
    function ($routeProvider, $locationProvider, $httpProvider) {
        // TODO use html5 *no hash) where possible
        //$locationProvider.html5Mode(true);
        $routeProvider.when('/', {
            templateUrl: 'partials/home.html'
        });
        /**
         * make delete type json
         */
        $httpProvider.defaults.headers["delete"] = {
            'Content-Type': 'application/json;charset=utf-8'
        };
        // Add Mailbox
        $routeProvider.when('/mailbox/addMailBox', {
            templateUrl: 'partials/mailbox/addmailbox.html',
            controller: 'AddMailBoxCntrlr'
        });
        $routeProvider.when('/mailbox/getMailBox', {
            templateUrl: 'partials/mailbox/searchmailbox.html',
            controller: 'SearchMailBoxCntrlr'
        });
        $routeProvider.when('/mailbox/processor', {
            templateUrl: 'partials/processor/processor.html',
            controller: 'ProcessorCntrlr'
        });
        $routeProvider.when('/profiles/addProfiles', {
            templateUrl: 'partials/profile/addprofile.html',
            controller: 'ProfileCntrlr'
        });
        $routeProvider.when('/profiles/trigger', {
            templateUrl: 'partials/profile/triggerprofile.html',
            controller: 'TriggerProfileCntrlr'
        });
        /* $routeProvider.when('/contact', {
        templateUrl:'partials/contact.html'
    });
    $routeProvider.when('/about', {
        templateUrl:'partials/about.html'
    });
    $routeProvider.when('/faq', {
        templateUrl:'partials/faq.html'
    });

    // note that to minimize playground impact on app.js, we
    // are including just this simple route with a parameterized 
    // partial value (see playground.js and playground.html)
    $routeProvider.when('/playground/:widgetName', {
        templateUrl:'playground/playground.html',
        controller:'PlaygroundCtrl'
    }); */
        // by default, redirect to site root
        $routeProvider.otherwise({
            redirectTo: '/'
        });
    }
]);
// this is run after angular is instantiated and bootstrapped
myApp.run(function ($rootScope, $location, $http, $timeout, AuthService, RESTService, $blockUI) {
    // *****
    // Eager load some data using simple REST client
    // *****
    //FOR USE WITH PYTHON
    //$rootScope.base_url = 'http://localhost:8080/g2mailboxservice/rest/v1/mailbox';
    $rootScope.base_url = '../rest/mailbox';
    $rootScope.kms_base_url='/kms/key-management';
    //$rootScope.kms_base_url='http://10.0.24.129:8989/key-management';
    $rootScope.url_upload_key = $rootScope.kms_base_url+'/upload/public';
    $rootScope.url_ssh_upload_key = $rootScope.kms_base_url+'/upload/keypair';
    $rootScope.url_link_key_store = $rootScope.kms_base_url+'/update/truststore/';
	$rootScope.url_secret_service = $rootScope.kms_base_url+'/secret/';
    //$rootScope.url_secret_service = 'http://10.0.24.129:8989/key-management/secret/';
    
	$rootScope.block = $blockUI.createBlockUI();
	
    $rootScope.restService = RESTService;
    // validation of user input pattern
    $rootScope.userInputPattern = /^[a-zA-Z0-9\-:_.,\s]+$/;
    //$rootScope.inputPatternForCredentialsURI =/^(ftp|ftps|sftp|http|https):\/\/([A-Za-z0-9_.]+:[A-Za-z0-9_*#@!.]+)?@[A-Za-z0-9_.]+([\/[A-Za-z0-9_]*]*)(.[a-z]*?)/
    $rootScope.inputPatternForFolderURI = /^[a-zA-Z0-9_\\\/!@#\$%^&()--+=\/{}.,;'~` ]+$/;
    $rootScope.inputPatternForURL = /^(ftp|ftps|sftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?$/;
    $rootScope.numberPattern = /^\d+$/;
    $rootScope.retryAttemptsPattern = /^[0-4]$/;
    $rootScope.numberTimeOutPattern = /^([1-9][0-9]{0,3}|[1-5][0-9]{4}|60000)$/;
    $rootScope.httpVersionPattern = /\b1.1\b/;
    $rootScope.multipleEmailPattern = /^(([a-zA-Z0-9_'+*$%\^&!\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9:]{2,7})([,]\W?(?!$))?)+$/;
	$rootScope.inputPatternForPort = /^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$/;
    // These variables can be used as attributes when the ng-maxlength issue is fixed in angular js.
    // As of now used only for displaying the no of characters in error message.
    $rootScope.maximumLengthAllowedInTextBox = 80;
    $rootScope.minimumLength = 5;
    $rootScope.maximumLengthAllowedInMailBoxDescription = 1024;
    $rootScope.maximumLengthAllowedInProcessorDescription = 512;
    $rootScope.minimumLengthAllowedInGrid = 3;
    $rootScope.maximumLengthAllowedInGridForPropertyValue = 512;
    $rootScope.maximumLengthAllowedInGridForFolderDetails = 250;
    $rootScope.maximumLengthAllowedInGridForCredentialDetails = 128;
    $rootScope.typeaheadMinLength = 3;
    
    // These variables used for displaying info icon  where the ng-maxlength  and ng-minlength validation.
	$rootScope.infoIconImgUrl = 'img/alert-triangle-red.png';
   
    // JSON which contains upload public key request
    $rootScope.pkObj;
    $rootScope.restService.get('data/publickeyrequest.json', function (data) {
        $rootScope.pkObj = data;
    });
    // JSON which contains public key - Trust Store Association request
    $rootScope.linkKeyTs;
    $rootScope.restService.get('data/truststore_update_request.json', function (data) {
        $rootScope.linkKeyTs = data;
    });
      // JSON which contains upload ssh key request
    $rootScope.sshKeyObj;
    $rootScope.restService.get('data/upload_keypair_request.json', function (data) {
        $rootScope.sshKeyObj = data;
    });
    // async load constants
    $rootScope.constants = [];
    $rootScope.restService.get('data/constants.json', function (data) {
        $rootScope.constants = data[0];
    });
    // async load data do be used in table (playgound grid widget)
    $rootScope.listData = [];
    $rootScope.restService.get('data/generic-list.json', function (data) {
        $rootScope.listData = data;
    });	
	//  load initial Processor Data
    $rootScope.initialProcessorData;
    $rootScope.restService.get('data/initialProcessorDetails_json.json', function (data) {
        $rootScope.initialProcessorData = data;
    });
    
    // testing purpose
    $rootScope.testJson = [];
    $rootScope.restService.get('data/sweeper.json', function (data) {
        $rootScope.testJson = data;
    });
	
	
    // *****
    // Initialize authentication
    // *****
    $rootScope.authService = AuthService;
	// pipeline id
    $rootScope.pipelineId = getParameterByName($location.absUrl(), "pipeLineId");
	// service instance id
	$rootScope.serviceInstanceId = getParameterByName($location.absUrl(), "sid");
	
	//getting values from java properties file
	$rootScope.javaProperties = {
		globalTrustStoreId: "",
		globalTrustStoreGroupId: "",
		mailboxPguidDisplayPrefix: "",
		defaultScriptTemplateName: ""	
	};
	$rootScope.restService.get($rootScope.base_url + '/serviceconfigurations',
		function (data, status) {
			if (status === 200 && data.getPropertiesValueResponseDTO.response.status === 'success') {
				$rootScope.javaProperties.globalTrustStoreId = data.getPropertiesValueResponseDTO.properties.trustStoreId;
				$rootScope.javaProperties.globalTrustStoreGroupId = data.getPropertiesValueResponseDTO.properties.trustStoreGroupId;
				$rootScope.javaProperties.mailboxPguidDisplayPrefix = data.getPropertiesValueResponseDTO.properties.mailboxPguidDisplayPrefix;
				$rootScope.javaProperties.defaultScriptTemplateName = data.getPropertiesValueResponseDTO.properties.defaultScriptTemplateName;
				
			} else {
				return;
			}
		}
	);
	
	
	/*$rootScope.serviceInstancePrimaryId = prompt("NOTE: This is a temporary arrangement till the INTEGRATION with ACL. " +
													"Actual impelentation is to retrive this from ACL manifest." +
													"Make sure you always use a same ID everytime you lauch the UI inorder to search/revise the mailbox and/or processors you added " +
													"Enter PRIMARY Service Instance Id : ", "");
	$rootScope.serviceInstanceSecondaryId = prompt("NOTE: This is a temporary arrangement till the INTEGRATION with ACL. " +
													"Actual impelentation is to retrive this from ACL manifest." +
													"Make sure you always use a same ID everytime you lauch the UI inorder to search/revise the mailbox and/or processors you added " +
													"Enter SECONDARY Service Instance Id : ", "");*/
    // text input for login/password (only)
    $rootScope.loginInput = 'rob@gmail.com';
    $rootScope.passwordInput = 'complexpassword';
    $rootScope.$watch('authService.authorized()', function () {
        // if never logged in, do nothing (otherwise bookmarks fail)
        if ($rootScope.authService.initialState()) {
            // we are public browsing
            return;
        }
        // instantiate and initialize an auth notification manager
        $rootScope.authNotifier = new NotificationManager($rootScope);
        // when user logs in, redirect to home
        if ($rootScope.authService.authorized()) {
            $location.path("/");
            $rootScope.authNotifier.notify('information', 'Welcome ' + $rootScope.authService.currentUser() + "!");
        }
        // when user logs out, redirect to home
        if (!$rootScope.authService.authorized()) {
            $location.path("/");
            $rootScope.authNotifier.notify('information', 'Thanks for visiting.  You have been signed out.');
        }
    }, true);
    // TODO move this out to a more appropriate place
    $rootScope.faq = [{
        key: "What is the service-nucleus?",
        value: "The service nucleus is a starting point for a full-blown Java webservice and accompanying UI."
    }, {
        key: "What are the pre-requisites for running the service nucleus?",
        value: "You need JDK 7 and Gradle>=1.6."
    }, {
        key: "How do I change styling (css)?",
        value: "See service-implementation/bootstrap.  First change the less modules, then compile using build.sh.  The resulting artifacts will be copied to the appropriate location."
    }, {
        key: "How do I implement a REST service?",
        value: "Simply add a new Jersey Resource.  See service-implementation/src/main/java/com/liaison/service/resources/examples for examples."
    }, {
        key: "How do I brand my project (rename from Hello-World)?",
        value: "This is currently a manual process with about a half-dozen steps.  See README.md for details."
    }, {
        key: "How do I expose JMX metrics?",
        value: "Checkout the MetricsResource example."
    }];
	
	/*
	* Pipeline Id code
	*/
	$rootScope.appendQueryParamAddMBox = function() {
		return "#/mailbox/addMailBox";
	};
	
	$rootScope.appendQueryParamManage = function() {
		return "#/profiles/addProfiles";
	};
	
	$rootScope.appendQueryParamTrigger = function() {
		return "#/profiles/trigger";
	};
});

function getParameterByName(url, name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(url);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}