/*
 Date range picker: Created 03.08.2014 by Keijo Hautajarvi (keijo.hautajarvi@liaison.com)

 Usage
 =====

 Date range picker options (the HTML attribues) are also possible to define in the javascript. See below.

 To HTML
 -------

 <daterangepickerapp ng-model="selectedRange" show-weeks="false" daterangepicker-mode="day" starting-day="1" date-format="dd.MM.yyyy" input-readonly="false" hide-clear-button="false"></daterangepickerapp>

 To Javascript
 -------------

 var testappcontroller = function($scope, daterangepickerappConfig) {

     // Set option values here after inject the daterangepickerappConfig - these are also possible to set statically as HTML attributes.
     // For example the date format can be set dynamically here
     daterangepickerappConfig.showWeeks = false;
     daterangepickerappConfig.daterangepickerMode = 'day';
     daterangepickerappConfig.startingDay = 1;
     daterangepickerappConfig.dateFormat = 'yyyy-MM.dd';
     daterangepickerappConfig.inputReadonly = false;
     daterangepickerappConfig.hideClearButton = false;

 // ng-model object - the selected date range will be here
 $scope.selectedRange;

     // for testing purpose to watch date range changes
     $scope.$watch('selectedRange', function() {
        if ($scope.selectedRange !== undefined) {
            console.log('DATE CHANGED TO: ', $scope.selectedRange);
        }
     });
};


 Localization
 ============

 The default language is english, but localization is also possible using the daterangepickerappConfig object.

 For example in French:
 ----------------------

 daterangepickerappConfig.menuThisWeekText = 'Cette semaine'

 */
angular.module('daterangepickerapp', [])

    .constant('daterangepickerappConfig', {
        // Default date range  picker options
        showWeeks: true,
        daterangepickerMode: 'day',
        startingDay: 0,
        dateFormat: 'dd-MM-yyyy',
        inputReadOnly: false,
        hideClearButton: false,

        // Localization strings, english by default
        okText: 'OK',
        cancelText: 'Cancel',
        fromText: 'From',
        toText: 'To',
        startDateSelectText: 'Select the start date from below:',
        endDateSelectText: 'Select the end date from below:',
        invalidRangeText: 'Invalid range: Start date is bigger!',
        menuTodayText: 'Today',
        menuThisWeekText: 'This week',
        menuThisMonthText: 'This month',
        menuThisYearText: 'This year',
        menuWeekBackwardsText: 'Week backwards',
        menuMonthBackwardsText: 'Month backwards',
        menuYearBackwardsText: 'Year backwards',
        menuCustomRangeText: 'Choose custom range',
        tooltipSelectorButtonText: 'Selector',
        tooltipClearButtonText: 'Clear'
    })
    .controller('daterangepickerappcontroller', function($rootScope, $scope, $filter, daterangepickerappConfig) {
        $scope.dateFormat = $rootScope.languageFormatData.dateRangePattern;   // Set default date format

        $scope.ngModelCtrl = {$setViewValue: angular.noop};

        $scope.selectorVisible = false;
        $scope.customSelectorVisible = false;

        $scope.selectedRange;
        $scope.validRange = true;
        $scope.startDate = new Date();
        $scope.endDate = new Date();

        this.init = function(_ngModelCtrl, _dateFormat) {
            $scope.ngModelCtrl = _ngModelCtrl;
            if (_dateFormat && _dateFormat !== '') {
                $scope.dateFormat = _dateFormat;
            }
        };

        this.getConfig = function() {
            return daterangepickerappConfig;
        };

        $scope.openSelector = function() {
            $scope.customSelectorVisible = false;
            $scope.selectorVisible = ($scope.selectorVisible ? false : true);
        };

        $scope.selectDateRange = function() {
            $scope.customSelectorVisible = false;
            $scope.selectorVisible = false;
            $scope.selectedRange = $filter('date')($scope.startDate, $scope.dateFormat) + ' - '
                + $filter('date')($scope.endDate, $scope.dateFormat);
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.cancelDateRange = function() {
            $scope.customSelectorVisible = false;
            $scope.selectorVisible = false;
        };

        $scope.clearRange = function() {
            $scope.customSelectorVisible = false;
            $scope.selectorVisible = false;
            $scope.selectedRange = '';
            $scope.ngModelCtrl.$setViewValue('');
        };

        $scope.$watch('startDate', function() {
            $scope.checkDateValues();
        });

        $scope.$watch('endDate', function() {
            $scope.checkDateValues();
        });

        $scope.checkDateValues = function() {
            $scope.validRange = ($scope.startDate > $scope.endDate) ? false : true;
        };

        $scope.todayClicked = function() {
            $scope.selectorVisible = false;
            $scope.selectedRange = $scope.getToday();
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.getToday = function() {
            var today = new Date();
            return $filter('date')(today, $scope.dateFormat) + ' - '
                + $filter('date')(today, $scope.dateFormat);
        };

        $scope.thisWeekClicked = function() {
            $scope.selectorVisible = false;
            $scope.selectedRange = $scope.getThisWeek();
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.getThisWeek = function() {
            var tempDate = new Date();
            tempDate.setDate(tempDate.getDate() - tempDate.getDay() + (tempDate.getDay() === 0 ? -6 : 1));
            return $filter('date')(tempDate, $scope.dateFormat) + ' - '
                + $filter('date')(new Date(tempDate.setDate(tempDate.getDate() + 6)), $scope.dateFormat);
        };

        $scope.thisMonthClicked = function() {
            $scope.selectorVisible = false;
            $scope.selectedRange = $scope.getThisMonth();
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.getThisMonth= function() {
            var tempDate = new Date();
            return $filter('date')(new Date(tempDate.getFullYear(), tempDate.getMonth(), 1), $scope.dateFormat) + ' - '
                + $filter('date')(new Date(tempDate.getFullYear(), tempDate.getMonth() + 1, 0), $scope.dateFormat);
        };

        $scope.thisYearClicked = function() {
            $scope.selectorVisible = false;
            $scope.selectedRange = $scope.getThisYear();
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.getThisYear = function() {
            var tempDate = new Date();
            return $filter('date')(new Date(tempDate.getFullYear(), 0, 1), $scope.dateFormat) + ' - '
                + $filter('date')(new Date(tempDate.getFullYear(), 12, 0), $scope.dateFormat);
        };

        $scope.weekBackwardsClicked = function() {
            $scope.selectorVisible = false;
            $scope.selectedRange = $scope.getWeekBackwards();
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.getWeekBackwards = function() {
            var tempDate = new Date();
            tempDate.setDate(tempDate.getDate() - 7);
            return $filter('date')(tempDate, $scope.dateFormat) + ' - '
                + $filter('date')(new Date(), $scope.dateFormat);
        };

        $scope.monthBackwardsClicked = function() {
            $scope.selectorVisible = false;
            $scope.selectedRange = $scope.getMonthBackwards();
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.getMonthBackwards = function() {
            var tempDate = new Date();
            tempDate.setMonth(tempDate.getMonth() - 1);
            return $filter('date')(tempDate, $scope.dateFormat) + ' - '
                + $filter('date')(new Date(), $scope.dateFormat);
        };

        $scope.yearBackwardsClicked = function() {
            $scope.selectorVisible = false;
            $scope.selectedRange = $scope.getYearBackwards();
            $scope.updateModelValue($scope.selectedRange);
        };

        $scope.getYearBackwards = function() {
            var tempDate = new Date();
            tempDate.setFullYear(tempDate.getFullYear() - 1);
            return $filter('date')(tempDate, $scope.dateFormat) + ' - '
                + $filter('date')(new Date(), $scope.dateFormat);
        };

        $scope.customRangeClicked = function() {
            $scope.customSelectorVisible = true;
        };

        $scope.updateModelValue = function(value) {
            $scope.ngModelCtrl.$setViewValue(value);
            $scope.ngModelCtrl.$render();
        };
    })
    .directive('daterangepickerapp', function($compile) {

        var getShowWeeks = function(value) {
            return (value === 'false') ? ' show-weeks=\"false\"' : ' show-weeks=\"true\"';
        };

        var getDaterangepickerMode = function(value) {
            if (value === 'year') {
                return ' datepicker-mode=\"year\"';
            }
            else if (value === 'month') {
                return ' datepicker-mode=\"month\"';
            }
            else if (value === 'day') {
                return ' datepicker-mode=\"day\"';
            }
            else {
                return '';
            }
        };
        var getStartingDay = function(value) {
            if (value === '0') {
                return ' starting-day=\"0\"';
            }
            else if (value === '1') {
                return ' starting-day=\"1\"';
            }
            else if (value === '2') {
                return ' starting-day=\"2\"';
            }
            else if (value === '3') {
                return ' starting-day=\"3\"';
            }
            else if (value === '4') {
                return ' starting-day=\"4\"';
            }
            else if (value === '5') {
                return ' starting-day=\"5\"';
            }
            else if (value === '6') {
                return ' starting-day=\"6\"';
            }
            else {
                return '';
            }
        };

        var getInputReadonly = function(value) {
            return (value === 'true') ? ' readonly' : '';
        };

        var getHideClearButton= function(value) {
            return (value === 'true') ? ' style="display: none;' : '';
        };

        var linker = function(scope, element, attrs, ctrl) {
            var daterangepickerCtrl = ctrl[0];
            var ngModelCtrl = ctrl[1];
            if (!daterangepickerCtrl || !ngModelCtrl) {
                return;
            }
            if (daterangepickerCtrl && ngModelCtrl) {
                // Init controller
                daterangepickerCtrl.init(ngModelCtrl, attrs.dateFormat);

                // Get template
                element.html(getTemplate(scope, element, attrs, ctrl));
                $compile(element.contents())(scope);
            }
        };

        var getTemplate = function(scope, element, attrs, ctrl) {

            // Builds the template
            config = ctrl[0].getConfig();

            var daterangepickerOptions = '<datepicker class="well well-sm datepickerStyle" min="minDate" '
                + getShowWeeks((attrs.showWeeks !== undefined && attrs.showWeeks !== null)?attrs.showWeeks:config.showWeeks)
                + getDaterangepickerMode((attrs.daterangepickerMode !== undefined && attrs.daterangepickerMode !== null)?attrs.daterangepickerMode:config.daterangepickerMode)
                + getStartingDay((attrs.startingDay !== undefined && attrs.startingDay !== null)?attrs.startingDay:config.startingDay)
                + '></datepicker>';

            var inputReadonly = getInputReadonly((attrs.inputReadonly !== undefined && attrs.inputReadonly !== null)?attrs.inputReadonly:config.inputReadonly);
            var hideClearButton = getHideClearButton((attrs.hideClearButton !== undefined && attrs.hideClearButton !== null)?attrs.hideClearButton:config.hideClearButton);

            return '<div>' +
                '<div class="input-group dateRangeInputGroupStyle">' +
                '<input type="text" class="form-control dateRangeHeaderInputStyle" ng-model="selectedRange" ' + inputReadonly + '/>' +
                '<span class="input-group-btn">' +
                '<button type="button" class="btn btn-default dateRangeHeaderButtonStyle" ng-click="openSelector()" tooltip-placement="bottom" tooltip="' + config.tooltipSelectorButtonText + '"><i class="glyphicon glyphicon-calendar"></i></button>' +
                '<button type="button" class="btn btn-default dateRangeHeaderButtonStyle" ng-click="clearRange()"' + hideClearButton + ' tooltip-placement="bottom" tooltip="' + config.tooltipClearButtonText + '"><i class="glyphicon glyphicon-remove"></i></button>' +
                '</span>' +
                '</div> ' +
                '<div class="dateRangeSelectorStyle" ng-show="selectorVisible" id="dateSelector">' +
                '<div class="dateRangeSelectionMenu" ng-show="!customSelectorVisible">' +
                '<ul>' +
                '<li ng-click="todayClicked()">' + getMenuItemFormattedText(config.menuTodayText, scope.getToday()) + '</li>' +
                '<li ng-click="thisWeekClicked()">' + getMenuItemFormattedText(config.menuThisWeekText, scope.getThisWeek()) + '</li>' +
                '<li ng-click="thisMonthClicked()">' + getMenuItemFormattedText(config.menuThisMonthText, scope.getThisMonth()) + '</li>' +
                '<li ng-click="thisYearClicked()">' + getMenuItemFormattedText(config.menuThisYearText, scope.getThisYear()) + '</li>' +
                '<li ng-click="weekBackwardsClicked()">' + getMenuItemFormattedText(config.menuWeekBackwardsText, scope.getWeekBackwards()) + '</li>' +
                '<li ng-click="monthBackwardsClicked()">' + getMenuItemFormattedText(config.menuMonthBackwardsText, scope.getMonthBackwards()) + '</li>' +
                '<li ng-click="yearBackwardsClicked()">' + getMenuItemFormattedText(config.menuYearBackwardsText, scope.getYearBackwards()) + '</li> ' +
                '<li ng-click="customRangeClicked()">' + config.menuCustomRangeText + '</li>' +
                '</ul>' +
                '</div>' +
                '<div class="well-small" ng-show="customSelectorVisible">' +
                '<pre class="dateRangeSelectionTextStyle dateRangeValidSelection" ng-show="validRange"><em>' + config.fromText + ': {{startDate | date: dateFormat}} ' + config.toText + ': {{endDate | date: dateFormat}}</em></pre>' +
                '<pre class="dateRangeSelectionTextStyle dateRangeInvalidSelection" ng-show="!validRange"><em>' + config.invalidRangeText + '</em></pre>' +
                '<div class="dateRangeTextLabelStyle">' + config.startDateSelectText + '</div>' +
                '<div class="dateRangeStyle" ng-model="startDate">' +
                daterangepickerOptions +
                '</div>' +
                '<div class="dateRangeTextLabelStyle">' + config.endDateSelectText + '</div>' +
                '<div class="dateRangeStyle" ng-model="endDate">' +
                daterangepickerOptions +
                '</div>' +
                '<div class="well well-small dateRangeCustomButtons">' +
                '<button class="btn btn-sm btn-primary dateRangeOkButtonStyle" ng-click="selectDateRange()" ng-disabled="!validRange">' + config.okText + '</button>' +
                '<button class="btn btn-sm btn-danger dateRangeCancelButtonStyle" ng-click="cancelDateRange()">' + config.cancelText + '</button>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '</div>';
        };

        var getMenuItemFormattedText = function(menuText, timeText)
        {
            return'<table class="dateRangeMenuText">' +
                '<tr>' +
                '<td>' + menuText + '</td>' +
                '<td>' + timeText + '</td>' +
                '</tr>' +
                '</table>';
        };

        var getMenuItemFormattedText = function(menuText, timeText)
        {
            return'<table class="dateRangeMenuText">' +
                '<tr>' +
                '<td>' + menuText + '</td>' +
                '<td>' + timeText + '</td>' +
                '</tr>' +
                '</table>';
        };

        return {
            restrict: 'EA',
            replace: true,
            scope: {},
            controller: 'daterangepickerappcontroller',
            require: ['daterangepickerapp', '^ngModel'],
            link: linker
        };
    });