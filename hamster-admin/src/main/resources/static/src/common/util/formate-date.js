function formatDate(date, format) {
	if(date) {
		// user parseInt to convert string date to int date
		var tempDate = new Date(parseInt(date));
	} else {

		return "";
	}
	var o = {   
		"M+" : tempDate.getMonth()+1,                 //月份   
		"d+" : tempDate.getDate(),                    //日   
		"h+" : tempDate.getHours(),                   //小时   
		"m+" : tempDate.getMinutes(),                 //分   
		"s+" : tempDate.getSeconds(),                 //秒   
		"q+" : Math.floor((tempDate.getMonth()+3)/3), //季度   
		"S"  : tempDate.getMilliseconds()             //毫秒   
	};   
	if(/(y+)/.test(format))   
		format = format.replace(RegExp.$1, (tempDate.getFullYear()+"").substr(4 - RegExp.$1.length));   
	for(var k in o) {
		if(new RegExp("("+ k +")").test(format))   
			format = format.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));   
	}
	return format;
}

export default formatDate