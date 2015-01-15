var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppConstants = require('../constants/AppConstants');

var AppActions = {

    fetch: function () {
        AppDispatcher.dispatch({
            actionType: AppConstants.FETCH
        });
    }

};

module.exports = AppActions;