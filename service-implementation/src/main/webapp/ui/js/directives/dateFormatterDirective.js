directives.directive('dateFormatter', function($rootScope){
    return{
        restrict: 'E',
        template: '{{date}}',
        scope: {
            value: '@'
        },
        link: function(scope, elem, attrs){
            scope.$watch('value', function (newValue) {
                var dateX;
                var timeB;
                var fullDate = scope.value;

                fullDate = new Date(fullDate);

                dateX = fullDate.toLocaleDateString($rootScope.languageFormatData.locale);
                timeB = fullDate.toLocaleTimeString($rootScope.languageFormatData.locale);

                if(dateX == "Invalid Date" || timeB == "Invalid Date"){
                    scope.date = "N/A"
                    return scope.date;
                }
                else{
                    dateX = separateDateAndConstruct(dateX);
                    timeB = separateDateAndConstruct(timeB);
                }

                scope.date = dateX + " - " + timeB;

                return scope.date;
            });
            /*
             Constructs a proper looking date string.
             */
            var constructDate = function(date, separator){
                var construct = "";

                for(var i = 0; i < date.length; i++){
                    if(date[i].length == 1){
                        date[i] = "0".concat(date[i]);
                    }
                    construct = construct.concat(date[i]);
                    if(i < date.length - 1){
                        construct = construct.concat(separator);
                    }
                }
                return construct;
            }

            // This is because "split()" is broken in IE.
            var removeDirectionMarks = function(str) {
                var rtlChar = /[\u0590-\u083F]|[\u08A0-\u08FF]|[\uFB1D-\uFDFF]|[\uFE70-\uFEFF]/mg;
                var dirMark = /\u200e|\u200f/mg;
                var ltrMark = "\u200e";
                var rtlMark = "\u200f";

                return str.replace(rtlChar,'').replace(dirMark,'').replace(ltrMark,'').replace(rtlMark,'');
            }

            var separateDateAndConstruct = function(timeX){
                var separator;

                if(timeX.indexOf('.') > -1){
                    separator = '.';
                }
                else if(timeX.indexOf("/") > -1){
                    separator = '/';

                }
                else if(timeX.indexOf(":") > -1){
                    separator = ':';
                }
                else{
                    separator = '-';
                }

                var separated = timeX.split(separator);

                var test = [];

                for( var i = 0; i < separated.length; i++){
                    test.push(removeDirectionMarks(separated[i].toString()));
                }

                separated = constructDate(test, separator);

                return separated;
            }
        }
    }
});
