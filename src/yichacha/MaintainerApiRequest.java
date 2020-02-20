package yichacha;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onerunsall.util.JdbcUtils;

import yichacha.module.DataSourceBox;

public class MaintainerApiRequest extends ApiRequest {

	public MaintainerApiRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		super(req, res);
	}

	public static Logger logger = Logger.getLogger(MaintainerApiRequest.class);

	public void record(MaintainerLoginStatus loginStatus, Connection connection) throws Exception {
		if (connection == null)
			return;
		JdbcUtils.runUpdate(connection,
				"insert into t_maintainer_api_request (id,maintainerId,url,ms,cookie,header,formParam,resData) values(?,?,?,?,?,?,?,?)",
				requestId, loginStatus == null ? null : loginStatus.getMaintainerId(), req.getRequestURI(),
				endTime.getTime() - requestTime.getTime(), cookieLog, headerLog, formParamLog, responseContent);
		DataSourceBox.commit(connection);
	}

}
