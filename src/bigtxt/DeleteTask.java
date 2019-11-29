package zaylt.service.bigtxt;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.onerunsall.util.JdbcUtil;
import com.onerunsall.util.Value;

import zaylt.util.DataSourceBox;

public class DeleteTask implements Runnable {

	private static Logger logger = Logger.getLogger(DeleteTask.class);

	@Override
	public void run() {
		logger.info("执行zayiliao.service.bigtxt.DeleteUnlocateTask");
		Connection connection = null;
		try {
			// 查询待删除的文件
			connection = DataSourceBox.dataSource.getConnection();
			connection.setAutoCommit(false);

			List<Map> rows = JdbcUtil.queryList(connection,
					"select id bigtxtId,locateSql from t_bigtxt order by alterTime asc,id desc limit 0,50");
			for (int i = 0; i < rows.size(); i++) {
				try {
					Map row = rows.get(i);
					String bigtxtId = Value.toString(row.get("bigtxtId"));
					String locateSql = Value.toString(row.get("locateSql"));

					boolean deleteIs = false;
					if (!StringUtils.isEmpty(locateSql))
						try {
							String locate = JdbcUtil.queryOneString(connection, locateSql);
							if (StringUtils.isEmpty(locate) || !locate.contains(bigtxtId)) {
								deleteIs = true;
							}
						} catch (Exception e) {
							deleteIs = true;
						}
					else
						deleteIs = true;

					if (deleteIs) {
						JdbcUtil.update(connection, "delete from t_bigtxt where id=? ", bigtxtId);
					} else {
						JdbcUtil.update(connection, "update t_bigtxt set alterTime=now()  where id=? ", bigtxtId);
					}
					connection.commit();
				} catch (Exception e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			}
		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
		}

	}

	public static void main(String[] args) throws MalformedURLException {
		String s = "http://121.40.168.181:81/zhu/j/ianlin1990.d";
		java.net.URL pathurl = new java.net.URL(s);
		String host = pathurl.getHost();
		Integer port = pathurl.getPort();
		System.out.println(port);
		System.out.println("q.qw.e".replaceAll("\\..*?&", ""));
	}

}
