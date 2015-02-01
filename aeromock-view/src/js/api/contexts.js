var Promise = require('es6-promise').Promise;
var request = require('superagent');

var API = '/api/contexts';

function Contexts() {};

Contexts.prototype = {
    /**
     * fetch contexts
     * @return {Object} Promise
     */
    fetch: function () {
        return new Promise((resolve, reject) => {
            request
                .get(API)
                // .get(API + '?_dataid=multi') // TODO:
                .set('Accept', 'application/json')
                .end(function (error, response) {
                    if (error) {
                        return reject(error);
                    }
                    resolve(response.body);
                });
        });
    }
}

module.exports = new Contexts();