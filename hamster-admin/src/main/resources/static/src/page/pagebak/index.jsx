"use strict"

import React from 'react'
import {Breadcrumb} from 'antd';
import { DatePicker } from 'antd';
const RangePicker = DatePicker.RangePicker;
import Page from 'framework/page'
import HomeImg from './wellcome.png'
import {local ,session} from 'common/util/storage.js'

local.set('yuntu', {yuntu: 'xixi'})
session.set('moyi', {login: false})
///
import ReactDOM from 'react-dom';
import Upload from 'rc-upload';

const props = {
  data: {
      name: 'moyi',
      sex: '1'
  },
  action: '/upload.do',
  onStart(file) {
    console.log('onStart', file, file.name);
  },
  onSuccess(ret) {
    console.log('onSuccess', ret);
  },
  onError(err) {
    console.log('onError', err);
  },
  beforeUpload(file) {
    return new Promise((resolve) => {
      console.log('start check');
      setTimeout(() => {
        console.log('check finshed');
        resolve(file);
      }, 3000);
    });
  },
};

const Test = React.createClass({
  render() {
    return (
      <div>
        <div>
            <Upload {...props}><a>开始上传</a></Upload>
        </div>
      </div>
    );
  },
});
///
class Home extends React.Component {
    render() {
        return (
            <Page>
                <Breadcrumb>
                    <Breadcrumb.Item>首页</Breadcrumb.Item>
                    <Breadcrumb.Item>应用列表</Breadcrumb.Item>
                    <Breadcrumb.Item>某应用</Breadcrumb.Item>
                </Breadcrumb>
                <h2>hello Yuntu</h2>
                <img style={{ maxWidth: 800 }} src={HomeImg} alt="Yuntu"/>
                <Test />
            </Page>
        )
    }
}

export default Home