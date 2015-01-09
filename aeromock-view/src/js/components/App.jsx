/** @jsx React.DOM */

var React = require('react');
var AppStore = require('../stores/AppStore');
var AppActions = require('../actions/AppActions');
var Contexts = require('../components/Contexts');

function getAppState() {
    return {
        contexts: AppStore.getContexts()
    };
}

var App = React.createClass({

    getInitialState: function () {
        return getAppState();
    },

    componentDidMount: function () {
        AppStore.addChangeListener(this._onChange);

        AppActions.initialize();
    },

    componentWillUnmount: function () {
        AppStore.removeChangeListener(this._onChange);
    },

    render: function () {
        var contexts = [];

        if (!this.state.contexts || !this.state.contexts.length) {
            return <div></div>;
        }

        this.state.contexts.forEach(function (context) {
            contexts.push(
                <Contexts
                    domain={context.domain}
                    directories={context.directories}
                    files={context.files} />
            );
        });

        return (
            <ul>{contexts}</ul>
        );
    },

    _onChange: function () {
        this.setState(getAppState());
    }
});

module.exports = App;