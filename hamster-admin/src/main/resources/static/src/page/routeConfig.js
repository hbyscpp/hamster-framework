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
        path: '/app/export-service-list',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./app-export-list'))
            })
        }
    },
    {
        path: '/app/refer-service-list',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./app-refer-list'))
            })
        }
    },
    {
        path: '/node/export-service-list',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./node-export-list'))
            })
        }
    },
    {
        path: '/node/refer-service-list',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./node-refer-list'))
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

