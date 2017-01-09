"use strict"

import React from 'react'
import { message, Button, Icon, Popover, Radio } from 'antd'
const RadioButton = Radio.Button
const RadioGroup = Radio.Group

import 'echarts/lib/chart/graph'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/title'
import ReactEcharts from 'component/echarts-for-react'

import Page from 'framework/page'
import request from 'common/request/request.js'
import _ from 'lodash'
import classNames from 'classnames'
import FAIcon from 'component/faicon'

import './index.scss'

class AppDependencyGraph extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            isLoading: true,
            appCyclesData: [],
            cycleValue: null,
            echartsDepdAllData: {},
            echartsOption: {
                title: {
                    text: '应用依赖关系图',
                    top: 'top',
                    left: 'left'
                },
                tooltip: {
                    show: true,
                    formatter: function (params, ticket, callback) {
                        if (params.dataType === 'node') {
                            return params.data.id
                        }
                        if (params.dataType === 'edge') {
                            return params.name + '<br>serviceName:<br>' + params.data.serviceName.join('<br>')
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

                    data: [],
                    links: [],

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
                            formatter: function (params, ticket, callback) {
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
                            // curveness: 0.1
                        }
                    }
                }]
            },
            colorArr: ['#c23531', '#2f4554', '#61a0a8', '#d48265', '#91c7ae', '#749f83', '#ca8622', '#bda29a', '#6e7074', '#546570', '#c4ccd3']
        }
        this.loadDepdData = this.loadDepdData.bind(this)
        this.setEchartsOption = this.setEchartsOption.bind(this)
        this.setTwoWayFlag = this.setTwoWayFlag.bind(this)
        this.getEchartsOptionCycle = this.getEchartsOptionCycle.bind(this)
        this.setEchartsOptionCycle = this.setEchartsOptionCycle.bind(this)
        this.setEchartsOptionAll = this.setEchartsOptionAll.bind(this)
        this.onCycleChange = this.onCycleChange.bind(this)

    }
    componentDidMount() {
        this.loadDepdData()
    }

    loadDepdData() {
        this.setState({
            isLoading: true
        })

        let appCyclesPromise = request({
            url: '/appCycles',
            type: 'get',
            dataType: 'json'
        })
        
        let appDependencyGraphPromise = request({
            url: '/appDependencyGraph',
            type: 'get',
            dataType: 'json'
        })

        Promise
        .all([appCyclesPromise, appDependencyGraphPromise])
        .then(res=>{
            if (res[0].code === 0) {
                this.setState({
                    appCyclesData: res[0].data
                })
            } else {
                message.error(res[0].msg)
            }

            if (res[1].code === 0) {
                this.setEchartsOption(res[1].data)
            } else {
                message.error(res[1].msg)
                this.setEchartsOption({ nodes: [], links: [] })
            }
            this.setState({
                isLoading: false
            })
        })
        .catch(err => {
            console.log(err)
                message.error('error status: ' + err.status)
                this.setEchartsOption({ nodes: [], links: [] })
                this.setState({
                    isLoading: false
                })
            })            

    }
    // @links
    // @return links
    setTwoWayFlag(links) {
        let n = {}
        let r = []
        for (let i = 0; i < links.length; i++) {
            r.push(links[i])
            if (n[links[i].target + links[i].source] === undefined) {
                n[links[i].source + links[i].target] = i

            } else {
                console.log(links[i].source + links[i].target)
                console.log(n[links[i].target + links[i].source])
                n[links[i].source + links[i].target] = i
                // 设置标记 isTwoWayFlag
                r[i].isTwoWayFlag = true
                r[n[links[i].target + links[i].source]].isTwoWayFlag = true
            }
        }

        return r
    }
    setEchartsOption(data) {
        let colors = this.state.colorArr
        data.nodes.forEach(function (node, index) {

            node.itemStyle = {
                normal: {
                    color: colors[index % colors.length]
                }
            }
        })

        data.links = this.setTwoWayFlag(data.links)

        data.links.forEach(function (link, index) {
            if (link.isTwoWayFlag) {
                link.lineStyle = {
                    normal: {
                        // color: 'red',
                        curveness: 0.1
                    }
                }
            }
        })

        let option = this.state.echartsOption

        option.series[0].data = data.nodes
        option.series[0].links = data.links

        this.setState({
            echartsOption: option,
            echartsDepdAllData: data
        })
        // let datahaha = this.getEchartsOptionCycle(this.state.appCyclesData[0], data)
        
        // option.series[0].data = datahaha.nodes
        // option.series[0].links = datahaha.links
        // this.setState({
        //     echartsOption: option
        // })
    }

    //@cycle ['a', 'b', 'c']
    //@echartsDepdAllData this.state.echartsDepdAllData
    //@return {nodes:[],links:[]}
    getEchartsOptionCycle(cycle, echartsDepdAllData){
        // 找出点(easy)
        let nodes = []
        nodes = cycle.map(v=>{
            return {id:v}
        })

        nodes = nodes.map((v,i)=>{
            let nodeIndex = _.findIndex(echartsDepdAllData.nodes, v)
            let node = echartsDepdAllData.nodes[nodeIndex]
            return node
        })
        // 找出线(根据点找线,首尾相连)
        // [a,b,c]
        // ab bc ca
        let nodesLink = [
            {
                source: cycle[cycle.length-1],
                target: cycle[0]
            }
        ]
        cycle.reduce((a,b)=>{
            nodesLink.push({
                source: a,
                target: b
            })
            return b
        })

        nodesLink = nodesLink.map((v,i)=>{
            let linkIndex = _.findIndex(echartsDepdAllData.links, v)
            let link = echartsDepdAllData.links[linkIndex]
            return link
        })
        
        return {
            nodes: nodes,
            links: nodesLink
        }

    }
    //@index index in appCyclesData
    setEchartsOptionCycle(index){
        console.log(index)
        console.log(this.state.appCyclesData[index])
        let data = this.getEchartsOptionCycle(this.state.appCyclesData[index], this.state.echartsDepdAllData)
        let option = this.state.echartsOption

        option.series[0].data = data.nodes
        option.series[0].links = data.links

        this.setState({
            echartsOption: option
        })

    }

    setEchartsOptionAll(){
        let option = this.state.echartsOption

        option.series[0].data = this.state.echartsDepdAllData.nodes
        option.series[0].links = this.state.echartsDepdAllData.links

        this.setState({
            echartsOption: option,
            cycleValue: null
        })
    }

    onCycleChange(e){
        let index = e.target.value

        this.setEchartsOptionCycle(index)
        this.setState({
            cycleValue: index
        })
    }

    render() {
       
        const cycleList = (
            <RadioGroup onChange={this.onCycleChange} value={this.state.cycleValue}>
                {
                    this.state.appCyclesData.map((v, i) => {
                        return (
                             <RadioButton className="hehe" size="small" key={i} value={i}>{v[0]} <FAIcon type='chain' /> {v[v.length - 1]}</RadioButton>
                        )
                    })
                }
            </RadioGroup>
        )
        
        return (
            <Page loading={this.state.isLoading}>
                <ul className="dependdency-graph-cycle-tags">
                    <li>
                        <Button onClick={this.setEchartsOptionAll} size="small" type="ghost">显示全部</Button>
                    </li>
                    <li>
                        <Popover placement="bottomLeft" trigger="click" content={this.state.appCyclesData.length>0?cycleList:'暂无循环依赖'}>
                            <Button size="small" type="ghost">循环依赖</Button>
                        </Popover>
                    </li>
                </ul>
                <ReactEcharts
                    ref='echartsInstance'
                    style={{ height: 'calc(100vh - 50px)' }}
                    option={this.state.echartsOption} />
            </Page>
        )
    }
}

export default AppDependencyGraph