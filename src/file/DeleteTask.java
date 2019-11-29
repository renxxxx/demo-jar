package file;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.onerunsall.util.JdbcUtil;
import com.onerunsall.util.Value;

import zayiliao.util.DataSourceBox;
import zayiliao.util.Util;

public class DeleteTask implements Runnable {

	private static Logger logger = Logger.getLogger(DeleteTask.class);

	@Override
	public void run() {
		logger.debug("execute " + DeleteTask.class);
		Connection connection = null;
		try {
			// 查询待删除的文件

			connection = DataSourceBox.dataSource.getConnection();
			connection.setAutoCommit(false);

			// 删除临时文件
			List<Map> rows = JdbcUtil.queryList(connection,
					"select id fileId,url from t_file where temp=1 and now() >SUBDATE(alterTime,interval -3 minute) order by id asc limit 0,50");
			for (int i = 0; i < rows.size(); i++) {
				try {
					Map row = rows.get(i);
					String fileId = Value.toString(row.get("fileId"));
					String url = Value.toString(row.get("url"));

					JdbcUtil.update(connection, "delete from t_file where id=? ", fileId);
					File file = FileService.getFile(url);
					logger.debug(
							"del " + file.getAbsolutePath() + " exists:" + file.exists() + " delete:" + file.delete());
					if (file.exists())
						throw new RuntimeException(file.getAbsolutePath() + " 文件删除失败");
					connection.commit();
				} catch (Exception e) {
					connection.rollback();
					Util.occurException(e);
				}
			}

			// 删除无法定位的文件
			rows = JdbcUtil.queryList(connection,
					"select id fileId,url,locateSql from t_file where temp=0 order by alterTime asc,id desc limit 0,50");
			for (int i = 0; i < rows.size(); i++) {
				try {
					Map row = rows.get(i);
					String fileId = Value.toString(row.get("fileId"));
					String url = Value.toString(row.get("url"));
					String locateSql = Value.toString(row.get("locateSql"));

					boolean deleteIs = false;
					File file = FileService.getFile(url);
					if (!StringUtils.isEmpty(locateSql)) {
						try {
							String locate = JdbcUtil.queryOneString(connection, locateSql);
							if (StringUtils.isEmpty(locate) || !locate.contains(url)) {
								deleteIs = true;
							}
						} catch (Exception e) {
							deleteIs = true;
						}
					} else {
						deleteIs = true;
					}

					if (JdbcUtil.queryOneInteger(connection, "select count(1) from t_file where targetFileId=?",
							fileId) > 0)
						deleteIs = false;

					if (deleteIs) {
						JdbcUtil.update(connection, "delete from t_file where id=? ", fileId);
						logger.debug("del " + file.getAbsolutePath() + " exists:" + file.exists() + " delete:"
								+ file.delete());
						if (file.exists())
							throw new RuntimeException(file.getAbsolutePath() + " 文件删除失败");
					} else {
						JdbcUtil.update(connection, "update t_file set alterTime=now()  where id=? ", fileId);
					}
					connection.commit();
				} catch (Exception e) {
					connection.rollback();
					Util.occurException(e);
				}
			}
		} catch (Exception e) {
			Util.occurException(e);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					Util.occurException(e);
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
