function TSP650ll() { }

TSP650ll.prototype.CheckStatus = function (id, success, error) {
cordova.exec(success, error, 'TSP650ll', 'CheckStatus', [id]);
};

module.exports = new TSP650ll();


