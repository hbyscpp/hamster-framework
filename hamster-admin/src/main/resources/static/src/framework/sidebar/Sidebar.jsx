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
            current: '1',
            mode: 'vertical',
        }

        this.go404 = this.go404.bind(this)
        this.goHome = this.goHome.bind(this)
        this.changeMode = this.changeMode.bind(this)
        this.handleClick = this.handleClick.bind(this)
       
    }
    componentDidMount(){
        // console.log(location.pathname)
        browserHistory.replace(location.pathname)
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
        console.log('click ', e);
        this.setState({
            current: e.key,
        });
    }
/**
 * menuData
 * path name icon
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
                    <SubMenu key={key+'-'+index} title={<span><FAIcon type={val.icon} /><span>{val.name}{key+'-'+index}</span></span>}>
                        {
                            this.convertSidebarMenu(val.children, key+'-'+index)
                        }
                    </SubMenu>
                )

            } else {
                return (
                    <Menu.Item key={key+'-'+index}>
                        <Link to={val.path}><FAIcon type={val.icon} /><span>{val.name}{key+'-'+index}</span></Link>
                    </Menu.Item>
                )

            }
        })
    }
    getSideBarMenu(){
         let menuData = [{
              name: '404',
              icon: 'leaf',
              children: [
                  {
                      name: '405',
                      path: '/405',
                      icon: 'circle'
                  },
                  {
                      name: '401',
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
              name: 'home-detail',
              path: '/home/:id',
              icon: 'circle'
         }]

        return this.convertSidebarMenu(menuData, 'yt')
    
    }

    render(){

        const mini = local.get('mini')
        const mode = mini ? 'vertical' : 'inline'

        return (
            <aside className="yt-admin-framework-sidebar">
                <Menu theme="light"
                    style={{ width: 200 }}
                    onClick={this.handleClick}
                    defaultOpenKeys={['sub1']}
                    selectedKeys={[this.state.current]}
                    mode={mode}
                    >
                    {
                        this.getSideBarMenu()
                    }
                    <SubMenu key="sub1" title={<span><Icon type="setting" /><span>导航一</span></span>}>
                        <Menu.Item key="1"><Link to="/home">home</Link></Menu.Item>
                        <Menu.Item key="2"><Link to="/404">404</Link></Menu.Item>
                    </SubMenu>
                </Menu>
            </aside>
        )
    }
}

export default Sidebar
