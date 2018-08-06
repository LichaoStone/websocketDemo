package cn.qingk.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.TextMessage;

import com.allook.frame.BaseController;

import cn.qingk.entity.User;
import cn.qingk.service.TestService;
import cn.qingk.socket.WebScoketHandler;
import cn.qingk.socket.WebSocketStatus;

@Controller
@RequestMapping("/testController")
public class TestController extends BaseController{
    public static final Logger LOGGER = Logger.getLogger(TestController.class);
    
    @Autowired
    private WebScoketHandler handler;
    
    @RequestMapping("/test")
    public void test(HttpServletRequest request,HttpServletResponse response) {
        try{
        	//String reString=testService.listUsers();
        	Map<String, Object> infoMap = new HashMap<String, Object>();
        	infoMap.put("type", "qwzx");
        	infoMap.put("title", "全网资讯");
        	
        	TextMessage infoMessage = new TextMessage(handler.makeInfoResponseJson(WebSocketStatus.CODE_SUCCESS, WebSocketStatus.TYPE_QWRD,WebSocketStatus.ACTION_SIMPLE, WebSocketStatus.MSG_SUCCESS, infoMap));
        	
        	int totalCount = 3;
        	User user1 = new User();
        	user1.setAddress("address 1");
        	user1.setAge(18);
        	user1.setId(1);
        	user1.setName("name 1");
        	
        	User user2 = new User();
        	user2.setAddress("address 2");
        	user2.setAge(18);
        	user2.setId(1);
        	user2.setName("name 2");
        	
        	User user3 = new User();
        	user3.setAddress("address 3");
        	user3.setAge(18);
        	user3.setId(1);
        	user3.setName("name 3");
        	
        	List<Object> userList = new ArrayList<Object>();
        	userList.add(user1);
        	userList.add(user2);
        	userList.add(user3); 
        	TextMessage listMessage = new TextMessage(handler.makeListResponseJson(WebSocketStatus.CODE_SUCCESS, WebSocketStatus.TYPE_QWRD,WebSocketStatus.ACTION_DETAIL, WebSocketStatus.MSG_SUCCESS, totalCount,userList));
        	
        	String pageFlag = "p1";
        	
        	//向所有打开P1的浏览器发送消息
        	boolean sendFlag1 = this.handler.sendMessageToPage(pageFlag, WebSocketStatus.ACTION_SIMPLE,infoMessage);
        	System.out.println("sendFlag1:"+sendFlag1);
            response.getWriter().print(sendFlag1);
            
            
            boolean sendFlag2 = this.handler.sendMessageToPage(pageFlag, WebSocketStatus.ACTION_DETAIL,listMessage);
        	System.out.println("sendFlag1:"+sendFlag2);
            response.getWriter().print(sendFlag2);
        	response.getWriter().print(infoMap.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @RequestMapping("/toList")
	public ModelAndView toList(HttpServletRequest request) {
		LOGGER.info("【测试】toList");
		Map<String, Object> model = new HashMap<String, Object>();
		return new ModelAndView("/index",model);
	}
}

