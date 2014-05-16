/**
 * Created by vim on 5/15/14.
 */

'use strict';

/* Directives */

angular.module('amazonScrap.directives', []).
    directive('appVersion', function (version) {
        return function(scope, elm, attrs) {
            elm.text(version);
        };
    });

