angular.module(
    'myApp.dynamicPropertyValueFieldDirective', []
    ).directive (
       'dynamicPropertyValueFieldDirective', 
        function($compile, $rootScope, $timeout) {
            
            var getTemplateUrl = function(currentRowObject) {
            
                var type = currentRowObject.type;
                var templateUrl = '';

                switch(type) {                 
                    case 'textarea':
                        templateUrl = 'partials/directive-templates/textarea.html';
                        break;
                    case 'select':
                        templateUrl = 'partials/directive-templates/select.html';
                        break;
                }
                return templateUrl;
            };
            

            return {
            
                restrict: 'E',
                replace: true,
                scope : {
                    currentRowObject: '='
                },        
                link: function(scope, elem, attrs) {
                
                   scope.$watch("currentRowObject.name", function() {
                    var templateUrl = getTemplateUrl(scope.currentRowObject);                    
                        $rootScope.restService.get(templateUrl, function (data) {
                              elem.html(data);
                              $compile(elem.contents())(scope);
                        }); 
                   });
                   scope.infoIconImgUrl = 'img/alert-triangle-red.png';
                                       
                    scope.optionSelected = function(selectedOption) {
                        scope.currentRowObject.value = selectedOption;
                    };
                   
                }
            };   
        }
        ); 