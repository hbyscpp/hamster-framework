import React from 'react'
import { Form, Input, Button, Checkbox, Spin, message } from 'antd'
const FormItem = Form.Item;
const createForm = Form.create;
import './index.scss'
import { hashHistory } from 'react-router'
import request from 'common/request/request.js'
import {local ,session} from 'common/util/storage.js'

@createForm()
class Login extends React.Component{
    constructor(props){
        super(props)
        this.state = {
            loading: false
        }
        this.login = this.login.bind(this)
        this.onKeyPressLogin = this.onKeyPressLogin.bind(this)
    }
    
    componentWillMount() {
        session.remove('descConfig')
    }
    
    login(){
        this.setState({
            loading: true
          })
        let loginData = this.props.form.getFieldsValue()
        request({
            url: '/checkpwd',
            type: 'get',
            dataType: 'json',
            data: loginData
        })
            .then(res => {
                console.log(res)
                this.setState({
                    loading: false
                })
                if(res.code == 0){
                    session.set('isLogin', true)
                    hashHistory.push('/home')
                }else{
                    message.error(res.msg)
                }
                
            })
            .catch(err => {
                
                message.error('error status: '+err.status)
                this.setState({
                    loading: false
                })
            })
    }
    onKeyPressLogin(event){
        if(event.which === 13) {
            this.login();
        }
    }
    render(){

        const { getFieldDecorator } = this.props.form

        return(
            <div className="login-page">
                <div className="login-box">
                <Spin spinning={this.state.loading} size="large">
                    <Form className="login-form" onKeyPress={this.onKeyPressLogin}>
                        <h2>系统登录</h2>
                        
                        <FormItem>
                        {getFieldDecorator('username')(
                            <Input placeholder="账户"/>
                        )}
                        </FormItem>
                        <FormItem>
                        {getFieldDecorator('password')(
                            <Input type="password" placeholder="密码"/>
                        )}
                        </FormItem>
                        <Button type="primary"  onClick={this.login}>登录</Button>
                    </Form>
                     </Spin>
                </div>
            </div>
        )
    }
}

export default Login