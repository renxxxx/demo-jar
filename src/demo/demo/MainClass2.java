package demo.demo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MainClass2 {

	public static void main(String[] args) throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.URL_PKG_PREFIXES, "demo1:demo2.demo3:demo");
		Context cntxt = new InitialContext(env);
		cntxt.bind("demo:a", "aaa");
		System.out.println(cntxt.lookup("demo:a"));
	}

	public void name() {
		List<String>[] lsa = new ArrayList[1];
		lsa[1]= new ArrayList<Integer>();
		
		Object o = lsa;
	}
}
