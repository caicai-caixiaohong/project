package nc.impl.ic.m4k.gdxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.logging.Logger;
import nc.impl.ic.m4k.ylpt.DataSourceReadUtil;
import nc.vo.pub.BusinessException;
import net.sf.json.JSONObject;

import com.google.gson.GsonBuilder;
import com.yonyou.nc.ic.itf.IBlendingSystemSyncInfo;
/**
 * 勾兑系统出入库单据同步接口
 * @author caixh
 *
 */
public class BlendingSystemSyncInfoHttpImpl implements IHttpServletAdaptor {

	private static final String METHOD_POST = "POST";

	@Override
	public void doAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		nc.bs.logging.Logger
				.error("#####勾兑系统推送出入库业务单据到NC系统   BlendingSystemSyncInfoHttpImpl  doAction进入#####");
		String method = req.getMethod();//

		NCLocator.getInstance().lookup(ISecurityTokenCallback.class)
				.token("NCSystem".getBytes(), "pfxx".getBytes());

		// 设置数据源
		String dsname = DataSourceReadUtil.getDataSourceValue("dsname");
		InvocationInfoProxy.getInstance().setUserDataSource(dsname);
		InvocationInfoProxy.getInstance().setGroupId("0001A110000000000BSR");

		if (method == METHOD_POST) {
			doPost(req, resp);
		} else {
			resp.setContentType("application/json;charset=UTF-8");
			// resp.getWriter().write("{\"code\":-2,\"msg\":\"请用post方法请求!\"}");

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("STATE_CODE", "0003");
			map.put("STATE_DESC", "请用post方法请求!");
			resp.getWriter().write(JSONObject.fromObject(map).toString());
			Logger.error("非POST请求，非法请求!");
		}

	}

	private void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		nc.bs.logging.Logger
				.error("#####勾兑系统出入库业务单据推送到NC系统          GSyncPurchaseInfoHttpImpl  doPost进入#####");
		resp.setContentType("application/json;charset=UTF-8");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(ServletInputStream) req.getInputStream(), "utf-8"));
		StringBuffer sb = new StringBuffer("");
		String temp;
		while ((temp = br.readLine()) != null) {
			sb.append(temp);
		}
		br.close();
		String params = sb.toString();
		// 判断参数是否为空
		GsonBuilder builder = new GsonBuilder();
		String result = "";
		Map<String, Object> map = JSONObject.fromObject(params);
		// String json = URLDecoder.decode(map.get("json").toString(), "UTF-8");
		String json = map.get("data").toString();
		Map<String, Object> data = JSONObject.fromObject(json);
		String type = (data == null ? null : (String) data.get("type"));
		if (params == null || "".equals(params) || data == null || type == null) {
			Map<String, Object> retMap = new HashMap<String, Object>();
			retMap.put("STATE_CODE", "0003");
			retMap.put("STATE_DESC", "数据接受失败，请求参数、data、type都不能为空。");
			result = builder.create().toJson(retMap);
		} else {
			try {
				switch (type) {
					case "finprodin":// 供应链-库存管理-产成品入库（新增入库类型：散酒入库）
						result = syncFinProdInInfo(data);
						
						break;
					case "transform":// 供应链-库存管理- 库存量管理-库存调整-形态转换（新增转换类型：散酒分类定级）
						result = syncTransformInfo(data);
						
						break;
					case "sapply":// 供应链-库存管理- 出库申请单（新增出库类型：基酒入库）
						result = syncSapplyInfo(data);
						
						break;
					case "invcount":// 供应链-库存管理-库存调整-盘点
						result = syncInvcountInfo(data);
						
						break;
						
					default:
						Map<String, Object> res = new HashMap<String, Object>();
						res.put("STATE_CODE", "0002");
						res.put("STATE_DESC", "type参数有误！请检查！");
						result = builder.create().toJson(res);
						Logger.error("GSyncPurchaseInfoHttpImpl    type参数有误！");
						break;
				}
			}catch(Exception e){
				Map<String, Object> retMap = new HashMap<String, Object>();
				retMap.put("STATE_CODE", "0003");
				retMap.put("STATE_DESC", "数据同步出现异常："+e.getMessage());
				result = builder.create().toJson(retMap);
				Logger.error("GSyncPurchaseInfoHttpImpl  参数："+type+" 数据同步出现异常："+e.toString());
			}
		}

		resp.getWriter().write(result);
		nc.bs.logging.Logger
		.error("#####勾兑系统出入库业务单据推送到NC系统          GSyncPurchaseInfoHttpImpl  doPost正常结束#####");
	}
	
	private String sys(){
		return "caixh";
	}

	/**
	 * 供应链-库存管理-库存调整-盘点
	 * 
	 * @param data
	 * @throws BusinessException 
	 */
	private String syncInvcountInfo(Map<String, Object> data) throws BusinessException {
		IBlendingSystemSyncInfo iBlendingSystemSyncInfo = NCLocator.getInstance()
				.lookup(IBlendingSystemSyncInfo.class);
		return iBlendingSystemSyncInfo.syncInvcountInfo(data);
	}

	/**
	 * 供应链-库存管理- 出库申请单（新增出库类型：基酒入库）
	 * 
	 * @param data
	 */
	private String syncSapplyInfo(Map<String, Object> data) {
		IBlendingSystemSyncInfo iBlendingSystemSyncInfo = NCLocator.getInstance()
				.lookup(IBlendingSystemSyncInfo.class);
		return iBlendingSystemSyncInfo.syncSapplyInfo(data);
	}

	/**
	 * 供应链-库存管理- 库存量管理-库存调整-形态转换（新增转换类型：散酒分类定级）
	 * 
	 * @param data
	 */
	private String syncTransformInfo(Map<String, Object> data) {
		IBlendingSystemSyncInfo iBlendingSystemSyncInfo = NCLocator.getInstance()
				.lookup(IBlendingSystemSyncInfo.class);
		return iBlendingSystemSyncInfo.syncTransformInfo(data);
	}

	/**
	 * 供应链-库存管理-产成品入库（新增入库类型：散酒入库）
	 * 
	 * @param data
	 * @throws BusinessException 
	 */
	private String syncFinProdInInfo(Map<String, Object> data) throws BusinessException {
		IBlendingSystemSyncInfo iBlendingSystemSyncInfo = NCLocator.getInstance()
				.lookup(IBlendingSystemSyncInfo.class);
		return iBlendingSystemSyncInfo.syncFinProdInInfo(data);
	}
}
