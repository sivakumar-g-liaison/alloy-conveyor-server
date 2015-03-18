angular.module(
    'myApp.dynamicFolderUriValueFieldDirective', []
    ).directive (
       'dynamicFolderUriValueFieldDirective', 
        function($compile, $rootScope, $timeout) {
            
            var getTemplateUrl = function() {               
                var templateUrl = 'partials/directive-templates/folderUri.html';                
                return templateUrl;
            };
            return {
            
                restrict: 'E',
                replace: true,
                scope : {
                    currentRowObject: '='
                },        
                link: function(scope, elem, attrs) {     
                   
                    var templateUrl = getTemplateUrl();                    
                        $rootScope.restService.get(templateUrl, function (data) {
                              elem.html(data);
                              $compile(elem.contents())(scope);
                        });
						
                   scope.infoIconImgUrl = 'img/alert-triangle-red.png';                   
                }
         };   
       }
    ); 
