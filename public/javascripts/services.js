/**
 * Created by vim on 5/15/14.
 */

angular.module('amazonScrap.services', []).
    factory('ServerChannel', ['$q', '$rootScope', function ($q, $rootScope) {

        // We return this object to anything injecting our service
        var Service = {};

        // Keep all pending requests here until they get responses
        var callbacks = {};

        // Create a unique callback ID to map requests to responses
        var currentCallbackId = 0;

        // Create our websocket object with the address to the websocket
        var wsUri = "ws://"+document.location.host+"/ws/";
        var ws = new WebSocket(wsUri);

        ws.onopen = function () {
            console.log("Socket has been opened!");
        };

        ws.onmessage = function (message) {
            listener(JSON.parse(message.data));
        };

        function sendRequest(request) {

            var defer = $q.defer();

            var callbackId = getCallbackId();
            callbacks[callbackId] = {
                time: new Date(),
                cb: defer
            };

            request.id = callbackId;

            console.log('Sending request', request);
            ws.send(JSON.stringify(request));

            return defer.promise;
        }

        function listener(data) {

            var messageObj = data;

            console.log("Received data from websocket: ", messageObj);

            if (!_.isUndefined(messageObj.id)) {
                // This is callback for async command

                // If an object exists with callback_id in our callbacks object, resolve it
                if (callbacks.hasOwnProperty(messageObj.id)) {

                    console.log(callbacks[messageObj.id]);

                    $rootScope.$apply(callbacks[messageObj.id].cb.resolve(messageObj.response));
                    delete callbacks[messageObj.id];
                }

            } else if (!_.isUndefined(messageObj.channel)) {
                // This is message for info channel

                console.log("Channel msg: "+messageObj.channel, messageObj);

                var channel = "channel_"+messageObj.channel;

                $rootScope.$apply(function () {
                    $rootScope.$broadcast(channel, messageObj);
                });
            }
        }

        // This creates a new callback ID for a request
        function getCallbackId() {
            currentCallbackId += 1;
            if (currentCallbackId > 10000) {
                currentCallbackId = 0;
            }
            return currentCallbackId;
        }

        Service.startScrapTask = function(url) {
            var request = {
                command: "start_scrap_task",
                data: {
                    url: url
                }
            };
            // Storing in a variable for clarity on what sendRequest returns
            var promise = sendRequest(request);
            return promise;
        };

        return Service;
    }]);
