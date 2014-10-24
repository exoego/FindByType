angular.module('symbolFilters', []).filter('arrow', function () {
    return function (input) {
        // workaround to ng-sanitize ignore type parameters such as Stream<T>
        return input.replace(">", "&gt;").replace("<", "&lt;").replace("-&gt;", '<span class="typefind-method-arrow glyphicon glyphicon-arrow-right"></span>');
    };
});

var app = angular
    .module('tutorial', ['elasticui', 'symbolFilters', 'ngSanitize', 'ngRoute'])
    .constant('euiHost', 'http://localhost:9200')
    .config(function ($routeProvider, $locationProvider) {
        $routeProvider
            .when("/", {
                templateUrl: "/partial/welcome.html"
            })
            .when("/q/", {
                templateUrl: "/partial/result.html"
            })
            .when("/q/:query", {
                templateUrl: "/partial/result.html"
            })
            .otherwise({
                templateUrl: "/partial/otherwise.html"
            });
        $locationProvider.html5Mode(true);
    }).controller("SearchController", function ($scope, $location, $rootScope, $log) {
        $rootScope.$on("$locationChangeStart", function (event, next, current) {
            var q = ($location.path().match("/([^/]*$)") || ["", ""])[1];
            $scope.querystring = q;
        });

        // TODO: change location if querystring changed
//        $scope.$watch("querystring", function (value) {
//            console.log("input:value", value);
//        });
    });

app.directive('methodArguments', ['$compile', function ($compile) {
    return {
        restrict: 'E',
        link: function (scope, element, attrs) {
            var tagName = 'arguments-' + (scope.doc._source.isStatic ? "static" : "instance");
            element.append($compile('<' + tagName + '></' + tagName + '>')(scope))
        }
    }
}]);
app.directive('argumentsStatic', function () {
    return {
        restrict: 'E',
        templateUrl: '/partial/argument-static-method.html',
        link: angular.noop
    };
});
app.directive('argumentsInstance', function () {
    return {
        restrict: 'E',
        templateUrl: '/partial/argument-instance-method.html',
        link: angular.noop
    };
});