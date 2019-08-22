
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ShuJuLaQu {

	public static void main(String[] args) throws MalformedURLException, IOException {
		zhongGuoJunWangWaiJun();
	}

	// 中国军网-外军
	public static void zhongGuoJunWangWaiJun() throws MalformedURLException, IOException {
		String root = "http://www.81.cn/wjsm"; 
		String indexUrl = "http://www.81.cn/wjsm/index.htm";
		// 获取新闻主页
		Document indexDoc = Jsoup.parse(new URL(indexUrl), 30000);
		// 获取主页新闻列表元素
		Elements xinWenLieBiaoEle = indexDoc.select(".list_ns_f14_dot a");
		for (int i = 0; i < xinWenLieBiaoEle.size(); i++) {
			Element xinwenEle = xinWenLieBiaoEle.get(i);
			// 新闻标题写入数据库
			System.out.println("标题：" + xinwenEle.text());
			// 新闻内容url写入数据库 原网页url是相对链接不是绝对链接，要拼接上root才是绝对链接
			System.out.println("内容url ：" + xinwenEle.attr("href"));
			// 获取新闻内容页面 拼接上root后才能拉取
			Document contentDoc = Jsoup.parse(new URL(root + "/" + xinwenEle.attr("href")), 30000);// 利用Jsoup实现document树
			// 获取新闻内容元素
			Element contentEle = contentDoc.getElementById("article-content");
			System.out.println("内容 ：");
			// 新闻内容写入数据库 。新闻内容字符太多，就不打印在控制台了。
			// System.out.println(contentEle.html());
			System.out.println("-----------------------------");
		}
	}

	// 求是网-政治
	public static void qiuShiWangZhengZhi() throws MalformedURLException, IOException {
		String indexUrl = "http://www.qstheory.cn/politics/index.htm";
		// 获取新闻主页
		Document indexDoc = Jsoup.parse(new URL(indexUrl), 30000);
		// 获取主页新闻列表元素
		Elements xinWenLieBiaoEle = indexDoc.select(".pd_list02");
		for (int i = 0; i < xinWenLieBiaoEle.size(); i++) {
			Element xinwenEle = xinWenLieBiaoEle.get(i);
			// 新闻图片写入数据库
			System.out.println("图片 ： " + xinwenEle.select(".pic img").attr("src"));
			// 新闻内容url写入数据库
			System.out.println("内容url ： " + xinwenEle.select(".pic a").attr("href"));
			// 新闻大标题写入数据库
			System.out.println("大标题 ： " + xinwenEle.select("h2 a").text());
			// 新闻内容简介写入数据库
			System.out.println("内容简介 ： " + xinwenEle.select(".abstr").text());
			// 新闻作者写入数据库
			System.out.println("作者  ： " + xinwenEle.select(".text p").text());
			// 获取新闻内容页面
			Document contentDoc = Jsoup.parse(new URL(xinwenEle.select(".pic a").attr("href")), 30000);
			// 获取新闻内容元素
			System.out.println("内容：");
			Elements contentEle = contentDoc.getElementsByClass("content");
			// 新闻内容写入数据库 。新闻内容字符太多，就不打印在控制台了。
			// System.out.println(contentEle.html());
			System.out.println("-----------------------------");

		}
	}
}
