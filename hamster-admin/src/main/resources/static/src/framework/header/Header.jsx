import React from 'react'
import {Button, Popconfirm} from "antd";

import FAIcon from 'component/faicon'
import { hashHistory } from 'react-router'
import request from 'common/request/request.js'
import {local ,session} from 'common/util/storage.js'

import './index.scss'

class Header extends React.Component{
    constructor(props){
        super(props)
        this.logout = this.logout.bind(this)
        this.onToggle = this.onToggle.bind(this)
    }

    logout(){
        request({
            url: '/logout',
            type: 'get',
            dataType: 'json'
        })
        .then(res=>{})
        .catch(err=>{})
        
        session.set('isLogin', false)
        hashHistory.push('/login')
    }
    onToggle(){
        this.props.onMiniChange(!this.props.miniMode)
    }
    render(){
        const mini = this.props.miniMode

        return (
            <header className="yt-admin-framework-header clearfix">
                <h1 className="yt-admin-framework-header-brand">
                    {mini? 'H': 'hamster'}
                    <Button type="ghost" style={{display: 'none'}}>only for use antd button styles</Button>
                </h1>
                <div className="yt-admin-framework-header-sidebar-toggle">
                    <a href="javascript:;" onClick={this.onToggle}>
                        <FAIcon type="bars" className="toggle-icon"/>
                    </a>
                </div>
                <ul className="yt-admin-framework-header-menu clearfix">
                    {
                    //     <li className="menu-item">
                    //     <a href="javascript:;">
                    //         <FAIcon type="leaf"/>
                    //         <span className="header-menu-text">叶子</span>
                    //     </a>
                    // </li>
                    // <li className="menu-item">
                    //     <a href="javascript:;">
                    //         <FAIcon type="heartbeat"/>
                    //         <span className="header-menu-text">个人信息</span>
                    //     </a>
                    // </li>
                    }
                    <li className="menu-item">
                        <Popconfirm placement="bottomRight" title="您确定要退出系统吗？" onConfirm={this.logout}>
                            <a href="javascript:;">
                                <FAIcon type="sign-out"/>
                                <span className="header-menu-text">退出系统</span>
                            </a>
                        </Popconfirm>
                    </li>
                </ul>
            </header>
        )
    }
}

export default Header