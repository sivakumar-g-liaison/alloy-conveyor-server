angular.module(
    'myApp.dynamicActionFieldDirective', []
    ).directive (
       'dynamicActionFieldDirective', 
        function($rootScope) {
            console.log("Action Directive");
             return {
                restrict: 'E', 
                scope:{
                    availableProperties : '=',
                    addedProperties : '=',
                    currentRowObject : '=',
                    initialStateObject : '@'
                },
                templateUrl: 'partials/directive-templates/propertyActionDirectiveTemplate.html',
                link: function(scope) {
                    
                    // function which handles addition of properties to grid 
                    // and modifies the datasource array accordingly
                     scope.addGridProperty = function() {
                     
                        if (scope.currentRowObject.name === "" || typeof scope.currentRowObject === 'undefined' || typeof scope.currentRowObject.value === 'undefined' || scope.currentRowObject.value === "") {
                             showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                             return;
                        }
                        console.log("addedProperties"+scope.addedProperties);
                         var isPropertyFound = false;
                         scope.currentRowObject.isValueProvided = true;
                        for (var i = 0; i < scope.availableProperties.length; i ++) {
                            var propertyToBeChanged = scope.availableProperties[i];
                            if (propertyToBeChanged.name === scope.currentRowObject.name) {
                                scope.availableProperties.splice(i, 1);
                                break;
                            }
                        }
                         // add empty property
                        scope.addEmptyProperty();
                        $rootScope.$emit("propertyModificationActionEvent");
                      };
                     
                     // function which handles removal of properties from grid
                    // and modifies the datasource array accordingly
                     scope.removeGridProperty = function() {
                         for (var i = 0; i < scope.addedProperties.length; i ++) {
                            var propertyToBeRemoved = scope.addedProperties[i];
                            if (propertyToBeRemoved.name === scope.currentRowObject.name) {
                                if (scope.currentRowObject.defaultValue !== "") {
                                    scope.currentRowObject.value = scope.currentRowObject.defaultValue;
                                } else {
                                    scope.currentRowObject.value = "";
                                }                               
                                scope.currentRowObject.isValueProvided = false;
                                if(scope.currentRowObject.isCustomized === false) {
                                    scope.availableProperties.push(scope.currentRowObject);
                                }    
                                scope.addedProperties.splice(i , 1);
                                break;
                            }
                        };
                        console.log("added properties"+scope.addedProperties);
                        console.log("available properties"+scope.availableProperties);
                     };
                     
                     // funtion that determines whether + icon to be displayed or not in the Action column
                     scope.isAdditionAllowed = function() {
                        var initialStateObject = angular.copy(angular.fromJson(scope.initialStateObject));
                        if(!initialStateObject.isMandatory && !initialStateObject.isValueProvided ) return true;
                        return false;
                     };
                                        
                     // function to add the empty property
                     scope.addEmptyProperty = function() {
                         scope.addedProperties.push({
                            "name":"",
                            "displayName" : "",
                            "value":"",
                            "type":"textarea",
                            "readOnly":"",
                            "isMandatory":false,
                            "isCustomized":false,
                            "isValueProvided":false,
                            "validationRules": {}
                            }); 
                     };
                    
                }
             }
        }
)        
