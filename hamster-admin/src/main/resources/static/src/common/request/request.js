import _ from 'lodash'

function request(option){
    let opt = _.cloneDeep(option)

    if(opt.url && process.env.NODE_ENV === 'development'){
        opt.url = '/api' + opt.url
    }

    return Promise
    .resolve($.ajax.call($, opt))
    .then(res=>{
        // 此处可以根据返回值做权限控制
        return res
    })
    .catch(function(error){
        console.log('global handle ajax error:', error)
        return error
    })
}

export default request