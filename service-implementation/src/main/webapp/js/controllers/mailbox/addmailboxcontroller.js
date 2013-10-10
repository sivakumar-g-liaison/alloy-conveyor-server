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
            value: "5",
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

        /*$scope.dynamicProps = [
			'PROP1','PROP2','PROP3','PROP4'
		];*/

        $scope.dynamicProp = $scope.dynamicProps[0];

        /*$scope.countries = [
        algeria={name:'ALGERIA', phoneCode:'213'},
        andorra={name:'ANDORRA', phoneCode:'376'},
        angola={name:'ANGOLA', phoneCode:'244'}
    ];*/

        $scope.tpl = '<button ng-click="addRow()">Add Row</button>';

        $scope.gridOptions = {
            data: 'val',
            columnDefs: [{
                    field: "name",
                    displayName: "Name",
                    cellTemplate: '<div class="swapComboOrText" my-data="{{row.getProperty(col.field)}}" prop-data="{{dynamicProps}}" prop-model="{{dynamicProp}}"></div>'
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

                //This won't trigger addRow() because the template is present in the chooseButton Directive.
                //{field: "isPlus", displayName: "Action", cellTemplate: '<div class="swapButton" //get-Btn="{{row.getProperty(col.field)}}" add-row="addRow()" delete-row="deleteRow(row)"></div>'}

                //,
                /*
						  Ganesh this will trigger addRow(); because the template is within the scope.
						  ,{field: "isPlus", displayName: "Action", cellTemplate: $scope.tpl}
						  */
            ]
        };

        // Dummy needs to be implemented.
        $scope.deleteRow = function (row) {

            alert(row.rowIndex);
            //$scope.val.splice(row.rowIndex, 1);
        };

        // Dummy needs to be implemented.
        $scope.addRow = function () {

            alert("Succs");
            //$scope.val.splice(row.rowIndex, 1);
        };

    }
])