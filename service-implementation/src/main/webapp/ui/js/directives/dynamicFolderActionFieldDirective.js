angular.module(
    'myApp.dynamicFolderActionFieldDirective', []
    ).directive (
       'dynamicFolderActionFieldDirective', 
        function($rootScope) {            
             return {
                restrict: 'E', 
                scope:{
                    folderAvailableProperties : '=',
                    folderAddedProperties : '=',
                    currentRowObject : '=',
                    initialStateObject : '@'
                },
                templateUrl: 'partials/directive-templates/folderActionDirectiveTemplate.html',
                link: function(scope) {
                    
                    // function which handles addition of properties to grid 
                    // and modifies the datasource array accordingly
                     scope.addGridProperty = function() {
                     
                        if (!scope.currentRowObject.folderURI) {
                             showAlert('It is mandatory to set the folderURI of the property being added.', 'error');
                             return;
                        }                        
                         scope.currentRowObject.valueProvided = true;
						 scope.currentRowObject.mandatory = true;
                        for (var i = 0; i < scope.folderAvailableProperties.length; i ++) {
                            var propertyToBeChanged = scope.folderAvailableProperties[i];
                            if (propertyToBeChanged.folderDisplayType === scope.currentRowObject.folderDisplayType) {
                                scope.folderAvailableProperties.splice(i, 1);
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
                         for (var i = 0; i < scope.folderAddedProperties.length; i ++) {
                            var propertyToBeRemoved = scope.folderAddedProperties[i];
                            if (propertyToBeRemoved.folderDisplayType === scope.currentRowObject.folderDisplayType) {                                                 
                                scope.currentRowObject.valueProvided = false;
						        scope.currentRowObject.mandatory = false;
								scope.currentRowObject.folderURI = "";
                                if(scope.currentRowObject.valueProvided === false) {
                                    scope.folderAvailableProperties.push(scope.currentRowObject);
                                }    
                                scope.folderAddedProperties.splice(i , 1);
                                break;
                            }
                        };
                        console.log("added properties"+scope.folderAddedProperties);
                        console.log("available properties"+scope.folderAvailableProperties);
                     };
                     
                     // funtion that determines whether + icon to be displayed or not in the Action column
                     scope.isAdditionAllowed = function() {
					  console.log("added properties"+scope.folderAddedProperties);
                        var initialStateObject = angular.copy(angular.fromJson(scope.initialStateObject));
                        if(!initialStateObject.mandatory && !initialStateObject.valueProvided ) return true;
                        return false;
                     };
                                        
                     // function to add the empty property
                     scope.addEmptyProperty = function() {
                         scope.folderAddedProperties.push({
                          "folderURI": "",
						  "folderDisplayType": "",
						  "folderType": "",
						  "folderDesc": "",
						  "mandatory": false,
						  "readOnly":false,
						  "valueProvided":false,
						  "validationRules":{}
                            }); 
                     };
                    
                }
             }
        }
)        
