function TSP650ll() { }
/*
 * Check Status of printer.
 */
TSP650ll.prototype.CheckStatus = function (id, success, error) {
cordova.exec(success, error, 'TSP650ll', 'CheckStatus', [id]);
};

/*
 * Test hello world print
 */
TSP650ll.prototype.PrintHello = function (id, success, error) {
cordova.exec(success, error, 'TSP650ll', 'printHello', [id]);
};

module.exports = new TSP650ll();


