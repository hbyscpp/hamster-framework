import React from 'react'
import {Button, Popconfirm} from "antd";

import FAIcon from 'component/faicon'
import { hashHistory } from 'react-router'
import LogoImg from './logo-white.png'
import LogoImgMini from './logo-white-mini.png'
import ytfEvent from '../ytfEvent'
import {local ,session} from 'common/util/storage.js'

import './index.scss'

class Header extends React.Component{
    constructor(props){
        super(props)
        this.state = {
            mini: local.get('mini')
        }
        this.logout = this.logout.bind(this)
        this.onToggle = this.onToggle.bind(this)
        this.setMini = this.setMini.bind(this)
    }
    componentDidMount(){
        ytfEvent.on('Sidebar:Toggle', this.setMini)
    }
    componentWillUnmount(){
        ytfEvent.removeListener('Sidebar:Toggle', this.setMini)
    }
    setMini(){
        this.setState({
            mini: local.get('mini')
        })
    }
    logout(){
        session.set('isLogin', false)
        hashHistory.push('/login')
    }
    onToggle(){
        if(local.get('mini')){
            local.set('mini', false)
        }else{
            local.set('mini', true)
        }
        ytfEvent.emit('Sidebar:Toggle')
    }
    render(){
        const brandLogo = this.state.mini ? LogoImgMini : LogoImg

        return (
            <header className="yt-admin-framework-header clearfix">
                <h1 className="yt-admin-framework-header-brand">
                    <img className="brand-logo" src={brandLogo} alt="Yuntu"/>
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