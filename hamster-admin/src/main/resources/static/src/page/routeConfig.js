export default [
    {
        path: '/home',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./home'))
            })
        }
    },
    {
        path: '/service/service-instance-list',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./service-instance-list'))
            })
        }
    },
    {
        path: '/service/refer-instance-list',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./refer-instance-list'))
            })
        }
    },
    {
        path: '/404',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./not-found'))
            })
        }
    }
    ]

