package cn.qingk.socket;

/**
 * 连接中的状态封装
 * @author ThatWay
 * 2018-5-9
 */
public class WebSocketStatus {
	
	/*********************状态码 开始**********************/
	//需要根据业务具体情况扩展状态码
	// 处理成功
	public static final int CODE_SUCCESS = 200;
	// 处理失败
	public static final int CODE_FAIL = 500;
	/*********************状态码 结束**********************/
	
	/*********************信息 开始**********************/
	//需要根据业务具体情况扩展信息
	// 处理成功
	public static final String MSG_SUCCESS = "OK";
	// 处理失败
	public static final String MSG_FAIL = "FAIL";
	/*********************信息 结束**********************/
	
	/*********************数据类型 开始**********************/
	// 全网热点
	public static final String TYPE_QWRD = "qwrd";
	// 本地新闻
	public static final String TYPE_BDXW = "bdxw";
	// 网络热搜
	public static final String TYPE_WLRS = "wlrs";
	// 地方舆论
	public static final String TYPE_DFYL = "dfyl";
	// 新闻选题
	public static final String TYPE_XWXT = "xwxt";
	// 外采调度
	public static final String TYPE_WCDD = "wcdd";
	// 生产力统计
	public static final String TYPE_SCLTJ = "scltj";
	// 影响力统计
	public static final String TYPE_YXLTJ = "yxltj";
	// 任务统计
	public static final String TYPE_RWTJ = "rwtj";
	// 资讯热榜
	public static final String TYPE_ZXRB = "zxrb";
	// 视频热榜
	public static final String TYPE_SPRB = "sprb";
	// 列表自定义
	public static final String TYPE_LBZDY = "lbzdy";
	// 图表自定义
	public static final String TYPE_TBZDY = "tbzdy";
	/*********************数据类型 结束**********************/
	
	/*********************动作类型 开始**********************/
	// 基本信息
	public static final String ACTION_SIMPLE = "simple";
	// 详情信息
	public static final String ACTION_DETAIL = "detail";
	//后台连接:视频通话
	public static final String ACTION_LOCAL_VIDEOCALL = "localVideoCall";
	//后台连接:消息提醒
	public static final String ACTION_LOCAL_NOTICE = "notice";
	/*********************动作类型 开始**********************/
	
}
