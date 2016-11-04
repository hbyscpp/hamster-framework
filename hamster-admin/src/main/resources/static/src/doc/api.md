### 登录 
checkpwd?username=  & password=

###
服务接口  serviceSearch?q=xxx

###
应用搜索接口 appSearch?q=xxx

###
节点搜索接口 nodeSearch?q=xxx

###
serviceInstanceList?serviceName=xxx
提供者   消费者
消费者 referInstanceList?serviceName=xxx

### 应用暴露服务的列表
appExportServiceList?app=xxx
服务列表 
### 应用引用的服务列表
appReferServiceList?app=xxx
引用服务列表
### 节点暴露服务的列表
nodeExportServiceList?node=xxx
服务列表
### 节点引用服务的列表
nodeReferServiceList?node=xxx
引用服务列表

### 这个请求包含字段描述
configDesc
这个请求包含字段描述

###
appDependencyGraph
应用依赖关系图
### 节点list
nodes = [
    {
        id: 0 // 唯一ID ，其他属性在这个对象里面随便加
    },
    {
        id: 1
    }
]
### 关系list
links = [
    {
        source: 0, 
        target: 1
    }
]

