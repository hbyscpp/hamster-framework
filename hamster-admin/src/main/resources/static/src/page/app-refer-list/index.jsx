"use strict"

import React from 'react'
import { Icon, Button, message, Table, Collapse } from 'antd';
const Panel = Collapse.Panel

import _ from 'lodash'
import Page from 'framework/page'
import request from 'common/request/request.js'
import formateDate from 'common/util/formate-date.js'
import classNames from 'classnames';

import './index.scss'

class AppReferList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: true,
            instanceListData: []
        }

        this.searchList = this.searchList.bind(this);

    }

    componentDidMount() {
        let name = this.props.location.query.name
        this.searchList(name)
    }
    // @name
    searchList(name){
        // appReferServiceList?app=xxx
        request({
            url: '/appReferServiceList',
            type: 'get',
            dataType: 'json',
            data: {
                app: name
            }
        })
        .then(res => {
            this.setState({
                isLoading: false,
            })

            if(res.code === 0){
                this.setState({
                    instanceListData: res.data
                });
            }
            console.log(res)
        })
        .catch(err=>{
            this.setState({
                isLoading: false,
            })
            console.log(err)
        })

    }
    render() {

        let listPanel = []
        _.forOwn(this.state.instanceListData, function (value, key) {
            let header = (
                <h3 className="list-title">
                    <span>应用:{key.split('~')[3]}</span>
                    <span>版本:{key.split('~')[0]}</span>
                    <span>Group:{key.split('~')[1]}</span>
                    <span>协议:{key.split('~')[2]}</span>
                </h3>
            )
            listPanel.push(
                <Panel header={header} key={key}>
                <Collapse>
                        {
                            value.map((v, i) => {
                                let list = []
                                _.forOwn(v, function (vu, ke) {
                                    // registTime
                                    let vue = _.isBoolean(vu)?String(vu):vu;
                                    if(ke === 'registTime'){
                                        vue = formateDate(vue, 'yyyy-MM-dd hh:mm:ss')
                                    }

                                    list.push(
                                        <tr key={ke}>
                                            <td>{ke}</td>
                                            <td>{vue}</td>
                                        </tr>
                                    )
                                })
                                return (
                                    <Panel header={'节点'+v.referHost} key={i}>
                                        <table className="detail-table">
                                             <tbody>
                                                {list}
                                             </tbody>
                                        </table>
                                    </Panel>
                                )
                            })
                        }
                </Collapse>
            </Panel>
            )
        })
        
        return (

            <Page loading={this.state.isLoading}>
                <h2 className="yt-admin-page-title">应用引用服务列表</h2>
                <div className="service-instance-list-wrap">
                    <Collapse accordion>
                        {listPanel}
                    </Collapse>
                </div>
                
            </Page>
        )
    }
}

export default AppReferList