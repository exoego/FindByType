angular.module('symbolFilters', []).filter('arrow', function () {
    return function (input) {
        return input.replace("->", '<span class="typefind-method-arrow glyphicon glyphicon-arrow-right"></span>');
    };
});

var app = angular
    .module('tutorial', ['elasticui', 'symbolFilters', 'ngSanitize'])
    .constant('euiHost', 'http://192.168.1.236:9200')
    .config(['$locationProvider', function ($locationProvider) {
        $locationProvider.html5Mode(true);
    }]);


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