'use strict';


// declare top-level module which depends on filters,and services
var myApp = angular.module('myApp', ['myApp.filters',
    'myApp.directiveSwapPropInput',
    'myApp.directiveSwapPropButton',
    'myApp.directiveSwapPropCombo',
    'myApp.directiveCustomCell', // custom directives
    'myApp.dynamicComponentDirectiveForName',
    'myApp.passwordDirective',
    'myApp.cellWithTextBox',
    'ngGrid', // angular grid
    'ui', // angular ui
    'ngSanitize', // for html-bind in ckeditor
    'ui.ace', // ace code editor
    'ui.bootstrap', // jquery ui bootstrap
    '$strap.directives', // angular strap
    'angularTreeview', // for tree view
    'BlockUI'
]);


var filters = angular.module('myApp.filters', []);
var directives = angular.module('myApp.directives', []);

// bootstrap angular

myApp.config(['$routeProvider', '$locationProvider', '$httpProvider',
    function ($routeProvider, $locationProvider, $httpProvider) {

        /**
         * make delete type json
         */
        $httpProvider.defaults.headers["delete"] = {
            'Content-Type': 'application/json;charset=utf-8'
        };

        // TODO use html5 *no hash) where possible
        // $locationProvider.html5Mode(true);

        $routeProvider.when('/', {
            templateUrl: 'partials/home.html'
        });

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

        $routeProvider.when('/account/addAccount', {
            templateUrl: 'partials/account/account.html',
            controller: 'AccountCntrlr'
        });

        $routeProvider.when('/account/getAccount', {
            templateUrl: 'partials/account/searchaccount.html',
            controller: 'SearchAccountCntrlr'
        });

        /*$routeProvider.when('/contact', {
	        templateUrl:'partials/contact.html'
	    });
	    $routeProvider.when('/about', {
	        templateUrl:'partials/about.html'
	    });
	    $routeProvider.when('/faq', {
	        templateUrl:'partials/faq.html'
	    });*/

        // note that to minimize playground impact on app.js, we
        // are including just this simple route with a parameterized 
        // partial value (see playground.js and playground.html)
        /*$routeProvider.when('/playground/:widgetName', {
	        templateUrl:'playground/playground.html',
	        controller:'PlaygroundCtrl'
	    });*/

        // by default, redirect to site root
        $routeProvider.otherwise({
            redirectTo: '/'
        });

    }
]);


// this is run after angular is instantiated and bootstrapped
myApp.run(function ($rootScope, $location, $http, $timeout, AuthService, RESTService, SharedService, Informer) {

    // *****
    // Eager load some data using simple REST client
    // *****

    $rootScope.base_url = '../g2mailboxservice/rest/v1/mailbox';

    // validation of user input pattern
    $rootScope.userInputPattern = /^[a-zA-Z0-9_ ]+$/;
    //$rootScope.inputPatternForCredentialsURI =/^(ftp|ftps|sftp|http|https):\/\/([A-Za-z0-9_.]+:[A-Za-z0-9_*#@!.]+)?@[A-Za-z0-9_.]+([\/[A-Za-z0-9_]*]*)(.[a-z]*?)/
    $rootScope.inputPatternForFolderURI = /^[a-zA-Z0-9_\\\/!@#\$%^&()--+=\/{}.,;'~` ]+$/;
    $rootScope.inputPatternForURL = /^(ftp|ftps|sftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?$/;
    $rootScope.numberPattern = /^\d+$/;
    $rootScope.httpVersionPattern = /\b1.1\b/;

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

    //FOR USE WITH PYTHON
    //$rootScope.base_url = 'http://localhost:8080/g2mailboxservice/rest/v1/mailbox';
    $rootScope.restService = RESTService;
    $rootScope.informer = Informer;

    $rootScope.sharedService = SharedService;

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

    // *****
    // Initialize authentication
    // *****
    $rootScope.authService = AuthService;

    // text input for login/password (only)
    $rootScope.loginInput = 'user@gmail.com';
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


});