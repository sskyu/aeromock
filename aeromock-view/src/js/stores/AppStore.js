var $ = require('jquery');
var Promise = require('es6-promise').Promise;
var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');
var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppConstants = require('../constants/AppConstants');

var CHANGE_EVENT = 'change';
var API_FETCH = '/api/contexts';

var _contexts = [];

function fetchContexts() {
    return new Promise(function (resolve, reject) {
        $.ajax({
            url: API_FETCH,
            type: 'get',
            success: function (resp) {
                console.log(resp);
                resolve(resp);
            },
            error: function (xhr) {
                reject(xhr);
            }
        });
    });
}

function xhrErrorHandler(xhr) {
    console.log('onError:', xhr);
}

var AppStore = assign({}, EventEmitter.prototype, {

    getContexts: function () {
        return _contexts;
    },

    emitChange: function () {
        this.emit(CHANGE_EVENT);
    },

    /**
     * @param {function} callback
     */
    addChangeListener: function (callback) {
        this.on(CHANGE_EVENT, callback);
    },

    /**
     * @param {function} callback
     */
    removeChangeListener: function (callback) {
        this.removeListener(CHANGE_EVENT, callback);
    }

});

AppDispatcher.register(function (action) {
    switch (action.actionType) {
        case AppConstants.FETCH:
            fetchContexts().then(function (resp) {
                _contexts = resp.contexts;
                AppStore.emitChange();
            }, xhrErrorHandler);
            break;
    }

});

module.exports = AppStore;