"use strict"

import React from 'react'
import { browserHistory, Link } from 'react-router'
import { Form, Icon, Input, Button, Select, message, Table } from 'antd';
const FormItem = Form.Item;
const createForm = Form.create;

import Page from 'framework/page'
import request from 'common/request/request.js'
import classNames from 'classnames';

import './index.scss'

@createForm()
class Home extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: false,
            alreadyHaveResult: false,

            pagination: {
                showSizeChanger: true,
                simple: false
            },
            paginationMini: {
                showSizeChanger: true,
                simple: true
            },
            dataSource: [],
            columns: [
                {
                    title: '内容',
                    key: 'value',
                    dataIndex: 'value'
                },
                {
                    title: '提供者',
                    key: 'provider',
                    render: function (text, record) {
                        // serviceInstanceList?serviceName=xxx
                        // link query
                        return (
                            <Link
                                target="_blank"
                                className="operate-btn-48"
                                to={{ pathname: '/service/service-instance-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                提供者
                            </Link>
                        )
                    }
                },
                {
                    title: '消费者',
                    key: 'customer',
                    render: function (text, record) {
                        // referInstanceList?serviceName=xxx
                        return (
                            <Link
                                className="operate-btn-48"
                                target="_blank"
                                to={{ pathname: '/service/refer-instance-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                消费者
                            </Link>
                        )
                    }
                },
            ]
        }

        this.handleSubmit = this.handleSubmit.bind(this)
        this.searchList = this.searchList.bind(this)
    }
    handleSubmit(e) {
        e.preventDefault()
        
        console.log('Received values of form:', this.props.form.getFieldsValue())
        this.searchList(this.props.form.getFieldValue('type'), this.props.form.getFieldValue('q'))
    }
    // @type 类型
    // @q 搜索内容
    searchList(type, q){
        let url

        if(type === 'service'){
            url = '/serviceSearch'
        } else if(type === 'app'){
            url = '/appSearch'
        } else if(type === 'node'){
            url = '/nodeSearch'
        } else{
            message.error('错误的搜索类型')
            return
        }
        this.setState({
            isLoading: true
        });
        request({
            url: url,
            type: 'get',
            dataType: 'json',
            data: {
                q: q
            }
        })
        .then(res => {
            this.setState({
                isLoading: false,
                alreadyHaveResult: true
            })
            // 把数据转变为table能够用的
            let data = res.data.map((v,i)=>{
                return {
                    index: i,
                    value: v
                }
            })

            if(res.code === 0){
                this.setState({
                    dataSource: data
                });
            }
            console.log(res)
        })
        .catch(err=>{
            this.setState({
                isLoading: false
            })
            console.log(err)
        })

    }
    render() {
        const { getFieldDecorator } = this.props.form
        const { alreadyHaveResult } = this.state
        const formCls = classNames({
            'home-search': true,
            'already-have-result': alreadyHaveResult,
        })
        const resultCls = classNames({
            'home-search-result': true,
            'show': alreadyHaveResult,
        })
        const resultMiniCls = classNames({
            'home-search-result-mini': true,
            'show': alreadyHaveResult,
        })

        return (
            <Page loading={this.state.isLoading}>
                <Form inline
                    className={formCls}
                    onSubmit={this.handleSubmit}>
                    <FormItem>
                        {getFieldDecorator('type', { initialValue: 'service'})(
                            <Select className="search-type">
                                <Select.Option value="service">服务</Select.Option>
                                <Select.Option value="app">应用</Select.Option>
                                <Select.Option value="node">节点</Select.Option>
                            </Select>
                        )}
                    </FormItem>
                    <FormItem>
                        {getFieldDecorator('q')(
                            <Input className="search-name" placeholder="请输入" />
                        )}
                    </FormItem>
                    <Button type="primary" className="search-btn" size="large" htmlType="submit">搜索</Button>
                </Form>
                <Table
                    className={resultMiniCls}
                    dataSource={this.state.dataSource}
                    columns={this.state.columns}
                    pagination={this.state.paginationMini} />
                <Table
                    className={resultCls}
                    dataSource={this.state.dataSource}
                    columns={this.state.columns}
                    pagination={this.state.pagination} />
            </Page>
        )
    }
}

export default Home