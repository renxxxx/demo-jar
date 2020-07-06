
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Uin {
	public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		Uin uin = new Uin();
		Map map = new HashMap();
		map.put("appver", "1");
		map.put("timestamp", "20200706090401");
		map.put("user", "4000928501_dev");
		map.put("account", "4000928501");
		map.put("startTime", "20200703000000");
		map.put("endTime", "20200703235959");
		//map.put("pwd", "656f3949969745bb9fa316afe6be980e");
		System.out.println(uin.secretToRequest(map, "8c7be118-66d5-4d4a-9ae2-44836aca7bde"));
	}

	public String secretToRequest(Map<String, String> params, String token)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		// 1 删除 secret
		params.remove("secret");
		// 2 检查参数是否已经排序
		String[] keys = params.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		// 3 把所有参数名和参数值串在一起
		StringBuilder query = new StringBuilder();
		for (String key : keys) {
			String value = params.get(key);
			if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
				query.append(key).append(URLEncoder.encode(value, "utf8"));
			}
		}
		// 4 使用 MD5 加密
		query.append(token);
		System.out.println(query);
		byte[] bytes = encryptMD5(query.toString());

		// 5 转换为字符串
		return byte2hex(bytes);
	}

	public byte[] encryptMD5(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		return md5.digest(data.getBytes("utf-8"));
	}

	public String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}
}
