angular.module(
    'myApp.dynamicPropertyValueFieldDirective', []
    ).directive (
       'dynamicPropertyValueFieldDirective', 
        function($compile) {
            console.log("Directive");
            return {
                restrict: 'E',
                replace: true,
                scope : {
                    currentRowObject: '=',
                },
                template: '<div ng-switch on ="currentRowObject.type">' + 
                            '<div ng-switch-when="textarea">\n\
                                 <textarea class="form-control" ng-model="COL_FIELD" ng-input="COL_FIELD" style="width:90%;height: 45px" placeholder="required" />\n\
                            </div>'+
                            '<div ng-switch-when="select">\n\
                                <select ng-model="COL_FIELD" ng-input="COL_FIELD" ng-init="COL_FIELD" ng-options="property.name for property in currentRowObject.options"><option value="">--select--</option></select>\n\
                            </div>'+
                          '</div>',    
                                 
                link: function(scope, elem, attrs) {
                
                	/*var type = (scope.currentRowObject.type == "")?"textarea":scope.currentRowObject.type;
                    var e = document.createElement(type);
                    e.setAttribute("ng-required", true);
                    e.setAttribute("ng-disabled", scope.currentRowObject.readOnly);
                    if (scope.currentRowObject.hasOwnProperty('validationRules') && typeof scope.currentRowObject.validationRules != 'undefined' && scope.currentRowObject.validationRules != "") {
                        for (var key in scope.currentRowObject.validationRules) {
                            console.log(key);
                            switch(key.toLowerCase()) {
                                case 'pattern':
                                e.setAttribute("ng-pattern",  scope.currentRowObject.validationRules.pattern);
                                break;
                                case 'maxlength':
                                e.setAttribute("ng-maxLength", scope.currentRowObject.validationRules.maxLength);
                                break;
                                case 'minlength':
                                e.setAttribute("ng-minLength", scope.currentRowObject.validationRules.minLength);
                                break;                               
                            }                           
                        }                    
                    }           
                    elem.append(e); 
                    $compile(elem.contents())(scope);*/ 
                    
                    scope.getFieldType = function() {
                        currentRowObj = angular.copy(angular.fromJson(scope.currentRowObject));
                        return currentRowObj.type;
                    };

                    if (scope.currentRowObject.hasOwnProperty('validationRules') && typeof scope.currentRowObject.validationRules != 'undefined' && scope.currentRowObject.validationRules != "") {
                         elem.attr("ng-disabled", scope.currentRowObject.readOnly);
                         elem.attr("ng-required", true);
                        for (var key in scope.currentRowObject.validationRules) {
                           
                            switch(key.toLowerCase()) {
                                case 'pattern':
                                elem.attr("ng-pattern",  scope.currentRowObject.validationRules.pattern);
                                break;
                                case 'maxlength':
                                elem.attr("ng-maxLength", scope.currentRowObject.validationRules.maxLength);
                                break;
                                case 'minlength':
                                elem.attr("ng-minLength", scope.currentRowObject.validationRules.minLength);
                                break;                               
                            }                           
                        }                    
                    }           
                    $compile(elem.contents())(scope);                    
                   
                }
            };   
        }
        ); 