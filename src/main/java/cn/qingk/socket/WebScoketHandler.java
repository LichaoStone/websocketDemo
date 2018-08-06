package cn.qingk.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.alibaba.fastjson.JSONObject;
import com.allook.frame.Constants;
import com.allook.frame.Constants.ACTION;
import com.allook.frame.Constants.CODE;
import com.allook.frame.Constants.COMMAND;
import com.allook.frame.Constants.MSG_STATUS;
import com.allook.frame.Constants.RET;
import com.allook.frame.Constants.TYPE;
import com.allook.frame.Constants.VIDEOCALLLOCK;
import com.allook.local.bean.DictionariesBean;
import com.allook.local.service.LocalService;
import com.allook.mobile.bean.UserBean;
import com.allook.mobile.service.MobileService;
import com.allook.screen.bean.ScreenBean;
import com.allook.screen.service.ScreenService;
import com.allook.utils.PropertiesUtil;


/**
 * 消息处理类
 * @author ThatWay
 * 2018-5-5
 */
@Service
public class WebScoketHandler extends TextWebSocketHandler{
	private static final Logger logger = Logger.getLogger(WebScoketHandler.class);
	
	@Autowired
    private ScreenService screenService;
	@Autowired
    private LocalService  localService;
	@Autowired
    private MobileService mobileService;
	
    //在线websocket集合
	public static Map<String, WebSocketSession> clients = new ConcurrentHashMap<String, WebSocketSession>(); 
    // 静态变量，用来记录当前在线连接数
    public static final AtomicInteger connectCount = new AtomicInteger(0);
 	
	/**
	 * 连接建立成功后的回调
	 */
	@Override    
    public void afterConnectionEstablished(WebSocketSession session) throws Exception { 
		//返回结果
    	String returnJson = "";
        try {
        	 // 页面标识
            String pageFlag = getAttributeFlag(session,Constants.CLIENT_ID);
            // 初始化动作标识
            String reqAction = getAttributeFlag(session,Constants.ACTION_INIT);
            
            
            if (!StringUtils.isEmpty(pageFlag) && !StringUtils.isEmpty(reqAction)) {
	        	  // 连接数加一,为了保证多个同页面标识的请求能被处理
	              addOnlineCount();
	              
	              int onlineCount = getOnlineCount();
	              //session唯一标识
	              String key = pageFlag+"_"+reqAction+"_"+this.getUUID();
	              logger.info("【websocket回调】websockt创建成功:"+key);
	              
	              //管理已连接的session
	              clients.put(key,session);
	              logger.info("【websocket回调】在线屏数:"+onlineCount);
              
    		 
	    		  if (reqAction.toLowerCase().equals(ACTION.SIMPLE)) {
	    				 // DB基本数据
	    				logger.info("【基本数据SIMPLE】数据库查询【"+pageFlag+"】的基本数据");
	    				
	    				// 查询数据库得到type
	    	    		ScreenBean screenBean=screenService.getPageFlagSetting(pageFlag);
	    	    		
	    	    		
	    				Map<String, Object> infoMap = new HashMap<String, Object>();
	    				infoMap.put("type", screenBean.getShowTypeName());                    //模块（例:WCDD）
	    				infoMap.put("title", screenBean.getScreenName());                     //模块名称
	    				infoMap.put("highlightSecond", screenBean.getHighlightSecond());	  //高亮时间
	    				infoMap.put("showCount", screenBean.getShowCount());                  //显示条数
	    				
	    				returnJson = this.makeInfoResponseJson(
		    						CODE.SUCCESS,
		    						screenBean.getShowTypeName(),
		    						reqAction,
		    						MSG_STATUS.SUCCESS,
		    						infoMap
	    						);
	    		  } else if (reqAction.toLowerCase().equals(ACTION.DETAIL)){
		    			logger.info("【详细数据DETAIL】数据库查询【"+pageFlag+"】的列表数据");
		    			
		    			//根据屏号刷新大屏数据
		    			ScreenBean screenBean=screenService.getPageFlagSetting(pageFlag);
		    			screenService.refreshScreen(screenBean);
		    			
		    			return;
	    		 }else if(reqAction.equals(ACTION.LOCAL_VIDEOCALL)) {
	    			 logger.info("【视频通话LOCAL_VIDEOCALL】本地管理系统websocket创建成功...");
	    			 returnJson = getJson(CODE.SUCCESS,RET.SUCCESS,ACTION.LOCAL_VIDEOCALL);
				 }else if(reqAction.toLowerCase().equals(ACTION.LOCAL_NOTICE)) {
					 logger.info("【消息提醒LOCAL_NOTICE】本地管理系统websocket创建成功...");
	    			 returnJson = getJson(CODE.SUCCESS,RET.SUCCESS,ACTION.LOCAL_NOTICE);
				 }else{
					 returnJson = getJson(CODE.SUCCESS,RET.ACTION_ERROR,null);
	    			 logger.error("【websocket回调】ACTION行为不存在,ACTION为:"+reqAction);
	    		 }
            } else {
            	returnJson = getJson(CODE.SUCCESS,RET.MISSING_PARAMETER,null);
                session.sendMessage(new TextMessage(returnJson)); 
                session.close();
                return;
            }
            
            TextMessage returnMessage = new TextMessage(returnJson); 
            session.sendMessage(returnMessage); 
		} catch (Exception e) {
			returnJson = getJson(CODE.FAIL,RET.FAIL,null);
            TextMessage returnMessage = new TextMessage(returnJson); 
            session.sendMessage(returnMessage); 
			logger.error("【websocket回调】afterConnectionEstablished出错:",e);
		}
    }    
	
	/**
	 * 接收消息处理
	 */
    @Override    
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {   
    	logger.info("【websocket会话】handleTextMessage开始... ...");
    	long start = System.currentTimeMillis();
    	
    	//接收终端发过来的消息
    	String reqMsg = message.getPayload();
    	
    	// 根据页面标识进行逻辑处理，提取需要的数据
    	if (!StringUtils.isEmpty(reqMsg)) {
    		JSONObject terminalMsg = JSONObject.parseObject(reqMsg);
    		if (!terminalMsg.isEmpty()) {
    			
    			if (terminalMsg.containsKey("pageFlag") 
    					&& terminalMsg.containsKey("actionFlag")) {
    				
    				String reqPageFlag = terminalMsg.getString("pageFlag");
    				String reqAction = terminalMsg.getString("actionFlag");
    				
    				
    				if (reqAction.toLowerCase().equals(ACTION.SIMPLE)) {
        				logger.info("【基本数据SIMPLE】数据库查询【"+reqPageFlag+"】的基本数据");
    				}else if (reqAction.toLowerCase().equals(ACTION.DETAIL)) {
    	    			logger.info("【详情数据DETAIL】数据库查询【"+reqPageFlag+"】的列表数据");
    	    			// 查询数据库得到type
        	    		ScreenBean screenBean=screenService.getPageFlagSetting(reqPageFlag);
        	    		
    	    			if(Constants.MODULE.WCDD.equals(screenBean.getShowTypeName())){//视频通话
    	    		 	     String command=terminalMsg.getString("command");
    	    		 	     String ret=terminalMsg.getString("ret");
    	    		 	     String pageFlageUniqueKey=terminalMsg.getString("pageFlageUniqueKey");
    	    		 	     String type=terminalMsg.getString("type");
    	    		 	     String userKey=terminalMsg.getString("userKey");//被叫人userKey
    	    		 	     String isResetStatus=terminalMsg.getString("isResetStatus");//是否重置
    	    		 	     
    	    		 	     logger.info("【视频通话】command="+command
    	    		 	    		 +",ret="+ret
    	    		 	    		 +",pageFlageUniqueKey="+pageFlageUniqueKey
    	    		 	    		 +",type="+type
    	    		 	    		 +",userKey="+userKey
    	    		 	    		 +",isResetStatus="+isResetStatus);
    	    		 	    
    	    		 	     if(TYPE.WEB_CALL.equals(type)){//触摸屏发起通话
    	    		 	    	    if (screenService.isAllowCall()){//判断剩余时长,是否允许通话
    	    		 	    	    	logger.info("【视频通话webCall】videoCallLock="+Constants.VIDEO_CALL_LOCK);
    	    	    				    if (VIDEOCALLLOCK.OFF.equals(Constants.VIDEO_CALL_LOCK)||Constants.VIDEO_CALL_LOCK==null){//视频通话未锁定
    	    	    				    	Constants.VIDEO_CALL_LOCK=VIDEOCALLLOCK.ON;
    	    	    				    	logger.info("【视频通话webCall】videoCallLock="+Constants.VIDEO_CALL_LOCK);
    	    	    					    
    	    	    						try {
    	    	    							UserBean userBean=new UserBean();
    	    	    							userBean.setUserKey(userKey);
    	    	    							userBean.setImgDomain(PropertiesUtil.getValue("imgIP"));
    	    	    							userBean=localService.getUserInfo(userBean);
    	    	    							
    	    	    							
    	    	    							DictionariesBean dictionariesBean=new DictionariesBean();
    	    	    							dictionariesBean.setDictionariesName("'appInformationKey','appKey','appSecret','adminWyyxToken','adminUserKey'");
    	    	    							List<DictionariesBean> dictionariesList=(List<DictionariesBean>) screenService.getDictValuesByNames(dictionariesBean);
    	    	    							
    	    	    							String wyyxToken="";   
    	    	    							String netEASEKey="";             
    	    	    							String appKey="";
    	    	    							String initiativeId="";
    	    	    							String appSecret="";
    	    	    							if (dictionariesList!=null&&dictionariesList.size()>0) {
    												for(DictionariesBean tempBean:dictionariesList){
    													String dictionaries_name=tempBean.getDictionariesName();
    													String dictionaries_value=tempBean.getDictionariesValue();
    													
    													if ("adminWyyxToken".equals(dictionaries_name)) {
    														  wyyxToken=dictionaries_value;
    													}else if ("appKey".equals(dictionaries_name)) {
    														  netEASEKey=dictionaries_value;
    													}else if ("appInformationKey".equals(dictionaries_name)) {
    														  appKey=dictionaries_value;
    													}else if ("adminUserKey".equals(dictionaries_name)) {
    														  initiativeId=dictionaries_value;
    													}else if ("appSecret".equals(dictionaries_name)) {
    														  appSecret=dictionaries_value;
    													}
    												}
    											}
    	    	    							logger.info("【视频通话webCall】wyyxToken="+wyyxToken);
    	    	    							
    	    	    							
    	    	    							UserBean resultUserBean=new UserBean();
    	    	    							resultUserBean.setAppKey(appKey);  							      //应用key
    	    	    							resultUserBean.setCommand("call"); 					              //操作:触摸屏发起通话
    	    	    							resultUserBean.setPassiveId(userKey); 				              //接收任id
    	    	    							resultUserBean.setInitiativeId(initiativeId);			          //发起人id
    	    	    							resultUserBean.setNetEASEKey(netEASEKey);				          //网易云唯一注册码
    	    	    							resultUserBean.setUserName(userBean.getUserName()); 	          //接收人账号
    	    	    							resultUserBean.setRealName(userBean.getRealName()); 	          //接收人姓名
    	    	    							resultUserBean.setHeadImgUrl(userBean.getHeadImgUrl());           //接收人头像
    	    	    							resultUserBean.setPageFlageUniqueKey(pageFlageUniqueKey);         //用于发送websocket使用，相当于屏号，标志唯一
    	    	    							resultUserBean.setUserKey(userKey); 							  //接收任id
    	    	    							
    	    	    							if (wyyxToken==null||"".equals(wyyxToken)){//密码为空，需要先注册网易账号密码
    												//注册网易账号
    	    	    								UserBean tmpbean=new UserBean();
    	    	    								tmpbean.setUserKey(userKey);
    	    	    								tmpbean.setHeadImgUrl(userBean.getHeadImgUrl());
    	    	    								tmpbean.setUserName(userBean.getUserName());
    	    	    								
    	    	    								tmpbean=mobileService.updateYxToken(tmpbean);
    	    	    								
    	    	    								logger.info("【视频通话webCall】新注册账号密码:"+tmpbean.getWyyxToken());
    	    	    								resultUserBean.setWyyxToken(tmpbean.getWyyxToken());
    											}else{
    												resultUserBean.setWyyxToken(wyyxToken);  //发起人网易云信key
    											}
    	    	    							
    	    	    				            List<Object> list=new ArrayList<Object>();
    	    	    				            list.add(resultUserBean);
    	    	    							
    	    	    					    	TextMessage listMessage = new TextMessage(this.makeListResponseJson(
    	    	    					    			WebSocketStatus.CODE_SUCCESS, 
    	    	    					    			TYPE.NET_CALL,
    	    	    					    			WebSocketStatus.ACTION_DETAIL, 
    	    	    					    			WebSocketStatus.MSG_SUCCESS, 
    	    	    					    			list.size(),
    	    	    					    			list
    	    	    					    			)
    	    	    					    	  );
    	    	    					    	 
    	    	    					    	 this.sendMessageToPage(reqPageFlag,ACTION.DETAIL,listMessage);
    	    	    					} catch (Exception e) {
    	    	    						logger.error("【视频通话webCall】视频连接失败:",e);
    	    	    					}finally {
    	    	    						Constants.VIDEO_CALL_LOCK=VIDEOCALLLOCK.OFF;
    	    	    					}
    	    	    				  }else{
    	    	    					  
    	    	    					  /**
    	    	    					   * JSON串格式:
    	    	    					   * {
    	    	    					   * 	"msg":"OK",
    	    	    					   * 	"code":200,
    	    	    					   * 	"action":"detail",
    	    	    					   * 	"type":"notCall",
    	    	    					   * 	"body":{
    	    	    					   * 			"userKey":xxx,
    	    	    					   * 			"ret":'xxxx'
    	    	    					   * 			}
    	    	    					   * }
    	    	    					   */
    	    	    					 Map<String, Object> map = new HashMap<String, Object>();
 			    	    		    	 map.put("msg",WebSocketStatus.MSG_SUCCESS);
 			    	    		    	 map.put("code",WebSocketStatus.CODE_SUCCESS);
 			    	    		    	 map.put("action",WebSocketStatus.ACTION_DETAIL);
 			    	    		    	 map.put("type",TYPE.NOT_CALL);
 			    	    		    	 
 			    	    		    	 Map<String,Object> map1 = new HashMap<String, Object>();
 			    	    		    	 map1.put("userKey",userKey);
 			    	    		    	 map1.put("ret",RET.CALL_BUSSY);
 			    	    		    	 map.put("body", map1);
    	    	    					  
    	    	    					  
    	    	    					 TextMessage listMessage = new TextMessage(net.sf.json.JSONObject.fromObject(map).toString());
	    	    					     this.sendMessageToPage(reqPageFlag,ACTION.DETAIL,listMessage);
    	    	    					 logger.info("【视频通话webCall】视频通话占线... ...");
    	    	    				  }
									}else{//剩余通话时长不够,不允许通话
										
										/**
	  	    	    					   * JSON串格式:
	  	    	    					   * {
	  	    	    					   * 	"msg":"OK",
	  	    	    					   * 	"code":200,
	  	    	    					   * 	"action":"detail",
	  	    	    					   * 	"type":"notCall",
	  	    	    					   * 	"body":{
	  	    	    					   * 			"userKey":xxx,
	  	    	    					   * 			"ret":'xxxx'
	  	    	    					   * 			}
	  	    	    					   * }
	  	    	    					   */
	  	    	    					     Map<String, Object> map = new HashMap<String, Object>();
				    	    		    	 map.put("msg",WebSocketStatus.MSG_SUCCESS);
				    	    		    	 map.put("code",WebSocketStatus.CODE_SUCCESS);
				    	    		    	 map.put("action",WebSocketStatus.ACTION_DETAIL);
				    	    		    	 map.put("type",TYPE.NOT_CALL);
				    	    		    	 
				    	    		    	 Map<String,Object> map1 = new HashMap<String, Object>();
				    	    		    	 map1.put("userKey",userKey);
				    	    		    	 map1.put("ret",RET.NO_MORE_CALLTIME);
				    	    		    	 map.put("body", map1);
	  	    	    					  
	  	    	    					  
	  	    	    					     TextMessage listMessage = new TextMessage(net.sf.json.JSONObject.fromObject(map).toString());
		    	    					     this.sendMessageToPage(reqPageFlag,ACTION.DETAIL,listMessage);
	    	    					    	 logger.info("【视频通话websocket】通话时长不够,不允许通话... ...");
									}
							  }else if (TYPE.RET.equals(type)) {//发送通话状态到后台
								    if (pageFlageUniqueKey!=null&&!"".equals(pageFlageUniqueKey)) {
								    	 //通知后台视频通话状态
			    	    				 Map<String, Object> map = new HashMap<String, Object>();
			    	    		    	 map.put("code",200);
			    	    		    	 Map<String,Object> map1 = new HashMap<String, Object>();
			    	    		    	 map1.put("ret",ret);
			    	    		    	 map1.put("msg",Constants.VIDEOCALL_INFO.get(ret));
			    	    		    	 map1.put("action",ACTION.LOCAL_VIDEOCALL);
			    	    		    	 map1.put("command",command);
			    	    		    	 map.put("results", map1);
			    	    				 TextMessage listMessage = new TextMessage(net.sf.json.JSONObject.fromObject(map).toString());
			    	    			     	
			    	    		    	 //向所有打开pageFlage的浏览器发送消息
			    	    		         boolean sendFlag2 = this.sendMessageToPage(
			    	    		        		pageFlageUniqueKey,                 //相当于屏号
			    	    		        		ACTION.LOCAL_VIDEOCALL,
			    	    		        		listMessage
			    	    		        		);
			    	    		         logger.info("【视频通话ret】通知后台消息发送状态:"+sendFlag2);
									}
								     
		    	    		         if ("1".equals(isResetStatus)) {
		    	    		        	 //通知大屏状态
									     Map<String, Object> map = new HashMap<String, Object>();
			    	    		    	 map.put("code",200);
			    	    		    	 Map<String,Object> map1 = new HashMap<String, Object>();
			    	    		    	 map1.put("ret",ret);
			    	    		    	 map1.put("msg",Constants.VIDEOCALL_INFO.get(ret));
			    	    		    	 map1.put("userKey",userKey);
			    	    		    	 map1.put("command",command);
			    	    		    	 map.put("body", map1);
			    	    		    	 map.put("type","resetStatus");
			    	    				 TextMessage listMessage = new TextMessage(net.sf.json.JSONObject.fromObject(map).toString());
			    	    			     	
			    	    				 boolean sendFlag2=this.sendMessageToPage(reqPageFlag,ACTION.DETAIL,listMessage);
			    	    				 logger.info("【视频通话ret】通知大屏消息发送状态:"+sendFlag2);
									}
							  }else if (TYPE.WEB_HANGUP.equals(type)) {//webHangUp大屏挂断
		    	    		        //通知大屏状态
		    	 					Map<String,Object> map=new HashMap<String,Object>();
		    	 					map.put("command",COMMAND.HANGUP);						//操作:call/hangUp
		    	 					map.put("passiveId",userKey);							//接收任id
		    	 					map.put("pageFlageUniqueKey",pageFlageUniqueKey);		//用于发送websocket使用，相当于屏号，标志唯一
		    	 					map.put("userKey", userKey);							//接收任id
		    	 		            List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
		    	 		            list.add(map);
		    	 					
		    	 			    	TextMessage listMessage = new TextMessage(this.makeListResponseJson(
		    	 			    			WebSocketStatus.CODE_SUCCESS, 
		    	 			    			TYPE.NET_CALL,
		    	 			    			WebSocketStatus.ACTION_DETAIL, 
		    	 			    			WebSocketStatus.MSG_SUCCESS, 
		    	 			    			list.size(),
		    	 			    			list
		    	 			    			)
		    	 			    		);
		    	 			    	
		    	 			    	 boolean sendFlag2=this.sendMessageToPage(reqPageFlag,ACTION.DETAIL,listMessage);
		    	 			    	 logger.info("【视频通话webHangUp】通知大屏消息发送状态:"+sendFlag2);
							  }
						 }
    				}else if (reqAction.equals(ACTION.LOCAL_VIDEOCALL)) {
						logger.info("【视频通话·后台】本地管理系统websocket获取信息...");
					}else if (reqAction.toLowerCase().equals(ACTION.LOCAL_NOTICE)) {
						logger.info("【消息提醒·后台】本地管理系统websocket获取信息...");
					}else {
    			    	logger.error("客户端请求的action为："+reqAction);
    				}
    			}
    		} else {
    			logger.error("客户端请求的消息转换json为空");
    		}
    	} else {
    		logger.error("客户端请求的消息为空");
    	}
    	
        long pass = System.currentTimeMillis() - start;
        logger.info("【websocket会话】handleTextMessage耗时:"+pass+"ms");
        logger.info("【websocket会话】handleTextMessage结束... ...");
   }    
    
    /**
     * 出现异常时的回调
     */
    @Override  
    public void handleTransportError(WebSocketSession session, Throwable thrwbl) throws Exception {    
        if(session.isOpen()){    
        	session.close();  
        }    
       logger.info("websocket 连接出现异常准备关闭");
    }    
    
    /**
     * 连接关闭后的回调
     */
    @Override    
    public void afterConnectionClosed(WebSocketSession session, CloseStatus cs) throws Exception {    
    	// 连接数减1
		for (Entry<String, WebSocketSession> entry : clients.entrySet()) {
            String clientKey = entry.getKey();
            WebSocketSession closeSession = entry.getValue();
        	
            if(closeSession == session){
            	logger.info("移除clientKey:"+clientKey);
            	clients.remove(clientKey);
				decOnlineCount();
				
				int leftOnlineCount = getOnlineCount();
				logger.info("剩余在线屏数:"+leftOnlineCount);
            }
        }
        logger.info("websocket 连接关闭了");    
    }    
    
    @Override    
    public boolean supportsPartialMessages() {    
        return false;    
    }  
    
    /**
     * 发送信息给指定页面
     * @param clientId
     * @param actionType
     * @param message
     * @return
     */
    public boolean sendMessageToPage(String pageFlag,String actionType, TextMessage message) {
    	boolean flag = false;
    	int all_counter = 0;
		int send_counter = 0;
		long start = System.currentTimeMillis();
		
    	if(!StringUtils.isEmpty(pageFlag) && !StringUtils.isEmpty(actionType)){
    		logger.info(clients);
    		
    		 // 给所有以此标识标识开头的终端发送消息
            String startFlag = pageFlag + "_" + actionType;
            logger.info("startFlag="+startFlag);
            
    		for (Entry<String, WebSocketSession> entry : clients.entrySet()) {
                String clientKey = entry.getKey();
                
                if(clientKey.startsWith(startFlag)){
                	all_counter++;
                	WebSocketSession session = entry.getValue();
                	
                	if (!session.isOpen()) {
                	  logger.info("session.isOpen():"+session.isOpen());
                	  flag = false;
                	} else {
						try {
						    session.sendMessage(message);
						    send_counter++;
						    flag =  true;
						    logger.info("sendMessageToPage：[clientKey:"+clientKey+"],flag:"+flag);
						} catch (IOException e) {
							logger.error("【发送websocket失败】",e);
						    flag = false;
						}
                	}
                }
            }
    	}
    	
    	long pass = System.currentTimeMillis() - start;
    	logger.info("sendMessageToPage："+pageFlag+",flag:"+flag+",all_counter:"+all_counter+",send_counter:"+send_counter+",pass:"+pass+"ms");   
    	
        return flag;
    }
    
    /**
     * 发送信息给所有页面
     * @param clientId
     * @param message
     * @return
     */
    public boolean sendMessageToAll(TextMessage message) {
    	
    	boolean flag = false;
    	int all_counter = 0;
		int send_counter = 0;
		long start = System.currentTimeMillis();
    	
    	for (Entry<String, WebSocketSession> entry : clients.entrySet()) {  
    		
    		all_counter++;
    		String clientKey = entry.getKey();
    		WebSocketSession session = entry.getValue();
    		if (!session.isOpen()) {
    		  flag =  false;
          	} else {
				try {
				    session.sendMessage(message);
				    flag = true;
				    send_counter++;
				    logger.info("sendMessageToAll：[clientKey:"+clientKey+"],flag:"+flag);
				} catch (IOException e) {
				    e.printStackTrace();
				    flag = false;
				}
          	} 
        }  
    	long pass = System.currentTimeMillis() - start;
    	logger.info("sendMessageToAll,flag:"+flag+",all_counter:"+all_counter+",send_counter:"+send_counter+",pass:"+pass+"ms"); 
        return flag;
    }
    
    /**
     * 给指定的精准发送消息
     * @param message
     * @param toUser
     * @throws IOException
     */
    public boolean sendMessageToId(String clientId,TextMessage message) throws IOException {  
    	boolean flag = false;
    	int all_counter = 0;
		int send_counter = 0;
		long start = System.currentTimeMillis();
		
    	if(!StringUtils.isEmpty(clientId)){
    		all_counter++;
    		WebSocketSession session = clients.get(clientId);
    		if (!session.isOpen()) {
    			flag = false;
            } else {
  				try {
  				    session.sendMessage(message);
  				    flag = true;
  				    send_counter++;
  				} catch (IOException e) {
  				    e.printStackTrace();
  				    flag = false;
  				}
            } 
    	}
    	
    	long pass = System.currentTimeMillis() - start;
    	logger.info("sendMessageToId:"+clientId+",flag:"+flag+",all_counter:"+all_counter+",send_counter:"+send_counter+",pass:"+pass+"ms");
		return flag;
    }  
    
    /**
     * 获取参数标识
     * @param session
     * @return
     */
    private String getAttributeFlag(WebSocketSession session,String flagName) {
    	
    	String flag = null;
        try {
        	flag = (String) session.getHandshakeAttributes().get(flagName);
        } catch (Exception e) {
        	logger.error(e.getMessage());
        }
        
        return flag;
    }
    
    /**
     * 当前连接数
     * @return
     */
    private synchronized int getOnlineCount() {  
        return connectCount.get();  
    }  
  
    /**
     * 新增连接数
     */
    private synchronized void addOnlineCount() {  
    	connectCount.getAndIncrement();
    }  
  
    /**
     * 减连接数
     */
    private synchronized void decOnlineCount() {  
    	connectCount.getAndDecrement();
    }  
    
    /** 
     * 获得一个UUID 
     *  
     * @return String UUID 
     */  
    private String getUUID()  
    {  
        return UUID.randomUUID().toString().replaceAll("-", "");  
    } 
    
   
    /**
     * 生成列表响应json
     * @param code 状态码
     * @param type 数据类型
     * @param action 操作类选
     * @param msg 提示信息
     * @param totalCount 总数量
     * @param dataList 数据列表
     * @return json
     */
    public synchronized String makeListResponseJson(
    		int 	code,
    		String 	type,
    		String 	action,
    		String 	msg,
    		int 	totalCount,
    		List<?> dataList){
    	Map<String,Object> jsonObj = new HashMap<String,Object>();
    	jsonObj.put("code", code);
    	jsonObj.put("type", type);
    	jsonObj.put("action", action); 
    	jsonObj.put("msg", msg);
    	
    	Map<String,Object> contentObj = new HashMap<String,Object>();
    	contentObj.put("totalCount", totalCount);
    	contentObj.put("list",dataList);
    	
    	jsonObj.put("body", contentObj);
    	logger.info("生成list json:" + jsonObj.toString());
    	return net.sf.json.JSONObject.fromObject(jsonObj).toString();
    }
    
    
    /**
     *  【SIMPLE】生成详情响应json
     * @param code 状态
     * @param type 数据类型
     * @param action 操作类型
     * @param msg 提示消息
     * @param info 数据详情
     * @return json
     */
    public synchronized String makeInfoResponseJson(int code,String type,String action,String msg,Object info){
    	JSONObject jsonObj = new JSONObject();
    	jsonObj.put("code", code);
    	jsonObj.put("type", type);
    	jsonObj.put("action", action);
    	jsonObj.put("msg", msg);
    	jsonObj.put("body", info);
    	
    	logger.info("生成info json:" + jsonObj.toString());
    	return jsonObj.toString();
    }
    
    /**
	 * 获取json串（本地管理平台建立websocket使用）
	 * @param CODE
	 * @param RET
	 * @param action
	 * @return
	 */
	 protected static String getJson(int CODE,int RET,String action) {
		    JSONObject jsonObj = new JSONObject();  
			jsonObj.put("code",CODE);
		    
	    	JSONObject jsonObj2 = new JSONObject();
	    	jsonObj2.put("ret",RET);
	    	
	    	String msg="";
	    	if (RET==200) {
				msg="成功";
			}else if (RET==500) {
				msg="失败";
			}
	    	jsonObj2.put("msg",msg);
	    	jsonObj2.put("action",action);
	    	
	    	jsonObj.put("results",jsonObj2);
	    	logger.info("组装完成返回json:" + jsonObj.toString());
	    	return jsonObj.toString();
	  }
    
    
      public static void main(String[] args) {
    	WebScoketHandler ws=new WebScoketHandler();
    	  System.out.println(getJson(CODE.SUCCESS,RET.SUCCESS,"localVideoCall"));
    	  System.out.println(ws.makeInfoResponseJson(CODE.SUCCESS, "1", "localVideoCall", "成功", new Object()));
	 }
}
