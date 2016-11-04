"use strict"

import React from 'react'
import { message } from 'antd'
import ReactEcharts from 'echarts-for-react'

import Page from 'framework/page'
import request from 'common/request/request.js'
import classNames from 'classnames'

import './index.scss'

class AppDependencyGraph extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: true,
            echartsOption: {},
            colorArr: ['#c23531','#2f4554', '#61a0a8', '#d48265', '#91c7ae','#749f83',  '#ca8622', '#bda29a','#6e7074', '#546570', '#c4ccd3']
        }
        this.loadDepdData = this.loadDepdData.bind(this)
        this.setEchartsOption = this.setEchartsOption.bind(this)

    }
    componentDidMount() {
        this.loadDepdData()
    }
    
    loadDepdData() {
        this.setState({
            isLoading: true
        })
        
        request({
            url: '/appDependencyGraph',
            type: 'get',
            dataType: 'json'
        })
            .then(res => {
                if (res.code === 0) {
                    this.setEchartsOption(res.data)
                }else{
                    message.error(res.msg)
                    this.setEchartsOption({nodes:[],links:[]})
                }
                this.setState({
                    isLoading: false
                })
            })
            .catch(err => {
                message.error('error status: '+err.status)
                this.setEchartsOption({nodes:[],links:[]})
                this.setState({
                    isLoading: false
                })
            })

    }
    setEchartsOption(data) {
        let colors = this.state.colorArr
        data.nodes.forEach(function(node, index){
            
            node.itemStyle = {
                    normal: {
                        color: colors[index%colors.length]
                    }
                }
        })

        let option = {
            title: {
                text: '应用依赖关系图',
                top: 'top',
                left: 'left'
            },
            tooltip: {
                show: true,
                formatter: function (params, ticket, callback){
                    if(params.dataType === 'node'){
                        return params.data.id
                    }
                    if(params.dataType === 'edge'){
                        return params.name +'<br>serviceName:<br>' + params.data.serviceName.join('<br>')
                    }
                }
            },
            
            animationDuration: 1500,
            animationEasingUpdate: 'quinticInOut',
            series: [{
                name: '应用',
                type: 'graph',
                layout: 'force',
                force: {
                    gravity: 0.1,
                    edgeLength: 150,
                    repulsion: 1500
                },
                
                data: data.nodes,
                links: data.links,

                symbolSize: 40,
                roam: true,
                draggable: true,
                focusNodeAdjacency: true,

                edgeSymbol: ['circle', 'arrow'],
                edgeSymbolSize: [4, 8],
                label: {
                    normal: {
                        show: true,
                        // position: 'top',
                        textStyle: {
                            color: '#000'
                        },
                        formatter: function (params, ticket, callback){
                            return params.data.id 
                        },
                    }
                },
                // itemStyle: {
                //     normal: {
                //         color: '#fa727d'
                //     }
                // },
                lineStyle: {
                    normal: {
                        curveness: 0.1
                    }
                }
            }]
        }

        this.setState({
            echartsOption: option
        })

    }

    render() {

        return (
            <Page loading={this.state.isLoading}>
                <ReactEcharts
                    ref='echartsInstance'
                    style={{height: 'calc(100vh - 50px)'}}
                    option={this.state.echartsOption} />
            </Page>
        )
    }
}

export default AppDependencyGraph