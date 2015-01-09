/** @jsx React.DOM */

(function () {
    var React = require('react');
    var App = require('./components/App.jsx');

    window.React = React;

    React.renderComponent(
        <App />,
        document.getElementById('app')
    );
})();