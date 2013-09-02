'use strict';

// declare top-level module which depends on filters,and services
var myApp = angular.module('myApp',
    [   'myApp.filters',
        'myApp.directives', // custom directives
        'ngGrid', // angular grid
        'ui', // angular ui
        'ngSanitize', // for html-bind in ckeditor
        'ui.ace', // ace code editor
        'ui.bootstrap', // jquery ui bootstrap
        '$strap.directives' // angular strap
    ]);

// bootstrap angular
myApp.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {

    // TODO use html5 *no hash) where possible
    // $locationProvider.html5Mode(true);

    $routeProvider.when('/', {
        templateUrl:'partials/home.html'
    });
    $routeProvider.when('/contact', {
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
    });

    // by default, redirect to site root
    $routeProvider.otherwise({
        redirectTo:'/'
    });

}]);

// this is run after angular is instantiated and bootstrapped
myApp.run(function ($rootScope, $location, $http, $timeout, AuthService, RESTService) {

    // *****
    // Eager load some data using simple REST client
    // *****

    $rootScope.restService = RESTService;

    // async load constants
    $rootScope.constants = [];
    $rootScope.restService.get('data/constants.json', function (data) {
            $rootScope.constants = data[0];
        }
    );

    // async load data do be used in table (playgound grid widget)
    $rootScope.listData = [];
    $rootScope.restService.get('data/generic-list.json', function (data) {
            $rootScope.listData = data;
        }
    );


    // *****
    // Initialize authentication
    // *****
    $rootScope.authService = AuthService;

    // text input for login/password (only)
    $rootScope.loginInput = 'git@github.com';
    $rootScope.passwordInput = 'git';

    $rootScope.$watch('authService.authorized()', function () {

        // if never logged in, do nothing (otherwise bookmarks fail)
        if ($rootScope.authService.initialState()) {
            // we are public browsing
            return;
        }

        // when user logs in, redirect to home
        if ($rootScope.authService.authorized()) {
            $location.path("/");
        }

        // when user logs out, redirect to home
        if (!$rootScope.authService.authorized()) {
            $location.path("/");
        }

    }, true);

    // TODO move this out to a more appropriate place
    $rootScope.faq = [
        {key: "What is the service-nucleus?", value: "The service nucleus is a starting point for a full-blown Java webservice and accompanying UI."},
        {key: "What are the pre-requisites for running the service nucleus?", value: "You need JDK 7 and Gradle>=1.6."},
        {key: "How do I change styling (css)?", value:  "See service-implementation/bootstrap.  First change the less modules, then compile using build.sh.  The resulting artifacts will be copied to the appropriate location."},
        {key: "How do I implement a REST service?", value:  "Simply add a new Jersey Resource.  See service-implementation/src/main/java/com/liaison/service/resources/examples for examples."},
        {key: "How do I brand my project (rename from Hello-World)?", value: "This is currently a manual process with about a half-dozen steps.  See README.md for details."},
        {key: "How do I expose JMX metrics?", value: "Checkout the MetricsResource example."}
    ];

});