package demo.demo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MainClass2 {

	public static void main(String[] args) throws NamingException {
		lambda();
	}

	public static void lambda() {
		final int num = 1;
		Converter<Integer, String> s = (param) -> {
			int a = 1;
			System.out.println(String.valueOf(param + num));
			a = 2;
			param = 3;
		};
		s.convert(2); // 输出结果为 3

	}

	@FunctionalInterface
	public interface Converter<T1, T2> {
		void convert(int i);
	}

}
