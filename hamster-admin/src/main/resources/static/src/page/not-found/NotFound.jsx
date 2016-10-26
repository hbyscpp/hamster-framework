"use strict"
import React from 'react'
import FAIcon from 'component/faicon'
import Page from 'framework/page'

const NotFound = (props) => 

    <Page loading={true}>
        <p>
            <FAIcon type="leaf" />
            404 not found
        </p>
    </Page>

export default NotFound