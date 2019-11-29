package zaylt.service.bigtxt;

import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RedissonClient;

import com.onerunsall.util.HtmlUtil;
import com.onerunsall.util.JdbcUtil;
import com.onerunsall.util.UnitBreak;
import com.onerunsall.util.Value;

import redis.clients.jedis.Jedis;
import zaylt.service.oss.Oss;

public class Bigtxt {

	private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
	private static DeleteTask deleteUnlocateTask = new DeleteTask();

	public static void init() throws Exception {
		scheduledThreadPoolExecutor.scheduleWithFixedDelay(deleteUnlocateTask, 0, 1, TimeUnit.MINUTES);
	}

	public static String update(String id, String data, String locateSql, Jedis jedis, Connection connection)
			throws Exception {
		if (data == null)
			return id;
		if (StringUtils.isEmpty(locateSql))
			throw new UnitBreak(99, "locateSql不能空");

		List sqlParams = null;
		boolean autoCommitSrc = false;
		try {
			autoCommitSrc = connection.getAutoCommit();
			if (autoCommitSrc)
				connection.setAutoCommit(false);
			if (id == null)
				return insert(data, locateSql, connection);

			Map oldRow = JdbcUtil.queryOne(connection, "select data,innerUrls from t_bigtxt where id=? ", id);

			if (oldRow == null)
				return insert(data, locateSql, connection);

			List<String> newInnerUrls = HtmlUtil.extractUrls(data);

			Oss.realize(connection, "select innerUrls from t_bigtxt where id='" + id + "'",
					newInnerUrls == null ? null : newInnerUrls.toArray(new String[] {}));

			sqlParams = new ArrayList();
			sqlParams.add(data);
			sqlParams.add(StringUtils.join(newInnerUrls, ","));
			sqlParams.add(locateSql);
			sqlParams.add(id);
			JdbcUtil.update(connection,
					"update t_bigtxt set alterTime=now(),data=?,innerUrls=?,locateSql=? where id=? ",
					sqlParams.toArray());

			if (autoCommitSrc)
				connection.commit();

			jedis.del("bigtxt" + id);
			return id;
		} catch (Exception e) {
			if (autoCommitSrc)
				connection.rollback();
			throw e;
		} finally {
			if (autoCommitSrc)
				connection.setAutoCommit(autoCommitSrc);
		}
	}

	public static String insert(String data, String locateSql, Connection connection) throws Exception {
		if (data == null || data.isEmpty())
			return null;
		if (StringUtils.isEmpty(locateSql))
			throw new UnitBreak(99, "locateSql不能空");
		List sqlParams = null;
		boolean autoCommitSrc = false;
		try {
			autoCommitSrc = connection.getAutoCommit();
			if (autoCommitSrc)
				connection.setAutoCommit(false);

			List<String> innerUrls = HtmlUtil.extractUrls(data);

			String id = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
					+ RandomStringUtils.randomNumeric(15);
			sqlParams = new ArrayList();
			sqlParams.add(id);
			sqlParams.add(data);
			sqlParams.add(StringUtils.join(innerUrls, ","));
			sqlParams.add(locateSql);
			sqlParams.add(new Date());
			sqlParams.add(new Date());
			JdbcUtil.update(connection,
					"insert into t_bigtxt (id,data,innerUrls,locateSql,addTime,alterTime) values(?,?,?,?,?,?)",
					sqlParams.toArray());

			Oss.realize(connection, "select innerUrls from t_bigtxt where id='" + id + "'",
					innerUrls == null ? null : innerUrls.toArray(new String[] {}));

			if (autoCommitSrc)
				connection.commit();
			return id;
		} catch (Exception e) {
			if (autoCommitSrc)
				connection.rollback();
			throw e;
		} finally {
			if (autoCommitSrc)
				connection.setAutoCommit(autoCommitSrc);
		}
	}

	public static InputStream getData(String id, RedissonClient redissonClient, Connection connection)
			throws Exception {
		try {
			// 获取请求参数
			RBinaryStream stream = redissonClient.getBinaryStream("bigtxt" + id);
			InputStream in = null;
			if (stream == null || !stream.isExists()) {
				in = JdbcUtil.queryOneStream(connection, "select data from t_bigtxt where id=?", id);
				if (in != null) {
					IOUtils.copy(in, stream.getOutputStream());
				} else
					return null;
			}
			stream.expireAt(new Date().getTime() + 1 * 24 * 60 * 60 * 1000);
			stream = redissonClient.getBinaryStream("bigtxt" + id);
			return stream.getInputStream();
		} catch (Exception e) {
			throw e;
		} finally {
			// 释放资源
		}
	}

	public static Integer getDataSize(String id, Connection connection) throws Exception {
		return JdbcUtil.queryOneInteger(connection, "select length(data) from t_bigtxt where id=?", id);
	}
}
