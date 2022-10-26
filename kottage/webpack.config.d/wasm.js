const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            '../../node_modules/@jlongster/sql.js/dist/sql-wasm.wasm',
            '../../node_modules/@jlongster/sql.js/dist/worker.sql-wasm.js'
        ]
    })
);
