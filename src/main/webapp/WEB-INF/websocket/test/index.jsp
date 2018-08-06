<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
	pageContext.setAttribute("path", path);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>数据服务</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
  </head>
  
  <body>
    <div style="text-align: left;">  
	  <form action="">  
	    <input onclick="req_detail()" value="请求详情" type="button">  
	    <input onclick="req_simple()" value="请求基本" type="button"> 
	    <input onclick="closeWebSocket()" value="关闭socket长链接" type="button">  
	    <br>  
	  </form>  
	</div>  
	<div id="output"></div> 
	<script src="/websocket/js/sockjs.min.js"></script>
	<script src="js/index.js"></script>
  </body>
</html>
