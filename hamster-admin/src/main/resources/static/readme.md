# 云途管理台前端框架

## 技术栈

- react
- antd
- webpack
- es6(babel)

## 兼容性

原则上支持 IE9+ 及现代浏览器

## 环境

- node LTS 版本
- npm 建议 3+
- webpack 1+

> npm 建议使用cnpm, 通过设置 alias 的方式;(在私有npm还没搭建起来的时候)

## 开发

```bash

    cpm install -g webpack webpack-dev-server

    cnpm install

    npm run local

```
### 设置 api 代理

可在 webpack.dev.config.js 里面的 devServer 配置项设置 api 代理

## TODO

1.图片自动压缩配置项暂未添加
2.发布时候的优化设置逐步加强