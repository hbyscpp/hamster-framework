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
                simple: false,
                current: 1,
                onChange: this.onChange.bind(this)
            },
            paginationMini: {
                showSizeChanger: true,
                simple: true,
                current: 1,
                onChange: this.onChange.bind(this)
            },
            dataSource: [],
            columns: [],
            serviceColumns: [
                {
                    title: '内容',
                    key: 'value',
                    dataIndex: 'value'
                },
                {
                    title: '提供者',
                    key: 'operate1',
                    render: function (text, record) {
                        // serviceInstanceList?serviceName=xxx
                        // link query
                        return (
                            <Link
                                target="_blank"
                                className="operate-btn"
                                to={{ pathname: '/service/service-instance-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                提供者
                            </Link>
                        )
                    }
                },
                {
                    title: '消费者',
                    key: 'operate2',
                    render: function (text, record) {
                        // referInstanceList?serviceName=xxx
                        return (
                            <Link
                                className="operate-btn"
                                target="_blank"
                                to={{ pathname: '/service/refer-instance-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                消费者
                            </Link>
                        )
                    }
                },
            ],
            appColumns: [
                {
                    title: '内容',
                    key: 'value',
                    dataIndex: 'value'
                },
                {
                    title: '服务列表',
                    key: 'operate1',
                    render: function (text, record) {
                        // appExportServiceList?app=xxx
                        // link query
                        return (
                            <Link
                                target="_blank"
                                className="operate-btn"
                                to={{ pathname: '/app/export-service-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                服务列表
                            </Link>
                        )
                    }
                },
                {
                    title: '引用服务列表',
                    key: 'operate2',
                    render: function (text, record) {
                        // appReferServiceList?app=xxx
                        return (
                            <Link
                                className="operate-btn"
                                target="_blank"
                                to={{ pathname: '/app/refer-service-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                引用服务列表
                            </Link>
                        )
                    }
                },
            ],
            nodeColumns: [
                {
                    title: '内容',
                    key: 'value',
                    dataIndex: 'value'
                },
                {
                    title: '服务列表',
                    key: 'operate1',
                    render: function (text, record) {
                        // nodeExportServiceList?node=xxx
                        // link query
                        return (
                            <Link
                                target="_blank"
                                className="operate-btn"
                                to={{ pathname: '/node/export-service-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                服务列表
                            </Link>
                        )
                    }
                },
                {
                    title: '引用服务列表',
                    key: 'operate2',
                    render: function (text, record) {
                        // nodeReferServiceList?node=xxx
                        return (
                            <Link
                                className="operate-btn"
                                target="_blank"
                                to={{ pathname: '/node/refer-service-list', query: { name: record.value } }}>
                                <Icon type="link" />
                                引用服务列表
                            </Link>
                        )
                    }
                },
            ]
        }

        this.handleSubmit = this.handleSubmit.bind(this)
        this.searchList = this.searchList.bind(this)
    }
    onChange(page) {
        console.log(page);
        this.setState({
            pagination: {
                ...this.state.pagination,
                current: page
            },
            paginationMini: {
                ...this.state.paginationMini,
                current: page
            }
        });
    }
    handleSubmit(e) {
        e.preventDefault()
        request({
            url: 'appDependencyGraph',
            type: 'get',
            dataType: 'json'
        })
        
        console.log('Received values of form:', this.props.form.getFieldsValue())
        this.searchList(this.props.form.getFieldValue('type'), this.props.form.getFieldValue('q'))
    }
    // @type 类型
    // @q 搜索内容
    searchList(type, q){
        let url

        if(type === 'service'){
            url = '/serviceSearch'
            this.setState({
                columns: this.state.serviceColumns
            })
        } else if(type === 'app'){
            url = '/appSearch'
            this.setState({
                columns: this.state.appColumns
            })
        } else if(type === 'node'){
            url = '/nodeSearch'
            this.setState({
                columns: this.state.nodeColumns
            })
        } else{
            message.error('错误的搜索类型')
            this.setState({
                columns: []
            })
            return
        }
        this.setState({
            isLoading: true,
            pagination: {
                ...this.state.pagination,
                current: 1
            },
            paginationMini: {
                ...this.state.paginationMini,
                current: 1
            }
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
            }else{
                message.error(res.msg)
            }
            console.log(res)
        })
        .catch(err=>{
            this.setState({
                isLoading: false
            })
            message.error('error status: '+err.status)
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
                    rowKey={record => record.index}
                    dataSource={this.state.dataSource}
                    columns={this.state.columns}
                    pagination={this.state.paginationMini} />
                <Table
                    className={resultCls}
                    rowKey={record => record.index}
                    dataSource={this.state.dataSource}
                    columns={this.state.columns}
                    pagination={this.state.pagination} />
            </Page>
        )
    }
}

export default Home