var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'StreamingMediaIMA', 'coolMethod', [arg0]);
};

module.exports.playVideo = function (url, options) {
	options = options || {};
	cordova.exec(options.successCallback || null, options.errorCallback || null, "StreamingMediaIMA", "playVideo", [url, options]);
};