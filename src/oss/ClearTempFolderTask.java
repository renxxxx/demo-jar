package zaylt.service.oss;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class ClearTempFolderTask implements Runnable {

	private static Logger logger = Logger.getLogger(ClearTempFolderTask.class);

	public void run() {
		logger.info("执行ClearTmpFolderTask");
		try {
			File temp = new File(Oss.ossroot, Oss.tempPath);
			logger.info("清理临时文件夹: " + temp);
			if (temp.exists() && temp.isDirectory()) {
				temp.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						if ((new Date().getTime() - file.lastModified()) > 30 * 60 * 1000) {
							logger.info("del " + file.getAbsolutePath() + " exists:" + file.exists() + " delete:"
									+ file.delete());

							File rFile = new File(file.getAbsolutePath().replace(Oss.tempPath, "/"));
							logger.info("del " + rFile.getAbsolutePath() + " exists:" + rFile.exists() + " delete:"
									+ rFile.delete());
						}
						return false;
					}
				});
			}
		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
		}
	}
}