import React from 'react'

import { Router, hashHistory } from 'react-router'

import 'normalize.css'
import './base.css'

import request from 'common/request/request.js'
import {local ,session} from 'common/util/storage.js'

import Layout from '../layout/Layout.jsx'
import Home from '../../page/home'
import pageRoutes from '../../page/routeConfig.js'

import ytfEvent from '../ytfEvent'

let routes = [
    {
        path: '/',
        component: Layout,
        indexRoute: { component: Home },
        childRoutes: pageRoutes,
        onEnter(nextState, replace) {
            // 可以验证是否登录
            console.info("%c nextState >>>", "color:orange", nextState)
            if(!session.get('isLogin')){
                replace('/login')
            }
        }
    }, {
        path: '/login',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('../login/Login.jsx'))
            })
        }
    }, {
        path: '*',
        indexRoute: {
            onEnter(nextState, replace) {
                if(!session.get('isLogin')){
                    replace('/login')
                }else{
                    replace('/404')
                }
            }
        }
    }
]

class App extends React.Component{
    constructor(props){
        super(props)
        
    }
    
    componentWillMount(){ 
    }
    componentDidMount(){

    }

    render(){
        return <Router routes={routes} history={hashHistory}/>
    }
}

export default App