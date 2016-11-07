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

        this.changeMode = this.changeMode.bind(this)
    }
    componentWillMount(){
        let menuData = [
            {
            name: 'dashboard',
            path: '/home',
            icon: 'home'
            },
            {
                name: '应用依赖关系图',
                path: '/app-dependency-graph',
                icon: 'puzzle-piece'
            }
            ]
         this.setState({
             menuData: menuData
         })
    }
    componentWillUnmount(){
    }

    changeMode(value) {
        this.setState({
            mode: value ? 'vertical' : 'inline',
        })
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
