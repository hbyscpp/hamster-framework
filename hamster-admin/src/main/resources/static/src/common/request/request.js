function request(opt){
    return new Promise(function (resolve, reject) {
        // 此处可以根据返回值做权限控制
        $.ajax(opt)
            .then(res => {
                resolve(res)
            }, err => {
                reject(new Error(err))
            })
    }).catch(err=>{
        console.log('全局提示ajax请求错误')
    })
}

export default request