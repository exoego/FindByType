angular.module('symbolFilters', []).filter('arrow', function () {
    return function (input) {
        return input.replace("->", '<span class="typefind-method-arrow glyphicon glyphicon-arrow-right"></span>');
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
            .when("/q/:query", {
                templateUrl: "/partial/result.html"
            })
            .otherwise({
                templateUrl: "/partial/otherwise.html"
            });
        $locationProvider.html5Mode(true);
    }).controller("SearchController", function ($scope, $location) {

        $scope.$watch(function () {
            return $location.path();
        }, function (newVal, oldVal) {
            var q = (newVal.match("/([^/]*$)") || ["", ""])[1];
            $scope.querystring = q;
        });
    });

jQuery(document).ready(function () {
    var q = (location.href.match("/([^/]*$)") || ["", ""])[1];
    if (q.length != 0) {
        jQuery("#query-string input").val(decodeURIComponent(q)).trigger("input");
    }
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