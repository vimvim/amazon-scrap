/**
 * Created by vim on 5/15/14.
 */

angular.module('amazonScrap.controllers', []).
    controller('AppCtrl', ['ServerChannel', '$scope', function(ServerChannel, $scope){
        // $scope.customers = ServerChannel.getCustomers();

        $scope.scrapTasks = []

        $scope.sendUrl = function () {

            ServerChannel.sendRequest({
                command: 'startTask',
                url: $scope.url
            });

            // add the message to our model locally
            $scope.scrapTasks.push({
                url: $scope.url
            });

            // Clear url input
            $scope.url = '';
        };

    }]);

