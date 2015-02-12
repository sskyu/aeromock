/** @jsx React.DOM */

var React = require('react');
var ReactPropTypes = React.PropTypes;
var Directory = require('../components/Directory');
var File = require('../components/File');

var Contexts = React.createClass({

    propTypes: {
        domain: ReactPropTypes.string,
        directories: ReactPropTypes.array,
        files: ReactPropTypes.array
    },

    render: function () {

        return (
            <div>
                <p className="am-domainName">{this.props.domain}</p>
                <div className="am-childFiles">
                    <Directory directories={this.props.directories} />
                    <File
                        files={this.props.files}
                        domain={this.props.domain} />
                </div>
            </div>
        );
    }

});

module.exports = Contexts;