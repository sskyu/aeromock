var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');
var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppConstants = require('../constants/AppConstants');

var CHANGE_EVENT = 'change';
var API_FETCH = '/api/contexts';

var _contexts = [];

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
            _contexts = action.contexts;
            AppStore.emitChange();
            break;
    }

});

module.exports = AppStore;