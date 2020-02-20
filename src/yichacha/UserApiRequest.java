package yichacha;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onerunsall.util.JdbcUtils;

import yichacha.module.DataSourceBox;

public class UserApiRequest extends ApiRequest {

	public UserApiRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
		super(req, res);
	}

	public static Logger logger = Logger.getLogger(UserApiRequest.class);

	public void record(UserLoginStatus loginStatus, Connection connection) throws Exception {
		if (connection == null)
			return;
		JdbcUtils.runUpdate(connection,
				"insert into t_user_api_request (id,userId,url,ms,cookie,header,formParam,resData) values(?,?,?,?,?,?,?,?)",
				requestId, loginStatus == null ? null : loginStatus.getUserId(), req.getRequestURI(),
				endTime.getTime() - requestTime.getTime(), cookieLog, headerLog, formParamLog, responseContent);
		DataSourceBox.commit(connection);
	}

}
