import React from 'react'

import { Router, browserHistory } from 'react-router'

import 'normalize.css'
import './base.css'

import request from 'common/request/request.js'

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
                replace('/404')
            }
        }
    }
]

request({
	url: '/api/checkpwd',
	type: 'get',
	dataType: 'json',
    data: {
        username: 'admin',
        password: 'admin'
    }
})
.then(res=>{
    console.log(res)
})

/////
// $.ajax({
// 	url: '/xapi/checkpwd',
// 	type: 'get',
// 	dataType: 'json',
//     data: {
//         username: 'admin',
//         password: 'admin'
//     }
// })
// .done(function() {
// 	console.log("success");

//     setTimeout(function(){
//         $.ajax({
// 	url: '/api/serviceList',
// 	type: 'get',
// 	dataType: 'json'
// })
// .done(function() {
// 	console.log("success");




    
// })
// .fail(function() {
// 	console.log("error");
// })
// .always(function() {
// 	console.log("complete");
// });
//     }, 2000)



// })
// .fail(function() {
// 	console.log("error");
// })
// .always(function() {
// 	console.log("complete");
// });

////////
// browserHistory.listenBefore(location => {
//     console.log('browserHistory:listen', location)
//     // 发布地址栏改变事件
//     console.info('发布地址栏改变事件')
//     ytfEvent.emit('App:BrowserHistory', location)
    
// })
class App extends React.Component{
    constructor(props){
        super(props)
        
    }
    
    componentWillMount(){ 
    }
    componentDidMount(){
        browserHistory.listenBefore(location => {
            console.log('browserHistory:listen', location)
            // 发布地址栏改变事件
            console.info('发布地址栏改变事件')
            ytfEvent.emit('App:BrowserHistory', location)

        })

    }

    render(){
        return <Router routes={routes} history={browserHistory}/>
    }
}

export default App