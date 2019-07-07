'use strict';

angular.module('BlockUI', []).provider("$blockUI", function() {
  var defaults;
  defaults = {
    innerHTML: 'Loading ...',
    blockUIClass: "blockui-blocked"
  };
  this.$get = [
    "$document", function($document) {
      var BlockUI, body, createElement;
      body = $document.find('body');
      createElement = function(className) {
        return angular.element("<div>").addClass(className);
      };
      BlockUI = function(opts) {
        var backdropCss, messageBoxCss, options;
        options = this.options = angular.extend({}, defaults, opts);
        console.log("blockuiservice::constructor() - options:", options);
        if (options.backdropClass != null) {
          this.backdropEl = createElement(options.backdropClass);
        } else {
          backdropCss = {
            'z-index': 10001,
            border: 'none',
            margin: 0,
            padding: 0,
            width: '100%',
            height: '100%',
            top: 0,
            leff: 0,
            'background-color': '#000',
            opacity: 0.6,
            cursor: 'wait',
            position: 'fixed'
          };
          this.backdropEl = angular.element('<div>').css(backdropCss);
        }
        if (options.messageClass != null) {
          this.messageEl = createElement(options.messageClass);
        } else {
          messageBoxCss = {
            'z-index': 10002,
            position: 'fixed',
            'text-align': 'center',
            width: '30%',
            top: '40%',
            left: '30%',
            border: 'none',
            padding: '15px',
            backgroundColor: '#000',
            '-webkit-border-radius': '10px',
            '-moz-border-radius': '10px',
            'border-radius': '10px',
            opacity: 0.5,
            color: '#fff'
          };
          this.messageEl = angular.element('<div>').css(messageBoxCss).html(options.innerHTML);
        }
      };
      BlockUI.prototype.isBlocked = function() {
        return this._blocked;
      };
      BlockUI.prototype.blockUI = function() {
        var self;
        if (!!this._blocked) {
          return;
        }
        self = this;
        self._addElementsToDom();
        body.addClass(self.options.blockUIClass);
      };
      BlockUI.prototype.unblockUI = function() {
        var self;
        self = this;
        body.removeClass(self.options.blockUIClass);
        this._removeElementsFromDom();
      };
      BlockUI.prototype._addElementsToDom = function() {
        body.append(this.messageEl);
        body.append(this.backdropEl);
        this._blocked = true;
      };
      BlockUI.prototype._removeElementsFromDom = function() {
        this.messageEl.remove();
        this.backdropEl.remove();
        this._blocked = false;
      };
      return {
        createBlockUI: function(opts) {
          return new BlockUI(opts);
        }
      };
    }
  ];
});