/**
 * Created by vim on 5/15/14.
 */

angular.module('amazonScrap.controllers', []).
    controller('AppCtrl', ['ServerChannel', '$scope', '$rootScope', function(ServerChannel, $scope, $rootScope){

        $scope.query = "Sony";
        $scope.scrapTasks = [];

        var listener = $rootScope.$on("channel_control", function (e, msg) {

            console.log("New Message: " + msg);

            if (msg.name=="task_created") {

                var taskId = msg.data.task_id;
                var taskUrl = msg.data.task_url;
                var taskName = msg.data.task_name;

                $scope.scrapTasks.push({
                    id: taskId,
                    url: taskUrl,
                    name: taskName
                });
            }
        });

        $scope.sendQuery = function () {

            var query = $scope.query;
            $scope.query = '';

            var promise = ServerChannel.startScrapTask(query);
            promise.then(function(response) {

                var taskId = response.task_id;

                $scope.scrapTasks.push({
                    id: taskId,
                    url: query,
                    name: "Search for: "+query
                });
            });
        };
    }]).
    controller('TaskCtrl', ['ServerChannel', '$scope', '$rootScope', function(ServerChannel, $scope, $rootScope){

        var scrapTask = $scope.scrapTask;
        var taskChannel = "channel_task_"+scrapTask.id;

        var listener = $rootScope.$on(taskChannel, function (e, msg) {
            console.log("New Message: " + msg);

            if (msg.name=="task_status") {
                scrapTask.status = msg.data.status;
                scrapTask.msg = msg.data.msg;
            }

            if (msg.name=="product_data") {
                scrapTask.price = msg.data.price;
                scrapTask.availability = msg.data.availability;
            }
        });

    }]);
