package file;

import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.onerunsall.util.HtmlUtil;
import com.onerunsall.util.IOUtil;
import com.onerunsall.util.JdbcUtil;
import com.onerunsall.util.UnitBreak;
import com.onerunsall.util.UrlUtil;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import zayiliao.util.Util;

public class FileService {

	private static Logger logger = Logger.getLogger(FileService.class);
	private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

	private static DeleteTask deleteFileTask = new DeleteTask();
	private static ClearTempFolderTask clearTempFolderTask = new ClearTempFolderTask();

	public static String root = null;
	public static String urlroot = null;
	public static String tempPath = "/temp";
	public static String pagePath = "/page";

	public static void init(String root, String urlroot) throws Exception {
		logger.info(FileService.class + " 初始化");
		FileService.root = root;
		FileService.urlroot = urlroot;
		File rootFile = new File(FileService.root);
		if (!rootFile.exists() && !rootFile.mkdirs())
			throw new RuntimeException(rootFile.getAbsolutePath() + " 创建失败");
		File rootTempFile = new File(FileService.root, tempPath);
		if (!rootTempFile.exists() && !rootTempFile.mkdirs())
			throw new RuntimeException(rootTempFile.getAbsolutePath() + " 创建失败");
		File rootPageFile = new File(FileService.root, pagePath);
		if (!rootPageFile.exists() && !rootPageFile.mkdirs())
			throw new RuntimeException(rootPageFile.getAbsolutePath() + " 创建失败");

		scheduledThreadPoolExecutor.setMaximumPoolSize(2);
		scheduledThreadPoolExecutor.scheduleWithFixedDelay(deleteFileTask, 0, 1, TimeUnit.MINUTES);
		scheduledThreadPoolExecutor.scheduleWithFixedDelay(clearTempFolderTask, 0, 1, TimeUnit.MINUTES);
	}

	public static void realize(Connection connection, String locateSql, String... urls) throws Exception {
		if (urls == null || urls.length == 0)
			return;

		if (StringUtils.isEmpty(locateSql))
			throw new UnitBreak(99, "locateSql不能空");
		JdbcUtil.queryOneString(connection, locateSql);

		for (int i = 0; i < urls.length; i++) {
			String url = urls[i];
			if (url == null || url.trim().isEmpty())
				continue;
			String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
			String fileId = UrlUtil.stripExtName(fileName);

			if (JdbcUtil.update(connection, "update t_file set temp=0,locateSql=? where id=?", locateSql, fileId) > 0)
				continue;

			File tempFile = new File(FileService.root + FileService.tempPath, fileName);
			if (!tempFile.exists())
				continue;

			JdbcUtil.update(connection,
					"insert into t_file (id,name,size,url,locateSql,temp,addTime,alterTime) values(?,?,?,?,?,0,now(),now())",
					fileId, fileName, tempFile.length(), url, locateSql);

			File realFile = new File(FileService.root, fileName);
			realFile.delete();
			tempFile.renameTo(realFile);
			tempFile.delete();
		}
	}

	public static void realizeBigtxt(Connection connection, String locateSql, String url) throws Exception {
		if (url == null || url.length() == 0)
			return;

		if (StringUtils.isEmpty(locateSql))
			throw new UnitBreak(99, "locateSql不能空");
		JdbcUtil.queryOneString(connection, locateSql);

		if (url == null || url.trim().isEmpty())
			return;
		String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
		String fileId = fileName;

		File tempFile = new File(FileService.root + FileService.tempPath, fileName);
		if (!tempFile.exists())
			return;

		List<String> innerUrls = HtmlUtil.extractUrls(tempFile);

		///
		Map oldRow = JdbcUtil.queryOne(connection, "select innerUrls from t_file where id=? ", fileId);

		if (oldRow != null) {
			JdbcUtil.update(connection,
					"update t_file set alterTime=now(),size=?,innerUrls=?,temp=0,locateSql=? where id=? ",
					tempFile.length(), StringUtils.join(innerUrls, ","), locateSql, fileId);
		} else {
			JdbcUtil.update(connection,
					"insert into t_file (id,name,size,url,innerUrls,locateSql,temp,addTime,alterTime) values(?,?,?,?,?,0,now(),'now())",
					fileId, fileName, tempFile.length(), url, StringUtils.join(innerUrls, ","), locateSql);
		}

		FileService.realize(connection, "select innerUrls from t_file where id='" + fileId + "'",
				innerUrls == null ? null : innerUrls.toArray(new String[] {}));

		File realFile = new File(FileService.root, fileName);
		realFile.delete();
		tempFile.renameTo(realFile);
		tempFile.delete();
	}

	public static void delete(Connection connection, String... urls) throws Exception {
		if (urls == null || urls.length == 0)
			return;
		JdbcUtil.batch(connection, "update t_file set locateSql='select 1' where url=?", urls);
	}

	public static String link(Connection connection, String targetUrl) throws Exception {
		if (StringUtils.isEmpty(targetUrl))
			return null;

		String targetFileName = targetUrl.substring(targetUrl.lastIndexOf("/") + 1, targetUrl.length());
		String targetFileId = UrlUtil.stripExtName(targetFileName);
		String targetExt = UrlUtil.getExtName(targetFileName);

		String linkFileId = Util.newId();
		String linkFileName = linkFileId + "." + targetExt;
		File linkFile = new File(FileService.root, linkFileName);

		Path link = FileSystems.getDefault().getPath(linkFile.getAbsolutePath());
		Path target = FileSystems.getDefault().getPath(targetFileName);
		Files.createSymbolicLink(link, target);

		String linkFileUrl = UrlUtil.buildPath(FileService.urlroot, linkFileName);
		JdbcUtil.update(connection,
				"insert into t_file (id,targetFileId,name,size,path,temp,addTime,alterTime) values(?,?,?,?,?,1,now(),now())",
				linkFileId, targetFileId, linkFileName, linkFile.length(), linkFileUrl);
		return linkFileUrl;
	}

	public static File getFile(String url) throws Exception {
		url = url.replaceAll("\\\\", "/").replaceFirst("(?i)((http:/+)|(https:/+))", "").replaceFirst(".*?/", "/");
		File file = new File(FileService.root, url.replace(FileService.urlroot, ""));
		return file;
	}

	public static String newImage(Connection connection, InputStream is, String originalFileName, long size,
			Integer quality) throws Exception {
		if (quality == null)
			quality = 8;

		File ossFolder = new File(FileService.root);
		logger.debug(ossFolder.getAbsolutePath());
		if (!ossFolder.exists())
			ossFolder.mkdirs();
		// 保存文件

		String ext = UrlUtil.getExtName(originalFileName);
		String fileId = Util.newId();
		String fileName = StringUtils.isEmpty(ext) ? fileId : (fileId + "." + ext);
		String realFileUrl = UrlUtil.buildPath(FileService.urlroot, fileName);

		JdbcUtil.update(connection,
				"insert into t_file (id,name,size,url,temp,addTime,alterTime) values(?,?,?,?,?,1,now(),now())", fileId,
				fileName, size, realFileUrl);

		File realFile = new File(ossFolder, fileName);

		if (quality != null && quality >= 0 && quality < 10) {
			Builder builder = Thumbnails.of(is);
			builder.scale(1.0);
			builder.outputQuality(new Double(quality) / 10f);
			builder.toFile(realFile);
		} else {
			IOUtil.write(is, realFile);
		}

//		Path link = FileSystems.getDefault().getPath(realLinkFile.getAbsolutePath());
//		Path target = FileSystems.getDefault().getPath(tempFile.getAbsolutePath());
//		Files.createSymbolicLink(link, target);

		return realFileUrl;
	}

	public static String newFile(Connection connection, InputStream is, String originalFileName, String url, long size,
			String cover, Integer duration) throws Exception {
		File ossFolder = new File(FileService.root);
		logger.debug(ossFolder.getAbsolutePath());
		if (!ossFolder.exists())
			ossFolder.mkdirs();

		// 保存文件
		String fileId = null;
		String fileName = null;
		String realFileUrl = null;
		if (url != null && !url.isEmpty()) {
			fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
			fileId = UrlUtil.stripExtName(fileName);
			realFileUrl = url;
		} else {
			String ext = StringUtils.isEmpty(originalFileName) ? "" : UrlUtil.getExtName(originalFileName);
			fileId = Util.newId();
			fileName = StringUtils.isEmpty(ext) ? fileId : (fileId + "." + ext);
			realFileUrl = UrlUtil.buildPath(FileService.urlroot, fileName);
		}

		File realFile = new File(ossFolder, fileName);

		FileService.realize(connection, "select id from t_file where id=" + fileId, cover);

		if (JdbcUtil.queryOne(connection, "select id from t_file where id=?", fileId) != null) {
			JdbcUtil.update(connection, "update t_file set alterTime=now(),size=?,temp=1 where id=? ", size, fileId);
		} else
			JdbcUtil.update(connection,
					"insert into t_file (id,name,size,url,temp,addTime,alterTime,cover,duration) values(?,?,?,?,?,1,now(),now(),?,?)",
					fileId, fileName, size, realFileUrl, cover, duration);

//		File oldFile = getFile(url);
//		oldFile.delete();

		IOUtil.write(is, realFile);

//		Path link = FileSystems.getDefault().getPath(realLinkFile.getAbsolutePath());
//		Path target = FileSystems.getDefault().getPath(tempFile.getAbsolutePath());
//		Files.createSymbolicLink(link, target);

		return realFileUrl;
	}

}
