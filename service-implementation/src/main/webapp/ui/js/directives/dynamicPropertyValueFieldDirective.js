angular.module(
    'myApp.dynamicPropertyValueFieldDirective', []
    ).directive (
       'dynamicPropertyValueFieldDirective', 
        function($compile, $rootScope) {
            console.log("Directive");
            
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
                    currentRowObject: '=',
                    propertyToBeModified : '=',
                    availableProperties : '=',
                    addedProperties : '='
                    
                },        
                link: function(scope, elem, attrs) {
                    
                    var templateUrl = getTemplateUrl(scope.currentRowObject);
                    
                    $rootScope.restService.get(templateUrl, function (data) {
                          elem.html(data);
                          $compile(elem.contents())(scope);
                    });    
                    
                    scope.setPropertyValue = function(oldPropertyValue, newPropertyValue) {
                    
                        // if both values are same no need to do anything
                        if (oldPropertyValue === newPropertyValue) {
                            return;
                        }
                        
                        // if old property is not available just set the value in propertyToBeModified as it is a newly added property and will be handled via add button
                        if (oldPropertyValue === "") {
                             scope.propertyToBeModified.value.value = newPropertyValue;
                        	return;
                        }
                        // if propertyToBeModified is empty then it means the user just edits 
                        // already existing value in grid so assign the currentRowObject as property to be modified
                        if (scope.propertyToBeModified.value == "") {
                            scope.propertyToBeModified.value = angular.copy(scope.currentRowObject);
                        }
                        scope.propertyToBeModified.value.value = newPropertyValue;
                        if (scope.propertyToBeModified.value.name === "" || scope.propertyToBeModified.value.value === "") {
                             showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                             return;
                        }
                        var isPropertyFound = false;
                        // remove the empty property at the last and add it again once addition is done
                        scope.removeEmptyProperty();
                        for (var i = 0; i < scope.availableProperties.length; i ++) {
                            propertyToBeChanged = scope.availableProperties[i];
                            if (propertyToBeChanged.name === scope.propertyToBeModified.value.name) {
                                scope.availableProperties.splice(i, 1);
                                scope.addedProperties.push(scope.propertyToBeModified.value);
                                isPropertyFound = true;
                                break;
                            }
                        }
                        // if property is not found in available properties then it is a custom property added by user
                        if (!isPropertyFound && scope.propertyToBeModified.value !== "") {
                            scope.propertyToBeModified.value.isCustomized = "true";
                            scope.addedProperties.push(scope.propertyToBeModified.value);
                        }
                        // add empty property
                        scope.addEmptyProperty();
                        $rootScope.$emit("propertyModificationActionEvent");
                    };
                    
                       // function to remove the empty property
                     scope.removeEmptyProperty = function() {
                         scope.addedProperties.splice((scope.addedProperties.length - 1), 1);
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
                            "validationRules": {}
                            }); 
                     };
                   
                }
            };   
        }
        ); 