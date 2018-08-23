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
    'myApp.dynamicValidationDirective',
    'myApp.dynamicFolderTypeFieldDirective',
	'myApp.dynamicFolderActionFieldDirective',
	'myApp.dynamicFolderUriValueFieldDirective',
	'myApp.dynamicFolderDescValueFieldDirective',
    'myApp.urlValidation',
    'myApp.cellWithTextBox',
    'myApp.copyToClipboard',
    'myApp.directives',
    'ngGrid', // angular grid
    'ngSanitize', // for html-bind in ckeditor
    'ui.ace', // ace code editor
    'ui.bootstrap', // jquery ui bootstrap
    'ngRoute',
    'angularTreeview', // for tree view
    'BlockUI',
    'daterangepickerapp'
]);
var filters = angular.module('myApp.filters', []);
var directives = angular.module('myApp.directives', []);
// bootstrap angular
myApp.config(['$routeProvider', '$locationProvider', '$httpProvider',
    function ($routeProvider, $locationProvider, $httpProvider) {
        // TODO use html5 *no hash) where possible
        //$locationProvider.html5Mode(true);

		//GMB-472 Fix - Disable $http request cache
		$httpProvider.defaults.cache = false;
		if (!$httpProvider.defaults.headers.common) {
	        $httpProvider.defaults.headers.common = {};
	    }
	    $httpProvider.defaults.headers.common["Cache-Control"] = "no-cache";
	    $httpProvider.defaults.headers.common.Pragma = "no-cache";

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
        $routeProvider.when('/mailbox/getProcessor', {
            templateUrl: 'partials/processor/searchprocessor.html',
            controller: 'SearchProcessorCntrlr'
        });
        $routeProvider.when('/mailbox/getProcessorDc', {
            templateUrl: 'partials/processor/updateProcessorDc.html',
            controller: 'UpdateProcessorDcCntrlr'
        });
        $routeProvider.when('/mailbox/getProcessorScript', {
            templateUrl: 'partials/processor/processorScript.html',
            controller: 'ProcessorScriptCntrlr'
        });
        $routeProvider.when('/mailbox/getexecutingprocessors', {
            templateUrl: 'partials/processor/executingprocessors.html',
            controller: 'executingprocessorsCntrlr'
        });
        $routeProvider.when('/profiles/addProfiles', {
            templateUrl: 'partials/profile/addprofile.html',
            controller: 'ProfileCntrlr'
        });
        $routeProvider.when('/profiles/trigger', {
            templateUrl: 'partials/profile/triggerprofile.html',
            controller: 'TriggerProfileCntrlr'
        });
        $routeProvider.when('/mailbox/getStagedFiles', {
            templateUrl: 'partials/processor/stagedfiles.html',
            controller: 'StagedFilesCntrlr'
        });
        $routeProvider.when('/mailbox/getInboundFiles', {
            templateUrl: 'partials/processor/inboundfiles.html',
            controller: 'InboundFilesCntrlr'
        });
        // by default, redirect to site root
        $routeProvider.otherwise({
            redirectTo: '/'
        });
    }
]);
// this is run after angular is instantiated and bootstrapped
myApp.run(function ($rootScope, $location, $http, $timeout, RESTService, $blockUI) {

    // *****
    // Eager load some data using simple REST client
    // *****
    $rootScope.base_url = '../mailbox';
    $rootScope.kms_base_url='/kms/key-management';
	$rootScope.url_secret_service = $rootScope.kms_base_url+'/secret/';
	$rootScope.fetchTrustStore =  $rootScope.kms_base_url+'/fetch/truststore/current/';
	$rootScope.fetchSshKeyPair =  $rootScope.kms_base_url+'/fetch/group/keypair/current/';

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
	$rootScope.folderPathPattern = /^\/data\/(sftp|ftp|ftps)\/(.*?)\/(inbox|outbox)(\/(.*?))?$/;
	$rootScope.trustStore_SshKeypair_Pattern = /^[A-Z0-9]*$/;
	$rootScope.mailBoxIdPattern = /^[a-zA-Z0-9]*$/;

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
	$rootScope.typeaheadMaxLength = 80;
	$rootScope.mailBoxIdMaxLength = 32;

    // These variables used for displaying info icon  where the ng-maxlength  and ng-minlength validation.
	$rootScope.infoIconImgUrl = 'img/alert-triangle-red.png';

	// load initial Processor Data
	$rootScope.initialProcessorData;
	$rootScope.conveyorProcessorData;
	$rootScope.legacyRelayProcessorData;
	$rootScope.relayProcessorData;
	//  load edit Processor Data
	$rootScope.editProcessorData;
	$rootScope.editConveyorProcessorData;
	$rootScope.editLegacyRelayProcessorData;
	$rootScope.editRelayProcessorData;

    $rootScope.conveyorProcessorData = loadFile('data/createConveyorProcessorDetails.json');
    $rootScope.editConveyorProcessorData = loadFile('data/editConveyorProcessorDetails.json');
    $rootScope.legacyRelayProcessorData = loadFile('data/createLegacyRelayProcessorDetails.json');
    $rootScope.editLegacyRelayProcessorData = loadFile('data/editLegacyRelayProcessorDetails.json');
    $rootScope.relayProcessorData = loadFile('data/createRelayProcessorDetails.json');
    $rootScope.editRelayProcessorData = loadFile('data/editRelayProcessorDetails.json');

    $rootScope.languageFormatData = {
        'dateRangePattern':'',
        'locale':''
    };
    $rootScope.restService.get('../language/userLocale',
        function(data, status) {
        	if (status === 200) {
                $rootScope.languageFormatData.dateRangePattern = data.TreeMap.dateRangePattern;
                $rootScope.languageFormatData.locale = data.TreeMap.locale;
        	}
    });

    // load java script checkbox details
    $rootScope.javaScriptCheckBox = loadFile('data/javaScriptCheckBox.json');

    // load java script checkbox details for sweeper and conditional sweeper
    $rootScope.javaScriptCheckBoxSweeper = loadFile('data/javaScriptCheckBoxSweeper.json');

	// pipeline id
    $rootScope.pipelineId = getParameterByName($location.absUrl(), "pipeLineId");
	// service instance id
	$rootScope.serviceInstanceId = getParameterByName($location.absUrl(), "sid");

	//getting values from java properties file
	$rootScope.javaProperties = {
		globalTrustStoreId: "",
		globalTrustStoreGroupId: "",
		processorSecureSyncUrlDisplayPrefix: "",
		processorSecureAsyncUrlDisplayPrefix: "",
		processorLowSecureSyncUrlDisplayPrefix: "",
        processorLowSecureAsyncUrlDisplayPrefix: "",
		defaultScriptTemplateName: "",
	    deployAsDropbox:false,
	    clusterTypes:[],
	    dataCenters:[]
	};

    var responseJson = loadFile($rootScope.base_url + '/serviceconfigurations');
    if (responseJson) {
        var propertyResponse = responseJson.getPropertiesValueResponseDTO;
        if (propertyResponse.response.status === 'success') {
            var properties = propertyResponse.properties;

            $rootScope.javaProperties.globalTrustStoreId = properties.trustStoreId;
            $rootScope.javaProperties.globalTrustStoreGroupId = properties.trustStoreGroupId;
            $rootScope.javaProperties.processorSecureSyncUrlDisplayPrefix = properties.processorSecureSyncUrlDisplayPrefix;
            $rootScope.javaProperties.processorSecureAsyncUrlDisplayPrefix = properties.processorSecureAsyncUrlDisplayPrefix;
            $rootScope.javaProperties.processorLowSecureSyncUrlDisplayPrefix = properties.processorLowSecureSyncUrlDisplayPrefix;
            $rootScope.javaProperties.processorLowSecureAsyncUrlDisplayPrefix = properties.processorLowSecureAsyncUrlDisplayPrefix;
            $rootScope.javaProperties.defaultScriptTemplateName = properties.defaultScriptTemplateName;
            $rootScope.javaProperties.deployAsDropbox = properties.deployAsDropbox;
            $rootScope.javaProperties.clusterTypes = properties.clusterTypes;
            $rootScope.javaProperties.dataCenters = properties.dataCenters;
            $rootScope.javaProperties.dataCenters.push("ALL");

            var deploymentType = properties.deploymentType;
            if ('CONVEYOR' === deploymentType) {
                $rootScope.initialProcessorData = $rootScope.conveyorProcessorData;
                $rootScope.editProcessorData = $rootScope.editConveyorProcessorData;
            } else if ('LOW_SECURE_RELAY' === deploymentType) {
                $rootScope.initialProcessorData = $rootScope.legacyRelayProcessorData;
                $rootScope.editProcessorData = $rootScope.editLegacyRelayProcessorData;
            } else {
                $rootScope.initialProcessorData = $rootScope.relayProcessorData;
                $rootScope.editProcessorData = $rootScope.editRelayProcessorData;
            }
        }
    };

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

function loadFile(file) {

    var request = new XMLHttpRequest();
    request.open('GET', file, false);
    request.send(null);
    if (request.status === 200) {
        return JSON.parse(request.responseText);
    }
}
