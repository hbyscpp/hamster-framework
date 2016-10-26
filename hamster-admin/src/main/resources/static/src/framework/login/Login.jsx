import React from 'react'
import { Form, Input, Button, Checkbox, Spin } from 'antd'
const FormItem = Form.Item;
import './index.scss'
import { browserHistory } from 'react-router'

import LogoImg from './logo.png'

class Login extends React.Component{
    constructor(props){
        super(props)
        this.login = this.login.bind(this)
        this.onKeyPressLogin = this.onKeyPressLogin.bind(this)
    }
    login(){
        browserHistory.push('/home')
    }
    onKeyPressLogin(event){
        if(event.which === 13) {
            this.login();
        }
    }
    render(){
        return(
            <div className="login-page">
                <div className="login-box">
                    <Form className="login-form" onKeyPress={this.onKeyPressLogin}>
                        <h2>系统登录</h2>
                        <FormItem>
                            <Input placeholder="账户"/>
                        </FormItem>
                        <FormItem>
                            <Input type="password" placeholder="密码"/>
                        </FormItem>
                        <Button type="primary"  onClick={this.login}>登录</Button>
                    </Form>
                </div>
            </div>
        )
    }
}

export default Login