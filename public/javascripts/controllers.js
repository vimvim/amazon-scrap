/**
 * Created by vim on 5/15/14.
 */

angular.module('amazonScrap.controllers', []).
    controller('AppCtrl', ['ServerChannel', '$scope', function(ServerChannel, $scope){

        $scope.scrapTasks = [];

        $scope.sendUrl = function () {

            var url = $scope.url;
            $scope.url = '';

            var promise = ServerChannel.startScrapTask(url);
            promise.then(function(response) {

                var taskId = response.task_id;

                $scope.scrapTasks.push({
                    id: taskId,
                    url: url
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
        });
    }]);
