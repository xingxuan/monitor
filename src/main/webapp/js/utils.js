function isNull(str){
	if(str==""){return true;}
	var regu = "^[ ]+$";
	var re = new RegExp(regu);
	return re.test(str);
}
function isIPAndPort(strIP){
	if(isNull(strIP)){
		return false;
	}
	var re = /^(\d+)\.(\d+)\.(\d+)\.(\d+)\:(\d+)$/g;
	if(re.test(strIP)){
		if (RegExp.$1 < 256 && RegExp.$2 < 256 && RegExp.$3 < 256 && RegExp.$4 < 256 && RegExp.$5 < 65535) {
			return true;
		}
	}
	return false;
}
/*判断str是否为一个大于或等于0的数字*/
function isNumber(str){
	var regu = "^[0-9]*$";
	var re = new RegExp(regu);
	return re.test(str);
}