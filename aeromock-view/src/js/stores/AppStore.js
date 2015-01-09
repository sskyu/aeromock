var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var Constants = require('../constants/AppConstants');
var $ = require('jquery');
var assign = require('object-assign');
var Promise = require('es6-promise').Promise;

var CHANGE_EVENT = 'change';
var API_FETCH = 'http://localhost:3183/api/contexts';

var _contexts = [];

function init() {
    return new Promise(function (resolve, reject) {
        $.ajax({
            url: API_FETCH,
            type: 'get',
            success: function (resp) {
                console.log(resp);
                _contexts = resp.contexts;
                resolve(resp);
            },
            error: function (xhr) {
                _contexts = [];
                rejext(xhr);
            }
        });
    });
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
        case Constants.APP_INIT:
            init().then(function (resp) {
                AppStore.emitChange();
            }).catch(function (xhr) {
                conosle.log(xhr);
                AppStore.emitChange();
            });
            break;
    }

});

module.exports = AppStore;