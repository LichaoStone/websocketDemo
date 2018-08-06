var echo_websocket;  
var wsUri = "ws://localhost:8080/integrate_pipe/ws?pageFlag=p1&actionFlag=detail";  
  
function createWebsocket()  
{  
    echo_websocket = new WebSocket(wsUri);  
      
    echo_websocket.onopen = function (evt) {  
      writeToScreen("Connected !");  
      //doSend(textID.value);  
    };  
    echo_websocket.onmessage = function (evt) {  
      writeToScreen("Received message: " + evt.data);  
      //echo_websocket.close();  
    };  
    echo_websocket.onerror = function (evt) {  
      writeToScreen('<span style="color: red;">ERROR:</span> '  
        + evt.data);  
      echo_websocket.close();  
    };  
    echo_websocket.onclose = function () {  
        writeToScreen('<span style="color: red;">CLOSE</span> ');  
      };  
        
    clearScreen();  
}  
  
  
function init() {  
  output = document.getElementById("output");  
  writeToScreen("Connecting to " + wsUri);  
    
  createWebsocket();  
}  
  
function req_detail(){
	var reqObj = {
		pageFlag: "p1",
		action: "detail"	
	}
	var detailReqJson = JSON.stringify(reqObj);
	doSend(detailReqJson);
}

function req_simple(){
	var reqObj = {
		pageFlag: "p1",
		action: "simple"	
	}
	var detailReqJson = JSON.stringify(reqObj);
	doSend(detailReqJson);
}
    
function closeWebSocket() {
    echo_websocket.close();  
}  

function doSend(message) {  
 if(echo_websocket && echo_websocket.readyState==1) {
	 echo_websocket.send(message);  
	  writeToScreen("Sent message: " + message);  
 } else {
	 createWebsocket(); 
 }
}  
function writeToScreen(message) {  
  var pre = document.createElement("p");  
  pre.style.wordWrap = "break-word";  
  pre.innerHTML = message;  
  output.appendChild(pre);  
}      
function clearScreen(message) {  
    output.innerHTML="";  
  }         
window.addEventListener("load", init, false); 