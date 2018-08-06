package cn.qingk.socket;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * spring websocket配置
 * @author ThatWay
 * 2018-5-8
 */
@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	private static final Logger logger = Logger.getLogger(WebSocketConfig.class);
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		logger.info("--registerWebSocketHandlers--");
		//注册webscoket处理类、webscocket的访问地址、过滤处理类
		registry.addHandler(webSocketHandler(), "/ws").addInterceptors(webSocketInterceptor());
	}

	/**
	 * websocket请求处理
	 * @return
	 */
	@Bean
	public WebSocketHandler webSocketHandler() {
		logger.info("--webSocketHandler--");
		return new WebScoketHandler();
	}
	
	/**
	 * websocket拦截器
	 * @return
	 */
	@Bean
	public WebSocketInterceptor webSocketInterceptor(){
		logger.info("--webSocketInterceptor--");
		return new WebSocketInterceptor();
	}

}
