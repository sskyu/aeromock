var dest = './build';
var src = './src';

module.exports = {
  // global configurations
  dest: dest,

  // specific configurations
  js: {
    src: src + '/js/**',
    dest: dest + '/js',
    uglify: false
  },

  // task configurations
  webpack: {
    entry: src + '/js/app.js',
    output: {
      filename: 'bundle.js'
    },
    module: {
      loaders: [
        {
          test: /\.(js|jsx)$/,
          loader: 'jsx-loader?harmony'
        }
      ]
    },
    resolve: {
      extensions: ['', '.js', '.jsx']
    },
    external: {
      React: 'react'
    }
  },

  stylus: {
    src: src + '/stylus/**',
    dest: dest + '/css/',
    output: 'app.css',
    autoprefixer: {
      browsers: ['last 2 versions']
    },
    minify: false
  },

  copy: {
    src: [
      src + '/www/**'
    ],
    dest: dest
  },

  webserver: {
    src: dest,
    host: 'localhost',
    port: 9000,
    livereload: true,
    proxies: [
      {
        source: '/api',
        target: 'http://localhost:3183/api'
      }
    ]
  }
};