import React from 'react';
import {Breadcrumb,Spin} from 'antd';
import QueueAnim from 'rc-queue-anim'

import './page.scss'

class Page extends React.Component {
    constructor(props) {
        super(props);
    }

    static defaultProps = {
        loading: false,
        animConfig:{
             opacity: [1, 0] 
            },
    }

    render() {

        return (
            <QueueAnim className="yt-admin-framework-page" animConfig={this.props.animConfig} delay={100}>
                    <Spin key="yt-admin-framework-page" spinning={this.props.loading} size="large">
                        <div className="page-content">
                            {this.props.children}
                        </div>
                    </Spin>
            </QueueAnim>
        )
    }
}

export default Page
