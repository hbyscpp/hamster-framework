import './index.scss'
import React from 'react'
import Header from '../header/Header.jsx'
import Sidebar from '../sidebar/Sidebar.jsx'
import Content from '../content/Content.jsx'
import {local ,session} from 'common/util/storage.js'
import classNames from 'classnames';
import ytfEvent from '../ytfEvent'

class Layout extends React.Component{
    constructor(props){
        super(props)
        this.state = {
            mini: local.get('mini')
        }
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
    render(){
        const cls = classNames({
            'mini': this.state.mini,
            'yt-admin-framework': true
        })
        return (
            <div className={cls}>
                <Header />
                <Sidebar location={this.props.location}/>
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