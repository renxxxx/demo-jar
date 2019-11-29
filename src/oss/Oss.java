package zaylt.service.oss;

import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.onerunsall.util.IOUtil;
import com.onerunsall.util.JdbcUtil;
import com.onerunsall.util.UnitBreak;
import com.onerunsall.util.UrlUtil;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;

public class Oss {

	private static Logger logger = Logger.getLogger(Oss.class);
	private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

	private static DeleteTask deleteFileTask = new DeleteTask();
	private static ClearTempFolderTask clearTempFolderTask = new ClearTempFolderTask();

	public static String ossroot = null;
	public static String ossurlroot = null;
	public static String tempPath = "/temp";
	public static String pagePath = "/page";

	public static void init(String ossroot, String ossurlroot) throws Exception {
		logger.info(Oss.class + " 初始化");
		Oss.ossroot = ossroot;
		Oss.ossurlroot = ossurlroot;
		File ossrootFile = new File(Oss.ossroot);
		if (!ossrootFile.exists() && !ossrootFile.mkdirs())
			throw new RuntimeException(ossrootFile.getAbsolutePath() + " 创建失败");
		File ossrootTempFile = new File(Oss.ossroot, tempPath);
		if (!ossrootTempFile.exists() && !ossrootTempFile.mkdirs())
			throw new RuntimeException(ossrootTempFile.getAbsolutePath() + " 创建失败");
		File ossrootPageFile = new File(Oss.ossroot, pagePath);
		if (!ossrootPageFile.exists() && !ossrootPageFile.mkdirs())
			throw new RuntimeException(ossrootPageFile.getAbsolutePath() + " 创建失败");

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

			File tempFile = new File(Oss.ossroot + Oss.tempPath, fileName);
			if (!tempFile.exists())
				continue;

			JdbcUtil.update(connection,
					"insert into t_file (id,name,size,url,locateSql,temp,addTime,alterTime) values(?,?,?,?,?,0,now(),'1970-01-01')",
					fileId, fileName, tempFile.length(), url, locateSql);

			File realFile = new File(Oss.ossroot, fileName);
			realFile.delete();
			tempFile.renameTo(realFile);
			tempFile.delete();
		}
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

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String linkFileId = sdf.format(new Date()) + RandomStringUtils.randomNumeric(6);
		String linkFileName = linkFileId + "." + targetExt;
		File linkFile = new File(Oss.ossroot, linkFileName);

		Path link = FileSystems.getDefault().getPath(linkFile.getAbsolutePath());
		Path target = FileSystems.getDefault().getPath(targetFileName);
		Files.createSymbolicLink(link, target);

		String linkFileUrl = UrlUtil.buildPath(Oss.ossurlroot, linkFileName);
		JdbcUtil.update(connection,
				"insert into t_file (id,targetFileId,name,size,path,temp,addTime,alterTime) values(?,?,?,?,?,1,now(),now())",
				linkFileId, targetFileId, linkFileName, linkFile.length(), linkFileUrl);
		return linkFileUrl;
	}

	public static File getFile(String url) throws Exception {
		url = url.replaceAll("\\\\", "/").replaceFirst("(?i)((http:/+)|(https:/+))", "").replaceFirst(".*?/", "/");
		File file = new File(Oss.ossroot, url.replace(Oss.ossurlroot, ""));
		return file;
	}

	public static String newImage(InputStream is, String originalFileName, Integer quality) throws Exception {
		File ossFolder = new File(Oss.ossroot);
		logger.debug(ossFolder.getAbsolutePath());
		if (!ossFolder.exists())
			ossFolder.mkdirs();

		File tempFolder = new File(Oss.ossroot, Oss.tempPath);
		if (!tempFolder.exists())
			tempFolder.mkdirs();
		Date now = new Date();
		// 保存文件

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String ext = UrlUtil.getExtName(originalFileName);
		String fileId = sdf.format(now) + RandomStringUtils.randomNumeric(15);
		String fileName = StringUtils.isEmpty(ext) ? fileId : (fileId + "." + ext);
		String realLinkFileUrl = UrlUtil.buildPath(Oss.ossurlroot, fileName);

		File tempFile = new File(tempFolder, fileName);
		File realLinkFile = new File(ossFolder, fileName);

		// 保存上传的文件到临时目录
		if (quality != null && quality >= 0 && quality <= 10) {
			tempFile.createNewFile();
			Builder builder = Thumbnails.of(is);
			builder.scale(1.0);
			builder.outputQuality(new Double(quality) / 10f);
			builder.toFile(tempFile);
		} else {
			IOUtil.write(is, tempFile);
		}

		Path link = FileSystems.getDefault().getPath(realLinkFile.getAbsolutePath());
		Path target = FileSystems.getDefault().getPath(tempFile.getAbsolutePath());
		Files.createSymbolicLink(link, target);

		return realLinkFileUrl;
	}

	public static String newFile(InputStream is, String originalFileName, String cover, Integer duration)
			throws Exception {
		File ossFolder = new File(Oss.ossroot);
		logger.debug(ossFolder.getAbsolutePath());
		if (!ossFolder.exists())
			ossFolder.mkdirs();

		File tempFolder = new File(Oss.ossroot, Oss.tempPath);
		if (!tempFolder.exists())
			tempFolder.mkdirs();
		Date now = new Date();
		// 保存文件

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String ext = UrlUtil.getExtName(originalFileName);
		String fileId = sdf.format(now) + RandomStringUtils.randomNumeric(15);
		String fileName = StringUtils.isEmpty(ext) ? fileId : (fileId + "." + ext);
		String realLinkFileUrl = UrlUtil.buildPath(Oss.ossurlroot, fileName);

		File tempFile = new File(tempFolder, fileName);
		File realLinkFile = new File(ossFolder, fileName);

		// 保存上传的文件到临时目录
		IOUtil.write(is, tempFile);

		Path link = FileSystems.getDefault().getPath(realLinkFile.getAbsolutePath());
		Path target = FileSystems.getDefault().getPath(tempFile.getAbsolutePath());
		Files.createSymbolicLink(link, target);

		return realLinkFileUrl;
	}
}
