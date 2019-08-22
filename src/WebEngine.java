
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebEngine {

	Document mydoc = null;

	public WebEngine(String url) throws MalformedURLException, IOException {
		mydoc = Jsoup.parse(new URL(url), 30000);// 利用Jsoup实现document树
	}

	public Element getElementById(String id) {// 实现document的getElementById方法
		System.out.println("Java println:\t" + mydoc.getElementById(id));
		return mydoc.getElementById(id);// 返回的是Element对象
	}

	public static void main(String[] args) throws MalformedURLException, IOException {

		// ScriptEngineManager sem = new ScriptEngineManager();
		// ScriptEngine se = sem.getEngineByName("javascript"); //
		// 初始化Java内置的javascript引擎
		try {

			// se.eval("function test() {"
			// + "id='areaDefault'; print('js
			// print:'+document.getElementById(id));return
			// document.getElementById(id);}");
			// 测试用javascript自定义函数，功能为输出id为areaDefault的元素，并返回。
			// Invocable invocableEngine = (Invocable) se;// 转换引擎类型为Invocable
			// se.put("document", mydoc); //
			// 关联对象，这一步很重要，关联javascript的document对象为TaoDocument，亦即我自己实现的document对象
			// Element callbackvalue = (Element)
			// invocableEngine.invokeFunction("test"); // 直接运行函数，返回值为Element
			// System.out.println("callback return :" + callbackvalue); //
			// 打印输出返回内容
			// se.eval("test()");// 另外一种调用函数方式，我更偏爱此种方式
			// String src = mydoc.getElementById("J_ImgBooth").attr("src");
			// 获取请求参数
			String crowyUrl = "https://detail.tmall.com/item.htm?spm=a230r.1.14.8.5cd0e18ajLOC9J%26id=568712077271%26cm_id=140105335569ed55e27b%26abbucket=10%26skuId=3807741559396";
			String id = URLRequest(crowyUrl).get("id");
			crowyUrl = crowyUrl.split("\\?")[0] + "?id=" + id;
			List<String> detailPics = new ArrayList<String>();
			String cover = null;
			Document mydoc = Jsoup.parse(new URL(crowyUrl), 60000);
			List<String> pics = mydoc.getElementById("J_UlThumb").getElementsByTag("img").eachAttr("src");

			System.out.println(pics);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Map<String, String> URLRequest(String URL) {
		URL = URLDecoder.decode(URL);
		Map<String, String> mapRequest = new HashMap<String, String>();

		String[] arrSplit = null;

		String[] ss = URL.split("\\?");
		if (ss.length <= 1)
			return mapRequest;

		String strUrlParam = ss[1];
		if (strUrlParam == null) {
			return mapRequest;
		}
		// 每个键值为一组 www.2cto.com
		arrSplit = strUrlParam.split("[&]");
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = null;
			arrSplitEqual = strSplit.split("[=]");

			// 解析出键值
			if (arrSplitEqual.length > 1) {
				// 正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

			} else {
				if (arrSplitEqual[0] != "") {
					// 只有参数没有值，不加入
					mapRequest.put(arrSplitEqual[0], "");
				}
			}
		}
		return mapRequest;
	}
}