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
        path: '/404',
        getComponent: (location, cb) => {
            require.ensure([], (require) => {
                cb(null, require('./not-found'))
            })
        }
    }
    ]

