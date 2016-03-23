/*通过参数名获取参数的值*/
function getQueryString(name){
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = window.location.search.substr(1).match(reg);
	if (r != null){
		return unescape(r[2]);
	}
	return "no";
}
/*替换URL中指定参数的值*/
function replaceParamVal(url,paramName,replaceWith) {
	var oUrl = url;
	var re = eval('/('+ paramName+'=)([^&]*)/gi');
	var nUrl = oUrl.replace(re,paramName+'='+replaceWith);
	return nUrl;
}
/*上一页*/
function getPrePage(){
	var pNum = getNum();
	var url = window.location.href;
	if( url.indexOf("page") != -1){
		pNum = pNum - 1;
		if(pNum == 0){
			pNum = 1;
		}
		url = replaceParamVal(url,"page",pNum);
		location.href = url;
	}
}
function getNextPage(){
	var pNum = getNum();
	var url = window.location.href;
	if( url.indexOf("page") != -1){
		pNum = pNum + 1;
		url = replaceParamVal(url,"page",pNum);
		location.href = url;
	}else {
		location.href = url + "&page=2";
	}
}
function getOneBindPage(){
	var pNum = $("#gotoPageNum").val();
	var url = window.location.href;
	if( url.indexOf("page") != -1){
		url = replaceParamVal(url,"page",pNum);
		location.href = url;
	}else {
		location.href = url+"&page="+pNum;
	}
}
function getNum(){
	var num = getQueryString("page");
	if(num == "" || !isNumber(num)){
		return 1;
	}
	num = parseInt(num);
	return num;
}
function search(){
	var url = "applist?to=50e949d98c804e2ba2d5e7f141ad20ee";
	var ipandport = $("#ipandport").val();
	if(ipandport != null && ipandport != ""){
		url = "applist?to=50e949d98c804e2ba2d5e7f141ad20ee&ips="+ipandport;
	}
	location.href = url;
}
function searchDB(){
	var ipandport = $("#ipandport").val();
	var serverName = $("#serverName").val();
	var connectState = $("#connectState").val();
	var applicationName = $("#appNames").val().split(',');
	var monitorIp = $("#monitorIps").val().split(',');
	var erp = $("#erp").val();
	var url = "applistfromdb?to=50e949d98c804e2ba2d5e7f141ad20ee";
	url+="&jmxUrl="+ipandport;
	url+="&serverName="+serverName;
	url+="&state="+connectState;
	url += "&applicationName=" + applicationName[0];
	url += "&monitorIp=" + monitorIp[0];
	url += "&erp=" + erp;
	location.href = url;
}