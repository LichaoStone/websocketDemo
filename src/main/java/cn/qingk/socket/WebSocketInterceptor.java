package cn.qingk.socket;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * websocket请求过滤器
 * @author ThatWay
 * 2018-5-8
 */
public class WebSocketInterceptor implements HandshakeInterceptor {
	
	private static final Logger logger = Logger.getLogger(WebSocketInterceptor.class);

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
		logger.info("webscoket处理后过滤回调触发");
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		logger.info("webscoket处理前过滤回调触发");
		
		boolean flag = true;
		
		//在调用handler前处理方法
		if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request; 
            HttpServletRequest req = serverHttpRequest.getServletRequest();
        	// 从请求中获取页面标志
        	String pageFlag = req.getParameter("pageFlag");
        	// 获取初始化需要的数据
        	String actionFlag = req.getParameter("actionFlag");
        	
        	if(StringUtils.isEmpty(pageFlag) || StringUtils.isEmpty(actionFlag) ){
        		flag = false;
        		logger.info("webscoket连接请求，页面标志pageFlag："+pageFlag+",动作标志："+actionFlag+",参数不正确，请求拒绝");
        	} else {
        		logger.info("webscoket连接请求，页面标志pageFlag："+pageFlag+",动作标志："+actionFlag);
        		// 将页面标识和动作标识放入参数中，之后的session将根据这两个值来区分
        		attributes.put("pageFlag", pageFlag.trim());
        		attributes.put("actionFlag", actionFlag.trim());
        	}
        } else {
        	flag = false;
        }
		
        return flag;
	}
}
