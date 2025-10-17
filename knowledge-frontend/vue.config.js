module.exports = {
  devServer: {
    proxy: {
      '/uploads': {
        target: 'http://localhost:8080',  // 后端API地址
        changeOrigin: true,
        pathRewrite: {
          '^/uploads': '/uploads'  // 保持路径不变
        }
      }
    }
  },
  configureWebpack: {
    module: {
      rules: [
        {
          test: /\.tsx?$/,
          loader: 'ts-loader',
          exclude: /node_modules/,
          options: {
            appendTsSuffixTo: [/\.vue$/],
          }
        }
      ]
    }
  },
  chainWebpack: config => {
    config.module
        .rule('vue')
        .use('vue-loader')
        .tap(options => {
          return {
            ...options,
            compilerOptions: {
              // 支持装饰器
              isCustomElement: (tag) => tag.startsWith('icon-'),
              whitespace: 'condense'
            }
          }
        })
  }
}
