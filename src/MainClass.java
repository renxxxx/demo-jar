
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class MainClass {
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

	public static void main(String[] args) {
		String s = "zaylt";
		System.out.println(s.replace("-", "/").substring(s.indexOf("-")));
	}

	public static void f1() {
		String path = "\\root\\data\\test_zaylt_njshangka_com\\webapps\\ROOT\\";
		System.out.println(File.separator);
		String[] sa = StringUtils.splitByWholeSeparatorPreserveAllTokens(path, File.separator);
		System.out.println(Arrays.toString(sa));
		System.out.println(sa[sa.length - 4].replace("_", "."));
	}
}
