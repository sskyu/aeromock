/** @jsx React.DOM */

var React = require('react');
var ReactPropTypes = React.PropTypes;

var Directory = React.createClass({

    propTypes: {
        directories: ReactPropTypes.array
    },

    render: function () {
        var directories = [];

        this.props.directories.forEach((directory) => {
            directories.push(
                <li
                    className="am-tree-directory"
                    onClick={this.handleClick}>{directory}</li>
            );
        }, this);

        return (
            <ul>{directories}</ul>
        );
    },

    handleClick: function (e) {
        // TODO: handler
    }

});

module.exports = Directory;