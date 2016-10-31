"use strict"

import React from 'react'
import { Icon, Button, message, Table, Collapse } from 'antd';
const Panel = Collapse.Panel

import _ from 'lodash'
import Page from 'framework/page'
import request from 'common/request/request.js'
import classNames from 'classnames';

import './index.scss'

class ServiceInstanceList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: true,
            instanceListData: []
        }

        this.searchList = this.searchList.bind(this);

    }

    componentDidMount() {
        let serviceName = this.props.location.query.name
        this.searchList(serviceName)
    }
    // @serviceName 服务名字
    searchList(serviceName){
        // serviceInstanceList?serviceName=xxx
        request({
            url: '/serviceInstanceList',
            type: 'get',
            dataType: 'json',
            data: {
                serviceName: serviceName
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
                    <span>一:{key.split('~')[0]}</span>
                    <span>二:{key.split('~')[1]}</span>
                    <span>三:{key.split('~')[2]}</span>
                    <span>四:{key.split('~')[3]}</span>
                </h3>
            )
            listPanel.push(
                <Panel header={header} key={key}>
                <Collapse>
                        {
                            value.map((v, i) => {
                                let list = []
                                _.forOwn(v, function (vu, ke) {
                                    list.push(
                                        <tr key={ke}>
                                            <td>{ke}</td>
                                            <td>{_.isBoolean(vu)?String(vu):vu}</td>
                                        </tr>
                                    )
                                })
                                return (
                                    <Panel header={i} key={i}>
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
                <h2 className="yt-admin-page-title">提供者</h2>
                <div className="service-instance-list-wrap">
                    <Collapse accordion>
                        {listPanel}
                    </Collapse>
                </div>
                
            </Page>
        )
    }
}

export default ServiceInstanceList