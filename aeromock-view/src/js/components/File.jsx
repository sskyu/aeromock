/** @jsx React.DOM */

var React = require('react');
var ReactPropTypes = React.PropTypes;

var File = React.createClass({

    propTypes: {
        files: ReactPropTypes.array,
        domain: ReactPropTypes.string
    },

    render: function () {
        var files = [];
        var dataFiles = [];

        // TODO: refoctoring
        this.props.files.forEach((file) => {

            if (!file.data_files.length) {
                files.push(<li className="am-tree-file">{file.path}</li>);
                return;
            }

            file.data_files.forEach((dataFile) => {
                var hasId = !!dataFile.id;
                var className = hasId ? 'am-tree-file --hasOtherTemplate' : 'am-tree-file';

                files.push(
                    <li className={className}>
                        {this._wrapAnchorTag(file.path, dataFile.link, dataFile.id)}
                    </li>
                );
            }, this);

        }, this);

        return (
            <ul>{files}</ul>
        );
    },

    _wrapAnchorTag: function (filePath, link, id) {
        id = id ? '__' + id : '';
        return <a href={filePath} target="_blank">{filePath + id}</a>;
    }

});

module.exports = File;