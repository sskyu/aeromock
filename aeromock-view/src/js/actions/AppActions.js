var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppConstants = require('../constants/AppConstants');
var apiContexts = require('../api/contexts');
var xhrErrorHandler = require('../utils/xhrErrorHandler');

var AppActions = {

    fetch: function () {
        apiContexts.fetch().then((resp) => {
            AppDispatcher.dispatch({
                actionType: AppConstants.FETCH,
                contexts: resp.contexts
            });
        }, xhrErrorHandler);
    }

};

module.exports = AppActions;