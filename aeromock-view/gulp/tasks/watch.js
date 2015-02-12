var gulp = require('gulp');
var config = require('../config').watch;

gulp.task('watch', function () {
    // js
    gulp.watch(config.js, ['webpack']);
    // stylus
    gulp.watch(config.stylus, ['stylus']);
    // www
    gulp.watch(config.www, ['copy']);
    // webserver
    gulp.start('webserver');
});