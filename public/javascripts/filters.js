/**
 * Created by vim on 5/15/14.
 */

'use strict';

/* Filters */

angular.module('amazonScrap.filters', []).
    filter('interpolate', function (version) {
        return function (text) {
            return String(text).replace(/\%VERSION\%/mg, version);
        }
    });

