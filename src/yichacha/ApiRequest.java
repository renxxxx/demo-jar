package yichacha;

import java.sql.Connection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.onerunsall.util.Goback;
import com.onerunsall.util.JdbcUtils;
import com.onerunsall.util.ResBean;
import com.onerunsall.util.ServletUtils;

import yichacha.Config;
import yichacha.module.DataSourceBox;

public class ApiRequest {

	public static Logger logger = Logger.getLogger(ApiRequest.class);

	protected HttpServletRequest req;
	protected HttpServletResponse res;
	protected Date requestTime = new Date();
	protected Date endTime = null;
	protected String requestId = Config.sdfCache.getWithCreate("yyyyMMddHHmmssSSS").format(requestTime)
			+ RandomStringUtils.randomNumeric(4);

	protected String cookieLog = "";
	protected String headerLog = "";
	protected String formParamLog = "";
	protected String responseContent = "";

	public ApiRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		this.req = req;
		this.res = res;

		cookieLog = ServletUtils.cookiesToJson(req).toJSONString();
		headerLog = ServletUtils.headersToJson(req).toJSONString();

		if (req.getContentType() != null
				&& req.getContentType().toLowerCase().contains("application/x-www-form-urlencoded"))
			formParamLog = JSON.toJSONString(req.getParameterMap());

		String queryString = StringUtils.trimToEmpty(req.getQueryString());
		logger.info(new StringBuilder().append("track new req ").append(requestId).append(" 【").append(req.getMethod())
				.append(" ").append(req.getRequestURI()).append(queryString.isEmpty() ? "" : "?").append(queryString)
				.append("】").toString());
		logger.info("formParams: " + formParamLog);
		logger.info("headers: " + headerLog);
		logger.info("cookies: " + cookieLog);
	}

//	public void outputJson(JSONObject data, Integer code, String codeMsg) throws IOException {
//		endTime = new Date();
//		ResBean resJson = new ResBean();
//		if (data != null)
//			resJson.setData(data);
//		if (code != null)
//			resJson.setCode(code);
//
//		if (code != null && codeMsg == null) {
//			if (req.getLocale().getLanguage().equals("zh") || StringUtils.isEmpty(req.getHeader("accept-language")))
//				codeMsg = Sc.resCodeCnMapper.getProperty(code + "");
//			else
//				codeMsg = Sc.resCodeEnMapper.getProperty(code + "");
//		}
//		if (codeMsg != null)
//			resJson.setCodeMsg(codeMsg);
//
//		responseContent = JSON.toJSONString(resJson, SerializerFeature.WriteMapNullValue);
//		logger.info(new StringBuilder("response: " + requestId).append(" takes ")
//				.append(endTime.getTime() - requestTime.getTime()).append("ms.").append(" data: ")
//				.append(responseContent).toString());
//
//		res.setCharacterEncoding(Sc.SYS_CHARSET);
//		res.setStatus(200);
//		res.setContentType("application/json;charset=" + Sc.SYS_CHARSET);
//		res.getWriter().write(responseContent);
//	}

	public void Goback(Exception e) throws Exception {

		endTime = new Date();

		int code = 0;
		String codeMsg = null;
		Object data = null;
		String errParam = null;
		if (e instanceof Goback) {
			Goback ire = (Goback) e;
			code = ire.getCode();
			codeMsg = StringUtils.trim(ire.getCodeMsg());
			data = ire.getData();
			errParam = ire.getErrParam();
		} else {
			code = 98;
		}
		if (code != 0)
			logger.info("request " + requestId + ": " + ExceptionUtils.getStackTrace(e));

		data = data == null ? new JSONObject() : data;

		if (StringUtils.isEmpty(codeMsg)) {
			if (req.getLocale().getLanguage().equals("zh") || StringUtils.isEmpty(req.getHeader("accept-language")))
				codeMsg = Config.resCodeCnMapper.getProperty(code + "");
			else
				codeMsg = Config.resCodeEnMapper.getProperty(code + "");
		}

		ResBean resBean = new ResBean();
		resBean.setCode(code);
		resBean.setCodeMsg(codeMsg);
		resBean.setErrParam(errParam);
		resBean.setData(data);
		resBean.setRequestId(requestId);

		responseContent = JSON.toJSONString(resBean, SerializerFeature.WriteMapNullValue);
		logger.info(new StringBuilder("response: " + requestId).append(" takes ")
				.append(endTime.getTime() - requestTime.getTime()).append("ms.").append(" data: ")
				.append(responseContent).toString());

		res.setCharacterEncoding(Config.charset);
		res.setStatus(200);
		res.setContentType("application/json;charset=" + Config.charset);
		res.getWriter().write(responseContent);
	}

	public void record(Connection connection) throws Exception {
		if (connection == null)
			return;
		if (endTime == null)
			endTime = new Date();
		JdbcUtils.runUpdate(connection,
				"insert into t_api_request (id,url,ms,cookie,header,formParam,resData) values(?,?,?,?,?,?,?)",
				requestId, req.getRequestURI(), endTime.getTime() - requestTime.getTime(), cookieLog, headerLog,
				formParamLog, responseContent);
		DataSourceBox.commit(connection);
	}

}
