
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils1 {

	public static List<String> extractOffUrls(String oldcontent, String newcontent) throws InterruptedException {
		oldcontent = oldcontent == null ? "" : oldcontent;
		newcontent = newcontent == null ? "" : newcontent;
		List<String> offUrls = new ArrayList<String>();
		List<String> oldUrls = HtmlUtils1.extractUrls(oldcontent);
		List<String> newUrls = HtmlUtils1.extractUrls(newcontent);
		for (String oldUrl : oldUrls) {
			if (!newUrls.contains(oldUrl))
				offUrls.add(oldUrl);
		}
		return offUrls;
	}

	public static List<String> extractOffUrls(File oldFile, File newFile) throws Exception {
		return extractOffUrls(new FileInputStream(oldFile), new FileInputStream(newFile));
	}

	public static List<String> extractOffUrls(InputStream oldis, InputStream newis) throws Exception {
		List<String> oldUrls = extractUrls(oldis);
		List<String> newUrls = extractUrls(newis);
		List<String> oUrls = new ArrayList<String>();
		for (String oldUrl : oldUrls) {
			if (!newUrls.contains(oldUrl))
				oUrls.add(oldUrl);
		}
		return oUrls;
	}

	public static List<String> extractUrls(InputStream is) throws Exception {
		FileInputStream fis = null;
		BufferedReader br = null;
		List<String> pics = new ArrayList();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				pics.addAll(extractUrls(line));
			}
			br.close();
			return pics;
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
			if (br != null)
				br.close();
		}
	}

	public static List<String> extractUrls(String htmlStr) {
		List<String> pics = new ArrayList();
		String img = "";
		Pattern p_image;
		Matcher m_image;
		// String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址
		String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
		m_image = p_image.matcher(htmlStr);
		while (m_image.find()) {
			// 得到<img />数据
			img = m_image.group();
			// 匹配<img>中的src数据
			Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
			while (m.find()) {
				pics.add(m.group(1));
			}
		}
		return pics;
	}

	public static String removeTag(String htmlStr) {
		String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // script
		String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // style
		String regEx_html = "<[^>]+>"; // HTML tag
		String regEx_space = "\\s+|\t|\r|\n";// other characters

		Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll("");
		Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		Matcher m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll("");
		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll("");
		Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
		Matcher m_space = p_space.matcher(htmlStr);
		htmlStr = m_space.replaceAll(" ");
		return htmlStr;
	}

}
