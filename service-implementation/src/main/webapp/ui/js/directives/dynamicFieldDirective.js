angular.module(
    'myApp.dynamicFieldDirective', []
    ).directive (
       'dynamicFieldDirective', 
        function($rootScope) {
            console.log("Directive");
             return {
                restrict: 'C', 
                scope : {
                    customobject: '=customObject'
                },
                link: function(scope, elem, attrs) {
                
                    var e = document.createElement(scope.customobject.type);
                    e.setAttribute("ng-required", true);
                    e.setAttribute("ng-disabled", scope.customobject.readOnly);
                    if (scope.customobject.hasOwnProperty('validationRules') && typeof scope.customobject.validationRules != 'undefined' && scope.customobject.validationRules != "") {
                        for (var key in scope.customobject.validationRules) {
                            console.log(key);
                            switch(key.toLowerCase()) {
                                case 'pattern':
                                e.setAttribute("ng-pattern",  scope.customobject.validationRules.pattern);
                                break;
                                case 'maxlength':
                                e.setAttribute("ng-maxLength", scope.customobject.validationRules.maxLength);
                                break;
                                case 'minlength':
                                e.setAttribute("ng-minLength", scope.customobject.validationRules.minLength);
                                break;                               
                            }                           
                        }                    
                    }           
                    elem.append(e); 
                    $compile(elem.contents())(scope);                    
                   
                }
            };   
        }
        ); 