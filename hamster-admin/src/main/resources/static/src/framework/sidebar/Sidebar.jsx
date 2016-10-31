import React from 'react'
import { browserHistory, Link } from 'react-router'
import { Menu, Icon, Switch } from 'antd';
const SubMenu = Menu.SubMenu;
import FAIcon from 'component/faicon'
import './index.scss'
import {local ,session} from 'common/util/storage.js'
import ytfEvent from '../ytfEvent'

class Sidebar extends React.Component{
    constructor(props){
        super(props)
        this.state = {
            mode: 'vertical',
            menuData: []
        }

        this.go404 = this.go404.bind(this)
        this.goHome = this.goHome.bind(this)
        this.changeMode = this.changeMode.bind(this)
        this.handleClick = this.handleClick.bind(this)
       
    }
    componentWillMount(){
        let menuData = [{
              name: '405',
              path: '/405',
              icon: 'leaf',
              children: [
                  {
                      name: '406',
                      path: '/406',
                      icon: 'circle'
                  },
                  {
                      name: '401',
                      path: '/401',
                      icon: 'heart',
                      children: [{
                          name: '409',
                          path: '/409',
                          icon: 'circle'
                      }]
                  }
              ]
         },
            {
                name: 'setting',
                path: '/setting',
                icon: 'setting',
                children: [{
                    name: 'home',
                    path: '/home',
                    icon: 'circle'
                },
                {
                    name: '404',
                    path: '/404',
                    icon: 'circle'
                }]
            }
        ]

         this.setState({
             menuData: menuData
         })
    }
    componentWillUnmount(){
    }
    go404(){
        browserHistory.push('/404')
    }
    goHome(){
        browserHistory.push('/home')
    }
    changeMode(value) {
        this.setState({
            mode: value ? 'vertical' : 'inline',
        })
    }
    handleClick(e) {
        // console.log('click ', e);
        // this.setState({
        //     current: e.key,
        // });
    }
/**
 * menuData
 * path name icon
 * @path 重要 既做路劲，又作为唯一key
 * [{
 *      name: '404',
 *      icon: 'circle',
 *      children: [
 *          {
 *              name: '405',
 *              path: '/405',
 *              icon: 'circle'
 *          },
 *          {
 *              name: '401',
 *              icon: 'circle',
 *              children: [{
 *                  name: '409',
 *                  path: '/409',
 *                  icon: 'circle'
 *              }]
 *          }
 *      ]
 * },
 * {
 *      name: '403',
 *      path: '/403',
 *      icon: 'circle'
 * }]
 * 
 * 
 **/
    convertSidebarMenu(menuData, key){
        
        return menuData.map((val, index)=> {
            if (val.children) {
                return (
                    <SubMenu key={key+val.path} title={<span><FAIcon type={val.icon} /><span>{val.name}</span></span>}>
                        {
                            this.convertSidebarMenu(val.children, key)
                        }
                    </SubMenu>
                )

            } else {
                return (
                    <Menu.Item key={key+val.path}>
                        <Link to={val.path}><FAIcon type={val.icon} /><span>{val.name}</span></Link>
                    </Menu.Item>
                )

            }
        })
    }
    getSideBarMenu(){
         
        let menuData = this.state.menuData 
        return this.convertSidebarMenu(menuData, 'yt_')
    
    }
    getMenuPath(menuData, pathName) {
        
        let menuPath = []
        function getPath(data, pathName, parentPath) {
            if (!data) return

            for (var i = 0; i < data.length; i++) {
                var path = parentPath.slice()
                path.push(data[i].path)
                if (data[i].path == pathName) {
                    menuPath = path
                    break
                } else {
                    getPath(data[i].children, pathName, path)
                }
            }
        }

        getPath(menuData, pathName, [])
        return menuPath
    }

    render(){

        const mini = local.get('mini')
        const mode = mini ? 'vertical' : 'inline'

        let menuPath = this.getMenuPath(this.state.menuData, this.props.location.pathname).map(v=>'yt_'+v)
        menuPath.pop()

        return (
            <aside className="yt-admin-framework-sidebar">
                <Menu theme="light"
                    onClick={this.handleClick}
                    defaultOpenKeys={menuPath}
                    selectedKeys={['yt_'+this.props.location.pathname]}
                    mode={mode}
                    >
                    {
                        this.getSideBarMenu()
                    }
                </Menu>
            </aside>
        )
    }
}

export default Sidebar
