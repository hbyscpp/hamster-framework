import 'font-awesome/css/font-awesome.css';
import React from 'react';
import classNames from 'classnames';

class FAIcon extends React.Component{
    render() {
        const {type, className, ...others} = this.props
        const cls = classNames({
            'fa': true,
            [`fa-${type}`]: true,
            // ['fa-'+type]: true,
        }, className)
        return <i className={cls} {...others}></i>
    }
}

FAIcon.propTypes = {
    type: React.PropTypes.string,
}

export default FAIcon