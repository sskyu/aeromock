var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppConstants = require('../constants/AppConstants');

var AppActions = {

    initialize: function () {
        AppDispatcher.dispatch({
            actionType: AppConstants.APP_INIT
        });
    }

};

module.exports = AppActions;