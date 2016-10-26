"use strict"

import React from 'react'
import { Form, Icon, Input, Button, Select, message } from 'antd';
const FormItem = Form.Item;
const createForm = Form.create;

import Page from 'framework/page'

import './index.scss'

@createForm()
class Home extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isLoading: false
        }
        this.handleSubmit = this.handleSubmit.bind(this);
    }
    handleSubmit(e) {
        e.preventDefault()
        this.setState({
            isLoading: true
        });
        setTimeout(()=>{
            this.setState({
                isLoading: false
            });
            message.error('This is a message of error');
        }, 2000)
        console.log('Received values of form:', this.props.form.getFieldsValue())
    }
    render() {
        const { getFieldDecorator } = this.props.form

        return (
            <Page loading={this.state.isLoading}>
                <Form inline
                    className="home-search"
                    onSubmit={this.handleSubmit}>
                    <FormItem>
                        {getFieldDecorator('type', { initialValue: 'lucy'})(
                            <Select className="search-type">
                                <Select.Option value="jack">千与千寻</Select.Option>
                                <Select.Option value="lucy">大鱼海棠</Select.Option>
                                <Select.Option value="yiminghe">剑侠情缘</Select.Option>
                            </Select>
                        )}
                    </FormItem>
                    <FormItem>
                        {getFieldDecorator('name')(
                            <Input className="search-name" placeholder="请输入" />
                        )}
                    </FormItem>
                    <Button type="primary" className="search-btn" size="large" htmlType="submit">搜索</Button>
                </Form>
            </Page>
        )
    }
}

export default Home