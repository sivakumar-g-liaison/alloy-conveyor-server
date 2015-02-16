angular.module(
    'myApp.dynamicActionFieldDirective', []
    ).directive (
       'dynamicActionFieldDirective', 
        function($rootScope) {
            console.log("Action Directive");
             return {
                restrict: 'C', 
                scope:{
                    availableProperties : '=',
                    addedProperties : '=',
                    propertyToBeModified : '=',
                    currentRowObject : '@',
                    isMandatory : '=',
                    allowAdd : '='
                },
                template: '<div ng-switch on ="isAdditionAllowed()">'+
                              '<div ng-switch-when="true">'+ 
                                  '<button ng-click="addGridProperty()"><i class="glyphicon glyphicon-plus-sign "></i></button>'+
                              '</div>'+
                              '<div ng-switch-when="false">'+ 
                                '<div ng-switch on="isMandatory">' +
                                    '<div ng-switch-when="true">-NA-</div>' +
                                    '<div ng-switch-when="false"><button ng-click="removeGridProperty()"><i class="glyphicon glyphicon-trash glyphicon-white"></i></button></div>' +
                                '</div>'+
                              '</div>'+
                             '</div>' ,
                link: function(scope) {
                
                    // function which handles addition of properties to grid 
                    // and modifies the datasource array accordingly
                     scope.addGridProperty = function() {
                     
                        if (scope.propertyToBeModified.value.name === "" || scope.propertyToBeModified.value.value === "") {
                             showAlert('It is mandatory to set the name and value of the property being added.', 'error');
                             return;
                        }
                        var propertyToBeAdded = null;
                        var isPropertyFound = false;
                        // remove the empty property at the last and add it again once addition is done
                        scope.removeEmptyProperty();
                        for (var i = 0; i < scope.availableProperties.length; i ++) {
                            propertyToBeAdded = scope.availableProperties[i];
                            if (propertyToBeAdded.name === scope.propertyToBeModified.value.name) {
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
                     
                     // function which handles removal of properties from grid
                    // and modifies the datasource array accordingly
                     scope.removeGridProperty = function() {
                         var propertyToBeRemoved = null;
                         scope.currentRowObject = angular.fromJson(scope.currentRowObject);
                         for (var i = 0; i < scope.addedProperties.length; i ++) {
                            propertyToBeRemoved = scope.addedProperties[i];
                            if (propertyToBeRemoved.name === scope.currentRowObject.name) {
                                scope.currentRowObject.value = "";
                                if(scope.currentRowObject.isCustomized === "false") {
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
                        var currentRowObj = angular.copy(angular.fromJson(scope.currentRowObject));
                        if(!currentRowObj.isMandatory && currentRowObj.value === "") return true;
                        return false;
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
             }
        }
)        
             