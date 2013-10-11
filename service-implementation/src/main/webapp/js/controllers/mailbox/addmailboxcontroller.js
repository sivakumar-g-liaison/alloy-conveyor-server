var rest = myApp.controller('AddMailBoxCntrlr', ['$scope', '$routeParams', '$http', '$filter', '$injector',
    function ($scope, $routeParams, $http, $filter, $injector) {

        req = $scope.request = {
            addMailBoxRequest: {
                mailBox: {
                    name: "",
                    description: "",
                    status: "",
                    properties: []
                }
            }
        };

        $scope.enumstats = [
            'ACTIVE',
            'INACTIVE'
        ];

        req.addMailBoxRequest.mailBox.status = $scope.enumstats[0];
        
        // Loads the details initially if edit
        $scope.load = function() {
        	if ('test' !== $scope.sharedService.getProperty()) {
			
				 $injector.get('RESTService').get($scope.base_url + 'mailbox/' + $scope.sharedService.getProperty(),
					function (data) {
						alert(data.getMailBoxResponse.response.message);
				}
            );
			
			}
        };
		$scope.load();


        $scope.saveForm = function (request) {

            $injector.get('RESTService').post($scope.base_url + 'mailbox', $filter('json')(request),
                function (data, status) {

                    $scope.stats = data.addMailBoxResponse.response.message;
                }
            );
        }

        // Directive Section

        $scope.val = [{
            name: "-1",
            value: "-1",
            isPlus: true
        }, {
            name: "2",
            value: "-1",
            isPlus: false
        }];


        $scope.dynamicProps = [{
            name: 'black',
            shade: 'dark'
        }, {
            name: 'white',
            shade: 'light'
        }, {
            name: 'red',
            shade: 'dark'
        }, {
            name: 'blue',
            shade: 'dark'
        }, {
            name: 'yellow',
            shade: 'light'
        }];

        $scope.dynamicProp = $scope.dynamicProps[0];

        $scope.tpl = '<button ng-click="addRow()">Add Row</button>';
		
		$scope.isOpen = false;

        $scope.gridOptions = {
            data: 'val',
            columnDefs: [{
                    field: "name",
                    displayName: "Name",
                    cellTemplate: '<div class="swapComboOrText" my-data="{{row.getProperty(col.field)}}" prop-data="{{dynamicProps}}" prop-model="{{dynamicProp}}" is-open="isOpen"></div>'
                },

                {
                    field: "value",
                    displayName: "Value",
                    cellTemplate: '<div class="swapInputOrText" my-data="{{row.getProperty(col.field)}}"></div>'
                },

                {
                    field: "isPlus",
                    displayName: "Action",
                    cellTemplate: '<div class="swapButton" get-Btn="{{row.getProperty(col.field)}}" add-row="addRow()" delete-row="deleteRow(row)"></div>'
                }
            ]
        };

        // Dummy needs to be implemented.
        $scope.deleteRow = function (row) {

			alert(row.rowIndex);
            //$scope.val.splice(row.rowIndex, 1);
        };

        // Dummy needs to be implemented.
        $scope.addRow = function () {

			alert($scope.isOpen);
            //$scope.val.splice(row.rowIndex, 1);
        };

    }
])