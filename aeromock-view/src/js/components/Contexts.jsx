/** @jsx React.DOM */

var React = require('react');
var ReactPropTypes = React.PropTypes;

var Contexts = React.createClass({

    propTypes: {
        domain: ReactPropTypes.string,
        directories: ReactPropTypes.array,
        files: ReactPropTypes.array
    },

    render: function () {

        return (
            <div>
                <p>[domain] {this.props.domain}</p>
                <div>
                    <p>[directories]</p>
                    <div>{this.props.directories}</div>
                </div>
                <div>
                    <p>[files]</p>
                    <div>{this.props.files}</div>
                </div>
            </div>
        );
    }

});

module.exports = Contexts;