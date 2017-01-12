import './index.scss'
import React from 'react'
import Header from '../header/Header.jsx'
import Sidebar from '../sidebar/Sidebar.jsx'
import Content from '../content/Content.jsx'
import {local ,session} from 'common/util/storage.js'
import classNames from 'classnames';

class Layout extends React.Component{
    constructor(props){
        super(props)
        this.state = {
            mini: local.get('mini')
        }

    }

    handleMiniChange(mode){
        local.set('mini', mode)
        this.setState({
            mini: mode
        })
    }
    render(){
        const cls = classNames({
            'mini': this.state.mini,
            'yt-admin-framework': true
        })
        return (
            <div className={cls}>
                <Header miniMode={this.state.mini} onMiniChange={this.handleMiniChange.bind(this)} />
                <Sidebar miniMode={this.state.mini} location={this.props.location}/>
                <Content>
                    
                    {
                        this.props.children
                    }
                </Content>
            </div>
        )
    }
}

export default Layout