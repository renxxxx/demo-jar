
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainClass {
	public static void main(String[] args) {
		baidu();
	}

	public static List<Map> lineToCatalog(List<Map> srcList) {
		List<Map> aas = new ArrayList<>();
		Map<Integer, Map> am = new HashMap();
		while (true) {
			if (am.size() == srcList.size())
				break;
			for (Map a : srcList) {
				int id = (int) a.get("id");
				int upId = (int) a.get("upId");
				a.put("children", new ArrayList());

				if (upId == 0) {
					aas.add(a);
				} else {
					Map up = am.get(upId);
					if (up == null)
						break;
					List children = (List) up.get("children");
					children.add(a);
				}

				am.put(id, a);
			}
		}
		return aas;
	}

	public static void f1() {
		String path = "\\root\\data\\test_zaylt_njshangka_com\\webapps\\ROOT\\";
		System.out.println(File.separator);
		String[] sa = StringUtils.splitByWholeSeparatorPreserveAllTokens(path, File.separator);
		System.out.println(Arrays.toString(sa));
		System.out.println(sa[sa.length - 4].replace("_", "."));
	}

	public static void baidu() {
		String accessKey = "ndNxNFF2ZuizuVnAflaxWxRQ";
		String secretKey = "0OOTr1R7cIKkIRSRotebrmyWELZj8WW7";
		System.out.println(baidu_personIdmatch(baidu_getAuth(accessKey, secretKey)));
	}

	/**
	 * 获取API访问token 该token有一定的有效期，需要自行管理，当失效时需重新获取.
	 * 
	 * @param ak - 百度云官网获取的 API Key
	 * @param sk - 百度云官网获取的 Securet Key
	 * @return assess_token 示例：
	 *         "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
	 */
	public static String baidu_getAuth(String ak, String sk) {
		// 获取token地址
		String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
		String getAccessTokenUrl = authHost
				// 1. grant_type为固定参数
				+ "grant_type=client_credentials"
				// 2. 官网获取的 API Key
				+ "&client_id=" + ak
				// 3. 官网获取的 Secret Key
				+ "&client_secret=" + sk;
		try {
			URL realUrl = new URL(getAccessTokenUrl);
			// 打开和URL之间的连接
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				System.err.println(key + "--->" + map.get(key));
			}
			// 定义 BufferedReader输入流来读取URL的响应
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String result = "";
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			/**
			 * 返回结果示例
			 */
			System.err.println("result:" + result);
			JSONObject jsonObject = new JSONObject(result);
			String access_token = jsonObject.getString("access_token");
			return access_token;
		} catch (Exception e) {
			System.err.printf("获取token失败！");
			e.printStackTrace(System.err);
		}
		return null;
	}

	public static String baidu_personIdmatch(String accessToken) {
		// 请求url
		String url = "https://aip.baidubce.com/rest/2.0/face/v3/person/idmatch";
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("name", "任欣");
			map.put("id_card_number", "32011219900410041X");

			String param = JSON.toJSONString(map);

			// 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
			// String accessToken = "[调用鉴权接口获取的token]";

			// String result = HttpUtil.post(url, accessToken, "application/json", param);
			OkHttpClient okHttpClient = new OkHttpClient();
			Request request = new Request.Builder()
					.url(HttpUrl.parse(url).newBuilder().addQueryParameter("access_token", accessToken).build())
					.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), param)).build();
			Response response = okHttpClient.newCall(request).execute();

			return response.body().string();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
